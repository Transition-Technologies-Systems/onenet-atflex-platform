package pl.com.tt.flex.server.service.common;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Optional;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import lombok.extern.slf4j.Slf4j;
import pl.com.tt.flex.model.service.dto.file.FileDTO;

@Slf4j
public class XlsxUtil {

    /**
     * Zwraca dane arkusza w formie strumienia bajtów gotowego do zwrócenia z endpointa
     */
    public static ByteArrayOutputStream writeFileAndGetOutputStream(XSSFWorkbook workbook) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();
        return outputStream;
    }

    /**
     * Zwraca arkusz szablonu z podanej ścieżki
     */
    public static XSSFWorkbook getWorkbook(String templateFilePath) throws IOException {
        Resource templateFileResource = new ClassPathResource(templateFilePath);
        InputStream fileWithHeaders = templateFileResource.getInputStream();
        return new XSSFWorkbook(fileWithHeaders);
    }

    /**
     * Zwraca arkusz szablonu zawarty w podanym FileDTO
     */
    public static XSSFWorkbook getWorkbook(FileDTO fileDTO) throws IOException {
        ByteArrayInputStream stream = new ByteArrayInputStream(fileDTO.getBytesData());
        return new XSSFWorkbook(stream);
    }

    /**
     * Ustawia wartość komórki jeśli value != null
     */
    public static void setCellValueIfPresent(XSSFCell cell, BigDecimal value) {
        Optional.ofNullable(value)
            .map(BigDecimal::doubleValue)
            .ifPresent(cell::setCellValue);
    }

    /**
     * Zwraca zawartość komórki w formacie tekstowym,
     * niezależnie czy komórka jest typu tekstowego czy numerycznego
     */
    public static String getCellStringValue(Cell cell) {
        if (isNullableCellOfType(cell, CellType.STRING)) {
            return cell.getStringCellValue();
        }
        if (isNullableCellOfType(cell, CellType.NUMERIC)) {
            return Double.toString(cell.getNumericCellValue());
        }
        return null;
    }

    /**
     * Zwraca zawartość komórki jako BigDecimal,
     * obsługuje zapis z przecinkiem i z kropką, oraz z jednostką po spacji lub bez
     */
    public static BigDecimal getBigDecimalValueOrNullFromCellWithUnit(Cell cell) {
        return Optional.ofNullable(cell)
            .map(XlsxUtil::getCellStringValue)
            .filter(string -> !string.isBlank())
            .map(stringMaybeWithUnit -> stringMaybeWithUnit.split(" ")[0])
            .map(maybeCommaSeparatedValue -> maybeCommaSeparatedValue.replace(",", "."))
            .map(BigDecimal::new)
            .orElse(null);
    }

    private static boolean isNullableCellOfType(Cell cell, CellType type) {
        return Optional.ofNullable(cell).map(Cell::getCellTypeEnum).map(t -> t.equals(type)).orElse(false);
    }

}
