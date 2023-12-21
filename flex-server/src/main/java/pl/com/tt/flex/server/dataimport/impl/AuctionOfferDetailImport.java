package pl.com.tt.flex.server.dataimport.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.mapstruct.ap.internal.util.Strings;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import pl.com.tt.flex.model.service.dto.file.FileDTO;
import pl.com.tt.flex.server.common.errors.ObjectValidationException;
import pl.com.tt.flex.server.dataimport.DataImport;
import pl.com.tt.flex.server.dataimport.factory.DataImportFormat;
import pl.com.tt.flex.server.service.importData.auctionOffer.dto.AuctionOfferSchedulingUnitDTO;
import pl.com.tt.flex.server.util.DateUtil;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static pl.com.tt.flex.server.dataexport.exporter.offer.util.OfferExportUtils.*;
import static pl.com.tt.flex.server.dataexport.util.CellUtils.checkIfExistCellWithPatternInRow;
import static pl.com.tt.flex.server.util.DateUtil.EXTRA_HOUR_CONSTANT;
import static pl.com.tt.flex.server.util.MessageUtils.*;
import static pl.com.tt.flex.server.web.rest.errors.ErrorConstants.IMPORT_OFFERS_TEMPLATE_INCORRECT;

@Component
@Slf4j
public class AuctionOfferDetailImport implements DataImport<AuctionOfferSchedulingUnitDTO> {
    public static final Pattern BAND_NR_PATTERN = Pattern.compile("(JG ([+-][\\d]|PP))|(SU ([+-][\\d]|SS))"); //np. JG PP, JG -1, JG +1 lub SU SS, SU -1
    private final Pattern ACCEPTED_OFFER_ID_PATTERN = Pattern.compile("([\\d]+up)?[\\d|\\w|\\W]*|([\\d]+)[\\d|\\w|\\W]*\n"); //np. 1234 - Produkt1 - JG1
    private final Pattern OFFER_UPDATE_ID_PATTERN = Pattern.compile("([\\d]+up)[\\d|\\w|\\W]*"); //np. 1234up - Produkt1 - JG1
    private final MessageSource messageSource;

    public AuctionOfferDetailImport(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @Override
    public List<AuctionOfferSchedulingUnitDTO> doImport(MultipartFile multipartFile, Locale locale) throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook(multipartFile.getInputStream());
        XSSFSheet sheet = getSheetWithDetails(workbook);
        return findAndReadSelfScheduleSections(sheet);
    }

    @Override
    public List<AuctionOfferSchedulingUnitDTO> doImport(FileDTO fileDTO, Locale locale) throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(fileDTO.getBytesData()));
        XSSFSheet sheet = getSheetWithDetails(workbook);
        return findAndReadSelfScheduleSections(sheet);
    }

    /**
     * Czytanie pliku linia po lini i szukanie zaaktualizowanej oferty (np. 1234up')
     * i wczytanie sekcji z jednostką grafikową.
     */
    private List<AuctionOfferSchedulingUnitDTO> findAndReadSelfScheduleSections(XSSFSheet sheet) {
        log.info("findAndReadSelfScheduleSections() Start - try to find and read volumes in SchedulingUnit section");
        List<AuctionOfferSchedulingUnitDTO> schedulingUnitsBands = new ArrayList<>();
        if (sheet != null) {
            Iterator<Row> rowIterator = sheet.rowIterator();
            List<String> offerDescriptions = getOfferDescriptions(sheet);
            while (rowIterator.hasNext()) {
                XSSFRow maybeRowWithOfferId = (XSSFRow) rowIterator.next();
                if (checkIfExistCellWithPatternInRow(maybeRowWithOfferId, OFFER_ID_CELL_NR, CellType.STRING, ACCEPTED_OFFER_ID_PATTERN)) { //sprawdza poprawność wszystkich id w karcie
                    offerDescriptions.remove(maybeRowWithOfferId.getCell(OFFER_ID_CELL_NR).getStringCellValue());
                    if (checkIfExistCellWithPatternInRow(maybeRowWithOfferId, OFFER_ID_CELL_NR, CellType.STRING, OFFER_UPDATE_ID_PATTERN)) { //sprawdza czy id zawiera dopisek "up"
                        validateTimestamp(maybeRowWithOfferId);
                        String offerId = getOfferIdValue(maybeRowWithOfferId);
                        log.info("findAndReadSelfScheduleSections() Find accepted offer with name: {}.", offerId);
                        XSSFRow maybeLineWithBand = (XSSFRow) rowIterator.next();
                        do {
                            schedulingUnitsBands.addAll(getRowSchedulingUnitVolumes(offerId, maybeLineWithBand));
                            maybeLineWithBand = (XSSFRow) rowIterator.next();
                        } while (checkIfExistCellWithPatternInRow(maybeLineWithBand, SCHEDULING_UNIT_BAND_NR_CELL, CellType.STRING, BAND_NR_PATTERN));
                        validateVolumeAndPriceSign(maybeLineWithBand);
                    }
                }
            }
            if (offerDescriptions.size() > 0) {
                throw new ObjectValidationException("Incorrect offer descriptions: " + offerDescriptions, IMPORT_OFFERS_TEMPLATE_INCORRECT);
            }
        } else {
            log.info("findAndReadSelfScheduleSections() Sheet is empty or has wrong name!");
        }
        log.info("findAndReadSelfScheduleSections() End - try to find and read volumes in SchedulingUnit section");
        return schedulingUnitsBands;
    }

    /**
    * Sprawdzenie tiemstapow
    */
    private void validateTimestamp(XSSFRow currentRow) {
        AtomicInteger currentCell = new AtomicInteger(TIMESTAMP_START_CELL);
        for (int i = 1; i <= 25; i++) {
            String timestamp = i == 25 ? EXTRA_HOUR_CONSTANT : String.valueOf(i);
            int timestampCellNr = currentCell.getAndAdd(2);
            XSSFCell cellWithTimestamp = currentRow.getCell(timestampCellNr);
            if (Objects.isNull(cellWithTimestamp) || !timestamp.equals(cellWithTimestamp.toString())) {
                throw new ObjectValidationException(String.format("Invalid timestamp in row: %s, cell: %s", currentRow.getRowNum(), timestampCellNr), IMPORT_OFFERS_TEMPLATE_INCORRECT);
            }
        }
    }

    /**
    * Sprawedzenie znakow Wolumenow ('W' lub 'V') oraz Cen ('C' lub 'P')
    */
    private void validateVolumeAndPriceSign(XSSFRow currentRow) {
        AtomicInteger currentCell = new AtomicInteger(TIMESTAMP_START_CELL);
        List<String> volumeSigns = getMessagesForLanguages(messageSource, VOLUME_SHORTCUT, LOCALE_PL, LOCALE_EN);
        List<String> priceSigns = getMessagesForLanguages(messageSource, PRICE_SHORTCUT, LOCALE_PL, LOCALE_EN);

        for (int i = 1; i <= 25; i++) {
            int volumeCellNr = currentCell.getAndIncrement();
            int priceCellNr = currentCell.getAndIncrement();
            XSSFCell cellWithVolumeSign = currentRow.getCell(volumeCellNr);
            XSSFCell cellWithPriceSign = currentRow.getCell(priceCellNr);
            if (Objects.isNull(cellWithVolumeSign) || !volumeSigns.contains(cellWithVolumeSign.toString())) {
                throw new ObjectValidationException(String.format("Invalid volume sign in row: %s, cell: %s", currentRow.getRowNum(), volumeCellNr),
                    IMPORT_OFFERS_TEMPLATE_INCORRECT);
            }
            if (Objects.isNull(cellWithPriceSign) || !priceSigns.contains(cellWithPriceSign.toString())) {
                throw new ObjectValidationException(String.format("Invalid price sign in row: %s, cell: %s", currentRow.getRowNum(), priceCellNr),
                    IMPORT_OFFERS_TEMPLATE_INCORRECT);
            }
        }
    }

    /**
     * Metoda wczytuje z pliku komórki z opisami ofert (np. 3074 - EB - JG_3_DANO) i dodaje wartości tekstowe opisów do listy.
     */
    private List<String> getOfferDescriptions(XSSFSheet sheet) {
        List<String> cellsWithIds = new ArrayList<>();
        Iterator<Row> rowIterator = sheet.rowIterator();
        while (rowIterator.hasNext()) {
            String cell = rowIterator.next().getCell(OFFER_ID_CELL_NR).getStringCellValue();
            if (!StringUtils.isAllBlank(cell) && !cellsWithIds.contains(cell)) {
                cellsWithIds.add(cell);
            }
        }
        return cellsWithIds;
    }

    /**
     * Z komorki wyciąga tylko IDik: np.:
     * 1234up - Produkt1 - JG1 -> 1234up
     */
    private String getOfferIdValue(XSSFRow maybeRowWithOfferId) {
        String idCellValue = maybeRowWithOfferId.getCell(OFFER_ID_CELL_NR).getStringCellValue();
        Matcher matcher = ACCEPTED_OFFER_ID_PATTERN.matcher(idCellValue);
        matcher.matches();
        return matcher.group(1);
    }

    /**
     * Metoda znajdująca dla dnaego wiersza wszystkie uzupelnione timestampy
     */
    private List<AuctionOfferSchedulingUnitDTO> getRowSchedulingUnitVolumes(String offerId, XSSFRow rowWithBand) {
        List<AuctionOfferSchedulingUnitDTO> schedulingUnitsBands = new ArrayList<>();
        int bandNumberFromRom = getBandNumberFromRow(rowWithBand);
        AtomicInteger volumeCellIndex = new AtomicInteger(3);
        log.info("getRowSchedulingUnitVolumes() Start - read all timestamp volumes for offer={} and bandNr={}", offerId, bandNumberFromRom);
        for (int i = 1; i <= 25; i++) {
            String timestamp = i == 25 ? DateUtil.EXTRA_HOUR_CONSTANT : String.valueOf(i);
            addTimestampVolumeAndPriceIfExist(schedulingUnitsBands, offerId, bandNumberFromRom, timestamp, rowWithBand, volumeCellIndex.get());
            volumeCellIndex.addAndGet(2);
        }
        log.info("getRowSchedulingUnitVolumes() End - read all timestamp volumes for offer={} and bandNr={}", offerId, bandNumberFromRom);
        return schedulingUnitsBands;
    }

    /**
     * Dodanie do listy znalezionych cen i wolumenow dla pasmi i timestampwo, jezli cena lub wolumen w danym
     * pasmie i timestampie zostal uzupelniony
     */
    private void addTimestampVolumeAndPriceIfExist(List<AuctionOfferSchedulingUnitDTO> schedulingUnitsBands,
        String offerId, int bandNumberFromRom, String timestamp, XSSFRow row, int cellIndex) {
        XSSFCell volumeCell = row.getCell(cellIndex);
        String volume = getVolumeForBandAndTimestamp(offerId, bandNumberFromRom, timestamp, volumeCell);
        XSSFCell priceCell = row.getCell(cellIndex + 1);
        String price = getPriceForBandAndTimestamp(offerId, bandNumberFromRom, timestamp, priceCell);
        if (Objects.nonNull(price) || Objects.nonNull(volume)) {
            AuctionOfferSchedulingUnitDTO auctionOfferSchedulingUnitDTO = new AuctionOfferSchedulingUnitDTO(offerId, bandNumberFromRom, timestamp, volume, price);
            schedulingUnitsBands.add(auctionOfferSchedulingUnitDTO);
            log.debug("addTimestampVolumeAndPriceIfExist() Found: {}", auctionOfferSchedulingUnitDTO);
        }
    }

    /**
     * Metoda pobiarającega z danego timestampa i pasma wolumen z sekcji jednostki grafikowej.
     */
    private String getVolumeForBandAndTimestamp(String offerId, int bandNumberFromRom, String timestamp, XSSFCell volumeCell) {
        String volume = null;
        if (Objects.nonNull(volumeCell) && !volumeCell.getCellTypeEnum().equals(CellType.BLANK)) {
            volume = getStringCellValue(volumeCell);
            log.debug("addTimestampVolumeAndPriceIfExist() Found volume={}: offerId={}, bandNr={}, timestamp={}", volume, offerId, bandNumberFromRom, timestamp);
        }
        return volume;
    }

    /**
     * Metoda pobiarającega z danego timestampa i pasma cenę z sekcji jednostki grafikowej.
     */
    private String getPriceForBandAndTimestamp(String offerId, int bandNumberFromRom, String timestamp, XSSFCell priceCell) {
        String price = null;
        if (Objects.nonNull(priceCell) && !priceCell.getCellTypeEnum().equals(CellType.BLANK)) {
            price = getStringCellValue(priceCell);
            log.debug("addTimestampVolumeAndPriceIfExist() Found price={}: offerId={}, bandNr={}, timestamp={}", price, offerId, bandNumberFromRom, timestamp);
        }
        return price;
    }

    /**
     * Pobranie numeru pasma z sekcji jednostki grafikowej
     */
    private int getBandNumberFromRow(XSSFRow row) {
        XSSFCell cellWithBandNumber = row.getCell(SCHEDULING_UNIT_BAND_NR_CELL);
        List<String> selfScheduleShortcuts = getMessagesForLanguages(messageSource, SELF_SCHEDULE_SHORTCUT, LOCALE_PL, LOCALE_EN);
        String stringBandNumber = getStringBandNumberFromCell(cellWithBandNumber);
        return getBandNr(selfScheduleShortcuts, stringBandNumber);
    }

    private int getBandNr(List<String> selfScheduleShortcuts, String stringBandNumber) {
        if (Strings.isEmpty(stringBandNumber)) {
            throw new ObjectValidationException("Incorrect offer band nr: " + stringBandNumber, IMPORT_OFFERS_TEMPLATE_INCORRECT);
        }
        return selfScheduleShortcuts.contains(stringBandNumber) ? 0 : Integer.parseInt(stringBandNumber);
    }

    /**
     * Metoda pobiera numer pasma z komorki
     * Gdy eksportowany plik jest zapisany w jezyku polskim ucina z numeru pasma 'JG',
     * gdy eskportowany plik jest zapisany w jezyku angielskim ucina z bumeru pasma 'SU'
     */
    private String getStringBandNumberFromCell(XSSFCell cellWithBandNumber) {
        String stringCellValue = getStringCellValue(cellWithBandNumber);
        List<String> schedulingUnitShortcuts = getMessagesForLanguages(messageSource, SCHEDULING_UNIT_SHORTCUT, LOCALE_PL, LOCALE_EN);
        for (String shortcut : schedulingUnitShortcuts) {
            if (stringCellValue.contains(shortcut)) {
                return stringCellValue.replace(shortcut, StringUtils.EMPTY).trim();
            }
        }
        throw new ObjectValidationException(String.format("Invalid band number: %s. Row: %s, Cell: %s",
            stringCellValue, cellWithBandNumber.getRow().getRowNum(), cellWithBandNumber.getColumnIndex()), IMPORT_OFFERS_TEMPLATE_INCORRECT);
    }

    /**
     * Pobieranie sheet'a z workbook.
     */
    private XSSFSheet getSheetWithDetails(XSSFWorkbook workbook) {
        List<String> sheetNames = getMessagesForLanguages(messageSource, DETAILS_SHEET_NAME_MESSAGE, LOCALE_PL, LOCALE_EN);
        for (String name : sheetNames) {
            XSSFSheet sheet = workbook.getSheet(name);
            if (Objects.nonNull(sheet)) {
                return sheet;
            }
        }
        throw new ObjectValidationException("Cannot found sheet with offer details", IMPORT_OFFERS_TEMPLATE_INCORRECT);

    }

    private String getStringCellValue(XSSFCell cell) {
        cell.setCellType(CellType.STRING);
        return cell.getStringCellValue();
    }

    @Override
    public boolean supportClass(Class clazz) {
        return clazz.equals(AuctionOfferSchedulingUnitDTO.class);
    }

    @Override
    public boolean supportFormat(DataImportFormat format) {
        return format.equals(DataImportFormat.XLSX);
    }
}
