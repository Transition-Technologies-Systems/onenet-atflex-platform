package pl.com.tt.flex.server.service.algorithm.agnoAlgorithm.eneregy;

import static pl.com.tt.flex.server.service.algorithm.agnoAlgorithm.eneregy.AgnoResultsFileGeneratorImpl.extractTimestampFromFileDTO;
import static pl.com.tt.flex.server.service.algorithm.agnoAlgorithm.eneregy.AgnoResultsFileGeneratorImpl.isTimestampDoubleDigit;
import static pl.com.tt.flex.server.service.common.XlsxUtil.getWorkbook;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import pl.com.tt.flex.model.service.dto.file.FileDTO;
import pl.com.tt.flex.server.domain.algorithm.AlgorithmEvaluationEntity;
import pl.com.tt.flex.server.domain.auction.offer.da.AuctionDayAheadOfferEntity;
import pl.com.tt.flex.server.domain.auction.offer.da.AuctionOfferBandDataEntity;
import pl.com.tt.flex.server.domain.auction.offer.da.AuctionOfferDersEntity;
import pl.com.tt.flex.server.domain.unit.LocalizationTypeEntity;
import pl.com.tt.flex.server.domain.unit.UnitEntity;
import pl.com.tt.flex.server.util.ZipUtil;

@Slf4j
@Service
public class AgnoBmResultsFileReaderImpl implements AgnoBmResultsFileReader {

    @Override
    public Map<List<AuctionOfferBandDataEntity>, Long> getBandsByOfferId(AlgorithmEvaluationEntity algEvaluation) throws IOException {
        Map<String, Long> offerIdsByCouplingPoint = algEvaluation.getDaOffers().stream()
            .map(offer -> Pair.of(getCouplingPointName(offer), offer.getId()))
            .collect(Collectors.toMap(Pair::getLeft, Pair::getRight));
        List<FileDTO> outputFiles = ZipUtil.zipToFiles(algEvaluation.getOutputFilesZip());
        Map<String, List<FileDTO>> filesByCouplingPoint = outputFiles.stream()
            .collect(Collectors.groupingBy(this::extractCouplingPointNameFromFileDTO));
        Map<List<AuctionOfferBandDataEntity>, Long> pairs = new HashMap<>();
        for (Map.Entry<String, List<FileDTO>> entry : filesByCouplingPoint.entrySet()) {
            Long matchedOfferId = offerIdsByCouplingPoint.get(entry.getKey());
            if (Objects.nonNull(matchedOfferId)) {
                List<AuctionOfferBandDataEntity> agnoResultBands = getAllBandsFromFiles(entry.getValue());
                pairs.put(agnoResultBands, matchedOfferId);
            }
        }
        return pairs;
    }

    /**
     * Zwraca listę pasm zawartych w podanych plikach wyjściowych agno
     */
    private List<AuctionOfferBandDataEntity> getAllBandsFromFiles(List<FileDTO> files) throws IOException {
        List<AuctionOfferBandDataEntity> agnoResultBands = new ArrayList<>();
        for (FileDTO file : files) {
            Set<AuctionOfferBandDataEntity> bandsForTimestamp = readFileBands(file);
            agnoResultBands.addAll(bandsForTimestamp);
        }
        return agnoResultBands;
    }

    /**
     * Zwraca zestaw pasm odczytanych z pliku wyjściowego agno
     */
    private Set<AuctionOfferBandDataEntity> readFileBands(FileDTO agnoOutputFile) throws IOException {
        final int POWER_COL = 1;
        final int PRICE_COL = 2;
        String timestamp = extractTimestampFromFileDTO(agnoOutputFile);
        Map<Integer, AuctionOfferBandDataEntity> bands = new HashMap<>();
        XSSFWorkbook workbook = getWorkbook(agnoOutputFile);
        XSSFSheet bandsSheet = workbook.getSheetAt(1);
        if (bandsSheet.getLastRowNum() > 0) {
            XSSFRow currRow = bandsSheet.getRow(1);
            Optional<String> productType = tryReadProductType(currRow);
            while (productType.isPresent()) {
                double power = currRow.getCell(POWER_COL).getNumericCellValue();
                double price = currRow.getCell(PRICE_COL).getNumericCellValue();
                int bandNum = getNextBandNum(bands.keySet(), productType.get());
                AuctionOfferBandDataEntity band = new AuctionOfferBandDataEntity();
                band.setAcceptedVolume(BigDecimal.valueOf(power));
                band.setAcceptedPrice(BigDecimal.valueOf(price));
                band.setBandNumber(String.valueOf(bandNum));
                band.setHourNumber(timestamp);
                bands.put(bandNum, band);
                currRow = bandsSheet.getRow(currRow.getRowNum() + 1);
                productType = tryReadProductType(currRow);
            }
            addSelfScheduleBand(timestamp, bands, workbook);
        }
        return new HashSet<>(bands.values());
    }

    /**
     * Dodaje do mapy pasmo z planem pracy
     */
    private void addSelfScheduleBand(String timestamp, Map<Integer, AuctionOfferBandDataEntity> bands, XSSFWorkbook workbook) {
        double selfScheduleValue = workbook.getSheetAt(2).getRow(1).getCell(1).getNumericCellValue();
        AuctionOfferBandDataEntity selfSchedule = new AuctionOfferBandDataEntity();
        selfSchedule.setAcceptedVolume(BigDecimal.valueOf(selfScheduleValue));
        selfSchedule.setBandNumber("0");
        selfSchedule.setHourNumber(timestamp);
        bands.put(0, selfSchedule);
    }

    /**
     * Zwraca nazwę typu produktu zawartą w podanym wierszu lub null
     */
    private Optional<String> tryReadProductType(XSSFRow currRow) {
        return Optional.ofNullable(currRow).map(row -> row.getCell(0)).map(XSSFCell::getStringCellValue);
    }

    private String getCouplingPointName(AuctionDayAheadOfferEntity offer) {
        return offer.getUnits().stream()
            .map(AuctionOfferDersEntity::getUnit)
            .map(UnitEntity::getCouplingPointIdTypes)
            .flatMap(Set::stream)
            .map(LocalizationTypeEntity::getName)
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Cannot find coupling point name"));
    }

    /**
     * Zwraca nazwę punktu sprzężenia jednostki
     */
    private String extractCouplingPointNameFromFileDTO(FileDTO agnoOutputFile) {
        String filename = agnoOutputFile.getFileName();
        int charsToCutFromEnd;
        if (isTimestampDoubleDigit(filename)) {
            charsToCutFromEnd = 19;
        } else {
            charsToCutFromEnd = 18;
        }
        return filename.substring(14, filename.length() - charsToCutFromEnd);
    }

    /**
     * Zwraca numer następnego w kolejności pasma do wypełnienia na podstawie typu produktu i już uzupełnionych pasm
     */
    private int getNextBandNum(Set<Integer> filledBands, String productType) {
        if (productType.equals("up")) {
            return Optional.of(filledBands)
                .filter(set -> !set.isEmpty())
                .map(Collections::max)
                .filter(min -> min > 0)
                .map(min -> min + 1)
                .orElse(1);
        } else if (productType.equals("down")) {
            return Optional.of(filledBands)
                .filter(set -> !set.isEmpty())
                .map(Collections::min)
                .filter(min -> min < 0)
                .map(min -> min - 1)
                .orElse(-1);
        }
        throw new IllegalStateException("Unsupported product type");
    }

}
