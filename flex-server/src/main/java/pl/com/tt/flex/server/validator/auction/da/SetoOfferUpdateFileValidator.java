package pl.com.tt.flex.server.validator.auction.da;

import static org.apache.poi.ss.usermodel.CellType.NUMERIC;
import static pl.com.tt.flex.model.service.dto.auction.type.AuctionOfferStatus.ACCEPTED;
import static pl.com.tt.flex.model.service.dto.auction.type.AuctionOfferStatus.REJECTED;
import static pl.com.tt.flex.server.dataexport.exporter.offer.detail.SetoOfferDetailExporter.*;
import static pl.com.tt.flex.server.domain.common.enumeration.FileExtension.XLSX;
import static pl.com.tt.flex.server.validator.common.CommonValidatorUtil.checkFileExtensionValid;
import static pl.com.tt.flex.server.web.rest.errors.ErrorConstants.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import lombok.extern.slf4j.Slf4j;
import pl.com.tt.flex.model.service.dto.auction.offer.da.AuctionDayAheadOfferDTO;
import pl.com.tt.flex.model.service.dto.auction.offer.da.AuctionOfferBandDataDTO;
import pl.com.tt.flex.model.service.dto.auction.offer.da.AuctionOfferDersDTO;
import pl.com.tt.flex.model.service.dto.auction.type.AuctionOfferStatus;
import pl.com.tt.flex.model.service.dto.auction.type.AuctionStatus;
import pl.com.tt.flex.server.common.errors.ObjectValidationException;
import pl.com.tt.flex.server.domain.common.enumeration.FileExtension;
import pl.com.tt.flex.server.domain.unit.LocalizationTypeEntity;
import pl.com.tt.flex.server.domain.unit.UnitEntity;
import pl.com.tt.flex.server.domain.unit.self_schedule.UnitSelfScheduleEntity;
import pl.com.tt.flex.server.service.auction.da.AuctionDayAheadService;
import pl.com.tt.flex.server.service.unit.UnitService;

@Slf4j
@Component
public class SetoOfferUpdateFileValidator {

    private final Set<FileExtension> SUPPORTED_FILE_EXTENSIONS = Set.of(XLSX);
    private final Set<String> SUPPORTED_LANGUAGES = Set.of("PL", "EN");
    private final Set<AuctionOfferStatus> FORBIDDEN_OFFER_STATUSES = Set.of(ACCEPTED, REJECTED);
    private final Set<AuctionStatus> CLOSED_AUCTION_STATUSES = Set.of(AuctionStatus.CLOSED_CAPACITY, AuctionStatus.CLOSED_ENERGY);
    private static final int ENERGY_TIMESTAMP_GROUP_NUMBER = 3;
    private static final int CAPACITY_TIMESTAMP_GROUP_NUMBER = 2;
    private final AuctionDayAheadService auctionDayAheadService;
    private final UnitService unitService;
    protected final MessageSource messageSource;

    public SetoOfferUpdateFileValidator(final AuctionDayAheadService auctionDayAheadService, final UnitService unitService, final MessageSource messageSource) {
        this.auctionDayAheadService = auctionDayAheadService;
        this.unitService = unitService;
        this.messageSource = messageSource;
    }

    @Transactional(readOnly = true)
    public void checkOfferUpdateFileValid(MultipartFile multipartFile) throws IOException {
        checkFileExtensionValid(multipartFile, SUPPORTED_FILE_EXTENSIONS);
        XSSFWorkbook workbook = new XSSFWorkbook(multipartFile.getInputStream());
        try {
            List<Long> bidIds = getBidIds(workbook);
            Set<AuctionDayAheadOfferDTO> offers = auctionDayAheadService.findAllOffersById(bidIds);
            verifyOfferStatus(offers);
            verifyAuctionsClosed(offers);
            List<UnitSelfScheduleEntity> selfSchedules = auctionDayAheadService.getSelfSchedulesForDersInOffers(bidIds);
            for (int i = 1; i < workbook.getNumberOfSheets(); i++) {
                XSSFSheet sheet = workbook.getSheetAt(i);
                if (isEnergySheet(sheet)) {
                    checkEnergySheetValid(offers, selfSchedules, sheet);
                } else {
                    checkCapacitySheetValid(offers, selfSchedules, sheet);
                }
            }
        } catch (Exception e) {
            if (!e.getClass().equals(ObjectValidationException.class)) {
                log.info("Could not validate seto imported file with cause: {}", e.getMessage());
                throw new ObjectValidationException("Cannot process file", IMPORT_OFFERS_TEMPLATE_INCORRECT);
            } else {
                throw e;
            }
        }
    }

    //*************************************************** CAPACITY VALIDATOR ************************************

    private void checkCapacitySheetValid(Set<AuctionDayAheadOfferDTO> offers, List<UnitSelfScheduleEntity> selfSchedules, XSSFSheet sheet) {
        String timestamp = extractTimestamp(sheet, CAPACITY_TIMESTAMP_GROUP_NUMBER);
        verifyCapacityHeaders(sheet);
        verifyCapacityDerNames(sheet, offers);
        verifyCapacityUnmodifiableColumns(sheet, selfSchedules, timestamp);
        verifyCapacityVolumes(sheet, offers, timestamp);
    }

    private void verifyCapacityHeaders(XSSFSheet sheet) {
        XSSFRow headerRow = sheet.getRow(0);
        for (int colNum = 0; colNum < CAPACITY_HEADERS.size(); colNum++) {
            Set<String> allowedHeaders = new HashSet<>();
            for (String languageTag : SUPPORTED_LANGUAGES) {
                allowedHeaders.add(messageSource.getMessage(MESSAGE_PREFIX + CAPACITY_HEADERS.get(colNum), null, Locale.forLanguageTag(languageTag)));
            }
            if (!allowedHeaders.contains(headerRow.getCell(colNum).getStringCellValue())) {
                throw new ObjectValidationException("Incorrect column headers", IMPORT_OFFERS_TEMPLATE_INCORRECT);
            }
        }
    }

    private void verifyCapacityVolumes(XSSFSheet sheet, Set<AuctionDayAheadOfferDTO> offers, String timestamp) {
        Iterator<Row> rowIterator = sheet.rowIterator();
        rowIterator.next(); //Pomiń rząd nagłówków
        rowIterator.forEachRemaining(row -> {
            for (int colNum = 7; colNum < row.getLastCellNum(); colNum++) {
                Cell volumeCell = row.getCell(colNum);
                verifyNotADate(volumeCell);
                try {
                    String stringCellValue = getCellValue(volumeCell);
                    if (!Set.of("BRAK", "LACK").contains(stringCellValue)) {
                        verifyDecimalPlaces(stringCellValue);
                        double cellValue = Double.parseDouble(stringCellValue);
                        if (cellValue < 0) {
                            throw new ObjectValidationException("Volume value out of bounds", IMPORT_ACCEPTED_VOLUME_CANNOT_BE_LOWER_THAN_ZERO);
                        } else if (cellValue > getCapacityExpectedVolume(row, colNum, offers, timestamp, sheet)) {
                            throw new ObjectValidationException("Volume value out of bounds", IMPORT_ACCEPTED_VOLUME_CANNOT_BE_GREATER_THAN_VOLUME_OF_OFFER);
                        }
                    }
                } catch (NumberFormatException e) {
                    log.info(e.getMessage());
                    throw new ObjectValidationException("Wrong volume format", IMPORT_WRONG_FORMAT_OF_ACCEPTED_VOLUME);
                }
            }
        });
    }

    private void verifyCapacityDerNames(XSSFSheet sheet, Set<AuctionDayAheadOfferDTO> offers) {
        Set<String> derNames = offers.stream().map(AuctionDayAheadOfferDTO::getDers).flatMap(List::stream).map(der -> der.getDer().getName()).collect(Collectors.toSet());
        Iterator<Row> rowIterator = sheet.rowIterator();
        rowIterator.next(); //Pomiń rząd nagłówków
        rowIterator.forEachRemaining(row -> {
            String cellValue = row.getCell(0).getStringCellValue();
            if (derNames.contains(cellValue)) {
                derNames.remove(cellValue); // Usuwamy wykryte nazwy, żeby wykryć duplikaty w importowanym pliku
            } else {
                throw new ObjectValidationException("Cannot import because der names are incorrect", IMPORT_OFFERS_TEMPLATE_INCORRECT);
            }
        });
    }

    private void verifyCapacityUnmodifiableColumns(XSSFSheet sheet, List<UnitSelfScheduleEntity> selfSchedules, String timestamp) {
        Iterator<Row> rowIterator = sheet.rowIterator();
        rowIterator.next(); //Pomiń rząd nagłówków
        rowIterator.forEachRemaining(row -> {
            String derName = row.getCell(0).getStringCellValue();
            UnitEntity der = unitService.getByName(derName);
            BigDecimal selfScheduleValue = selfSchedules.stream().filter(ss -> ss.getUnit().getId().equals(der.getId()))
                .findAny().map(UnitSelfScheduleEntity::getVolumes).map(map -> map.get(timestamp)).get();
            boolean couplingPointModified = !getCellValue(row.getCell(1)).equals(der.getCouplingPointIdTypes().stream().findAny().get().getName());
            boolean powerStationModified2 = !getCellValue(row.getCell(2)).equals(der.getPowerStationTypes().stream().findAny().get().getName());
            boolean pointOfConnectionModified = !getCellValue(row.getCell(3)).equals(der.getPointOfConnectionWithLvTypes().stream().findAny().map(LocalizationTypeEntity::getName).orElse(""));
            boolean selfScheduleModified = !getCellValue(row.getCell(4)).equals(selfScheduleValue.toString());
            boolean pMinModified = !getCellValue(row.getCell(5)).equals(der.getPMin().toString());
            boolean pMaxModified = !getCellValue(row.getCell(6)).equals(der.getSourcePower().toString());
            if (couplingPointModified || powerStationModified2 || pointOfConnectionModified || selfScheduleModified || pMinModified || pMaxModified) {
                throw new ObjectValidationException("Cannot import because columns B-G have been modified", IMPORT_OFFERS_TEMPLATE_INCORRECT);
            }
        });
    }

    private double getCapacityExpectedVolume(Row row, int colNum, Set<AuctionDayAheadOfferDTO> offers, String timestamp, XSSFSheet sheet) {
        String productHeader = sheet.getRow(0).getCell(colNum).getStringCellValue();
        Long auctionId = Long.parseLong(productHeader.substring(productHeader.lastIndexOf("-") + 2));
        return offers.stream()
            .filter(offer -> offer.getAuctionDayAhead().getId().equals(auctionId))
            .map(AuctionDayAheadOfferDTO::getDers).flatMap(List::stream)
            .filter(der -> der.getDer().getName().equals(row.getCell(0).getStringCellValue()))
            .map(AuctionOfferDersDTO::getBandData).flatMap(List::stream)
            .filter(band -> band.getHourNumber().equals(timestamp) && band.getBandNumber() != 0)
            .map(AuctionOfferBandDataDTO::getAcceptedVolume)
            .findAny().orElseThrow(() -> new ObjectValidationException("Assigned volume to der for invalid auction", IMPORT_OFFERS_TEMPLATE_INCORRECT))
            .doubleValue();
    }

    //*************************************************** ENERGY VALIDATOR ************************************
    private void checkEnergySheetValid(Set<AuctionDayAheadOfferDTO> offers, List<UnitSelfScheduleEntity> selfSchedules, XSSFSheet sheet) {
        String timestamp = extractTimestamp(sheet, ENERGY_TIMESTAMP_GROUP_NUMBER);
        verifyEnergyHeaders(sheet);
        verifyEnergyDerNames(sheet, offers);
        verifyEnergyUnmodifiableRows(sheet, selfSchedules, timestamp);
        verifyEnergyVolumes(sheet, offers, timestamp);
    }

    private void verifyEnergyHeaders(XSSFSheet sheet) {
        AtomicInteger rowNum = new AtomicInteger(0);
        // wiersz z ID aukcji
        try{
            sheet.getRow(rowNum.get()).getCell(0).getNumericCellValue();
        } catch(Exception ex ){
            throw new ObjectValidationException("Auction ID is not a number", IMPORT_OFFERS_TEMPLATE_INCORRECT);
        }
        // wiersze z nagłówkami
        while (rowNum.incrementAndGet() < 6) {
            Set<String> allowedHeaders = new HashSet<>();
            for (String languageTag : SUPPORTED_LANGUAGES) {
                allowedHeaders.add(messageSource.getMessage(MESSAGE_PREFIX + ENERGY_HEADERS.get(rowNum.get() - 1), null, Locale.forLanguageTag(languageTag)));
            }
            if(!allowedHeaders.contains(getCellValue(sheet.getRow(rowNum.get()).getCell(0)))) {
                throw new ObjectValidationException("Incorrect column headers", IMPORT_OFFERS_TEMPLATE_INCORRECT);
            }
        }
        //wiersze z etykietami pasm
        int previousBandLabel = getBandNumber(getCellValue(sheet.getRow(rowNum.getAndIncrement()).getCell(0)));
        while( rowNum.get() <= sheet.getLastRowNum()){
            int currentBandLabel = getBandNumber(getCellValue(sheet.getRow(rowNum.getAndIncrement()).getCell(0)));
            if(previousBandLabel - currentBandLabel != 1){
                throw new ObjectValidationException("Incorrect band labels", IMPORT_OFFERS_TEMPLATE_INCORRECT);
            }
            previousBandLabel = currentBandLabel;
        }
    }

    private void verifyEnergyDerNames(XSSFSheet sheet, Set<AuctionDayAheadOfferDTO> offers) {
        Set<String> derNames = offers.stream().map(AuctionDayAheadOfferDTO::getDers).flatMap(List::stream).map(der -> der.getDer().getName()).collect(Collectors.toSet());
        Row row = sheet.getRow(0);
        // Pomijamy kolumnę z nagłówkami
        for (int currentColumn = 1; currentColumn < row.getLastCellNum(); currentColumn++) {
            String cellValue = getCellValue(row.getCell(currentColumn));
            if (derNames.contains(cellValue)) {
                derNames.remove(cellValue); // Usuwamy wykryte nazwy, żeby wykryć duplikaty w importowanym pliku
            } else {
                throw new ObjectValidationException("Cannot import because der names are incorrect", IMPORT_OFFERS_TEMPLATE_INCORRECT);
            }
        }
    }

    private void verifyEnergyUnmodifiableRows(XSSFSheet sheet, List<UnitSelfScheduleEntity> selfSchedules, String timestamp) {
        AtomicInteger colNum = new AtomicInteger(0);
        while(colNum.incrementAndGet() < sheet.getRow(0).getLastCellNum()){
            AtomicInteger rowNum = new AtomicInteger(0);
            String derName = getCellValue(sheet.getRow(rowNum.getAndIncrement()).getCell(colNum.get()));
            UnitEntity der = unitService.getByName(derName);
            BigDecimal selfScheduleValue = selfSchedules.stream().filter(ss -> ss.getUnit().getId().equals(der.getId()))
                .findAny().map(UnitSelfScheduleEntity::getVolumes).map(map -> map.get(timestamp)).get();
            boolean couplingPointModified = !getCellValue(sheet.getRow(rowNum.getAndIncrement()).getCell(colNum.get())).equals(der.getCouplingPointIdTypes().stream().findAny().get().getName());
            boolean powerStationModified2 = !getCellValue(sheet.getRow(rowNum.getAndIncrement()).getCell(colNum.get())).equals(der.getPowerStationTypes().stream().findAny().get().getName());
            boolean pointOfConnectionModified = !getCellValue(sheet.getRow(rowNum.getAndIncrement()).getCell(colNum.get())).equals(der.getPointOfConnectionWithLvTypes().stream().findAny().map(LocalizationTypeEntity::getName).orElse(""));
            boolean pMinModified = !getCellValue(sheet.getRow(rowNum.getAndIncrement()).getCell(colNum.get())).equals(der.getPMin().toString());
            boolean pMaxModified = !getCellValue(sheet.getRow(rowNum.getAndIncrement()).getCell(colNum.get())).equals(der.getSourcePower().toString());
            while(!List.of("SS", "PP").contains(getCellValue(sheet.getRow(rowNum.get()).getCell(0)))){
                rowNum.getAndIncrement();
                if(rowNum.get() > sheet.getLastRowNum()){
                    throw new ObjectValidationException("No self schedule row found", IMPORT_OFFERS_TEMPLATE_INCORRECT);
                }
            }
            boolean selfScheduleModified = !getCellValue(sheet.getRow(rowNum.getAndIncrement()).getCell(colNum.get())).equals(selfScheduleValue.toString());
            if (couplingPointModified || powerStationModified2 || pointOfConnectionModified || pMinModified || pMaxModified) {
                throw new ObjectValidationException("Cannot import because rows 1-6 have been modified", IMPORT_OFFERS_TEMPLATE_INCORRECT);
            }
            if (selfScheduleModified){
                throw new ObjectValidationException("Cannot import because self schedule was modified", IMPORT_OFFERS_TEMPLATE_INCORRECT);
            }
        }
    }

    private void verifyEnergyVolumes(XSSFSheet sheet, Set<AuctionDayAheadOfferDTO> offers, String timestamp) {
        AtomicInteger rowNum = new AtomicInteger(6);
        while(rowNum.get() <= sheet.getLastRowNum()){
            Row row = sheet.getRow(rowNum.getAndIncrement());
            if(List.of("SS", "PP").contains(getCellValue(row.getCell(0)))){
                // Pomijamy wiersz z planem pracy, który został zwalidowany wcześniej
                row = sheet.getRow(rowNum.getAndIncrement());
                if(Objects.isNull(row)){
                    break;
                }
            }
            for (int colNum = 1; colNum < row.getLastCellNum(); colNum++) {
                try {
                    String stringCellValue = getCellValue(row.getCell(colNum));
                    if (!Set.of("BRAK", "LACK").contains(stringCellValue)) {
                        verifyNotADate(row.getCell(colNum));
                        verifyDecimalPlaces(stringCellValue);
                        double cellValue = Double.parseDouble(stringCellValue);
                        if (cellValue < 0) {
                            throw new ObjectValidationException("Volume value out of bounds", IMPORT_ACCEPTED_VOLUME_CANNOT_BE_LOWER_THAN_ZERO);
                        } else {
                            double expectedVolume = getExpectedVolumeEnergy(row, offers, timestamp, sheet, colNum);
                            if(cellValue > expectedVolume){
                                throw new ObjectValidationException("Volume value out of bounds", IMPORT_ACCEPTED_VOLUME_CANNOT_BE_GREATER_THAN_VOLUME_OF_OFFER);
                            }
                            if(cellValue < expectedVolume && checkHigherBandsNotRemoved(sheet, colNum, row)){
                                throw new ObjectValidationException("Volume decreased in band, that isn't the highest present band in direction.", IMPORT_VOLUME_IN_BAND_CANNOT_BE_MODIFIED_IF_HIGHER_BAND_EXISTS);
                            }
                        }
                    }
                } catch (NumberFormatException e) {
                    log.info(e.getMessage());
                    throw new ObjectValidationException("Wrong volume format ", IMPORT_WRONG_FORMAT_OF_ACCEPTED_VOLUME);
                }
            }
        }
    }

    private boolean checkHigherBandsNotRemoved(XSSFSheet sheet, int colNum, Row row) {
        int currentBand = getBandNumber(getCellValue(row.getCell(0)));
        if(currentBand > 0){
            for(int i = 6; i<row.getRowNum(); i++){
                if (checkVolumeNotNullOrZero(sheet, colNum, i)) return true;
            }
        } else {
            for(int i = row.getRowNum() + 1; i <= sheet.getLastRowNum(); i++){
                if (checkVolumeNotNullOrZero(sheet, colNum, i)) return true;
            }
        }
        return false;
    }

    private boolean checkVolumeNotNullOrZero(XSSFSheet sheet, int colNum, int i) {
        String volumeToCheck = getCellValue(sheet.getRow(i).getCell(colNum));
        return !Set.of("BRAK", "LACK").contains(volumeToCheck) && Double.parseDouble(volumeToCheck) != 0;
    }

    private double getExpectedVolumeEnergy(Row row, Set<AuctionDayAheadOfferDTO> offers, String timestamp, XSSFSheet sheet, int colNum) {
        Long auctionId = (long) sheet.getRow(0).getCell(0).getNumericCellValue();
        return offers.stream()
            .filter(offer -> offer.getAuctionDayAhead().getId().equals(auctionId))
            .map(AuctionDayAheadOfferDTO::getDers).flatMap(List::stream)
            .filter(der -> der.getDer().getName().equals(sheet.getRow(0).getCell(colNum).getStringCellValue()))
            .map(AuctionOfferDersDTO::getBandData).flatMap(List::stream)
            .filter(band -> band.getHourNumber().equals(timestamp) && band.getBandNumber() == getBandNumber(getCellValue(row.getCell(0))))
            .map(AuctionOfferBandDataDTO::getAcceptedVolume)
            .filter(Objects::nonNull)
            .map(BigDecimal::doubleValue)
            .findAny().orElse(0.0);
    }

    private int getBandNumber(String bandLabel) {
        if(List.of("SS", "PP").contains(bandLabel)){
            return 0;
        }
        try{
            return (int)Double.parseDouble(bandLabel);
        } catch(Exception ex){
            throw new ObjectValidationException("Incorrect band label: " + bandLabel, IMPORT_OFFERS_TEMPLATE_INCORRECT);
        }
    }

    private boolean isEnergySheet(XSSFSheet sheet) {
        return sheet.getSheetName().contains("EB");
    }

    //*************************************************** COMMONS ************************************

    private String extractTimestamp(XSSFSheet sheet, int timestampGroupNumber) {
        String sheetName = sheet.getSheetName();
        return sheetName.split(" ")[timestampGroupNumber];
    }

    private List<Long> getBidIds(XSSFWorkbook workbook) {
        XSSFSheet sheet = workbook.getSheetAt(0);
        List<Long> bidIds = new ArrayList<>();
        Iterator<Row> rowIterator = sheet.rowIterator();
        rowIterator.next(); //Pomiń rząd nagłówków
        rowIterator.forEachRemaining(row -> {
            double cellValue = Double.parseDouble(row.getCell(0).getStringCellValue());
            bidIds.add(Math.round(cellValue));
        });
        return bidIds;
    }

    private String getCellValue(Cell cell) {
        if (cell.getCellTypeEnum().equals(CellType.STRING)) {
            return cell.getStringCellValue();
        }
        return String.valueOf(cell.getNumericCellValue());
    }

    private void verifyNotADate(Cell cellValue) {
        if (cellValue.getCellTypeEnum().equals(NUMERIC) && DateUtil.isCellDateFormatted(cellValue)) {   //Excel zapisuje daty jako pole numeryczne z liczbą dni od daty 1 Stycznia 1900
            throw new ObjectValidationException("Value is a date", IMPORT_WRONG_FORMAT_OF_ACCEPTED_VOLUME);
        }
    }

    private void verifyDecimalPlaces(String stringCellValue) {
        String[] numberParts = stringCellValue.split("\\.");
        if (numberParts.length > 1 && numberParts[1].length() > 3) {
            throw new ObjectValidationException("Too many decimal places", IMPORT_OFFERS_TEMPLATE_INCORRECT);
        }
    }

    private void verifyAuctionsClosed(Set<AuctionDayAheadOfferDTO> offers) {
        if(offers.stream().anyMatch(offer -> !CLOSED_AUCTION_STATUSES.contains(offer.getAuctionStatus()))) {
            log.info("Trying to import offer for auction with unsupported status");
            throw new ObjectValidationException("Cannot import offers for open auctions", IMPORT_OFFER_COULD_NOT_IMPORT_BECAUSE_AUCTION_IS_OPEN);
        }
    }

    private void verifyOfferStatus(Set<AuctionDayAheadOfferDTO> offers) {
        if(offers.stream().anyMatch(offer -> FORBIDDEN_OFFER_STATUSES.contains(offer.getStatus()))) {
            log.info("Trying to import offer with unsupported status");
            throw new ObjectValidationException("Cannot import offers with status accepted or rejected", IMPORT_CANNOT_IMPORT_OFFER_WITH_STATUS_REJECTED_OR_ACCEPTED);
        }
    }
}
