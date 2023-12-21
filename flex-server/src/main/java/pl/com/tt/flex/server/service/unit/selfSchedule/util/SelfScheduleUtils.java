package pl.com.tt.flex.server.service.unit.selfSchedule.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.context.MessageSource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import pl.com.tt.flex.model.service.dto.MinimalDTO;
import pl.com.tt.flex.server.service.fsp.FspService;
import pl.com.tt.flex.server.service.fsp.dto.FspDTO;
import pl.com.tt.flex.server.service.unit.UnitService;
import pl.com.tt.flex.server.service.unit.dto.UnitMinDTO;
import pl.com.tt.flex.server.service.unit.selfSchedule.dto.UnitSelfScheduleDTO;
import pl.com.tt.flex.server.service.unit.selfSchedule.dto.UnitSelfScheduleErrorDTO;
import pl.com.tt.flex.server.service.unit.selfSchedule.dto.UnitSelfScheduleMinDTO;
import pl.com.tt.flex.server.service.user.UserService;
import pl.com.tt.flex.server.service.user.dto.UserDTO;
import pl.com.tt.flex.server.util.DateUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.util.Locale.forLanguageTag;
import static pl.com.tt.flex.server.service.unit.selfSchedule.util.SelfScheduleCellLocalization.*;
import static pl.com.tt.flex.server.validator.selfSchedule.SelfScheduleFileValidator.getBigDecimalFromCell;
import static pl.com.tt.flex.server.validator.selfSchedule.SelfScheduleFileValidator.hasNextSection;

@Component
@AllArgsConstructor
public class SelfScheduleUtils {

    private final UserService userService;
    private final FspService fspService;
    private final UnitService unitService;
    private final MessageSource messageSource;

    /**
     * Plik po pliku pobieranie szablonów pracy
     */
    public List<UnitSelfScheduleDTO> getSelfScheduleFromFiles(boolean isAdminTemplate, MultipartFile[] files) throws IOException {
        List<UnitSelfScheduleDTO> selfSchedules = new ArrayList<>();
        for (MultipartFile file : files) {
            selfSchedules.addAll(getSelfSchedulesFromFile(isAdminTemplate, file));
        }
        return selfSchedules;
    }

    /**
     * Pobranie wszystkich planów pracy z jednego pliku
     */
    private List<UnitSelfScheduleDTO> getSelfSchedulesFromFile(boolean isAdminTemplate, MultipartFile file) throws IOException {
        List<UnitSelfScheduleDTO> selfSchedules = new ArrayList<>();
        XSSFWorkbook workbook = new XSSFWorkbook(file.getInputStream());
        Sheet sheet = workbook.getSheetAt(0);
        AtomicInteger atomicInteger = new AtomicInteger(0);
        while (hasNextSection(sheet, atomicInteger.get())) {
            int selfScheduleSection = atomicInteger.get();
            UnitMinDTO unitMinDTO = getUnitFromSheet(sheet, isAdminTemplate, selfScheduleSection);
            FspDTO fsp = getFsp(sheet, isAdminTemplate, selfScheduleSection);
            Instant date = getDateFromSheet(sheet, isAdminTemplate, selfScheduleSection);
            List<MinimalDTO<String, BigDecimal>> listOfHoursPowerFromSheet = getListOfHoursVolumesFromSheet(sheet, isAdminTemplate, selfScheduleSection);
            UnitSelfScheduleDTO unitSelfScheduleDTO = new UnitSelfScheduleDTO();
            unitSelfScheduleDTO.setSelfScheduleDate(date);
            unitSelfScheduleDTO.setUnit(unitMinDTO);
            unitSelfScheduleDTO.setFsp(fsp);
            unitSelfScheduleDTO.setVolumes(listOfHoursPowerFromSheet);
            selfSchedules.add(unitSelfScheduleDTO);
            atomicInteger.addAndGet(SECTION_SIZE.getRowNumber(isAdminTemplate));
        }
        return selfSchedules;
    }

    public FspDTO getFsp(Sheet sheet, boolean isAdmin, int selfScheduleSection) {
        if (isAdmin) {
            return getFspFromSheet(sheet, selfScheduleSection);
        }
        UserDTO fspUser = userService.getCurrentUserDTO().orElseThrow(() -> new RuntimeException("Current logged-in user not found"));
        return fspService.findFspDtoOfUser(fspUser.getId(), fspUser.getLogin())
            .orElseThrow(() -> new RuntimeException("Cannot find fsp of current logged-in user"));
    }

    /**
     * pobieranie FSP z pliku importowanego przez platforme ADMIN
     */
    public FspDTO getFspFromSheet(Sheet sheet, int selfScheduleSection) {
        int rowNumber = FSP_COMPANY_NAME.getRowNumber(true) + selfScheduleSection;
        String companyName = sheet
            .getRow(rowNumber)
            .getCell(FSP_COMPANY_NAME.getColNumber(true)).getStringCellValue(); // ADMIN komorka B1
        return fspService.findByCompanyName(companyName)
            .orElseThrow(() -> new RuntimeException("Cannot find FSP with company name: " + companyName));
    }

    public Instant getDateFromSheet(Sheet sheet, boolean isAdmin, int selfScheduleSection) {
        int rowNumber = SELF_SCHEDULE_DATE.getRowNumber(isAdmin) + selfScheduleSection;
        Cell dateCell = sheet
            .getRow(rowNumber)
            .getCell(SELF_SCHEDULE_DATE.getColNumber(isAdmin)); // ADMIN komorka B2, USER komorka B1
        return dateCell.getDateCellValue().toInstant();
    }

    public UnitMinDTO getUnitFromSheet(Sheet sheet, boolean isAdmin, int selfScheduleSection) {
        int rowNumber = UNIT_NAME.getRowNumber(isAdmin) + selfScheduleSection;
        String derName = sheet
            .getRow(rowNumber)
            .getCell(UNIT_NAME.getColNumber(isAdmin)).getStringCellValue(); // ADMIN komorka B3, USER komorka B2
        return unitService.findUnitByNameIgnoreCase(derName)
            .orElseThrow(() -> new RuntimeException("Cannot find UNIT with name: " + derName));
    }

    /**
     * zwraca sciezke do szablonu w jezyku jaki uzywa uzytkownik
     * oraz w odpowiedniej formie w zaleznosci od platformy z ktorej pobierany jest szablon FSP/ADMIN
     */
    public String getTemplatePath(boolean isAdminTemplate) {
        String userLang = userService.getLangKeyForCurrentLoggedUser();
        String dic = isAdminTemplate ? "admin" : "user";
        String templatePathFormat = "templates/xlsx/self_schedule/%s/import_self_schedule_%s.xlsx";
        return String.format(templatePathFormat, dic, userLang);
    }

    /**
     * uzupelnienie w szablonie komorki z Data harmonogramu (aktualny dzien + 1)
     */
    public ByteArrayOutputStream fillSelfScheduleDataInTemplate(boolean isAdminTemplate, String templatePath) throws IOException {
        Resource templateFileResource = new ClassPathResource(templatePath);
        InputStream template = templateFileResource.getInputStream();
        XSSFWorkbook workbook = new XSSFWorkbook(template);
        XSSFSheet sheet = workbook.getSheetAt(0);
        Instant instantToSet = Instant.now().plus(1, DAYS);
        Date dateToSet = DateUtil.trunc(Date.from(instantToSet), DAYS);
        sheet.getRow(SELF_SCHEDULE_DATE.getRowNumber(isAdminTemplate))
            .getCell(SELF_SCHEDULE_DATE.getColNumber(isAdminTemplate))
            .setCellValue(dateToSet);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();
        return outputStream;
    }

    /**
     * pobieranie z zaimportowanego harmonogramu mocy na dana godzine dnia
     */
    @SneakyThrows
    public List<MinimalDTO<String, BigDecimal>> getListOfHoursVolumesFromSheet(Sheet sheet, boolean isAdminTemplate, int selfScheduleSection) {
        Row hoursRow = sheet.getRow(DAY_HOURS_FIRST_CELL.getRowNumber(isAdminTemplate) + selfScheduleSection);
        Row pricesRow = sheet.getRow(POWER_FIRST_CELL.getRowNumber(isAdminTemplate) + selfScheduleSection);
        LinkedList<MinimalDTO<String, BigDecimal>> hoursList = new LinkedList<>();
        Instant date = getDateFromSheet(sheet, isAdminTemplate, selfScheduleSection);
        int calculatesHour = DateUtil.calculate(date.atZone(ZoneId.systemDefault()).toLocalDate());
        for (int i = 1; i <= calculatesHour; i++) {
            BigDecimal price = getBigDecimalFromCell(pricesRow.getCell(i));
            Cell cell = hoursRow.getCell(i);
            String hours = cell.getCellTypeEnum().equals(CellType.STRING) ? cell.getStringCellValue() : String.valueOf((int) cell.getNumericCellValue());
            hoursList.add(new MinimalDTO<>(hours, price));
        }
        return hoursList;
    }

    public String getTemplateFilename() {
        Locale locale = forLanguageTag(userService.getLangKeyForCurrentLoggedUser());
        return messageSource.getMessage("template.selfSchedule.filename", null, locale);
    }

    public UnitSelfScheduleMinDTO toMinDTO(UnitSelfScheduleDTO unitSelfScheduleDTO) {
        UnitSelfScheduleMinDTO unitSelfScheduleMinDTO = new UnitSelfScheduleMinDTO();
        unitSelfScheduleMinDTO.setSelfScheduleDate(unitSelfScheduleDTO.getSelfScheduleDate());
        unitSelfScheduleMinDTO.setUnitName(unitSelfScheduleDTO.getUnit().getName());
        unitSelfScheduleMinDTO.setFspName(unitSelfScheduleDTO.getFsp().getCompanyName());
        return unitSelfScheduleMinDTO;
    }

    public String selfScheduleErrorToJson(UnitSelfScheduleErrorDTO unitSelfScheduleErrorDTO) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        return objectMapper.writeValueAsString(unitSelfScheduleErrorDTO);
    }
}
