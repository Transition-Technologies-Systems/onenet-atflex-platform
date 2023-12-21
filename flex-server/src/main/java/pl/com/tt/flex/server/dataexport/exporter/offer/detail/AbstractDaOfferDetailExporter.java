package pl.com.tt.flex.server.dataexport.exporter.offer.detail;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Lazy;
import pl.com.tt.flex.model.service.dto.auction.offer.AbstractAuctionOfferDTO;
import pl.com.tt.flex.model.service.dto.auction.offer.AuctionOfferViewDTO;
import pl.com.tt.flex.model.service.dto.auction.offer.da.AuctionDayAheadOfferDTO;
import pl.com.tt.flex.model.service.dto.auction.offer.da.AuctionOfferBandDataDTO;
import pl.com.tt.flex.model.service.dto.auction.offer.da.AuctionOfferDersDTO;
import pl.com.tt.flex.server.dataexport.util.CellUtils;
import pl.com.tt.flex.server.service.auction.da.AuctionDayAheadService;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static pl.com.tt.flex.server.dataexport.exporter.offer.util.OfferExportUtils.*;
import static pl.com.tt.flex.server.dataexport.util.CellUtils.*;
import static pl.com.tt.flex.server.util.AuctionDayAheadDataUtil.existTimestampForBand;
import static pl.com.tt.flex.server.util.DateUtil.EXTRA_HOUR_CONSTANT;

@Slf4j
public abstract class AbstractDaOfferDetailExporter {

    protected final AuctionDayAheadService auctionDayAheadService;
    protected final MessageSource messageSource;

    private XSSFCellStyle defaultCellStyle;
    private XSSFCellStyle editedCellStyle;
    private XSSFCellStyle splitLineStyle;

    private final String ID_FORMAT = "%s - %s - %s"; // ID - nazwa_produktu - nazwa_JG
    private final String ID_UP_FORMAT = "%sup - %s - %s"; // IDup - nazwa_produktu - nazwa_JG

    public Locale locale;

    protected AbstractDaOfferDetailExporter(@Lazy AuctionDayAheadService auctionDayAheadService, MessageSource messageSource) {
        this.auctionDayAheadService = auctionDayAheadService;
        this.messageSource = messageSource;
    }

    /**
     * Uzupelnienie skoroszytu z szczegołami ofert DayAhead
     */
    public byte[] fillOfferDetailSheet(XSSFWorkbook workbook, List<AuctionOfferViewDTO> daOffers, Locale locale) throws IOException {
        this.locale = locale;
        createDefaultCellStyles(workbook);
        XSSFSheet sheet = workbook.createSheet(messageSource.getMessage(DETAILS_SHEET_NAME_MESSAGE, null, locale));
        List<AuctionDayAheadOfferDTO> offers = auctionDayAheadService.findAllOffersById(daOffers.stream().map(AbstractAuctionOfferDTO::getId).collect(Collectors.toList()))
            .stream().sorted(Comparator.comparingLong(AuctionDayAheadOfferDTO::getId))
            .collect(Collectors.toList());
        AtomicInteger currentRow = new AtomicInteger(0);
        setCellWidth(sheet, OFFER_ID_CELL_NR, 3840);
        for (AuctionDayAheadOfferDTO offer : offers) {
            // Uzupelnienie zlozonej ofery przez uzytkownika
            fillOfferSection(sheet, currentRow, offer, false);
            // Jezeli oferta byla modyfikowana przez uzytkownikow administracyjnych to zostaje dodana zmodyfikowana oferta
            if (isOfferEdited(offer)) fillOfferSection(sheet, currentRow, offer, true);
        }
        freezeColumns(sheet, Arrays.asList(1, 2, 3));
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        return outputStream.toByteArray();
    }

    protected boolean isOfferEdited(AuctionDayAheadOfferDTO offer){
        return offer.getDers().stream().flatMap(der -> der.getBandData().stream()).anyMatch(AuctionOfferBandDataDTO::isEdited);
    }

    /**
     * Metoda odpowiedzialan za uzupelnienie sekcji z ofertą
     */
    protected void fillOfferSection(XSSFSheet sheet, AtomicInteger currentRow, AuctionDayAheadOfferDTO offer, boolean isEdited) {
        int offerFirstRow = currentRow.get();
        fillOfferDetail(sheet, currentRow, offer, isEdited);
        String offerIdCellValue = getOfferIdCellValue(offer, isEdited);
        fillOfferIdCell(sheet, offerFirstRow, currentRow.get() - 1, offerIdCellValue);
        CellRangeAddress range = new CellRangeAddress(offerFirstRow, currentRow.get(), 0, MAX_CELL_INDEX);
        XSSFCellStyle cellStyle = isEdited ? editedCellStyle : defaultCellStyle;
        CellUtils.setCellStyleInRange(sheet, cellStyle, range);
        createSplitLine(sheet, currentRow);
    }

    /**
     * Dla oferty edytowanej do ID dodawne jest up
     * format dla oferty nie edytowanej: {ID_oferty} - {nazwa_produktu} - {nazwa JG}
     * format dla oferty edytowanej: {ID_oferty}up - {nazwa_produktu} - {nazwa JG}
     */
    protected String getOfferIdCellValue(AuctionDayAheadOfferDTO offer, boolean isEdited) {
        String productName = offer.getAuctionDayAhead().getProduct().getShortName();
        String suName = offer.getSchedulingUnit().getName();
        Long offerId = offer.getId();
        String format = isEdited ? ID_UP_FORMAT : ID_FORMAT;
        return String.format(format, offerId, productName, suName);
    }

    /**
     * Metoda odpowiedzialana za uzupelnienie szczegolow dla danej aukcji: pasma derow (Cena i Wolumen), sekcja związana z jednostaka grafikową
     */
    protected void fillOfferDetail(XSSFSheet sheet, AtomicInteger currentRow, AuctionDayAheadOfferDTO offer, boolean isEdited) {
        log.info("fillOfferDetail() Start - Fill offer detail for offerId: {}", offer.getId());
        fillTimestamp(sheet, currentRow, TIMESTAMP_START_CELL);
        createSchedulingUnitSection(sheet, currentRow, offer, isEdited);
        fillVolumeAndPriceSign(sheet, currentRow, TIMESTAMP_START_CELL);
        fillDersDetails(sheet, currentRow, offer, isEdited);
        log.info("fillOfferDetail() End - Fill offer detail for offerId: {}", offer.getId());
    }

    /**
     * Uzupelnienie komorki z nazwa oferty
     */
    protected void fillOfferIdCell(XSSFSheet sheet, int offerFirstRow, int i, String offerCellValue) {
        fillCellAndMergeRegion(sheet, offerFirstRow, i, OFFER_ID_CELL_NR, OFFER_ID_CELL_NR, offerCellValue);
    }

    /**
     * Metoda odpowiedzialna za stworzenie sekcji z scheduling untis.
     * Dla kazdego pasma tworzone sa komorki w ktorych obliczana jest suma wolumenow i srednia cena dla danego timestampa
     * Format nazwy danego pasma:
     * pasmo > 0 -> JG +1, JG +2 itd.
     * pasmo == 0 -> JG PP
     * pasmo < 0 -> JG -1, JG -2 itd.
     */
    protected void createSchedulingUnitSection(XSSFSheet sheet, AtomicInteger currentRow, AuctionDayAheadOfferDTO offer, boolean isEdited) {
        TreeSet<Integer> bandsInOffer = new TreeSet<>(Collections.reverseOrder());
        bandsInOffer.addAll(offer.getDers().stream().flatMap(d -> d.getBandData().stream())
            .map(AuctionOfferBandDataDTO::getBandNumber).collect(Collectors.toSet()));
        for (Integer bandNumber : bandsInOffer) {
            XSSFRow row = getRow(sheet, currentRow.getAndIncrement());
            XSSFCell cell = row.createCell(SCHEDULING_UNIT_BAND_NR_CELL);
            String schedulingUnitShortcut = messageSource.getMessage(SCHEDULING_UNIT_SHORTCUT, null, locale);
            cell.setCellValue(schedulingUnitShortcut + " " + getBandStr(bandNumber));
            fillSchedulingUnitsVolumeAndPrice(bandNumber, row, offer, isEdited);
        }
    }

    /**
     * Metoda ta dla danego pasma 'bandNumber' uzupelnia dla kazdego timestampa
     * sume wolumenow oraz cene dla danego timestampa i pasma
     */
    protected void fillSchedulingUnitsVolumeAndPrice(Integer bandNumber, XSSFRow row, AuctionDayAheadOfferDTO offerDTO, boolean isEdited) {
        AtomicInteger startCell = new AtomicInteger(3);
        for (int i = 1; i <= 25; i++) {
            String timestamp = i == 25 ? EXTRA_HOUR_CONSTANT : String.valueOf(i);
            if (existTimestampForBand(offerDTO, bandNumber, timestamp)) {
                setSumOfVolumesForCell(offerDTO, bandNumber, timestamp, row.createCell(startCell.getAndIncrement()), isEdited);
                setPriceForCell(offerDTO, bandNumber, timestamp, row.createCell(startCell.getAndIncrement()), isEdited);
            } else {
                startCell.getAndIncrement();
                startCell.getAndIncrement();
            }
        }
    }

    /**
     * Metoda ktora uzupelnia dla kazdego DERa szczegoly zlozonej oferty
     */
    protected void fillDersDetails(XSSFSheet sheet, AtomicInteger currentRow, AuctionDayAheadOfferDTO offer,
        boolean isEdited) {
        log.info("fillDersDetails() Start - Fill ders detail for offerId={}", offer.getId());
        int bandNumberCell = 2;
        int derNameCellNumber = 1;
        for (AuctionOfferDersDTO der : offer.getDers()) {
            log.info("fillDersDetails() Start - Fill der detail for: offerId={}, derId={}", offer.getId(), der.getDer().getId());
            int derFirsRow = currentRow.get();
            fillDerDetail(sheet, currentRow, bandNumberCell, der, isEdited);
            fillCellAndMergeRegion(sheet, derFirsRow, currentRow.get() - 1, derNameCellNumber, derNameCellNumber, der.getDer().getName());
            log.info("fillDersDetails() End - Fill der detail for: offerId={}, derId={}", offer.getId(), der.getDer().getId());
        }
        log.info("fillDersDetails() End - Fill ders detail for offerId={}", offer.getId());
    }

    /**
     * Metoda ktora grupuje pasam DERa i uzupelnia dane pasmo w skoroszycie
     */
    protected void fillDerDetail(XSSFSheet sheet, AtomicInteger currentRow, int bandNumberCell, AuctionOfferDersDTO der,
        boolean isEdited) {
        log.info("fillDerDetail() Start - Fill der detail for der: derId={}", der.getDer().getId());
        Map<Integer, List<AuctionOfferBandDataDTO>> bandsGroupingByBandNumber = groupDerBands(der);
        for (Map.Entry<Integer, List<AuctionOfferBandDataDTO>> band : bandsGroupingByBandNumber.entrySet()) {
            Integer bandNumber = band.getKey();
            fillBandDetail(sheet, currentRow, bandNumberCell, der, band, bandNumber, isEdited);
            currentRow.incrementAndGet();
        }
        log.info("fillDerDetail() End - Fill der detail for der: derId={}", der.getDer().getId());
    }

    /**
     * Uzupelnienie dla danego pasma timestampo'w z cena i wolumenem dla danego DERa
     */
    protected void fillBandDetail(XSSFSheet sheet, AtomicInteger currentRow, int bandNumberCell, AuctionOfferDersDTO der,
        Map.Entry<Integer, List<AuctionOfferBandDataDTO>> band, Integer bandNumber,
        boolean isEdited) {
        log.info("fillBandDetail() Start - Fill band der detail for: bandNumber={}, derId={}, derName={}", bandNumber, der.getDer().getId(), der.getDer().getName());
        int currentRowIndex = currentRow.get();
        addStringCellValueIfExist(sheet, currentRowIndex, bandNumberCell, getBandStr(bandNumber));
        List<AuctionOfferBandDataDTO> bandTimestamps = band.getValue();
        AtomicInteger currentCell = new AtomicInteger(3);
        for (int i = 1; i <= 25; i++) {
            String timestamp = i == 25 ? EXTRA_HOUR_CONSTANT : String.valueOf(i);
            Optional<AuctionOfferBandDataDTO> optTimestamp = bandTimestamps.stream().filter(t -> t.getHourNumber().equals(timestamp)).findFirst();
            int volumeCell = currentCell.getAndIncrement();
            int priceCell = currentCell.getAndIncrement();
            if (optTimestamp.isPresent()) {
                AuctionOfferBandDataDTO timestampValue = optTimestamp.get();
                log.debug("fillBandDetail() Fill timestamp: {}", timestampValue.getHourNumber());
                String volume = getStringFromBigDecimal(isEdited ? timestampValue.getAcceptedVolume() : timestampValue.getVolume());
                String price = getStringFromBigDecimal(isEdited ? timestampValue.getAcceptedPrice() : timestampValue.getPrice());
                addStringCellValueIfExist(sheet, currentRowIndex, volumeCell, volume);
                addStringCellValueIfExist(sheet, currentRowIndex, priceCell, price);
            } else {
                log.debug("fillBandDetail() Not found timestamp -> {}", timestamp);
            }
        }
        log.info("fillBandDetail() End - Fill band der detail for: bandNumber={}, derId={}, derName={}", bandNumber, der.getDer().getId(), der.getDer().getName());
    }

    protected String getStringFromBigDecimal(BigDecimal value) {
        String newValue = String.valueOf(value);
        if (Objects.isNull(newValue) || Objects.equals("null", newValue)) {
            return "";
        }
        return newValue.replaceAll("\\.", ",");
    }

    /**
     * Grupowanie cen i wolumenow danego DERa wedłg pasm
     */
    protected Map<Integer, List<AuctionOfferBandDataDTO>> groupDerBands(AuctionOfferDersDTO der) {
        List<AuctionOfferBandDataDTO> bands = der.getBandData().stream().sorted(Comparator.comparingInt(AuctionOfferBandDataDTO::getBandNumber)).collect(Collectors.toList());
        Map<Integer, List<AuctionOfferBandDataDTO>> bandsGroupingByBandNumber = new TreeMap<>(Collections.reverseOrder());
        bandsGroupingByBandNumber.putAll(bands.stream().collect(Collectors.groupingBy(AuctionOfferBandDataDTO::getBandNumber)));
        return bandsGroupingByBandNumber;
    }

    /**
     * Metoda odpowiedzialna za uzupelnienie Timestamp'ow dla oferty
     */
    protected void fillTimestamp(XSSFSheet sheet, AtomicInteger currentRow, int startCell) {
        AtomicInteger currentCell = new AtomicInteger(startCell);
        log.debug("fillTimestamp() Start - Fill timestamp: row={}, startCell={}", currentRow.get(), startCell);
        for (int i = 1; i <= 25; i++) {
            String timestamp = i == 25 ? EXTRA_HOUR_CONSTANT : String.valueOf(i);
            fillCellAndMergeRegion(sheet, currentRow.get(), currentRow.get(), currentCell.getAndIncrement(), currentCell.getAndIncrement(), timestamp);
        }
        log.debug("fillTimestamp() End - Fill timestamp: row={}, startCell={}, endCell={}", currentRow.get(), startCell, currentCell.get());
        currentRow.getAndIncrement();
    }

    /**
     * Metoda odpowiedzialna za uzupelnienie znakow Wolumen ('W') i Cena ('P') w timestamp'ach
     */
    protected void fillVolumeAndPriceSign(XSSFSheet sheet, AtomicInteger currentRow, int startCell) {
        AtomicInteger currentCell = new AtomicInteger(startCell);
        log.debug("fillVolumeAndPriceSign() Start -  Fill volume and price sign: row={}, startCell={}", currentRow.get(), startCell);
        String volumeShortcut = messageSource.getMessage(VOLUME_SHORTCUT, null, locale);
        String priceShortcut = messageSource.getMessage(PRICE_SHORTCUT, null, locale);

        for (int i = 1; i <= 25; i++) {
            addStringCellValueIfExist(sheet, currentRow.get(), currentCell.getAndIncrement(), volumeShortcut);
            addStringCellValueIfExist(sheet, currentRow.get(), currentCell.getAndIncrement(), priceShortcut);
        }
        log.debug("fillVolumeAndPriceSign() End - Fill volume and price sign: row={}, startCell={}, endCell={}", currentRow.get(), startCell, currentCell.get());
        currentRow.getAndIncrement();
    }

    /**
     * Uzupelnianie ceny w danym timestampie i pasmie.
     * - Dla pasma zerowego cena nie zostaje uzupelniania
     * - Dla pasm nie zerowych cena uzupelniana jest tylko wtedy gdy wysztkie ceny DERow sa takie same dla danego pasma i timestampa
     */
    protected void setPriceForCell(AuctionDayAheadOfferDTO offerDTO, Integer bandNumber, String timestamp, XSSFCell priceCell, boolean isEdited) {
        if (bandNumber == 0) {
            return;
        }
        if (isEdited) {
            setPriceForEditOffer(offerDTO, bandNumber, timestamp, priceCell);
        } else {
            setPriceForNonEditOffer(offerDTO, bandNumber, timestamp, priceCell);
        }
    }

    /**
     * Uzupelnienie ceny dla zedytowanej oferty, tylko wtedy gdy wszystkie ceny DERow w danym
     * timestampie i pasmie sa takie same - pod uwage brane jest pole acceptedPrice.
     */
    protected void setPriceForEditOffer(AuctionDayAheadOfferDTO offerDTO, Integer bandNumber, String timestamp, XSSFCell priceCell) {
        Optional<BigDecimal> acceptedPriceOpt = getAcceptedPriceForSuSection(offerDTO, bandNumber, timestamp);
        if (acceptedPriceOpt.isPresent()) {
            priceCell.setCellValue(getStringFromBigDecimal(acceptedPriceOpt.get()));
            log.debug("setPriceForEditOffer() Set price={} for band={}, timestamp={}", acceptedPriceOpt.get(), bandNumber, timestamp);
        }
    }

    /**
     * Uzupelnienie ceny dla nie edytowanej oferty, tylko wtedy gdy wszystkie ceny DERow w danym
     * timestampie i pasmie sa takie same - pod uwage brane jest pole price.
     */
    protected void setPriceForNonEditOffer(AuctionDayAheadOfferDTO offerDTO, Integer bandNumber, String timestamp, XSSFCell priceCell) {
        Optional<BigDecimal> priceOpt = getPriceForSuSection(offerDTO, bandNumber, timestamp);
        if (priceOpt.isPresent()) {
            priceCell.setCellValue(getStringFromBigDecimal(priceOpt.get()));
            log.debug("setPriceForNonEditOffer() Set price={} for band={}, timestamp={}", priceOpt.get(), bandNumber, timestamp);
        }
    }

    /**
     * Wyliczenie sumy wolumenow w danym timestampie i pasmie
     */
    protected void setSumOfVolumesForCell(AuctionDayAheadOfferDTO offerDTO, Integer bandNumber, String timestamp, XSSFCell sumCell, boolean isEdited) {
        if (!isEdited) {
            setVolumesForBandAndTimestamp(offerDTO, bandNumber, timestamp, sumCell);
        } else {
            setAcceptedVolumesForBandAndTimestamp(offerDTO, bandNumber, timestamp, sumCell);
        }
    }

    /**
     * Ustawienie sumy wolumenow dla nie edytowanej oferty.
     * Gdy w danym timestampie i pasmie nie ma uzupelnionych wolumenow, komorka zostaje pusta
     */
    protected void setVolumesForBandAndTimestamp(AuctionDayAheadOfferDTO offerDTO, Integer bandNumber, String timestamp, XSSFCell sumCell) {
        Optional<BigDecimal> volumeForBandAndTimestamp = getVolumeForBandAndTimestamp(offerDTO, bandNumber, timestamp);
        if (volumeForBandAndTimestamp.isPresent()) {
            sumCell.setCellValue(getStringFromBigDecimal(volumeForBandAndTimestamp.get()));
            log.debug("setVolumesForBandAndTimestamp() Set sum volumes={}, for band={}, timestamp={}", volumeForBandAndTimestamp.get(), bandNumber, timestamp);
        }
    }

    /**
     * Ustawienie sumy wolumenow dla edytowanej oferty.
     * Gdy w danym timestampie i pasmie nie ma uzupelnionych wolumenow, komorka zostaje pusta
     */
    protected void setAcceptedVolumesForBandAndTimestamp(AuctionDayAheadOfferDTO offerDTO, Integer bandNumber, String timestamp, XSSFCell sumCell) {
        Optional<BigDecimal> volumeForBandAndTimestamp = getAcceptedVolumeForBandAndTimestamp(offerDTO, bandNumber, timestamp);
        if (volumeForBandAndTimestamp.isPresent()) {
            sumCell.setCellValue(getStringFromBigDecimal(volumeForBandAndTimestamp.get()));
            log.debug("setAcceptedVolumesForBandAndTimestamp() Set sum volumes={}, for band={}, timestamp={}", volumeForBandAndTimestamp.get(), bandNumber, timestamp);
        }
    }

    /**
     * Blokada kolumn
     */
    public void freezeColumns(XSSFSheet sheet, List<Integer> columnNumbers) {
        columnNumbers.forEach(colNr -> sheet.createFreezePane(colNr, 0));
    }

    /**
     * Linia odzielajaca oferty
     */
    protected void createSplitLine(XSSFSheet sheet, AtomicInteger currentRow) {
        int firstCell = 0;
        int row = currentRow.get();
        for (int i = firstCell; i <= MAX_CELL_INDEX; i++) {
            XSSFRow xssfRow = getRow(sheet, row);
            XSSFCell cell = xssfRow.createCell(i);
            cell.setCellStyle(this.splitLineStyle);
        }
        mergeRange(sheet, row, row, firstCell, MAX_CELL_INDEX);
        currentRow.getAndIncrement();
    }

    /**
     * Pasma w skoroszycie ustawiane sa nastepujaco:
     * dla 0 -> PP
     * dla pasm dodatnich -> +1, +2 itd
     * dla pasm ujemnych -> -1, -2, itd
     */
    protected String getBandStr(Integer bandNumber) {
        if (bandNumber == 0) {
            return messageSource.getMessage(SELF_SCHEDULE_SHORTCUT, null, locale);
        } else {
            return bandNumber > 0 ? String.format("+%s", bandNumber) : bandNumber.toString();
        }
    }

    protected void createDefaultCellStyles(XSSFWorkbook workbook) {
        setDefaultCellStyles(workbook);
        setEditedCellStyles(workbook);
        setSplitLineCellStyle(workbook);
    }

    /**
     * styl wykorzystywany dla nie edytowanych ofert
     */
    protected void setDefaultCellStyles(XSSFWorkbook workbook) {
        XSSFCellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setAlignment(HorizontalAlignment.CENTER);
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        cellStyle.setBorderTop(BorderStyle.THIN);
        cellStyle.setBorderBottom(BorderStyle.THIN);
        cellStyle.setBorderLeft(BorderStyle.THIN);
        cellStyle.setBorderRight(BorderStyle.THIN);
        DataFormat format = workbook.createDataFormat();
        cellStyle.setDataFormat(format.getFormat("@"));
        cellStyle.setWrapText(true);
        this.defaultCellStyle = cellStyle;
    }

    /**
     * styl wykorzystywany dla edytowanych ofert
     */
    protected void setEditedCellStyles(XSSFWorkbook workbook) {
        XSSFCellStyle cellStyle = workbook.createCellStyle();
        XSSFColor xssfColor = new XSSFColor(new java.awt.Color(208, 206, 206));
        cellStyle.setFillForegroundColor(xssfColor);
        cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        cellStyle.setAlignment(HorizontalAlignment.CENTER);
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        cellStyle.setBorderTop(BorderStyle.THIN);
        cellStyle.setBorderBottom(BorderStyle.THIN);
        cellStyle.setBorderLeft(BorderStyle.THIN);
        cellStyle.setBorderRight(BorderStyle.THIN);
        DataFormat format = workbook.createDataFormat();
        cellStyle.setDataFormat(format.getFormat("@"));
        cellStyle.setWrapText(true);
        this.editedCellStyle = cellStyle;
    }

    /**
     * styl wykorzystywany do tworzenia podzialki
     */
    protected void setSplitLineCellStyle(XSSFWorkbook workbook) {
        XSSFCellStyle cellStyle = workbook.createCellStyle();
        XSSFColor xssfColor = new XSSFColor(new java.awt.Color(169, 208, 142));
        cellStyle.setFillForegroundColor(xssfColor);
        cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        cellStyle.setAlignment(HorizontalAlignment.CENTER);
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        cellStyle.setBorderTop(BorderStyle.THIN);
        cellStyle.setBorderBottom(BorderStyle.THIN);
        cellStyle.setBorderLeft(BorderStyle.THIN);
        cellStyle.setBorderRight(BorderStyle.THIN);
        this.splitLineStyle = cellStyle;
    }

    protected String getVolumeShortcut() {
        return messageSource.getMessage(VOLUME_SHORTCUT, null, locale);
    }

    protected String getPriceShortcut() {
        return messageSource.getMessage(PRICE_SHORTCUT, null, locale);
    }
}
