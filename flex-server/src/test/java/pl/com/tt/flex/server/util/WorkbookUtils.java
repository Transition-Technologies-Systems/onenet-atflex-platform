package pl.com.tt.flex.server.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Assertions;

import java.util.Objects;

@Slf4j
public class WorkbookUtils {

    private WorkbookUtils() {
    }

    public static void verifyThatTwoWorkbookAreSame(XSSFWorkbook expectedWorkbook, XSSFWorkbook workbook) {
        // Since we have already verified that both work books have same number of sheets so iteration can be done against any workbook sheet count
        int sheetCounts = expectedWorkbook.getNumberOfSheets();
        // So we will iterate through sheet by sheet
        for (int i = 0; i < sheetCounts; i++) {
            // Get sheet at same index of both work books
            XSSFSheet s1 = expectedWorkbook.getSheetAt(i);
            XSSFSheet s2 = workbook.getSheetAt(i);
            log.info("Compare sheet: {}", s1.getSheetName());
            // Iterating through each row
            int rowCounts = s1.getPhysicalNumberOfRows();
            for (int j = 0; j < rowCounts; j++) {
                // Iterating through each cell
                int lastCellNum = s1.getRow(j).getLastCellNum();
                for (int k = 0; k < lastCellNum; k++) {
                    XSSFCell c1 = s1.getRow(j).getCell(k);
                    XSSFCell c2 = s2.getRow(j).getCell(k);
                    if (Objects.nonNull(c1) && c1.getCellTypeEnum().equals(c2.getCellTypeEnum())) {
                        if (c1.getCellTypeEnum() == CellType.STRING) {
                            String v1 = c1.getStringCellValue();
                            String v2 = c2.getStringCellValue();
                            Assertions.assertEquals(v1, v2, String.format("Cell not match: row: %s, col: %s", j, k));
                        }
                        if (c1.getCellTypeEnum() == CellType.NUMERIC) {
                            // If cell type is numeric, we need to check if data is of Date type
                            if (org.apache.poi.ss.usermodel.DateUtil.isCellDateFormatted(c1) | DateUtil.isCellDateFormatted(c2)) {
                                // Need to use DataFormatter to get data in given style otherwise it will come as time stamp
                                DataFormatter df = new DataFormatter();
                                String v1 = df.formatCellValue(c1);
                                String v2 = df.formatCellValue(c2);
                                Assertions.assertEquals(v1, v2, String.format("Cell not match: row: %s, col: %s", j, k));
                            } else {
                                double v1 = c1.getNumericCellValue();
                                double v2 = c2.getNumericCellValue();
                                Assertions.assertEquals(v1, v2, String.format("Cell not match: row: %s, col: %s", j, k));
                            }
                        }
                        if (c1.getCellTypeEnum() == CellType.BOOLEAN) {
                            boolean v1 = c1.getBooleanCellValue();
                            boolean v2 = c2.getBooleanCellValue();
                            Assertions.assertEquals(v1, v2, String.format("Cell not match: row: %s, col: %s", j, k));
                        }
                    }
                }
            }
        }
    }
}
