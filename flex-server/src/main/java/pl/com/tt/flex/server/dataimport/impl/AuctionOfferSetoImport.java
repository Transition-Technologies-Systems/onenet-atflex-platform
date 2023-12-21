package pl.com.tt.flex.server.dataimport.impl;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pl.com.tt.flex.model.service.dto.auction.type.AuctionDayAheadType;
import pl.com.tt.flex.model.service.dto.file.FileDTO;
import pl.com.tt.flex.server.dataimport.DataImport;
import pl.com.tt.flex.server.dataimport.factory.DataImportFormat;
import pl.com.tt.flex.server.domain.auction.offer.da.AuctionDayAheadOfferEntity;
import pl.com.tt.flex.server.repository.auction.offer.AuctionDayAheadOfferRepository;
import pl.com.tt.flex.server.service.importData.auctionOffer.dto.AuctionOfferSetoImportData;

@Slf4j
@Component
@Transactional(readOnly = true)
@AllArgsConstructor
public class AuctionOfferSetoImport implements DataImport<AuctionOfferSetoImportData> {
    private static final int ENERGY_TIMESTAMP_GROUP_NUMBER = 3;
    private static final int CAPACITY_TIMESTAMP_GROUP_NUMBER = 2;
    private static final List<String> SELF_SCHEDULE_LABELS = List.of("SS", "PP");
    private static final List<String> EMPTY_CELL_LABELS = List.of("BRAK", "LACK");
    private final AuctionDayAheadOfferRepository auctionDayAheadOfferRepository;

    @Override
    public List<AuctionOfferSetoImportData> doImport(MultipartFile file, Locale locale) throws IOException {
        List<AuctionOfferSetoImportData> importedBids = new ArrayList<>();
        XSSFWorkbook workbook = new XSSFWorkbook(file.getInputStream());
        for (int i = 1; i < workbook.getNumberOfSheets(); i++) {
            XSSFSheet sheet = workbook.getSheetAt(i);
            List<Long> bidIds = getBidIds(workbook);
            List<AuctionDayAheadOfferEntity> bids = auctionDayAheadOfferRepository.findAllById(bidIds);
            Map<String, Long> energyBidsByDer = getEnergyBidIdsByDerName(bids);
            if (isEnergySheet(sheet)) {
                importedBids.addAll(readEnergySheetData(sheet, energyBidsByDer));
            } else {
                importedBids.addAll(readCapacitySheetData(sheet, getCapacityBidIdsByDerName(bidIds, energyBidsByDer)));
            }
        }
        return importedBids;
    }

    private List<Long> getBidIds(XSSFWorkbook workbook) {
        XSSFSheet sheet = workbook.getSheetAt(0);
        Iterator<Row> rowIterator = sheet.rowIterator();
        rowIterator.next(); //Pomiń rząd nagłówków
        List<Long> bidIds = new ArrayList<>();
        rowIterator.forEachRemaining(row -> bidIds.add(Long.parseLong(row.getCell(0).getStringCellValue())));
        return bidIds;
    }

    @Override
    public List<AuctionOfferSetoImportData> doImport(FileDTO fileDTO, Locale locale) {
        throw new NotImplementedException("This method is currently not used");
    }

    @Override
    public boolean supportClass(Class clazz) {
        return clazz.equals(AuctionOfferSetoImportData.class);
    }

    @Override
    public boolean supportFormat(DataImportFormat format) {
        return format.equals(DataImportFormat.XLSX);
    }

    //************************************************************************ CAPACITY IMPORT ************************************************************************************

    private Set<AuctionOfferSetoImportData> readCapacitySheetData(XSSFSheet sheet, Map<String, Long> bidIdsByDerName) {
        String timestamp = extractTimestamp(sheet, CAPACITY_TIMESTAMP_GROUP_NUMBER);
        Set<AuctionOfferSetoImportData> sheetData = new HashSet<>();
        Iterator<Row> rowIterator = sheet.rowIterator();
        rowIterator.next(); //Pomiń rząd nagłówków
        rowIterator.forEachRemaining(row -> {
            for (int colNum = 7; colNum < row.getLastCellNum(); colNum++) {
                getCapacityCellData(bidIdsByDerName, timestamp, sheetData, row, colNum);
            }
        });
        return sheetData;
    }

    private void getCapacityCellData(Map<String, Long> bidIdsByDerName, String timestamp, Set<AuctionOfferSetoImportData> sheetData, Row row, int colNum) {
        String derName = row.getCell(0).getStringCellValue();
        Long offerId = bidIdsByDerName.get(derName);
        Optional.of(row.getCell(colNum))
            .map(this::getCellValue)
            .filter(cellValue -> !EMPTY_CELL_LABELS.contains(cellValue))
            .map(volumeString -> AuctionOfferSetoImportData.builder()
                .timestamp(timestamp)
                .volume(Double.parseDouble(volumeString))
                .offerId(offerId)
                .derName(derName)
                .build())
            .ifPresent(sheetData::add);
    }

    private Map<String, Long> getCapacityBidIdsByDerName(List<Long> bids, Map<String, Long> energyBidsByDer) {
        List<Long> bidIds = bids.stream().filter(bid -> !energyBidsByDer.containsValue(bid)).collect(Collectors.toList());
        Map<String, Long> bidIdsByDerNameMap = new HashMap<>();
        auctionDayAheadOfferRepository.findByIdInFetchUnits(bidIds).forEach(offer -> offer.getUnits().forEach(offerDer ->
            bidIdsByDerNameMap.put(offerDer.getUnit().getName(), offer.getId())));
        return bidIdsByDerNameMap;
    }

    //************************************************************************ ENERGY IMPORT ************************************************************************************

    private Set<AuctionOfferSetoImportData> readEnergySheetData(XSSFSheet sheet, Map<String, Long> bidIdsByDerName) {
        String timestamp = extractTimestamp(sheet, ENERGY_TIMESTAMP_GROUP_NUMBER);
        Set<AuctionOfferSetoImportData> sheetData = new HashSet<>();
        int columnCount = sheet.getRow(0).getLastCellNum();
        for (int colNum = 1; colNum < columnCount; colNum++) {
            AtomicInteger rowNum = new AtomicInteger(6);
            while (rowNum.get() <= sheet.getLastRowNum()) {
                if (!SELF_SCHEDULE_LABELS.contains(getCellValue(sheet.getRow(rowNum.get()).getCell(0)))) {
                    getEnergyCellData(sheet, bidIdsByDerName, timestamp, sheetData, sheet.getRow(rowNum.get()), colNum);
                }
                rowNum.getAndIncrement();
            }
        }
        return sheetData;
    }

    private Map<String, Long> getEnergyBidIdsByDerName(List<AuctionDayAheadOfferEntity> bids) {
        List<Long> bidIds = bids.stream().filter(bid -> Objects.equals(bid.getAuctionDayAhead().getType(), AuctionDayAheadType.ENERGY))
            .map(AuctionDayAheadOfferEntity::getId).collect(Collectors.toList());
        Map<String, Long> bidIdsByDerNameMap = new HashMap<>();
        auctionDayAheadOfferRepository.findByIdInFetchUnits(bidIds).forEach(offer -> offer.getUnits().forEach(offerDer ->
            bidIdsByDerNameMap.put(offerDer.getUnit().getName(), offer.getId())));
        return bidIdsByDerNameMap;
    }

    private void getEnergyCellData(XSSFSheet sheet, Map<String, Long> bidIdsByDerName, String timestamp, Set<AuctionOfferSetoImportData> sheetData, Row row, int colNum) {
        String derName = getCellValue(sheet.getRow(0).getCell(colNum));
        Long offerId = bidIdsByDerName.get(derName);
        Optional.of(row.getCell(colNum))
            .map(this::getCellValue)
            .filter(cellValue -> !EMPTY_CELL_LABELS.contains(cellValue))
            .map(volumeString -> AuctionOfferSetoImportData.builder()
                .timestamp(timestamp)
                .volume(Double.parseDouble(volumeString))
                .band(String.valueOf(getBandNumber(getCellValue(row.getCell(0)))))
                .offerId(offerId)
                .derName(derName)
                .build())
            .ifPresent(sheetData::add);
    }

    private int getBandNumber(String bandLabel) {
        if (SELF_SCHEDULE_LABELS.contains(bandLabel)) {
            return 0;
        }
        return (int) Double.parseDouble(bandLabel);
    }

    private boolean isEnergySheet(XSSFSheet sheet) {
        return sheet.getSheetName().contains("EB");
    }

    //************************************************************************ COMMONS ************************************************************************************

    private String extractTimestamp(XSSFSheet sheet, int timestampGroupNumber) {
        String sheetName = sheet.getSheetName();
        return sheetName.split(" ")[timestampGroupNumber];
    }

    private String getCellValue(Cell cell) {
        if (cell.getCellTypeEnum().equals(CellType.STRING)) {
            return cell.getStringCellValue();
        }
        return String.valueOf(cell.getNumericCellValue());
    }
}
