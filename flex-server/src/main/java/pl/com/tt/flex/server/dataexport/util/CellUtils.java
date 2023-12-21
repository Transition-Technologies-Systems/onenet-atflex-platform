package pl.com.tt.flex.server.dataexport.util;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import pl.com.tt.flex.server.util.DateUtil;

import java.math.BigDecimal;
import java.text.ParseException;
import java.time.Instant;
import java.util.Date;
import java.util.Objects;
import java.util.regex.Pattern;

public final class CellUtils {

    public static void setCellStyleInRange(XSSFSheet sheet, CellStyle cellStyle, CellRangeAddress region) {
        for (int r = region.getFirstRow(); r < region.getLastRow(); r++) {
            XSSFRow row = getRow(sheet, r);
            for (int c = region.getFirstColumn(); c <= region.getLastColumn(); c++) {
                XSSFCell cell = getCell(sheet, row, c);
                if (Objects.isNull(cell)) {
                    cell = row.createCell(c);
                }
                cell.setCellStyle(cellStyle);
            }
        }
    }

    /**
     * Metoda uzupelnia wartosc dla komorki i scalanie zadanego obszaru
     */
    public static void fillCellAndMergeRegion(XSSFSheet sheet, int startRow, int endRow, int startCell, int endCell, String cellValue) {
        addStringCellValueIfExist(sheet, startRow, startCell, cellValue);
        mergeRange(sheet, startRow, endRow, startCell, endCell);
    }

    public static void setCellWidth(XSSFSheet sheet, int cell, int cellWidth) {
        sheet.setColumnWidth(cell, cellWidth);
    }

    /**
     * Scalenie komorek według zadanego obszaru
     */
    public static void mergeRange(XSSFSheet sheet, int firstRow, int lastRow, int firstCell, int lastCell) {
        CellRangeAddress region = new CellRangeAddress(firstRow, lastRow, firstCell, lastCell);
        sheet.addMergedRegion(region);
    }

    public static void addStringCellValueIfExist(XSSFSheet sheet, int row, int cell, String value) {
        if (Objects.nonNull(value)) {
            XSSFCell xssfCell = getRow(sheet, row).createCell(cell);
            xssfCell.setCellType(CellType.STRING);
            xssfCell.setCellValue(value);
        }
    }

    public static XSSFCell addNumberCellValueIfExist(XSSFSheet sheet, int row, int cell, Double value) {
        XSSFCell xssfCell = getRow(sheet, row).createCell(cell);
        if (Objects.nonNull(value)) {
            xssfCell.setCellValue(value);
        }
        return xssfCell;
    }

    public static Double getDoubleFromBigDecimal(BigDecimal value) {
        if (Objects.nonNull(value)) {
            return value.doubleValue();
        } else {
            return null;
        }
    }

    public static XSSFRow getRow(XSSFSheet sheet, int row) {
        if (Objects.isNull(sheet.getRow(row))) {
            return sheet.createRow(row);
        } else {
            return sheet.getRow(row);
        }
    }

    public static XSSFCell getCell(XSSFSheet sheet, int row, int cell) {
        XSSFRow xssfRow = getRow(sheet, row);
        XSSFCell xssfCell = xssfRow.getCell(cell);
        if (Objects.isNull(xssfCell)) {
            return xssfRow.createCell(cell);
        } else {
            return xssfCell;
        }
    }

    public static XSSFCell getCell(XSSFSheet sheet, XSSFRow row, int i) {
        XSSFRow xssfRow = Objects.isNull(row) ? sheet.createRow(i) : row;
        return xssfRow.getCell(i);
    }

    public static boolean checkIfExistCellWithPatternInRow(XSSFRow row, int cellNumber, CellType cellType, Pattern pattern) {
        return row.getCell(cellNumber).getCellTypeEnum().equals(cellType)
            && pattern.matcher(row.getCell(cellNumber).getStringCellValue()).matches();
    }

    public static Instant getDateFromCell(Cell dateCell) throws ParseException {
        Date date = DateUtil.getDateFromString(dateCell.getStringCellValue());
        return date.toInstant();
    }

    public static CellStyle getPercentageCellStyle(XSSFSheet sheet) {
        XSSFWorkbook workbook = sheet.getWorkbook();
        CellStyle style = workbook.createCellStyle();
        style.setDataFormat(workbook.createDataFormat().getFormat("0.00%")); // Format procentowy z dwoma miejscami po przecinku
        return style;
    }
}
