package pl.com.tt.flex.server.service.auction.da.file.reader;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.Range;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.util.Pair;
import pl.com.tt.flex.model.service.dto.auction.da.AuctionDayAheadDTO;
import pl.com.tt.flex.model.service.dto.auction.da.AuctionDayAheadMinDTO;
import pl.com.tt.flex.model.service.dto.auction.offer.da.AuctionDayAheadOfferDTO;
import pl.com.tt.flex.model.service.dto.auction.offer.da.AuctionOfferBandDataDTO;
import pl.com.tt.flex.model.service.dto.auction.offer.da.AuctionOfferDersDTO;
import pl.com.tt.flex.model.service.dto.auction.type.AuctionDayAheadType;
import pl.com.tt.flex.model.service.dto.auction.type.AuctionOfferType;
import pl.com.tt.flex.model.service.dto.der.DerMinDTO;
import pl.com.tt.flex.model.service.dto.schedulingUnit.SchedulingUnitMinDTO;
import pl.com.tt.flex.server.common.errors.ObjectValidationException;
import pl.com.tt.flex.server.service.schedulingUnit.dto.SchedulingUnitDTO;
import pl.com.tt.flex.server.service.unit.dto.UnitMinDTO;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.apache.poi.ss.usermodel.CellType.BLANK;
import static org.apache.poi.ss.usermodel.CellType.NUMERIC;
import static pl.com.tt.flex.server.util.AuctionDayAheadDataUtil.getDeliveryPeriodRange;
import static pl.com.tt.flex.server.web.rest.errors.ErrorConstants.*;

@Slf4j
public abstract class AbstractDAOfferImportFileReader {

    protected static AuctionDayAheadOfferDTO readDers(XSSFWorkbook workbook, Range<Integer> expectedBandRange, SchedulingUnitDTO schedulingUnit,
                                                      AuctionDayAheadDTO auction, AuctionDayAheadOfferDTO dbOffer, int derSectionHeight, int selfScheduleOffset,
                                                      int firstDerNameRow, Range<Instant> deliveryPeriod) throws ObjectValidationException {
        FormulaEvaluator formulaEvaluator = workbook.getCreationHelper().createFormulaEvaluator();
        evaluateAllWorkbook(workbook, formulaEvaluator);
        XSSFSheet sheet = workbook.getSheetAt(0);

        AuctionDayAheadOfferDTO offer = Optional.ofNullable(dbOffer)
            .orElse(prepareOfferDto(auction, schedulingUnit));
        CellAddress currDerNameCellAddress = new CellAddress(firstDerNameRow, 1);
        Map<String, AuctionOfferDersDTO> dbOfferDersDbByDerName = Optional.ofNullable(offer.getDers()).stream()
            .flatMap(List::stream)
            .collect(Collectors.toMap(offerDer -> offerDer.getDer().getName(), Function.identity()));
        Map<String, UnitMinDTO> dbDersByName = schedulingUnit.getUnits().stream()
            .collect(Collectors.toMap(der -> der.getName().toLowerCase(), Function.identity()));
        List<AuctionOfferDersDTO> dersOffer = new ArrayList<>();
        boolean offerEmpty = true;
        int dersCount = dbDersByName.size();
        for (int derNumber = 0; derNumber < dersCount; derNumber++) {
            boolean hasOffer = readBands(expectedBandRange, selfScheduleOffset, currDerNameCellAddress, sheet, dbOfferDersDbByDerName, dbDersByName, dersOffer);
            if (hasOffer) {
                offerEmpty = false;
            }
            currDerNameCellAddress = new CellAddress(currDerNameCellAddress.getRow() + derSectionHeight, currDerNameCellAddress.getColumn());
        }
        if (offerEmpty) {
            throw new ObjectValidationException("The file was not imported. At least one hour with volume and price filled in required.", AUCTION_DA_OFFER_IMPORT_NO_VALID_TIMESTAMPS);
        }
        offer.setDers(dersOffer);
        setDeliveryHoursAndTrimBands(offer, deliveryPeriod);
        return offer;
    }

    private static void setDeliveryHoursAndTrimBands(AuctionDayAheadOfferDTO offer, Range<Instant> deliveryPeriod) {
        List<AuctionOfferDersDTO> offerDers = offer.getDers();
        Range<Integer> deliveryPeriodHourRange = getDeliveryPeriodRange(deliveryPeriod.getMinimum(), deliveryPeriod.getMaximum());
        removeBandsOutOfRange(offerDers, deliveryPeriodHourRange);
        Instant deliveryPeriodFrom = deliveryPeriod.getMinimum();
        Instant deliveryPeriodTo = deliveryPeriod.getMaximum();
        offer.setDeliveryPeriodFrom(deliveryPeriodFrom);
        offer.setDeliveryPeriodTo(deliveryPeriodTo);
    }

    private static void removeBandsOutOfRange(List<AuctionOfferDersDTO> offerDers, Range<Integer> deliveryHoursRange) {
        for (AuctionOfferDersDTO offerDer : offerDers) {
            List<AuctionOfferBandDataDTO> derBands = offerDer.getBandData();
            List<AuctionOfferBandDataDTO> bandsToRemove = new ArrayList<>();
            for (AuctionOfferBandDataDTO band : derBands) {
                Integer bandHour = getTimestampInteger(band);
                if (!deliveryHoursRange.contains(bandHour)) {
                    bandsToRemove.add(band);
                }
            }
            derBands.removeAll(bandsToRemove);
        }
    }

    private static Integer getTimestampInteger(AuctionOfferBandDataDTO band) {
        return Optional.of(band.getHourNumber())
            .filter(hour -> !hour.equals("2a"))
            .map(Integer::valueOf)
            .orElse(2);
    }

    private static boolean readBands(Range<Integer> expectedBandRange, int selfScheduleOffset, CellAddress currDerNameCellAddress,
                                     XSSFSheet sheet, Map<String, AuctionOfferDersDTO> dbOfferDersDbByDerName, Map<String, UnitMinDTO> dbDersByName,
                                     List<AuctionOfferDersDTO> dersOffer) throws ObjectValidationException {
        boolean hasOffer = false;
        int row = currDerNameCellAddress.getRow();
        String currDerName = sheet.getRow(row).getCell(currDerNameCellAddress.getColumn()).getStringCellValue().strip().toLowerCase();
        log.debug("readBands() der name from row {}: {}", row, currDerName);
        AuctionOfferDersDTO tmpOfferDer = prepareOfferDerDto(dbDersByName.get(currDerName),
            dbOfferDersDbByDerName.get(currDerName));
        Map<Pair<Integer, String>, AuctionOfferBandDataDTO> bands = tmpOfferDer.getBandData().stream()
            .collect(Collectors.toMap(band -> Pair.of(band.getBandNumber(), band.getHourNumber()), Function.identity()));
        for (int bandNumber = expectedBandRange.getMinimum(); bandNumber <= expectedBandRange.getMaximum(); bandNumber++) {
            XSSFRow bandRowInSheet = sheet.getRow(row + selfScheduleOffset - bandNumber);
            boolean bandNotEmpty = readTimestamps(currDerNameCellAddress, bands, bandNumber, bandRowInSheet);
            hasOffer = hasOffer || bandNotEmpty;
        }
        int scheduleRow = row + selfScheduleOffset;
        boolean hasSchedule = hasSelfSchedule(sheet, currDerNameCellAddress, scheduleRow);
        List<AuctionOfferBandDataDTO> bandData = new ArrayList<>(bands.values());
        if (containsVolumePricePairs(bandData)) {
            if (!hasSchedule) {
                throw new ObjectValidationException("Cannot import offer, because der has no selfschedule", AUCTION_DA_OFFER_IMPORT_INCOHERENT_SELF_SCHEDULE);
            }
            tmpOfferDer.setBandData(bandData);
            dersOffer.add(tmpOfferDer);
        }
        return hasOffer;
    }

    private static boolean containsVolumePricePairs(List<AuctionOfferBandDataDTO> bandData) {
        return bandData.stream().anyMatch(band -> Objects.nonNull(band.getPrice()) && Objects.nonNull(band.getVolume()));
    }

    private static boolean hasSelfSchedule(XSSFSheet sheet, CellAddress currDerNameCellAddress, int scheduleRow) {
        for (int hour = 1; hour < 25; hour++) {
            int timestampVolumeCol = currDerNameCellAddress.getColumn() + hour * 2;
            Cell volumeCell = sheet.getRow(scheduleRow).getCell(timestampVolumeCol);
            if (Objects.isNull(volumeCell) || volumeCell.getCellTypeEnum().equals(BLANK)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Metoda pobiera wartości wolumenu i ceny dla wszystkich timestampów danego dera.
     * Metoda zwraca fałsz w przypadku, gdy der nie zgłosił żadnej oferty.
     */
    private static boolean readTimestamps(CellAddress currDerNameCellAddress,
                                          Map<Pair<Integer, String>, AuctionOfferBandDataDTO> bands,
                                          Integer bandNumber, XSSFRow bandRowInSheet) throws ObjectValidationException {
        boolean bandNotEmpty = false;
        for (int hour = 1; hour <= 25; hour++) {
            String hourString = Optional.of(hour)
                .filter(value -> value < 25)
                .map(value -> Integer.toString(value))
                .orElse("2a");
            int timestampVolumeCol = currDerNameCellAddress.getColumn() + hour * 2;
            bandNotEmpty = setBandVolumeAndPrice(bands, bandNumber, bandRowInSheet, bandNotEmpty, hourString, timestampVolumeCol);
        }
        return bandNotEmpty;

    }

    private static boolean setBandVolumeAndPrice(Map<Pair<Integer, String>, AuctionOfferBandDataDTO> bands,
        Integer bandNumber, XSSFRow bandRowInSheet, boolean notEmpty, String hourString,
        int timestampVolumeCol) throws ObjectValidationException {
        Double volume = getVolume(bandRowInSheet, timestampVolumeCol);
        XSSFCell priceCell = bandRowInSheet.getCell(timestampVolumeCol + 1);
        Double price = getPrice(priceCell);
        if (bandNumber == 1 && Set.of("13", "14").contains(hourString)) {
            log.info("DEBUG price {}", price);
        }
        if (Objects.isNull(volume) || (bandNumber != 0 && Objects.isNull(price))) {
            return notEmpty;
        }
        setBandVolume(bands, bandNumber, hourString, volume);
        if (Objects.nonNull(price)) {
            setBandPrice(bands, bandNumber, hourString, price);
        }
        if (bandNumber != 0 && volume != 0) {
            return true;
        }
        return notEmpty;
    }

    private static Double getPrice(XSSFCell priceCell) throws ObjectValidationException {
        if (Objects.isNull(priceCell)) {
            return null;
        }
        try {
            if (priceCell.getCellTypeEnum().equals(CellType.STRING)) {
                priceCell.setCellType(CellType.NUMERIC);
            }
            return priceCell.getNumericCellValue();
        } catch (Exception ex) {
            log.info("Could not read price with cause: {}", ex.getMessage());
            throw new ObjectValidationException("Cannot read because of invalid price value", IMPORT_OFFERS_TEMPLATE_INCORRECT);
        }
    }

    private static Double getVolume(XSSFRow bandRow, int timestampVolumeCol) throws ObjectValidationException {
        try {
            XSSFCell cell = bandRow.getCell(timestampVolumeCol);
            if (Objects.isNull(cell) || cell.getCellTypeEnum().equals(BLANK)) {
                return null;
            }
            if (cell.getCellTypeEnum().equals(NUMERIC)) {
                return cell.getNumericCellValue();
            }
        } catch (Exception ex) {
            throw new ObjectValidationException("Cannot import because of wrong volume", AUCTION_DA_OFFER_IMPORT_INCOHERENT_SELF_SCHEDULE);
        }
        return null;
    }

    private static void setBandVolume(Map<Pair<Integer, String>, AuctionOfferBandDataDTO> bands, Integer bandNumber, String hourString, double volume) {
        if (volume != 0.0D || bandNumber == 0) {
            Pair<Integer, String> bandHourPair = Pair.of(bandNumber, hourString);
            if (bands.containsKey(bandHourPair)) {
                AuctionOfferBandDataDTO bandDto = bands.get(bandHourPair);
                bandDto.setVolume(BigDecimal.valueOf(volume));
                bandDto.setAcceptedVolume(BigDecimal.valueOf(volume));
                bands.replace(bandHourPair, bandDto);
            } else {
                AuctionOfferBandDataDTO bandDto = new AuctionOfferBandDataDTO();
                bandDto.setBandNumber(bandNumber);
                bandDto.setHourNumber(hourString);
                bandDto.setVolume(BigDecimal.valueOf(volume));
                bandDto.setAcceptedVolume(BigDecimal.valueOf(volume));
                bands.put(bandHourPair, bandDto);
            }
        }
    }

    private static void setBandPrice(Map<Pair<Integer, String>, AuctionOfferBandDataDTO> bands, Integer bandNumber, String hourString, double price) {
        if (price != 0.0D) {
            Pair<Integer, String> bandHourPair = Pair.of(bandNumber, hourString);
            if (bands.containsKey(bandHourPair)) {
                AuctionOfferBandDataDTO bandDto = bands.get(bandHourPair);
                bandDto.setPrice(BigDecimal.valueOf(price));
                bandDto.setAcceptedPrice(BigDecimal.valueOf(price));
                bands.replace(bandHourPair, bandDto);
            } else {
                AuctionOfferBandDataDTO bandDto = new AuctionOfferBandDataDTO();
                bandDto.setBandNumber(bandNumber);
                bandDto.setHourNumber(hourString);
                bandDto.setPrice(BigDecimal.valueOf(price));
                bandDto.setAcceptedPrice(BigDecimal.valueOf(price));
                bands.put(bandHourPair, bandDto);
            }
        }
    }

    private static AuctionDayAheadOfferDTO prepareOfferDto(AuctionDayAheadDTO auction, SchedulingUnitDTO schedulingUnit) {
        AuctionDayAheadOfferDTO offer = new AuctionDayAheadOfferDTO();
        AuctionDayAheadMinDTO auctionMinDto = new AuctionDayAheadMinDTO(auction.getId(), auction.getName(), auction.getProduct());
        offer.setAuctionDayAhead(auctionMinDto);
        SchedulingUnitMinDTO schedulingUnitMinDTO = new SchedulingUnitMinDTO(schedulingUnit.getId(), schedulingUnit.getName(), schedulingUnit.getSchedulingUnitType());
        offer.setSchedulingUnit(schedulingUnitMinDTO);
        offer.setVolumeDivisibility(true);
        offer.setDeliveryPeriodDivisibility(true);
        if (auction.getType().equals(AuctionDayAheadType.CAPACITY)) {
            offer.setType(AuctionOfferType.CAPACITY);
        } else if (auction.getType().equals(AuctionDayAheadType.ENERGY)) {
            offer.setType(AuctionOfferType.ENERGY);
        } else {
            throw new IllegalStateException("Unsupported auction type");
        }
        return offer;
    }

    private static AuctionOfferDersDTO prepareOfferDerDto(UnitMinDTO der, AuctionOfferDersDTO auctionOfferDersDTO) {
        if (Objects.nonNull(auctionOfferDersDTO)) {
            return auctionOfferDersDTO;
        }
        AuctionOfferDersDTO offerDer = new AuctionOfferDersDTO();
        DerMinDTO derMinDto = new DerMinDTO();
        derMinDto.setId(der.getId());
        derMinDto.setName(der.getName());
        derMinDto.setSourcePower(der.getSourcePower());
        derMinDto.setPMin(der.getPMin());
        offerDer.setDer(derMinDto);
        return offerDer;
    }

    /**
     * Obliczenie wszystkich formul w danym pliku
     * Wydajnosciowo lepiej obliczac wszystkie formuly w danym pliku w jednej petli, niz dla kazdej komorki odzielnnie
     */
    private static void evaluateAllWorkbook(XSSFWorkbook workbook, FormulaEvaluator formulaEvaluator) {
        for (int i = 0; i < workbook.getNumberOfSheets(); ++i) {
            Sheet sheet = workbook.getSheetAt(i);
            Iterator iterator = sheet.iterator();
            while (iterator.hasNext()) {
                Row r = (Row) iterator.next();
                Iterator rowIterator = r.iterator();
                while (rowIterator.hasNext()) {
                    Cell c = (Cell) rowIterator.next();
                    if (c.getCellTypeEnum() == CellType.FORMULA) {
                        formulaEvaluator.evaluateFormulaCellEnum(c);
                    }
                }
            }
        }
    }

}
