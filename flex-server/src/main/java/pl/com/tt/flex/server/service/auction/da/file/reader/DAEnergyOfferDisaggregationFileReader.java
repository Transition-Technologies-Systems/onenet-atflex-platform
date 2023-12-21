package pl.com.tt.flex.server.service.auction.da.file.reader;

import static pl.com.tt.flex.server.service.algorithm.agnoAlgorithm.eneregy.AgnoResultsFileGeneratorImpl.extractTimestampFromFileDTO;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.data.util.Pair;

import pl.com.tt.flex.model.service.dto.file.FileDTO;
import pl.com.tt.flex.server.domain.auction.offer.da.AuctionOfferBandDataEntity;
import pl.com.tt.flex.server.service.common.XlsxUtil;

public class DAEnergyOfferDisaggregationFileReader extends AbstractDAOfferImportFileReader {

    private final static Set<String> SELF_SCHEDULE_LABELS = Set.of("SS", "PP");

    /**
     * Zwraca pasma zawarte w pliku impportu agno według klucza (numer pasma, timestamp)
     */
    public static Map<Pair<String, String>, AuctionOfferBandDataEntity> readInputFileBands(Workbook workbook) {
        final int FIRST_BAND_ROW_NUMBER = 2;
        Sheet sheet = workbook.getSheetAt(0);
        Map<Pair<String, String>, AuctionOfferBandDataEntity> bands = new HashMap<>();
        for (int rowNum = FIRST_BAND_ROW_NUMBER; rowNum <= sheet.getLastRowNum(); rowNum++) {
            Row row = sheet.getRow(rowNum);
            readTimestamps(bands, row);
        }
        return bands;
    }

    /**
     * Wypełnia mapę obiektami pasm odczytanymi z podanego wiersza
     */
    private static void readTimestamps(Map<Pair<String, String>, AuctionOfferBandDataEntity> bands, Row row) {
        final int BAND_NUMBER_COLUMN_NUMBER = 1;
        Cell bandNumberCell = row.getCell(BAND_NUMBER_COLUMN_NUMBER);
        String bandNumberString = tryGetCellValueString(bandNumberCell).strip().substring(3);
        for (int hour = 1; hour <= 25; hour++) {
            if (!SELF_SCHEDULE_LABELS.contains(bandNumberString)) {
                Cell volumeCell = row.getCell(hour * 2);
                if (Objects.nonNull(volumeCell) && volumeCell.getCellTypeEnum().equals(CellType.NUMERIC)) {
                    BigDecimal volume = BigDecimal.valueOf(volumeCell.getNumericCellValue() * 1000);
                    String hourString = Optional.of(hour)
                        .filter(val -> val != 25)
                        .map(String::valueOf)
                        .orElse("2a");
                    AuctionOfferBandDataEntity band = AuctionOfferBandDataEntity.builder()
                        .acceptedVolume(volume)
                        .hourNumber(hourString)
                        .bandNumber(bandNumberString)
                        .build();
                    bands.put(Pair.of(bandNumberString, hourString), band);
                }
            }
        }
    }

    /**
     * Zwraca moc derów zawartą w plikach wyjściowych dezagregacji
     * @param outputFiles
     */
    public static Map<Pair<String, Integer>, BigDecimal> readOutputFilesUnitPowerByName(List<FileDTO> outputFiles) throws IOException {
        final int DER_NAME_COLUMN_NUMBER = 0;
        final int POWER_COLUMN_NUMBER = 3;
        Map<Pair<String, Integer>, BigDecimal> powerByDerName = new HashMap<>();
        for(FileDTO outputFile : outputFiles) {
            Workbook workbook = XlsxUtil.getWorkbook(outputFile);
            Sheet sheet = workbook.getSheetAt(0);
            Integer timestamp = Integer.parseInt(extractTimestampFromFileDTO(outputFile));
            for (int rowNum = 1; rowNum <= sheet.getLastRowNum(); rowNum++) {
                Row row = sheet.getRow(rowNum);
                Cell derNameCell = row.getCell(DER_NAME_COLUMN_NUMBER);
                String derName = tryGetCellValueString(derNameCell);
                Cell powerCell = row.getCell(POWER_COLUMN_NUMBER);
                if(powerCell.getCellTypeEnum().equals(CellType.NUMERIC)) {
                    BigDecimal power = BigDecimal.valueOf(powerCell.getNumericCellValue() * 1000);
                    powerByDerName.put(Pair.of(derName, timestamp), power);
                }
            }
        }
        return powerByDerName;
    }

    /**
     * Zwraca wartość tekstową zawartą w podanej komórce arkusza lub wyrzuca IllegalStateException
     */
    private static String tryGetCellValueString(Cell stringCell) {
        if (!stringCell.getCellTypeEnum().equals(CellType.STRING)) {
            throw new IllegalStateException("Cannot import because band label is not a string");
        }
        return stringCell.getStringCellValue();
    }
}
