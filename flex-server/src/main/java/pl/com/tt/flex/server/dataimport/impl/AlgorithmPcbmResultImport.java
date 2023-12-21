package pl.com.tt.flex.server.dataimport.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import pl.com.tt.flex.model.service.dto.file.FileDTO;
import pl.com.tt.flex.server.dataimport.DataImport;
import pl.com.tt.flex.server.dataimport.factory.DataImportFormat;
import pl.com.tt.flex.server.service.algorithm.util.AlgorithmUtils;
import pl.com.tt.flex.server.service.importData.algorithm.AlgorithmPcbmImportData;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@Component
@Slf4j
public class AlgorithmPcbmResultImport implements DataImport<AlgorithmPcbmImportData> {
    private static final int DER_NAME_INDEX = 0;
    private static final int TYPE_INDEX = 1;
    private static final int PRODUCT_TYPE_INDEX = 2;
    private static final int POWER_INDEX = 3;
    private static final int PRICE_INDEX = 4;

    @Override
    public List<AlgorithmPcbmImportData> doImport(MultipartFile file, Locale locale) {
        throw new NotImplementedException("This method is currently not used");
    }

    @Override
    public List<AlgorithmPcbmImportData> doImport(FileDTO fileDTO, Locale locale) throws IOException {
        Map<Integer, List<XSSFCell>> importedDataMap = readFile(new ByteArrayInputStream(fileDTO.getBytesData()));
        List<AlgorithmPcbmImportData> importedData = new ArrayList<>();
        for (Map.Entry<Integer, List<XSSFCell>> sheet : importedDataMap.entrySet()) {
            importedData.add(parseToDTO(sheet.getValue()));
        }
        log.info("doImport() Found {} rows to import in file {}.", importedData.size(), fileDTO.getFileName());
        return importedData;
    }

    @Override
    public boolean supportClass(Class clazz) {
        return clazz.equals(AlgorithmPcbmImportData.class);
    }

    @Override
    public boolean supportFormat(DataImportFormat format) {
        return format.equals(DataImportFormat.XLSX);
    }

    private Map<Integer, List<XSSFCell>> readFile(InputStream inputStream) throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
        XSSFSheet sheet = workbook.getSheetAt(0);
        Map<Integer, List<XSSFCell>> sheetRow = new HashMap<>();
        Iterator<Row> rowIterator = sheet.rowIterator();
        if (rowIterator.hasNext()) {
            rowIterator.next(); // pomijamy pierwszy wiersz z nagłówkami
            while (rowIterator.hasNext()) {
                XSSFRow row = (XSSFRow) rowIterator.next();
                ArrayList<XSSFCell> rows = getCellsFromRow(row);
                if (!rows.isEmpty()) {
                    sheetRow.put(row.getRowNum(), rows);
                }
            }
        }
        return sheetRow;
    }

    private AlgorithmPcbmImportData parseToDTO(List<XSSFCell> data) {
        log.info("AlgorithmPcbmResultImport parseToDTO() -- BEGIN");
        // Wartości wejściowe mocy są w MWh, zatem należy je przeliczyć na KWh
        AlgorithmPcbmImportData algorithmPcbmImportData = AlgorithmPcbmImportData.builder()
            .derName(data.get(DER_NAME_INDEX).toString())
            .type(data.get(TYPE_INDEX).toString())
            .productType(data.get(PRODUCT_TYPE_INDEX).toString())
            .power(String.valueOf(AlgorithmUtils.convertPowerFromMwhToKwh(data.get(POWER_INDEX).toString())))
            .price(data.get(PRICE_INDEX).toString())
            .build();
        log.info("AlgorithmPcbmResultImport parseToDTO() -- Imported data: \n {}", algorithmPcbmImportData);
        log.info("AlgorithmPcbmResultImport parseToDTO() -- END");
        return algorithmPcbmImportData;
    }

    private ArrayList<XSSFCell> getCellsFromRow(XSSFRow row) {
        ArrayList<XSSFCell> rows = new ArrayList<>();
        Iterator<Cell> cells = row.cellIterator();
        while (cells.hasNext()) {
            XSSFCell cell = (XSSFCell) cells.next();
            cell.setCellType(CellType.STRING);
            rows.add(cell);
        }
        return rows;
    }
}
