package pl.com.tt.flex.server.validator.auction.da;

import com.google.common.collect.Range;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import pl.com.tt.flex.model.service.dto.MinimalDTO;
import pl.com.tt.flex.model.service.dto.auction.offer.da.AuctionDayAheadOfferDTO;
import pl.com.tt.flex.model.service.dto.auction.offer.da.AuctionOfferBandDataDTO;
import pl.com.tt.flex.model.service.dto.auction.type.AuctionOfferStatus;
import pl.com.tt.flex.model.service.dto.auction.type.AuctionStatus;
import pl.com.tt.flex.server.common.errors.ObjectValidationException;
import pl.com.tt.flex.server.domain.common.enumeration.FileExtension;
import pl.com.tt.flex.server.service.auction.da.AuctionDayAheadService;
import pl.com.tt.flex.server.service.importData.auctionOffer.AuctionOfferImportService;
import pl.com.tt.flex.server.service.importData.auctionOffer.dto.AuctionOfferImportDataResult;
import pl.com.tt.flex.server.validator.common.CommonValidatorUtil;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static pl.com.tt.flex.model.service.dto.auction.type.AuctionOfferStatus.ACCEPTED;
import static pl.com.tt.flex.model.service.dto.auction.type.AuctionOfferStatus.REJECTED;
import static pl.com.tt.flex.server.domain.common.enumeration.FileExtension.XLSX;
import static pl.com.tt.flex.server.service.algorithm.disaggregationAlgorithm.DisaggregationAlgorithmServiceImpl.extractOfferId;
import static pl.com.tt.flex.server.util.AuctionDayAheadDataUtil.areAllPricesTheSameInBandAndTimestamp;
import static pl.com.tt.flex.server.util.AuctionDayAheadDataUtil.getBandDataStreamForBandNumberAndTimestamp;
import static pl.com.tt.flex.server.util.DateUtil.EXTRA_HOUR_CONSTANT;
import static pl.com.tt.flex.server.web.rest.errors.ErrorConstants.*;

@Slf4j
@Component
public class AuctionDayAheadOfferUpdateFileValidator {

    private final Set<FileExtension> SUPPORTED_FILE_EXTENSIONS = Sets.newHashSet(XLSX);
    private final Set<String> SELF_SCHEDULE_LABELS = Set.of("SS", "PP");

    private final AuctionOfferImportService auctionOfferImportService;
    private final AuctionDayAheadService auctionDayAheadService;


    public AuctionDayAheadOfferUpdateFileValidator(AuctionOfferImportService auctionOfferImportService,
        AuctionDayAheadService auctionDayAheadService) {
        this.auctionOfferImportService = auctionOfferImportService;
        this.auctionDayAheadService = auctionDayAheadService;
    }

    public void checkOfferUpdateFileValid(MultipartFile multipartFile) throws ObjectValidationException, IOException {
        try {
            CommonValidatorUtil.checkFileExtensionValid(multipartFile, SUPPORTED_FILE_EXTENSIONS);
            checkOfferUpdateFileContextValid(multipartFile);
        } catch (ObjectValidationException e) {
            notifyBidNotImported(multipartFile, e.getMsgKey());
            throw e;
        } catch (Exception e) {
            notifyBidNotImported(multipartFile, IMPORT_OTHER);
            throw new ObjectValidationException(e.getMessage(), IMPORT_OFFERS_TEMPLATE_INCORRECT);
        }
    }

    private void checkOfferUpdateFileContextValid(MultipartFile multipartFile) throws IOException, ObjectValidationException {
        Workbook workbook = new XSSFWorkbook(multipartFile.getInputStream());
        Sheet sheet = workbook.getSheetAt(0);
        validateUpdateFileColumns(sheet);
        Long offerId = extractOfferId(workbook);
        AuctionDayAheadOfferDTO dbOffer = auctionDayAheadService.findOfferById(offerId)
            .orElseThrow(() -> new RuntimeException("AuctionDayAheadOffer not found with id: " + offerId));
        validateOffersFromAgnoResults(sheet, dbOffer);
        validateAuctionStatus(dbOffer);
        validateOfferStatus(dbOffer);
    }

    private void validateAuctionStatus(AuctionDayAheadOfferDTO dbOffer) throws ObjectValidationException {

        AuctionStatus auctionStatus = auctionDayAheadService.findAuctionStatusById(dbOffer.getAuctionDayAhead().getId());
        if (AuctionStatus.getOpenAuctionStatuses().contains(auctionStatus)) {
            log.debug("validAuctionStatus() Could not import bid with ID: {}, because auction with id {} is OPEN",
                dbOffer.getId(), dbOffer.getAuctionDayAhead().getId());
            throw new ObjectValidationException("Could not import offer because auction is OPEN", IMPORT_OFFER_COULD_NOT_IMPORT_BECAUSE_AUCTION_IS_OPEN);
        }
    }

    private void validateOfferStatus(AuctionDayAheadOfferDTO dbOffer) throws ObjectValidationException {
        List<AuctionOfferStatus> blockingModificationStatuses = Arrays.asList(REJECTED, ACCEPTED);
        if (blockingModificationStatuses.contains(dbOffer.getStatus())) {
            log.debug("validBidStatus() Could not import bid={} with status: {}", dbOffer.getId(), dbOffer.getStatus());
            throw new ObjectValidationException("Cannot import bid with status " + dbOffer.getStatus(), IMPORT_CANNOT_IMPORT_OFFER_WITH_STATUS_REJECTED_OR_ACCEPTED);
        }
        List<AuctionOfferStatus> permittedStatuses = Arrays.asList(AuctionOfferStatus.values());
        if (!permittedStatuses.contains(dbOffer.getStatus())) {
            log.debug("validBidStatus() Could not import bid={} with status: {}", dbOffer.getId(), dbOffer.getStatus());
            throw new ObjectValidationException("Could not identify name of status " + dbOffer.getStatus(), IMPORT_COULD_NOT_IDENTIFY_NAME_OF_STATUS);
        }
    }

    private void validateOffersFromAgnoResults(Sheet sheet, AuctionDayAheadOfferDTO dbOffer) throws ObjectValidationException {
        List<Range<Integer>> offerRowRanges = sheet.getMergedRegions().stream()
            .filter(region -> region.getLastColumn() == 0)
            .map(region -> Range.closed(region.getFirstRow(), region.getLastRow()))
            .collect(Collectors.toList());
        for (Range<Integer> offerRowRange : offerRowRanges) {
            validateAgnoResultOffer(sheet, offerRowRange, dbOffer);
        }
    }

    private void validateAgnoResultOffer(Sheet sheet, Range<Integer> offerRows, AuctionDayAheadOfferDTO dbOffer) throws ObjectValidationException {
        final int FIRST_BAND_ROW_OFFSET = 2;
        int firstBandRowNum = offerRows.lowerEndpoint() + FIRST_BAND_ROW_OFFSET;
        log.debug("validateAgnoResultOffer() - expecting offer bands on rows: {} - {}", firstBandRowNum, offerRows.upperEndpoint());
        Integer nextExpectedBand = null;
        Set<Integer> volumeCellsNr = getVolumeCellsNr(dbOffer);
        for (int rowNum = firstBandRowNum; rowNum <= offerRows.upperEndpoint(); rowNum++) {
            Row row = sheet.getRow(rowNum);
            Cell bandLabelCell = row.getCell(1);
            if (!bandLabelCell.getCellTypeEnum().equals(CellType.STRING)) {
                throw new ObjectValidationException("Cannot import because band label is not a string", IMPORT_OFFERS_TEMPLATE_INCORRECT);
            }
            String bandLabel = bandLabelCell.getStringCellValue().strip().substring(3);
            int bandNumber = 0;
            if (!SELF_SCHEDULE_LABELS.contains(bandLabel)) {
                bandNumber = Integer.parseInt(bandLabel);
            }
            if (Objects.nonNull(nextExpectedBand) && bandNumber != nextExpectedBand) {
                throw new ObjectValidationException("Cannot import because bands are out of order", IMPORT_OFFERS_TEMPLATE_INCORRECT);
            }
            volumeCellsNr.forEach(cellNr -> validateVolume(row, cellNr));
            // Komórka z ceną w pliku znajduje się zawsze po prawej stronie komórki z wolumenem
            volumeCellsNr.forEach(cellNr -> validatePrice(row, cellNr + 1, dbOffer));
            nextExpectedBand = bandNumber - 1;
        }
    }

    private void validatePrice(Row row, int cell, AuctionDayAheadOfferDTO dbOffer) {
        String priceToParse = row.getCell(cell).toString();
        validatePriceBigDecimalNumber(priceToParse);
        validatePriceNotModified(row, cell, dbOffer);
    }

    private void validatePriceNotModified(Row row, int cell, AuctionDayAheadOfferDTO dbOffer) {
        String priceToParse = row.getCell(cell).toString();
        BigDecimal price = null;
        if (!StringUtils.isAllEmpty(priceToParse)) {
            price = new BigDecimal(priceToParse.replaceAll(",", "."));
        }
        BigDecimal expectedPrice = getExpectedPriceForCell(row, cell, dbOffer);
        if (Objects.nonNull(price) && Objects.nonNull(expectedPrice) && price.compareTo(expectedPrice) != 0) {
            throw new ObjectValidationException("Cannot modify offer prices", IMPORT_CANNOT_CHANGE_SCHEDULING_UNIT_PRICE);
        }
    }

    private BigDecimal getExpectedPriceForCell(Row row, int cell, AuctionDayAheadOfferDTO dbOffer) {
        boolean areAllPricesSame = areAllPricesTheSameInBandAndTimestamp(dbOffer, getBandNumber(row.getCell(1).getStringCellValue()), getTimestamp(cell / 2));
        if (areAllPricesSame) {
            return getBandDataStreamForBandNumberAndTimestamp(dbOffer, getBandNumber(row.getCell(1).getStringCellValue()), getTimestamp(cell / 2))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Not found price"))
                .getAcceptedPrice();
        }
        return null;
    }

    private Integer getBandNumber(String band) {
        // Z komórki w formacie "SU +3" wybieramy część po spacji, zawierającą numer pasma
        String bandLabel = band.substring(band.indexOf(" ") + 1);
        if (SELF_SCHEDULE_LABELS.contains(bandLabel)) {
            return 0;
        }
        return Integer.parseInt(bandLabel);
    }

    private String getTimestamp(int timestamp) {
        return timestamp == 25 ? "2a" : String.valueOf(timestamp);
    }

    /**
     * Walidacji komorki z wolumenen
     */
    private void validateVolume(Row row, int cell) {
        String volumeToParse = row.getCell(cell).toString();
        validateVolumeBigDecimalNumber(volumeToParse);
    }

    private void validateUpdateFileColumns(Sheet sheet) throws ObjectValidationException {
        Set<String> volumeColLabels = Set.of("V", "W");
        Set<String> priceColLabels = Set.of("P", "C");
        Row colTypeRow = sheet.getRow(1);
        Row timestampRow = sheet.getRow(0);
        for (int hour = 1; hour <= 24; hour++) {
            boolean timestampsIncorrect = updateTimestampNameValid(timestampRow, hour);
            boolean valueTypeNameIncorrect;
            try {
                valueTypeNameIncorrect = !volumeColLabels.contains(colTypeRow.getCell(hour * 2).getStringCellValue()) ||
                    !priceColLabels.contains(colTypeRow.getCell(hour * 2 + 1).getStringCellValue());
            } catch (Exception ex) {
                throw new ObjectValidationException("Cannot import because of invalid columns", IMPORT_OFFERS_TEMPLATE_INCORRECT);
            }
            if (timestampsIncorrect || valueTypeNameIncorrect) {
                log.debug("validateColumns() - columns incorrect for hour {}, timestamps incorrect: {}, value type name incorrect: {}", hour, timestampsIncorrect, valueTypeNameIncorrect);
                throw new ObjectValidationException("Cannot import because of invalid columns", IMPORT_OFFERS_TEMPLATE_INCORRECT);
            }
        }
    }

    private boolean updateTimestampNameValid(Row timestampRow, int hour) throws ObjectValidationException {
        if (Objects.isNull(timestampRow)) {
            throw new ObjectValidationException("Cannot import because of invalid timestamp row", IMPORT_OFFERS_TEMPLATE_INCORRECT);
        } else if (hour == 25) {
            return true;
        } else {
            try {
                return timestampRow.getCell(hour * 2).getNumericCellValue() != hour;
            } catch (Exception ex) {
                throw new ObjectValidationException("Cannot import because of invalid timestamp row", IMPORT_OFFERS_TEMPLATE_INCORRECT);
            }
        }
    }

    private void notifyBidNotImported(MultipartFile multipartFile, String msgKey) throws IOException {
        log.debug("notifyBidNotImported(): validation failed");
        XSSFWorkbook workbook = new XSSFWorkbook(multipartFile.getInputStream());
        Long offerId = extractOfferId(workbook);
        String idString = null;
        String notImportedCause;
        if (Objects.isNull(offerId)) {
            log.debug("notifyBidNotImported() OfferId is null");
            notImportedCause = IMPORT_OFFER_COULD_NOT_FIND_MATCHING_ID;
        } else {
            idString = offerId.toString();
            notImportedCause = msgKey;
        }
        AuctionOfferImportDataResult importResult = new AuctionOfferImportDataResult();
        importResult.addNotImportedBids(new MinimalDTO<>(idString, notImportedCause));
        auctionOfferImportService.sendNotificationAboutImportOffer(importResult);
    }

    /**
     * Zwraca numery kolumn w ktorych moga byc uzupelnione wolumeny
     */
    private Set<Integer> getVolumeCellsNr(AuctionDayAheadOfferDTO dbOffer) {
        Set<String> hoursNumber = dbOffer.getDers().stream()
            .flatMap(d -> d.getBandData().stream())
            .map(AuctionOfferBandDataDTO::getHourNumber)
            .collect(Collectors.toUnmodifiableSet());

        return hoursNumber.stream()
            .map(hour -> {
                if (hour.equals(EXTRA_HOUR_CONSTANT)) {
                    return 25 * 2;
                }
                return Integer.parseInt(hour) * 2;
            })
            .collect(Collectors.toUnmodifiableSet());
    }

    private void validateVolumeBigDecimalNumber(String volumeToParse) {
        if (!isValidBigDecimalNumber(volumeToParse)) {
            throw new ObjectValidationException("Invalid volume value", IMPORT_INVALID_SCHEDULING_UNIT_VOLUME);
        }
    }

    private void validatePriceBigDecimalNumber(String priceToParse) {
        if (!isValidBigDecimalNumber(priceToParse)) {
            throw new ObjectValidationException("Invalid price value", IMPORT_INVALID_SCHEDULING_UNIT_PRICE);
        }
    }

    private boolean isValidBigDecimalNumber(String value) {
        try {
            if (Strings.isNotEmpty(value)) {
                new BigDecimal(value.replaceAll(",", "."));
            }
        } catch (Exception e) {
            log.debug("isValidBigDecimalNumber() Invalid number value: {}", value);
            return false;
        }
        return true;
    }
}
