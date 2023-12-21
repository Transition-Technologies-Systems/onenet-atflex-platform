package pl.com.tt.flex.server.dataexport.exporter.offer.detail;

import static org.apache.poi.ss.usermodel.IndexedColors.AQUA;
import static org.apache.poi.ss.usermodel.IndexedColors.BRIGHT_GREEN;
import static org.apache.poi.ss.usermodel.IndexedColors.CORAL;
import static org.apache.poi.ss.usermodel.IndexedColors.CORNFLOWER_BLUE;
import static org.apache.poi.ss.usermodel.IndexedColors.LAVENDER;
import static org.apache.poi.ss.usermodel.IndexedColors.LEMON_CHIFFON;
import static org.apache.poi.ss.usermodel.IndexedColors.LIGHT_CORNFLOWER_BLUE;
import static org.apache.poi.ss.usermodel.IndexedColors.LIGHT_GREEN;
import static org.apache.poi.ss.usermodel.IndexedColors.LIGHT_TURQUOISE;
import static org.apache.poi.ss.usermodel.IndexedColors.LIGHT_YELLOW;
import static org.apache.poi.ss.usermodel.IndexedColors.ROSE;
import static org.apache.poi.ss.usermodel.IndexedColors.TAN;
import static org.apache.poi.ss.usermodel.IndexedColors.TURQUOISE;
import static org.apache.poi.ss.usermodel.IndexedColors.YELLOW;
import static pl.com.tt.flex.server.dataexport.exporter.offer.util.OfferExportUtils.DETAILS_SHEET_NAME_MESSAGE;
import static pl.com.tt.flex.server.dataexport.exporter.offer.util.OfferExportUtils.SELF_SCHEDULE_SHORTCUT;
import static pl.com.tt.flex.server.dataexport.util.CellUtils.addStringCellValueIfExist;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import pl.com.tt.flex.model.service.dto.auction.da.AuctionDayAheadDTO;
import pl.com.tt.flex.model.service.dto.auction.offer.AbstractAuctionOfferDTO;
import pl.com.tt.flex.model.service.dto.auction.offer.AuctionOfferViewDTO;
import pl.com.tt.flex.model.service.dto.auction.offer.da.AuctionDayAheadOfferDTO;
import pl.com.tt.flex.model.service.dto.auction.offer.da.AuctionOfferBandDataDTO;
import pl.com.tt.flex.model.service.dto.auction.offer.da.AuctionOfferDersDTO;
import pl.com.tt.flex.model.service.dto.auction.type.AuctionDayAheadType;
import pl.com.tt.flex.server.dataexport.exporter.offer.detail.factory.OfferDetailExporter;
import pl.com.tt.flex.server.dataexport.util.CellUtils;
import pl.com.tt.flex.server.domain.unit.LocalizationTypeEntity;
import pl.com.tt.flex.server.domain.unit.UnitEntity;
import pl.com.tt.flex.server.domain.unit.self_schedule.UnitSelfScheduleEntity;
import pl.com.tt.flex.server.service.auction.da.AuctionDayAheadService;

@Slf4j
@Component
public class SetoOfferDetailExporter extends AbstractDaOfferDetailExporter implements OfferDetailExporter {

    public static final List<String> CAPACITY_HEADERS = List.of("der", "couplingPoint", "powerStationId", "pointOfConnectionWithLV", "selfSchedule", "pMin", "pMax");
    public static final List<String> ENERGY_HEADERS = List.of("couplingPoint", "powerStationId", "pointOfConnectionWithLV", "pMin", "pMax");
    public static String MESSAGE_PREFIX = "exporter.bids.evaluation.";

    public Locale locale;
    private int LAST_USED_COLOR_INDEX = 0;
    private final IndexedColors[] COLORS = {AQUA, CORAL, BRIGHT_GREEN, CORNFLOWER_BLUE, LAVENDER, LEMON_CHIFFON,
        LIGHT_CORNFLOWER_BLUE, LIGHT_GREEN, LIGHT_YELLOW, LIGHT_TURQUOISE, ROSE, TAN, TURQUOISE, YELLOW};

    public SetoOfferDetailExporter(@Lazy AuctionDayAheadService auctionDayAheadService, MessageSource messageSource) {
        super(auctionDayAheadService, messageSource);
    }

    @Override
    public byte[] fillOfferDetailSheet(XSSFWorkbook workbook, List<AuctionOfferViewDTO> daOffers, Locale locale) throws IOException {
        this.locale = locale;
        List<Long> auctionIds = daOffers.stream().map(AuctionOfferViewDTO::getAuctionId).collect(Collectors.toList());
        List<AuctionDayAheadDTO> auctions = auctionDayAheadService.findAll(auctionIds);
        List<Long> capacityAuctionIds = auctions.stream().filter(auction -> Objects.equals(auction.getType(), AuctionDayAheadType.CAPACITY)).map(AuctionDayAheadDTO::getId).collect(Collectors.toList());
        List<Long> energyAuctionIds = auctionIds.stream().filter(id -> !capacityAuctionIds.contains(id)).collect(Collectors.toList());
        Map<String, CellStyle> styleByCouplingPoint = new HashMap<>();
        fillCapacityDetailSheets(workbook, daOffers.stream().filter(offer -> capacityAuctionIds.contains(offer.getAuctionId())).collect(Collectors.toList()), locale, styleByCouplingPoint);
        fillEnergyDetailSheets(workbook, daOffers.stream().filter(offer -> energyAuctionIds.contains(offer.getAuctionId())).collect(Collectors.toList()), locale, styleByCouplingPoint);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        return outputStream.toByteArray();
    }

    //************************************************************************ CAPACITY EXPORT ************************************************************************************

    private void fillCapacityDetailSheets(XSSFWorkbook workbook, List<AuctionOfferViewDTO> daOffers, Locale locale, Map<String, CellStyle> styleByCouplingPoint) {
        Map<Long, UnitEntity> dersById = getDersByIdFromOffers(daOffers);
        Map<String, Set<AuctionDayAheadOfferDTO>> offersByTimestamp = sortOffersByTimestamp(daOffers);
        Map<String, Map<UnitEntity, List<AuctionDayAheadOfferDTO>>> offerByDerByTimestamp = mapOffersByDerForEachTimestamp(offersByTimestamp, dersById);
        for (String timestamp : offerByDerByTimestamp.keySet()) {
            List<UnitSelfScheduleEntity> selfSchedules = getSelfSchedules(offersByTimestamp.get(timestamp));
            XSSFSheet sheet = workbook.createSheet(messageSource.getMessage(DETAILS_SHEET_NAME_MESSAGE, null, locale).concat(" timestamp ").concat(timestamp));
            AtomicInteger currentRow = new AtomicInteger(0);
            List<Pair<String, Long>> productAuctionPairsOrdered = fillHeaderRow(sheet, sheet.createRow(currentRow.getAndIncrement()), offersByTimestamp.get(timestamp));
            offerByDerByTimestamp.get(timestamp).entrySet().stream()
                .sorted(Comparator.comparing(entry -> getCouplingPointName(entry.getKey())))
                .forEach(entry -> {
                    BigDecimal selfScheduleValue = selfSchedules.stream().filter(ss -> ss.getUnit().getId().equals(entry.getKey().getId()))
                        .findAny().map(UnitSelfScheduleEntity::getVolumes).map(map -> map.get(timestamp)).get();
                    fillDerRow(sheet, currentRow.getAndIncrement(), entry, selfScheduleValue, styleByCouplingPoint, productAuctionPairsOrdered, timestamp);
                    sheet.autoSizeColumn(0);
                });
        }
    }

    private Map<Long, UnitEntity> getDersByIdFromOffers(List<AuctionOfferViewDTO> offers) {
        List<Long> offerIds = offers.stream().map(AbstractAuctionOfferDTO::getId).collect(Collectors.toList());
        return auctionDayAheadService.getDersByOfferIds(offerIds).stream().collect(Collectors.toMap(UnitEntity::getId, Function.identity()));
    }

    private List<UnitSelfScheduleEntity> getSelfSchedules(Set<AuctionDayAheadOfferDTO> offers) {
        List<Long> offerIds = offers.stream().map(AuctionDayAheadOfferDTO::getId).collect(Collectors.toList());
        return auctionDayAheadService.getSelfSchedulesForDersInOffers(offerIds);
    }

    private List<Pair<String, Long>> fillHeaderRow(XSSFSheet sheet, Row row, Set<AuctionDayAheadOfferDTO> offersInTimestamp) {
        AtomicInteger colIndex = new AtomicInteger(0);
        CAPACITY_HEADERS.forEach(header -> {
            String cellValue = messageSource.getMessage(MESSAGE_PREFIX + header, null, locale);
            createHeaderCell(sheet, row, colIndex, cellValue);
        });
        Set<Pair<String, Long>> productAndAuctionIdByTimestamp = getProductAndAuctionIdByTimestamp(offersInTimestamp);
        List<Pair<String, Long>> productAuctionPairsOrdered = new ArrayList<>();
        productAndAuctionIdByTimestamp.forEach(productAuctionPair -> {
            productAuctionPairsOrdered.add(productAuctionPair);
            String cellValue = productAuctionPair.getFirst().concat(" - ").concat(productAuctionPair.getSecond().toString());
            createHeaderCell(sheet, row, colIndex, cellValue);
        });
        return productAuctionPairsOrdered;
    }

    private void createHeaderCell(XSSFSheet sheet, Row row, AtomicInteger colIndex, String value) {
        Cell cell = row.createCell(colIndex.getAndIncrement());
        cell.setCellValue(value);
        sheet.autoSizeColumn(cell.getColumnIndex());
    }

    private void fillDerRow(XSSFSheet sheet, int currentRow, Entry<UnitEntity, List<AuctionDayAheadOfferDTO>> offersForDer, BigDecimal selfSchedule,
        Map<String, CellStyle> styleByCouplingPoint, List<Pair<String, Long>> productAuctionPairsOrdered, String timestamp) {
        AtomicInteger currentCell = new AtomicInteger();
        UnitEntity der = offersForDer.getKey();
        addStringCellValueIfExist(sheet, currentRow, currentCell.getAndIncrement(), der.getName());
        addStringCellValueIfExist(sheet, currentRow, currentCell.getAndIncrement(), getCouplingPointName(der));
        addStringCellValueIfExist(sheet, currentRow, currentCell.getAndIncrement(), getPowerStationId(der));
        der.getPointOfConnectionWithLvTypes().stream()
            .findAny().map(LocalizationTypeEntity::getName)
            .ifPresentOrElse(
                pocWithLv -> addStringCellValueIfExist(sheet, currentRow, currentCell.getAndIncrement(), pocWithLv),
                () -> addStringCellValueIfExist(sheet, currentRow, currentCell.getAndIncrement(), ""));
        addStringCellValueIfExist(sheet, currentRow, currentCell.getAndIncrement(), selfSchedule.toString());
        addStringCellValueIfExist(sheet, currentRow, currentCell.getAndIncrement(), der.getPMin().toString());
        addStringCellValueIfExist(sheet, currentRow, currentCell.getAndIncrement(), der.getSourcePower().toString());

        //dodaj kolumny generowane na podstawie par produkt-aukcja
        for (Pair<String, Long> productAuctionPair : productAuctionPairsOrdered) {
            String cellValue = offersForDer.getValue().stream()
                .filter(offerDTO -> offerDTO.getAuctionDayAhead().getProduct().getShortName().equals(productAuctionPair.getFirst())
                    && offerDTO.getAuctionDayAhead().getId().equals(productAuctionPair.getSecond()))
                .findFirst()
                .map(offerDTO -> offerDTO.getDers().stream()
                    .filter(offerDer -> offerDer.getDer().getId().equals(der.getId()))
                    .map(AuctionOfferDersDTO::getBandData)
                    .flatMap(List::stream)
                    .filter(band -> band.getHourNumber().equals(timestamp) && band.getBandNumber() != 0)
                    .map(bandData -> Objects.isNull(bandData.getAcceptedVolume()) ? BigDecimal.ZERO : bandData.getAcceptedVolume())
                    .findAny().get().toString())
                .orElseGet(() -> messageSource.getMessage(MESSAGE_PREFIX + "lack", null, locale));
            addStringCellValueIfExist(sheet, currentRow, currentCell.getAndIncrement(), cellValue);
        }
        applyColor(sheet, currentRow, styleByCouplingPoint, getCouplingPointName(der));
    }

    private void applyColor(Sheet sheet, int currentRow, Map<String, CellStyle> styleByCouplingPoint, String couplingPointName) {
        Optional.ofNullable(styleByCouplingPoint.get(couplingPointName))
            .ifPresentOrElse(style -> sheet.getRow(currentRow).cellIterator().forEachRemaining(cell -> cell.setCellStyle(style)),
                () -> {
                    CellStyle style = Optional.ofNullable(sheet.getRow(currentRow).getRowStyle()).orElse(sheet.getWorkbook().createCellStyle());
                    style.setFillForegroundColor(getNextColorIndex());
                    style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                    styleByCouplingPoint.put(couplingPointName, style);
                    sheet.getRow(currentRow).cellIterator().forEachRemaining(cell -> cell.setCellStyle(style));
                });
    }

    private Set<Pair<String, Long>> getProductAndAuctionIdByTimestamp(Set<AuctionDayAheadOfferDTO> offersInTimestamp) {
        Set<Pair<String, Long>> productAuctionIdPairsForTimestamp = new HashSet<>();
        for (AuctionDayAheadOfferDTO offerDTO : offersInTimestamp) {
            productAuctionIdPairsForTimestamp.add(Pair.of(offerDTO.getAuctionDayAhead().getProduct().getShortName(), offerDTO.getAuctionDayAhead().getId()));
        }
        return productAuctionIdPairsForTimestamp;
    }

    private Map<String, Map<UnitEntity, List<AuctionDayAheadOfferDTO>>> mapOffersByDerForEachTimestamp(Map<String, Set<AuctionDayAheadOfferDTO>> offersByTimestamp, Map<Long, UnitEntity> dersById) {
        return offersByTimestamp.entrySet().stream()
            .collect(Collectors.toMap(Entry::getKey, entry -> entry.getValue().stream()
                .flatMap(offerDTO -> offerDTO.getDers().stream().map(offerDer -> Pair.of(offerDer.getDer(), offerDTO)))
                .collect(Collectors.groupingBy(derMinOfferPair -> dersById.get(derMinOfferPair.getFirst().getId()), Collectors.mapping(Pair::getSecond, Collectors.toList())))));
    }

    private Map<String, Set<AuctionDayAheadOfferDTO>> sortOffersByTimestamp(List<AuctionOfferViewDTO> daOffers) {
        Map<String, Set<AuctionDayAheadOfferDTO>> offersByTimestamp = new HashMap<>();
        auctionDayAheadService.findAllOffersById(daOffers.stream().map(AbstractAuctionOfferDTO::getId).collect(Collectors.toList()))
            .forEach(offer -> offer.getDers().forEach(der -> der.getBandData()
                .forEach(band -> offersByTimestamp.computeIfAbsent(band.getHourNumber(), ifAbsent -> new HashSet<>()).add(offer))));
        return offersByTimestamp;
    }

    private String getPowerStationId(UnitEntity der) {
        return der.getPowerStationTypes().stream().findAny().get().getName();
    }

    private String getCouplingPointName(UnitEntity der) {
        return der.getCouplingPointIdTypes().stream().findAny().get().getName();
    }

    private short getNextColorIndex() {
        return COLORS[++LAST_USED_COLOR_INDEX % COLORS.length].index;
    }

    //************************************************************************ ENERGY EXPORT ************************************************************************************

    private void fillEnergyDetailSheets(XSSFWorkbook workbook, List<AuctionOfferViewDTO> daOffers, Locale locale, Map<String, CellStyle> styleByCouplingPoint) {
        Map<Long, UnitEntity> dersById = getDersByIdFromOffers(daOffers);
        Map<String, Set<AuctionDayAheadOfferDTO>> offersByTimestamp = sortOffersByTimestamp(daOffers);
        Map<String, Map<Long, List<AuctionDayAheadOfferDTO>>> offersByAuctionByTimestamp = mapOffersByAuctionForEachTimestamp(offersByTimestamp);
        offersByAuctionByTimestamp.forEach((timestamp, offersByAuction) -> {
            offersByAuction.forEach((auctionId, offers) -> {
                XSSFSheet sheet = workbook.createSheet(String.valueOf(auctionId).concat(" ")
                    .concat(messageSource.getMessage(DETAILS_SHEET_NAME_MESSAGE, null, locale)).concat(" timestamp ").concat(timestamp).concat(" EB"));
                AtomicInteger currentColumn = new AtomicInteger(0);
                List<Integer> presentBands = getPresentBands(offers, timestamp);
                fillEnergyHeaderColumn(sheet, currentColumn.getAndIncrement(), auctionId, presentBands);
                fillEnergyDers(sheet, currentColumn, offers, presentBands, styleByCouplingPoint, timestamp, dersById);
                freezeColumns(sheet, List.of(1));
            });
        });
    }

    private Map<String, Map<Long, List<AuctionDayAheadOfferDTO>>> mapOffersByAuctionForEachTimestamp(Map<String, Set<AuctionDayAheadOfferDTO>> offersByTimestamp) {
        return offersByTimestamp.entrySet().stream().collect(Collectors.toMap(Entry::getKey, entry -> entry.getValue().stream()
            .collect(Collectors.groupingBy(offer -> offer.getAuctionDayAhead().getId()))));
    }

    private void fillEnergyHeaderColumn(XSSFSheet sheet, Integer columnIndex, Long auctionId, List<Integer> presentBands) {
        AtomicInteger rowIndex = new AtomicInteger(0);
        CellUtils.getCell(sheet, rowIndex.getAndIncrement(), columnIndex).setCellValue(auctionId);
        var headers = List.of("couplingPoint", "powerStationId", "pointOfConnectionWithLV", "pMin", "pMax");
        headers.forEach(header -> {
            String cellValue = messageSource.getMessage("exporter.bids.evaluation." + header, null, locale);
            CellUtils.getCell(sheet, rowIndex.getAndIncrement(), columnIndex).setCellValue(cellValue);
        });
        presentBands.forEach(bandNumber -> {
            String bandLabel = getBandLabel(bandNumber);
            CellUtils.getCell(sheet, rowIndex.getAndIncrement(), columnIndex)
                .setCellValue(bandLabel);
        });
        sheet.autoSizeColumn(columnIndex);
    }

    /**
     * Metoda zwraca etykietę pasma o podanym numerze. Dla pasma dodatniego jest to numer pasma z plusem, np. "+2",
     * dla ujemnego - z minusem, a dla pasma zerowego - etykieta planu pracy.
     */
    private String getBandLabel(Integer bandNumber) {
        String bandLabel;
        if (bandNumber == 0) {
            bandLabel = messageSource.getMessage(SELF_SCHEDULE_SHORTCUT, null, locale);
        } else if (bandNumber > 0) {
            bandLabel = "+" + bandNumber;
        } else {
            bandLabel = String.valueOf(bandNumber);
        }
        return bandLabel;
    }

    private void fillEnergyDers(XSSFSheet sheet, AtomicInteger currentColumn, List<AuctionDayAheadOfferDTO> offers, List<Integer> presentBands,
        Map<String, CellStyle> styleByCouplingPoint, String timestamp, Map<Long, UnitEntity> dersById) {
        offers.stream().flatMap(offer -> offer.getDers().stream())
            .sorted(Comparator.comparing(offerDer -> getCouplingPointName(dersById.get(offerDer.getDer().getId())))).forEach(der -> {
                fillEnergyDerColumn(sheet, currentColumn.getAndIncrement(), der, styleByCouplingPoint, presentBands, timestamp, dersById);
            });
    }

    private void fillEnergyDerColumn(XSSFSheet sheet, int currentColumn, AuctionOfferDersDTO offerDer, Map<String, CellStyle> styleByCouplingPoint, List<Integer> presentBands, String timestamp, Map<Long, UnitEntity> dersById) {
        AtomicInteger currentRow = new AtomicInteger(0);
        UnitEntity der = dersById.get(offerDer.getDer().getId());
        addStringCellValueIfExist(sheet, currentRow.getAndIncrement(), currentColumn, offerDer.getDer().getName());
        addStringCellValueIfExist(sheet, currentRow.getAndIncrement(), currentColumn, getCouplingPointName(der));
        addStringCellValueIfExist(sheet, currentRow.getAndIncrement(), currentColumn, getPowerStationId(der));
        der.getPointOfConnectionWithLvTypes().stream()
            .findAny().map(LocalizationTypeEntity::getName)
            .ifPresentOrElse(
                pocWithLv -> addStringCellValueIfExist(sheet, currentRow.getAndIncrement(), currentColumn, pocWithLv),
                () -> addStringCellValueIfExist(sheet, currentRow.getAndIncrement(), currentColumn, ""));
        addStringCellValueIfExist(sheet, currentRow.getAndIncrement(), currentColumn, offerDer.getDer().getPMin().toString());
        addStringCellValueIfExist(sheet, currentRow.getAndIncrement(), currentColumn, offerDer.getDer().getSourcePower().toString());
        setVolumesForDer(sheet, offerDer, presentBands, currentRow, currentColumn, timestamp);
        sheet.autoSizeColumn(currentColumn);
        applyColorEnergy(sheet, currentColumn, currentRow.get(), styleByCouplingPoint, getCouplingPointName(der));
    }

    /**
     * Metoda uzupełnia kolor dla konkretnej kolumny dera w arkuszu oferty energy ze względu na coupling point, do którego należy ten der.
     */
    private void applyColorEnergy(XSSFSheet sheet, int currentColumn, int lastRow, Map<String, CellStyle> styleByCouplingPoint, String couplingPointName) {
        AtomicInteger currentRow = new AtomicInteger(0);
        do {
            Optional.ofNullable(styleByCouplingPoint.get(couplingPointName))
                .ifPresentOrElse(style -> CellUtils.getCell(sheet, currentRow.get(), currentColumn).setCellStyle(style),
                    () -> {
                        CellStyle style = Optional.ofNullable(sheet.getRow(currentRow.get()).getRowStyle()).orElse(sheet.getWorkbook().createCellStyle());
                        style.setFillForegroundColor(getNextColorIndex());
                        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                        styleByCouplingPoint.put(couplingPointName, style);
                        CellUtils.getCell(sheet, currentRow.get(), currentColumn).setCellStyle(style);
                    });
        } while (currentRow.incrementAndGet() < lastRow);
    }

    /**
     * Metoda wypełnia wolumeny dla każdego pasma danej oferty, dera i timestampa. Jeżeli dla danego pasma została złożona oferta, ale nie należy ona
     * do uzupełnianego dera, wpisywane jest słowo 'brak'/'lack'.
     */
    private void setVolumesForDer(XSSFSheet sheet, AuctionOfferDersDTO der, List<Integer> presentBands, AtomicInteger currentRow, Integer currentColumn, String timestamp) {
        presentBands.forEach(bandNumber -> {
            List<BigDecimal> volumes = der.getBandData().stream()
                .filter(bandData -> Objects.equals(bandData.getBandNumber(), bandNumber) && Objects.equals(bandData.getHourNumber(), timestamp))
                .map(AuctionOfferBandDataDTO::getAcceptedVolume).collect(Collectors.toList());
            if (volumes.size() != 0) {
                BigDecimal volume = Objects.isNull(volumes.get(0)) ? BigDecimal.ZERO : volumes.get(0);
                CellUtils.getCell(sheet, currentRow.getAndIncrement(), currentColumn).setCellValue(String.valueOf(volume));
            } else {
                CellUtils.getCell(sheet, currentRow.getAndIncrement(), currentColumn).setCellValue(messageSource.getMessage("exporter.bids.evaluation.lack", null, locale));
            }
        });
    }

    /**
     * Metoda sprawdza, jakie numery pasm są obecne w ofertach złożonych na dany timestamp.
     */
    private List<Integer> getPresentBands(List<AuctionDayAheadOfferDTO> offers, String timestamp) {
        return offers.stream().flatMap(offer -> offer.getDers().stream()).flatMap(der -> der.getBandData().stream())
            .filter(band -> Objects.equals(band.getHourNumber(), timestamp))
            .map(AuctionOfferBandDataDTO::getBandNumber).sorted(Comparator.reverseOrder()).distinct().collect(Collectors.toList());
    }
}
