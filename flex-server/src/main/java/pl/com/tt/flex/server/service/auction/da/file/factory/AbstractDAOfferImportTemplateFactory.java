package pl.com.tt.flex.server.service.auction.da.file.factory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.apache.poi.ss.usermodel.CellCopyPolicy;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFFormulaEvaluator;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ClassPathResource;

import lombok.extern.slf4j.Slf4j;
import pl.com.tt.flex.model.service.dto.auction.da.AuctionDayAheadDTO;
import pl.com.tt.flex.model.service.dto.file.FileDTO;
import pl.com.tt.flex.server.service.schedulingUnit.dto.SchedulingUnitDTO;

@Slf4j
public abstract class AbstractDAOfferImportTemplateFactory {

    private static final String FORMULA_SEPARATOR = ",";
    private static final String VOLUME_FORMULA_PATTERN = "IF(SUM(%s)=0" + FORMULA_SEPARATOR + "\"\"" + FORMULA_SEPARATOR + "SUM(%s))";
    private static final String PRICE_FORMULA_PATTERN = "IF(%s=\"\"" + FORMULA_SEPARATOR + "\"\"" + FORMULA_SEPARATOR + "%s)";

    protected static XSSFWorkbook getWorkbook(String pathVariable, String fileNameVariable, String userLang) throws IOException {
        final var PATH_PATTERN = "templates/xlsx/day_ahead_offer/%s/import_%s_offer_%s.xlsx";
        var path = String.format(PATH_PATTERN, pathVariable, fileNameVariable, userLang);
        InputStream templateStream = new ClassPathResource(path).getInputStream();
        return new XSSFWorkbook(templateStream);
    }

    protected static FileDTO writeFileAndGetDTO(XSSFWorkbook workbook, String filename) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();
        return new FileDTO(filename, outputStream.toByteArray());
    }

    protected static String getTemplateFilename(AuctionDayAheadDTO dbAuction, SchedulingUnitDTO dbSchedulingUnit, String userLang) {
        String prefix;
        if (userLang.equals("pl")) {
            prefix = "oferta_";
        } else {
            prefix = "bid_";
        }
        return prefix + dbAuction.getName() + "_" + dbSchedulingUnit.getName() + ".xlsx";
    }

    protected static void copySection(int sectionHeight, int firstSectionStartRow, CellAddress previousSectionStartCell, XSSFSheet sheet, CellAddress currentSectionStartCell) {
        if (currentSectionStartCell.getRow() > firstSectionStartRow) {
            sheet.copyRows(previousSectionStartCell.getRow(), previousSectionStartCell.getRow() + sectionHeight, currentSectionStartCell.getRow(), new CellCopyPolicy());
        }
    }

    protected static void addDersToTemplate(SchedulingUnitDTO schedulingUnit, Map<Long, Map<String, BigDecimal>> derSelfSchedules, XSSFWorkbook workbook, int derSectionHeight, int firstDerStartRow, int selfScheduleRowOffset) {
        var previousSectionStartCell = new CellAddress(1, 1);
        var sheet = workbook.getSheetAt(0);
        for (var der : schedulingUnit.getUnits()) {
            log.debug("addDersToTemplate() adding der {} to template file", der.getName());
            var currentSectionStartCell = new CellAddress(previousSectionStartCell.getRow() + derSectionHeight, previousSectionStartCell.getColumn());
            copySection(derSectionHeight, firstDerStartRow, previousSectionStartCell, sheet, currentSectionStartCell);
            var selfSchedule = derSelfSchedules.get(der.getId());
            var selfScheduleRow = sheet.getRow(currentSectionStartCell.getRow() + selfScheduleRowOffset);
            for (var hour = 1; hour <= 25; hour++) {
                var key = Integer.toString(hour);
                if (hour == 25) {
                    key = "2a";
                }
                Double selfScheduleValue = null;
                if(Objects.nonNull(selfSchedule)){
                    selfScheduleValue = Optional.ofNullable(selfSchedule.get(key))
                        .map(BigDecimal::doubleValue)
                        .orElse(null);
                }
                XSSFCell selfScheduleCell = selfScheduleRow.getCell(currentSectionStartCell.getColumn() + hour * 2);
                if (Objects.isNull(selfScheduleValue)) {
                    selfScheduleCell.setCellType(CellType.BLANK);
                } else {
                    selfScheduleCell.setCellValue(selfScheduleValue);
                }
            }
            sheet.getRow(currentSectionStartCell.getRow()).getCell(currentSectionStartCell.getColumn()).setCellValue(der.getName());
            previousSectionStartCell = currentSectionStartCell;
        }
    }

    protected static void setFormulas(XSSFWorkbook workbook, int numberOfDersToSum, int numberOfBands, int derSectionHeight) {
        var sheet = workbook.getSheetAt(0);
        for (var band = 1; band <= numberOfBands; band++) {
            var schedulingUnitRow = sheet.getRow(band + 1);
            for (var hour = 0; hour < 24; hour++) {
                var formulaVolumeCol = 3 + hour * 2;
                var formulaPriceCol = formulaVolumeCol + 1;
                setPriceFormula(numberOfDersToSum, derSectionHeight, sheet, band, formulaPriceCol);
                String cellsForVolumeFormula = getCellsForVolumeFormula(numberOfDersToSum, derSectionHeight, band, formulaVolumeCol);
                fillSchedulingUnitSection(schedulingUnitRow, formulaVolumeCol, formulaPriceCol, cellsForVolumeFormula);
            }
        }
        XSSFFormulaEvaluator.evaluateAllFormulaCells(workbook);
    }

    private static String getCellsForVolumeFormula(int numberOfDersToSum, int derSectionHeight, int band, int formulaVolumeCol) {
        StringBuilder cellsForVolumeFormula = new StringBuilder();
        for (var der = 1; der <= numberOfDersToSum; der++) {
            var formulaRowNum = 2 + band + der * derSectionHeight;
            cellsForVolumeFormula.append(CellReference.convertNumToColString(formulaVolumeCol)).append(formulaRowNum);
            if (der < numberOfDersToSum) {
                cellsForVolumeFormula.append(FORMULA_SEPARATOR);
            }
        }
        return cellsForVolumeFormula.toString();
    }

    private static void setPriceFormula(int numberOfDersToSum, int derSectionHeight, XSSFSheet sheet, int band, int formulaPriceCol) {
        String cellForPriceFormula = CellReference.convertNumToColString(formulaPriceCol).concat(String.valueOf(2 + band));
        var priceFormula = String.format(PRICE_FORMULA_PATTERN, cellForPriceFormula, cellForPriceFormula);
        for (var der = 1; der <= numberOfDersToSum; der++) {
            var formulaRowNum = 2 + band + der * derSectionHeight;
            sheet.getRow(formulaRowNum - 1).getCell(formulaPriceCol).setCellFormula(priceFormula);
        }
    }

    private static void fillSchedulingUnitSection(XSSFRow schedulingUnitRow, int formulaVolumeCol, int formulaPriceCol, String cellsForVolumeFormula) {
        var volumeFormula = String.format(VOLUME_FORMULA_PATTERN, cellsForVolumeFormula, cellsForVolumeFormula);
        schedulingUnitRow.getCell(formulaVolumeCol).setCellFormula(volumeFormula);
        schedulingUnitRow.getCell(formulaPriceCol).setCellType(CellType.BLANK);
    }

}
