package pl.com.tt.flex.server.service.algorithm.agnoAlgorithm.eneregy;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.Range;
import org.apache.poi.ss.usermodel.CellCopyPolicy;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import pl.com.tt.flex.model.service.dto.file.FileDTO;
import pl.com.tt.flex.server.domain.algorithm.AlgorithmEvaluationEntity;
import pl.com.tt.flex.server.domain.auction.offer.da.AuctionOfferBandDataEntity;
import pl.com.tt.flex.server.service.user.UserService;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.Map.Entry;

import static pl.com.tt.flex.server.service.common.XlsxUtil.*;

@Service
@Slf4j
public class AgnoResultsFileGeneratorImpl implements AgnoResultsFileGenerator {

    private static final String TEMPLATE_FILE_PATH_FORMAT = "templates/xlsx/agno_file_template/agno_results_template_%s.xlsx";
    private static final String FILENAME_FORMAT = "agno_results_%s.xlsx";
    private static final String OFFER_NAME_SUFFIX_EN = " - AGNO results";
    private static final String OFFER_NAME_SUFFIX_PL = " - AGNO wyniki";
    private static final int SECTION_HEIGHT = 23;

    private final UserService userService;
    private final AgnoBmResultsFileReader agnoBmResultsFileReader;

    public AgnoResultsFileGeneratorImpl(final UserService userService, final AgnoBmResultsFileReader agnoBmResultsFileReader) {
        this.userService = userService;
        this.agnoBmResultsFileReader = agnoBmResultsFileReader;
    }

    @Override
    public FileDTO getResultsFile(AlgorithmEvaluationEntity algEvaluation) throws IOException {
        log.debug("getResultsFile() AGNO result file generation - START");
        String userLanguage = userService.getLangKeyForCurrentLoggedUser();
        log.debug("getResultsFile() detected user language: {}", userLanguage);
        String filePath = String.format(TEMPLATE_FILE_PATH_FORMAT, userLanguage);
        XSSFWorkbook template = getWorkbook(filePath);
        fillTemplate(template, algEvaluation, userLanguage);
        log.debug("getResultsFile() AGNO result file generation - END");
        return buildFileDTO(template, algEvaluation.getDeliveryDate());
    }

    /**
     * Zwraca oznaczenie godziny której dotyczy plik wyjściowy agno
     */
    public static String extractTimestampFromFileDTO(FileDTO agnoOutputFile) {
        String filename = agnoOutputFile.getFileName();
        int nameLength = filename.length();
        int firstTimestampCharFromEnd;
        if (isTimestampDoubleDigit(filename)) {
            firstTimestampCharFromEnd = 7;
        } else {
            firstTimestampCharFromEnd = 6;
        }
        return filename.substring(nameLength - firstTimestampCharFromEnd, nameLength - 5);
    }

    /**
     * Wypełnia szablon danymi ofert z podanych plików wyjściowych agno
     */
    private void fillTemplate(XSSFWorkbook template, AlgorithmEvaluationEntity algEvaluation, String userLanguage) throws IOException {
        Map<List<AuctionOfferBandDataEntity>, Long> agnoDbOfferPairs = agnoBmResultsFileReader.getBandsByOfferId(algEvaluation);
        XSSFSheet sheet = template.getSheetAt(0);
        int sectionStartRow = 0;
        for (Entry<List<AuctionOfferBandDataEntity>, Long> pair : agnoDbOfferPairs.entrySet()) {
            List<AuctionOfferBandDataEntity> agnoBands = pair.getKey();
            copySection(sectionStartRow, sheet);
            String offerName = buildOfferName(pair.getValue(), userLanguage);
            setOfferBands(sheet, agnoBands, sectionStartRow, offerName);
            sectionStartRow += SECTION_HEIGHT;
        }
        removeEmptyBands(sheet, agnoDbOfferPairs.size());
        mergeOfferNameCol(sheet);
        freezeColumns(sheet, Arrays.asList(1, 2));
    }

    /**
     * Wycina z arkusza puste wiersze pasm
     */
    private void removeEmptyBands(XSSFSheet sheet, int offerCount) {
        final int BAND_LABEL_COL = 1;
        final Set<String> BAND_LABELS = Set.of("SU", "JG");
        for (int rowNum = 2; rowNum < SECTION_HEIGHT * offerCount; rowNum++) {
            XSSFRow row = sheet.getRow(rowNum);
            if (Objects.isNull(row)) {
                break;
            }
            String bandLabel = "";
            XSSFCell bandLabelCell = row.getCell(BAND_LABEL_COL);
            if (bandLabelCell.getCellTypeEnum() == CellType.STRING) {
                bandLabel = bandLabelCell.getStringCellValue().substring(0, 2);
            }
            if (BAND_LABELS.contains(bandLabel) && isBandRowEmpty(row)) {
                int lastRowNum = sheet.getLastRowNum();
                if (row.getRowNum() < lastRowNum) {
                    sheet.shiftRows(row.getRowNum() + 1, lastRowNum, -1);
                    rowNum--;
                } else {
                    sheet.removeRow(row);
                }
            }
        }
    }

    /**
     * Scala komórki pierwszej kolumny arkusza
     */
    private void mergeOfferNameCol(XSSFSheet sheet) {
        List<Range<Integer>> regionsToMerge = new ArrayList<>();
        int mergeRegionStartRow = 0;
        for (int row = 1; row <= sheet.getLastRowNum(); row++) {
            XSSFCell cell = sheet.getRow(row).getCell(0);
            if (cell.getCellTypeEnum() == CellType.STRING && !cell.getStringCellValue().isBlank()) {
                regionsToMerge.add(Range.between(mergeRegionStartRow, row - 1));
                mergeRegionStartRow = row;
            }
        }
        regionsToMerge.add(Range.between(mergeRegionStartRow, sheet.getLastRowNum()));
        for (Range<Integer> range : regionsToMerge) {
            sheet.addMergedRegion(new CellRangeAddress(range.getMinimum(), range.getMaximum(), 0, 0));
        }
    }

    /**
     * Zwraca true jeśli wiersz z pasmem jest pusty
     */
    private boolean isBandRowEmpty(XSSFRow row) {
        for (int colNum = 2; colNum <= 48; colNum += 2) {
            double volume = 0;
            XSSFCell volumeCell = row.getCell(colNum);
            if (volumeCell.getCellTypeEnum() == CellType.NUMERIC) {
                volume = row.getCell(colNum).getNumericCellValue();
            }
            if (volume != 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * Zwraca nazwę oferty wstawianą do pierwszej kolumny pliku wyjściowego
     */
    private String buildOfferName(Long id, String userLanguage) {
        String suffix;
        if (userLanguage.equals("pl")) {
            suffix = OFFER_NAME_SUFFIX_PL;
        } else {
            suffix = OFFER_NAME_SUFFIX_EN;
        }
        return id.toString().concat(suffix);
    }

    /**
     * Wypełnia wybraną sekcję szablonu podanymi pasmami
     */
    private void setOfferBands(XSSFSheet sheet, List<AuctionOfferBandDataEntity> bands, int sectionStartRow, String offerId) {
        log.debug("setOfferBands() adding offer {} to template file", offerId);
        sheet.getRow(sectionStartRow).getCell(0).setCellValue(offerId);
        for (AuctionOfferBandDataEntity band : bands) {
            int rowOffset = 12 - Integer.parseInt(band.getBandNumber());
            XSSFRow row = sheet.getRow(sectionStartRow + rowOffset);
            int volumeColOffset = Integer.parseInt(band.getHourNumber()) * 2;
            XSSFCell volumeCell = row.getCell(volumeColOffset);
            int priceColOffset = volumeColOffset + 1;
            XSSFCell priceCell = row.getCell(priceColOffset);
            setCellValueIfPresent(volumeCell, band.getAcceptedVolume());
            setCellValueIfPresent(priceCell, band.getAcceptedPrice());
        }
    }

    /**
     * Zwraca FileDTO z plikiem szablonu o odpowiedniej nazwie
     */
    private FileDTO buildFileDTO(XSSFWorkbook workbook, Instant deliveryDate) throws IOException {
        byte[] outputByteArray = writeFileAndGetOutputStream(workbook).toByteArray();
        LocalDateTime deliveryLocalDate = LocalDateTime.ofInstant(deliveryDate, ZoneId.of("Europe/Paris"));
        String deliveryDateString = deliveryLocalDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String filename = String.format(FILENAME_FORMAT, deliveryDateString);
        return new FileDTO(filename, outputByteArray);
    }

    /**
     * Zwraca true jeśli oznaczenie godziny w nazwie pliku wyjściowego agno jest dwuznakowe
     */
    static boolean isTimestampDoubleDigit(String agnoOutputFilename) {
        return agnoOutputFilename.charAt(agnoOutputFilename.length() - 7) != '_';
    }

    /**
     * Modyfikuje podany formularz dodając kolejną sekcję na koniec arkusza
     */
    private void copySection(int sectionStartRow, XSSFSheet sheet) {
        if (sectionStartRow > 0) {
            sheet.copyRows(sectionStartRow - SECTION_HEIGHT, sectionStartRow - 1, sectionStartRow, new CellCopyPolicy());
        }
    }

    /**
     * Blokada kolumn
     */
    public void freezeColumns(XSSFSheet sheet, List<Integer> columnNumbers) {
        columnNumbers.forEach(colNr -> sheet.createFreezePane(colNr, 0));
    }

}
