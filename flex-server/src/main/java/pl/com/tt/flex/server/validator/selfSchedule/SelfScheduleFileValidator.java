package pl.com.tt.flex.server.validator.selfSchedule;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;
import pl.com.tt.flex.model.security.permission.Role;
import pl.com.tt.flex.server.common.errors.ObjectValidationException;
import pl.com.tt.flex.server.domain.common.enumeration.FileExtension;
import pl.com.tt.flex.server.domain.user.UserEntity;
import pl.com.tt.flex.server.service.auction.da.AuctionDayAheadService;
import pl.com.tt.flex.server.service.fsp.FspService;
import pl.com.tt.flex.server.service.fsp.dto.FspDTO;
import pl.com.tt.flex.server.service.unit.UnitService;
import pl.com.tt.flex.server.service.unit.dto.UnitMinDTO;
import pl.com.tt.flex.server.service.unit.selfSchedule.UnitSelfScheduleService;
import pl.com.tt.flex.server.service.unit.selfSchedule.dto.UnitSelfScheduleDTO;
import pl.com.tt.flex.server.service.unit.selfSchedule.dto.UnitSelfScheduleErrorDTO;
import pl.com.tt.flex.server.service.unit.selfSchedule.dto.UnitSelfScheduleMinDTO;
import pl.com.tt.flex.server.service.unit.selfSchedule.util.SelfScheduleUtils;
import pl.com.tt.flex.server.service.user.UserService;
import pl.com.tt.flex.server.service.user.dto.UserDTO;
import pl.com.tt.flex.server.util.DateUtil;
import pl.com.tt.flex.server.util.InstantUtil;
import pl.com.tt.flex.server.validator.common.CommonValidatorUtil;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static pl.com.tt.flex.server.domain.activityMonitor.ActivityEvent.SELF_SCHEDULED_CREATED_ERROR;
import static pl.com.tt.flex.server.domain.activityMonitor.ActivityEvent.SELF_SCHEDULED_DELETED_ERROR;
import static pl.com.tt.flex.server.domain.common.enumeration.FileExtension.XLSX;
import static pl.com.tt.flex.server.service.unit.selfSchedule.util.SelfScheduleCellLocalization.*;
import static pl.com.tt.flex.server.web.rest.errors.ErrorConstants.*;
import static pl.com.tt.flex.server.web.rest.product.forecastedPrices.ForecastedPricesResourceAdmin.ENTITY_NAME;

@Slf4j
@Component
public class SelfScheduleFileValidator {

    private static final String TEMPLATE_INCORRECT_MSG = "Template incorrect";
    private static final String LANG_PL = "pl";
    private static final String LANG_EN = "en";
    private static final Set<FileExtension> SUPPORTED_FILE_EXTENSIONS = Sets.newHashSet(XLSX);

    private final UserService userService;
    private final UnitService unitService;
    protected final UnitSelfScheduleService unitSelfScheduleService;
    protected final FspService fspService;
    private final AuctionDayAheadService auctionDayAheadService;
    private final SelfScheduleUtils selfScheduleUtils;

    public SelfScheduleFileValidator(UserService userService, UnitService unitService,
                                     @Lazy UnitSelfScheduleService unitSelfScheduleService, FspService fspService,
                                     @Lazy AuctionDayAheadService auctionDayAheadService, SelfScheduleUtils selfScheduleUtils) {
        this.userService = userService;
        this.unitService = unitService;
        this.auctionDayAheadService = auctionDayAheadService;
        this.unitSelfScheduleService = unitSelfScheduleService;
        this.fspService = fspService;
        this.selfScheduleUtils = selfScheduleUtils;
    }

    public void checkValid(MultipartFile[] multipartFile, boolean isAdminImport) throws ObjectValidationException, IOException {
        validFiles(multipartFile, isAdminImport);
        checkSavingSameSelfSchedule(multipartFile, isAdminImport);
    }

    /**
     * Nie można usunąć planu pracy gdy:
     * - probuje usunąć się plan pracy z data dzisiejszą lub przeszłą
     * - plan pracy używany jest w ofercie
     */
    public void checkDeletable(long id) throws ObjectValidationException {
        UnitSelfScheduleDTO selfScheduleFileDTO = unitSelfScheduleService.findById(id)
            .orElseThrow(() -> new RuntimeException("Cannot find SelScheduleFile with id: " + id));
        if (selfScheduleFileDTO.getSelfScheduleDate().compareTo(InstantUtil.now()) <= 0) {
            throw new ObjectValidationException("Cannot delete because the self scheduled date has already started or ended",
                CANNOT_DELETE_BECAUSE_SELF_SCHEDULED_DAY_HAS_ALREADY_STARTED_OR_ENDED, ENTITY_NAME, SELF_SCHEDULED_DELETED_ERROR, id);
        }
        var isSelfScheduleUsed = auctionDayAheadService.existsDaOfferByDeliveryDateAndUnitId(
            selfScheduleFileDTO.getSelfScheduleDate(),
            selfScheduleFileDTO.getUnit().getId()
        );
        if (isSelfScheduleUsed) {
            throw new ObjectValidationException("Cannot delete because self schedule is already used in auction offer",
                CANNOT_DELETE_BECAUSE_SELF_SCHEDULE_USED_IN_OFFER, ENTITY_NAME, SELF_SCHEDULED_DELETED_ERROR, id);
        }
    }

    /**
     * Rzuca błąd (SAVING_THE_SAME_SELF_SCHEDULES) gdy probuje się zaimportować w ramach jednego importu kilka takich
     * samych planów pracy. W odpowiedzi z bledem do params dodawany jest obiekt {@link UnitSelfScheduleErrorDTO} ktory
     * zawiera informacje ktore plany pracy spowodowały błąd.
     */
    public void checkSavingSameSelfSchedule(MultipartFile[] multipartFile, boolean isAdminImport) throws ObjectValidationException, IOException {
        List<UnitSelfScheduleDTO> selfScheduleFromFile = selfScheduleUtils.getSelfScheduleFromFiles(isAdminImport, multipartFile);
        List<UnitSelfScheduleMinDTO> minSelfScheduleList = selfScheduleFromFile.stream().map(selfScheduleUtils::toMinDTO)
            .collect(Collectors.toList());
        Set<UnitSelfScheduleMinDTO> notUnique = getNonUniqueSelfSchedule(minSelfScheduleList);
        if (!CollectionUtils.isEmpty(notUnique)) {
            log.debug("checkSavingSameSelfSchedule() Found {} saving the same selfSchedule", notUnique.size());
            UnitSelfScheduleErrorDTO unitSelfScheduleErrorDTO = new UnitSelfScheduleErrorDTO();
            unitSelfScheduleErrorDTO.getInvalidSelfSchedule().put(SAVING_THE_SAME_SELF_SCHEDULES, new ArrayList<>(notUnique));
            throw new ObjectValidationException("Saving the same SelfSchedules", SELF_SCHEDULE_TEMPLATE_INCORRECT, selfScheduleUtils.selfScheduleErrorToJson(unitSelfScheduleErrorDTO));
        }
    }

    /**
     * Znajduje z listy planów pracy te plany prace które występują kilkukrotnie
     */
    private Set<UnitSelfScheduleMinDTO> getNonUniqueSelfSchedule(List<UnitSelfScheduleMinDTO> minSelfScheduleList) {
        Set<UnitSelfScheduleMinDTO> notUnique = new HashSet<>();
        Set<UnitSelfScheduleMinDTO> uniqueSet = new HashSet<>();
        for (UnitSelfScheduleMinDTO minSelfSchedule : minSelfScheduleList) {
            boolean isNotUnique = uniqueSet.stream()
                .anyMatch(selfSchedule ->
                    selfSchedule.getSelfScheduleDate().equals(minSelfSchedule.getSelfScheduleDate())
                        && selfSchedule.getUnitName().equals(minSelfSchedule.getUnitName()));
            if (isNotUnique) {
                notUnique.add(minSelfSchedule);
            } else
                uniqueSet.add(minSelfSchedule);
        }
        return notUnique;
    }

    public boolean isSelfScheduleBelongsToFspUser(long id) {
        UnitSelfScheduleDTO scheduleFileDTO = unitSelfScheduleService.findById(id)
            .orElseThrow(() -> new RuntimeException("Cannot find SelScheduleFile with id: " + id));
        UserDTO fspUser = userService.getCurrentUserDTO().orElseThrow(() -> new RuntimeException("Current logged-in user not found"));
        return scheduleFileDTO.getFsp().getId().equals(fspUser.getFspId());
    }

    private void validFiles(MultipartFile[] multipartFile, boolean isAdminImport) throws IOException, ObjectValidationException {
        UnitSelfScheduleErrorDTO unitSelfScheduleErrorDTO = new UnitSelfScheduleErrorDTO();
        DerExceedTechnicalLimits dersExceedTechnicalLimits = new DerExceedTechnicalLimits();
        for (MultipartFile file : multipartFile) {
            try {
                CommonValidatorUtil.checkFileExtensionValid(file, SUPPORTED_FILE_EXTENSIONS);
                checkFileContextValid(file, isAdminImport, dersExceedTechnicalLimits);
            } catch (ObjectValidationException e) {
                unitSelfScheduleErrorDTO.addInvalidFilename(e.getMsgKey(), file.getOriginalFilename());
            }
        }
        addDerExceedTechnicalLimitInvalidFilename(unitSelfScheduleErrorDTO, dersExceedTechnicalLimits);
        if (!CollectionUtils.isEmpty(unitSelfScheduleErrorDTO.getInvalidFiles())) {
            ObjectMapper objectMapper = new ObjectMapper();
            throw new ObjectValidationException(TEMPLATE_INCORRECT_MSG, SELF_SCHEDULE_TEMPLATE_INCORRECT,
                objectMapper.writeValueAsString(unitSelfScheduleErrorDTO));
        }
    }

    /**
     * Importowany plik może zaweirać wiele sekcji z planem pracy.
     * Jeżeli dany plik zawiera kilka planów pracy to sekcja po sekcji sprawdzana
     * jest poprawność danego planu pracy
     */
    private void checkFileContextValid(MultipartFile multipartFile, boolean isAdminImport,
                                       DerExceedTechnicalLimits dersExceedTechnicalLimits) throws ObjectValidationException, IOException {
        XSSFWorkbook workbook = new XSSFWorkbook(multipartFile.getInputStream());
        Sheet sheet = workbook.getSheetAt(0);
        validTemplate(sheet, isAdminImport);
        AtomicInteger sectionNum = new AtomicInteger(0);
        while (hasNextSection(sheet, sectionNum.get())) {
            validateDate(sheet, isAdminImport, sectionNum.get());
            validDer(sheet, isAdminImport, sectionNum.get());
            if (isAdminImport) validFspAndDerForAdminTemplate(sheet, sectionNum.get());
            validPower(sheet, isAdminImport, sectionNum.get(), dersExceedTechnicalLimits, multipartFile.getOriginalFilename());
            sectionNum.addAndGet(SECTION_SIZE.getRowNumber(isAdminImport));
        }
    }

    /**
     * Sprawdzenie czy dany plik posiada następny plan pracy
     */
    public static boolean hasNextSection(Sheet sheet, int sectionNum) {
        Row row = sheet.getRow(sectionNum);
        return isRowEmpty(row);
    }

    /**
     * sprawdzanie ilości godzin względem doby - jeśli doba ma 25h to na końcu ma być dodatkowo godzina 2a
     */
    private void validateDate(Sheet sheet, boolean isAdminImport, int sectionNum) throws ObjectValidationException {
        if (!isValidDate(sheet, isAdminImport, sectionNum)) {
            log.debug("validateDate() - Self schedule date is incorrect");
            throw new ObjectValidationException(TEMPLATE_INCORRECT_MSG, SELF_SCHEDULE_TEMPLATE_INCORRECT,
                ENTITY_NAME, SELF_SCHEDULED_CREATED_ERROR, null);
        }

        LocalDate date = getDateFromSheet(sheet, isAdminImport, sectionNum);
        //Użytkownik nie powinien móc zaimportować prognozowanej ceny, jeżeli dotyczy on aktualnego dnia lub dni poprzednich
        if (date.compareTo(LocalDate.now()) <= 0) {
            log.debug("validateDate() - self schedule cannot be related to the current and the past day");
            throw new ObjectValidationException(TEMPLATE_INCORRECT_MSG, NOT_ALLOWED_TO_IMPORT_SELF_SCHEDULE_WITH_PAST_OR_CURRENT_DATE,
                ENTITY_NAME, SELF_SCHEDULED_CREATED_ERROR, null);
        }
    }

    /**
     * sprawdzanie czy moc została wprowadzona dla każdej godziny
     * dopuszczalne tylko liczby z 3 miejscami po przecinku
     */
    private void validPower(Sheet sheet, boolean isAdminImport, int sectionNum,
                            DerExceedTechnicalLimits dersExceedTechnicalLimits, String filename) throws ObjectValidationException {
        Row valuesRow = sheet.getRow(POWER_FIRST_CELL.getRowNumber(isAdminImport) + sectionNum);
        LocalDate date = getDateFromSheet(sheet, isAdminImport, sectionNum);
        UnitMinDTO unitMinDTO = getUnitFromSheet(sheet, isAdminImport, sectionNum);
        for (int i = 1; i <= DateUtil.calculate(date); i++) {
            Cell cell = valuesRow.getCell(i);
            throwExceptionIfCellIsBlank(cell);
            BigDecimal power = getBigDecimalFromCell(cell);
            if(power.scale() > 3){
                log.debug("validPower() - not all values have been completed. Problem with cell: [row={},col={}]", valuesRow.getRowNum(), i);
                throw new ObjectValidationException("Self schedule precision exceeded for value: " + power, SELF_SCHEDULE_PRECISION_TOO_HIGH,
                    ENTITY_NAME, SELF_SCHEDULED_CREATED_ERROR, null);
            } else if (getIntegerDigits(power) > 10) {
                log.debug("validPower() - not all values have been completed. Problem with cell: [row={},col={}]", valuesRow.getRowNum(), i);
                throw new ObjectValidationException(TEMPLATE_INCORRECT_MSG, SELF_SCHEDULE_TEMPLATE_INCORRECT,
                    ENTITY_NAME, SELF_SCHEDULED_CREATED_ERROR, null);
            } else if (isPowerExceedingLimit(power, unitMinDTO)) {
                log.debug("validPower() - timestamp at [row={}, col={}] exceeds technical limit of DER", valuesRow.getRowNum(), i);
                dersExceedTechnicalLimits.addDerExceedTechnicalLimits(unitMinDTO.getName(), filename);
                return;
            }
        }
        validExtraHour(sheet, isAdminImport, date, sectionNum);
    }

    /**
     * Sprawdzenie czy są nieprawidłowe DERy ktore przekroczyly limit techniczny podczas procesu walidacji
     * i wyrzucany jest odpowiedni komunikat w zależności od liczby nieprawidłowych DERów.
     */
    private void addDerExceedTechnicalLimitInvalidFilename(UnitSelfScheduleErrorDTO selfScheduleErrorDTO,
                                                           DerExceedTechnicalLimits dersExceedTechnicalLimits) throws ObjectValidationException {
        if (!CollectionUtils.isEmpty(dersExceedTechnicalLimits.getDers())) {
            Map<String, List<String>> invalidDerNames = dersExceedTechnicalLimits.getDers();
            String userLocale = userService.getLangKeyForCurrentLoggedUser();
            String filePlaceholder = userLocale.equalsIgnoreCase("pl") ? " (Plik(i): " : " (File(s): ";
            StringBuilder stringBuilder = new StringBuilder();
            invalidDerNames.forEach((derName, filenames) -> stringBuilder.append(derName).append(filePlaceholder)
                .append(String.join(", ", filenames)).append("), "));
            if (invalidDerNames.size() > 1) {
                selfScheduleErrorDTO.addInvalidFilename(SELF_SCHEDULE_DERS_EXCEED_TECHNICAL_LIMITS, stringBuilder.substring(0, stringBuilder.length() - 2));
            } else if (invalidDerNames.size() == 1) {
                selfScheduleErrorDTO.addInvalidFilename(SELF_SCHEDULE_DER_EXCEEDS_TECHNICAL_LIMITS, stringBuilder.substring(0, stringBuilder.length() - 2));
            }
        }
    }

    /**
     * Sprawdzenie czy wartości podane w timestampie przekraczają limity.
     * Jeżeli odchylenie jest w dół, to sprawdzić czy możliwości techniczne DERa przekraczają Pmin,
     * natomiast gdy odchylenie jest w górę to sprawdzić czy możliwości techniczne DERa przekraczają Pmax (source power)
     */
    private boolean isPowerExceedingLimit(BigDecimal powerFromSheet, UnitMinDTO unitMinDTO) {
        return powerFromSheet.compareTo(unitMinDTO.getPMin()) < 0 || powerFromSheet.compareTo(unitMinDTO.getSourcePower()) > 0;
    }

    /**
     * sprawdzenie czy dodatkowa godzina (2a) jest wymagana
     */
    private void validExtraHour(Sheet sheet, boolean isAdminImport, LocalDate date, int sectionNum) throws ObjectValidationException {
        int rowNumber = EXTRA_HOUR_PRICE.getRowNumber(isAdminImport) + sectionNum;
        int colNumber = EXTRA_HOUR_PRICE.getColNumber(isAdminImport);
        Cell cell = sheet.getRow(rowNumber).getCell(colNumber);
        if (DateUtil.calculate(date) == 24 && Objects.nonNull(cell) && cell.getCellTypeEnum() != CellType.BLANK) {
            log.debug("validPrice() - is not required to add extra hour. Problem with cell: [row={},col={}]", rowNumber, colNumber);
            throw new ObjectValidationException(TEMPLATE_INCORRECT_MSG, SELF_SCHEDULE_TEMPLATE_INCORRECT,
                ENTITY_NAME, SELF_SCHEDULED_CREATED_ERROR, null);
        }
    }

    /**
     * porownanie szablonu oraz importowanego pliku
     * sprawdzenie czy importowany plik jest zgodny z jednym z szablonow PL i EN
     */
    private void validTemplate(Sheet sheet, boolean isAdminImport) throws ObjectValidationException, IOException {
        boolean isTemplateForAdmin = isImportFileCompatible(sheet, true);
        boolean isTemplateForFsp = isImportFileCompatible(sheet, false);
        if (!isTemplateForAdmin && !isTemplateForFsp) {
            throw new ObjectValidationException(TEMPLATE_INCORRECT_MSG, SELF_SCHEDULE_TEMPLATE_INCORRECT,
                ENTITY_NAME, SELF_SCHEDULED_CREATED_ERROR, null);
        }
        checkIfUserUsedCorrectTemplate(isTemplateForAdmin, isTemplateForFsp, isAdminImport);
    }

    /**
     * Sprawdza czy przeslany plik jest poprawny z szablonem
     * isAdminImport:
     * - true - z szablonem ADMINA
     * - false - z szablonem FSP
     */
    private boolean isImportFileCompatible(Sheet sheet, boolean isAdminImport) throws IOException {
        Sheet sheetExpectedPl = getExpectedSheet(isAdminImport, LANG_PL);
        Sheet sheetExpectedEn = getExpectedSheet(isAdminImport, LANG_EN);
        boolean importFileCompatibleWithTemplateEn = validFirstColumn(sheet, sheetExpectedEn, isAdminImport) && validHours(sheet, sheetExpectedEn, isAdminImport);
        boolean importFileCompatibleWithTemplatePl = validFirstColumn(sheet, sheetExpectedPl, isAdminImport) && validHours(sheet, sheetExpectedPl, isAdminImport);
        return importFileCompatibleWithTemplateEn || importFileCompatibleWithTemplatePl;
    }

    /**
     * Sprawdzenie czy użytkownik użył prawidłowego szablonu.
     * Gdy FSP użyje szablonu przeznaczonego dla administratora lub administrator użyje szablonu dla FSP, to
     * wyrzucić wyjątek.
     */
    private void checkIfUserUsedCorrectTemplate(boolean isTemplateForAdmin, boolean isTemplateForFsp, boolean isAdminImport) throws ObjectValidationException {
        if (isAdminImport && !isTemplateForAdmin) {
            log.error("Admin used template for FSP. Use template for admin");
            throw new ObjectValidationException("Admin used template for FSP", CANNOT_IMPORT_IF_ADMIN_USED_FSPS_TEMPLATE);
        } else if (!isAdminImport && !isTemplateForFsp) {
            log.error("FSP used template for admin. Use template for FSP");
            throw new ObjectValidationException("FSP used template for admin", CANNOT_IMPORT_IF_FSPS_USED_ADMIN_TEMPLATE);
        }
    }

    /**
     * walidacja pierwszej kolumny
     */
    private boolean validFirstColumn(Sheet sheet, Sheet sheetExpected, boolean isAdminImport) {
        int maxHeaderExpectedRow = SECTION_SIZE.getRowNumber(isAdminImport) - 1;
        int expectedHeaderRow = 0;
        for (int i = 0; i < getLastRow(sheet); i++) {
            Cell cell = sheet.getRow(i).getCell(0);
            Cell cellExpected = sheetExpected.getRow(expectedHeaderRow).getCell(0);
            if (!(isNumberCellValid(cell, cellExpected) || isStringCellValid(cell, cellExpected))) {
                log.debug("validTemplate() - Incorrect template (headers are incorrect). Problem with cell: [row={},col={}]", i, 0);
                return false;
            }
            if (expectedHeaderRow >= maxHeaderExpectedRow) expectedHeaderRow = 0;
            else expectedHeaderRow += 1;
        }
        return true;
    }

    /**
     * walidacja sprawdzajaca poprawnosc godzin
     */
    private boolean validHours(Sheet sheet, Sheet sheetExpected, boolean isAdminImport) {
        AtomicInteger atomicInteger = new AtomicInteger();
        while (Objects.nonNull(sheet.getRow(DAY_HOURS_FIRST_CELL.getRowNumber(isAdminImport) + atomicInteger.get()))) {
            int rowNumber = DAY_HOURS_FIRST_CELL.getRowNumber(isAdminImport) + atomicInteger.get();
            Row hoursRow = sheet.getRow(rowNumber);
            Row hoursRowExpected = sheetExpected.getRow(DAY_HOURS_FIRST_CELL.getRowNumber(isAdminImport));
            LocalDate date = getDateFromSheet(sheet, isAdminImport, atomicInteger.get());
            for (int i = 1; i <= DateUtil.calculate(date); i++) {
                Cell cell = hoursRow.getCell(i);
                Cell cellExpected = hoursRowExpected.getCell(i);
                if (!(isNumberCellValid(cell, cellExpected) || isStringCellValid(cell, cellExpected))) {
                    log.debug("validHours() - hours in sheet are incorrect. Problem with cell: [row={},col={}]", hoursRow.getRowNum(), i);
                    return false;
                }
            }
            atomicInteger.addAndGet(SECTION_SIZE.getRowNumber(isAdminImport));
        }
        return true;
    }

    /**
     * Walidacji nazwy dera z importowanego pliku
     */
    private void validDer(Sheet sheet, boolean isAdminImport, int sectionNum) throws ObjectValidationException {
        int unitRowNumber = UNIT_NAME.getRowNumber(isAdminImport) + sectionNum;
        Cell derCell = checkAndGetDerCell(sheet, isAdminImport, unitRowNumber);
        String derName = derCell.getStringCellValue();
        Optional<UnitMinDTO> unitMinOpt = unitService.findUnitByNameIgnoreCase(derName);
        // Metoda findUnitByNameIgnoreCase zwraca tylko certyfikowane DERy, natomiast existsByNameLowerCase sprawdza wszystkie.
        // Jeżeli unitMinOpt jest pusty, a existsByNameLowerCase zwraca 'true', to DER jest niecertyfikowany.
        if (unitMinOpt.isEmpty() && unitService.existsByNameLowerCase(derName)) {
            log.debug("validateDer() - Der is not certified: {}", derName);
            throw new ObjectValidationException(TEMPLATE_INCORRECT_MSG, SELF_SCHEDULE_IMPORT_UNCERTIFIED_DER,
                ENTITY_NAME, SELF_SCHEDULED_CREATED_ERROR, null);
        }
        if (unitMinOpt.isEmpty()) {
            log.debug("validateDer() - Cannot find Der by name: {}", derName);
            throw new ObjectValidationException(TEMPLATE_INCORRECT_MSG, SELF_SCHEDULE_TEMPLATE_INCORRECT,
                ENTITY_NAME, SELF_SCHEDULED_CREATED_ERROR, null);
        }

        UserEntity currentLoggedUser = userService.getCurrentUser();
        if (isFspUser(currentLoggedUser) && !unitMinOpt.get().getFspId().equals(currentLoggedUser.getFsp().getId())) {
            log.debug("validateDer() - User has no privileges to DER");
            throw new ObjectValidationException(TEMPLATE_INCORRECT_MSG, SELF_SCHEDULE_TEMPLATE_INCORRECT,
                ENTITY_NAME, SELF_SCHEDULED_CREATED_ERROR, null);
        }
    }

    private Cell checkAndGetDerCell(Sheet sheet, boolean isAdminImport, int unitRowNumber) throws ObjectValidationException {
        Cell derCell = sheet.getRow(unitRowNumber).getCell(UNIT_NAME.getColNumber(isAdminImport));
        if (Objects.isNull(derCell)) {
            log.debug("checkAndGetDerCell() - Cell with Der is empty");
            throw new ObjectValidationException(TEMPLATE_INCORRECT_MSG, SELF_SCHEDULE_TEMPLATE_INCORRECT,
                ENTITY_NAME, SELF_SCHEDULED_CREATED_ERROR, null);
        }
        return derCell;
    }

    private boolean isFspUser(UserEntity currentUser) {
        return currentUser.getRoles().contains(Role.ROLE_FLEX_SERVICE_PROVIDER) && !currentUser.getRoles().contains(Role.ROLE_ADMIN);
    }

    /**
     * Walidacji nazwy firmy z importowanego pliku (jesli import z aplikacji ADMIN)
     * sprawdzenie czy podany FSP jest wlascicielem podanego DERa.
     */
    private void validFspAndDerForAdminTemplate(Sheet sheet, int sectionNum) throws ObjectValidationException {
        int fspCompanyRow = FSP_COMPANY_NAME.getRowNumber(true) + sectionNum;
        Cell fspCell = checkAndGetFsp(sheet, fspCompanyRow);
        String companyName = fspCell.getStringCellValue();
        Optional<FspDTO> fspOpt = fspService.findByCompanyName(companyName);
        if (fspOpt.isEmpty()) {
            log.debug("validFsp() - Cannot find FSP by Company name: {}", companyName);
            throw new ObjectValidationException(TEMPLATE_INCORRECT_MSG, SELF_SCHEDULE_TEMPLATE_INCORRECT,
                ENTITY_NAME, SELF_SCHEDULED_CREATED_ERROR, null);
        }
        UnitMinDTO unitMinDTO = getUnitFromSheet(sheet, true, sectionNum);
        if (!unitMinDTO.getFspId().equals(fspOpt.get().getId())) {
            log.debug("validFsp() - Fsp with company name {} has not privileges to DER with name {}", companyName, unitMinDTO.getName());
            throw new ObjectValidationException(TEMPLATE_INCORRECT_MSG, SELF_SCHEDULE_TEMPLATE_INCORRECT,
                ENTITY_NAME, SELF_SCHEDULED_CREATED_ERROR, null);
        }
    }

    private Cell checkAndGetFsp(Sheet sheet, int fspCompanyRow) throws ObjectValidationException {
        Cell fspCell = sheet.getRow(fspCompanyRow).getCell(FSP_COMPANY_NAME.getColNumber(true));
        if (Objects.isNull(fspCell)) {
            log.debug("checkAndGetFsp() - Cell with Fsp is empty");
            throw new ObjectValidationException(TEMPLATE_INCORRECT_MSG, SELF_SCHEDULE_TEMPLATE_INCORRECT,
                ENTITY_NAME, SELF_SCHEDULED_CREATED_ERROR, null);
        }
        return fspCell;
    }

    private Sheet getExpectedSheet(boolean isAdminImport, String langKey) throws IOException {
        String extension = ".xlsx";
        String dic = isAdminImport ? "admin" : "user";
        String template = "templates/xlsx/self_schedule/" + dic + "/import_self_schedule_" + langKey + extension;
        Resource templateFileResource = new ClassPathResource(template);
        InputStream fileWithHeaders = templateFileResource.getInputStream();
        XSSFWorkbook workbookExpected = new XSSFWorkbook(fileWithHeaders);
        return workbookExpected.getSheetAt(0);
    }

    private LocalDate getDateFromSheet(Sheet sheet, boolean isAdminImport, int sectionNum) {
        int selfScheduleRow = SELF_SCHEDULE_DATE.getRowNumber(isAdminImport) + sectionNum;
        Cell dateCell = sheet.getRow(selfScheduleRow).getCell(SELF_SCHEDULE_DATE.getColNumber(isAdminImport));
        try {
            return dateCell.getDateCellValue().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
        } catch (Exception ex) {
            throw new ObjectValidationException("Incorrect date.", SELF_SCHEDULE_TEMPLATE_INCORRECT);
        }
    }

    private UnitMinDTO getUnitFromSheet(Sheet sheet, boolean isAdminImport, int sectionNum) {
        int unitNameRow = UNIT_NAME.getRowNumber(isAdminImport) + sectionNum;
        String derName = sheet.getRow(unitNameRow).getCell(UNIT_NAME.getColNumber(isAdminImport)).getStringCellValue();
        return unitService.findUnitByNameIgnoreCase(derName).orElseThrow(() -> new RuntimeException("Cannot find current logged-in User"));
    }

    private boolean isValidDate(Sheet sheet, boolean isAdminImport, int sectionNum) {
        int rowNumber = SELF_SCHEDULE_DATE.getRowNumber(isAdminImport) + sectionNum;
        int colNumber = SELF_SCHEDULE_DATE.getColNumber(isAdminImport);
        Cell dateCell = sheet.getRow(rowNumber).getCell(colNumber);
        try {
            dateCell.getDateCellValue().toInstant();
        } catch (Exception e) {
            log.debug("isValidDate() Date {} in cell [row={},col={}] is incorrect", dateCell.getStringCellValue(), rowNumber, colNumber);
            return false;
        }
        return true;
    }

    private boolean isNumberCellValid(Cell cell, Cell cellExpected) {
        return Objects.nonNull(cell) && cell.getCellTypeEnum() == CellType.NUMERIC && cellExpected.getCellTypeEnum() == CellType.NUMERIC &&
            cell.getNumericCellValue() == cellExpected.getNumericCellValue();
    }

    private boolean isStringCellValid(Cell cell, Cell cellExpected) {
        return Objects.nonNull(cell) && cell.getCellTypeEnum() == CellType.STRING && cellExpected.getCellTypeEnum() == CellType.STRING &&
            cell.getStringCellValue().equals(cellExpected.getStringCellValue());
    }

    private static boolean isCellOfNumericType(Cell cell) {
        return cell != null && cell.getCellTypeEnum() != CellType.BLANK && cell.getCellTypeEnum() == CellType.NUMERIC;
    }

    private static void throwExceptionIfCellIsBlank(Cell cell) throws ObjectValidationException {
        if (Objects.isNull(cell) || cell.getCellTypeEnum() == CellType.BLANK) {
            throw new ObjectValidationException(TEMPLATE_INCORRECT_MSG, SELF_SCHEDULE_TEMPLATE_INCORRECT,
                ENTITY_NAME, SELF_SCHEDULED_CREATED_ERROR, null);
        }
    }

    /**
     * Pobiera z komórki BigDecimal
     */
    public static BigDecimal getBigDecimalFromCell(Cell cell) throws ObjectValidationException {
        throwExceptionIfCellIsBlank(cell);
        if (isCellOfNumericType(cell)) {
            return BigDecimal.valueOf(cell.getNumericCellValue());
        }
        return getBigDecimalFromStringCell(cell);
    }

    private static BigDecimal getBigDecimalFromStringCell(Cell cell) throws ObjectValidationException {
        String stringCellValue = cell.getStringCellValue();
        try {
            return BigDecimal.valueOf(Double.parseDouble(stringCellValue));
        } catch (Exception e) {
            log.info("getBigDecimalFromCell() Invalid BigDecimal value -> {}. CellAddress: {}", stringCellValue, cell.getAddress().toString());
            throw new ObjectValidationException(TEMPLATE_INCORRECT_MSG, SELF_SCHEDULE_TEMPLATE_INCORRECT,
                ENTITY_NAME, SELF_SCHEDULED_CREATED_ERROR, null);
        }
    }

    /**
     * Zwraca ostatni uizupelniony wiersz
     */
    private int getLastRow(Sheet sheet) {
        int lastFillRow = 0;
        for (int i = 0; i < sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (isRowEmpty(row)) {
                lastFillRow = i;
            } else {
                break;
            }
        }
        return lastFillRow;
    }

    private static boolean isRowEmpty(Row row) {
        return Objects.nonNull(row) && Objects.nonNull(row.getCell(row.getFirstCellNum())) && !row.getCell(row.getFirstCellNum()).getStringCellValue().isEmpty();
    }

    /**
     * Wylicza liczbę miejsc przed przecinkiem
     */
    private long getIntegerDigits(BigDecimal power) {
        BigDecimal n = power.stripTrailingZeros();
        return n.signum() == 0 ? 1 : n.precision() - n.scale();
    }
}
