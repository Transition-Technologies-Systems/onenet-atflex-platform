package pl.com.tt.flex.server.dataimport.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import pl.com.tt.flex.model.service.dto.file.FileDTO;
import pl.com.tt.flex.model.service.dto.product.type.ProductBidSizeUnit;
import pl.com.tt.flex.server.dataimport.DataImport;
import pl.com.tt.flex.server.dataimport.ImportDataException;
import pl.com.tt.flex.server.dataimport.factory.DataImportFormat;
import pl.com.tt.flex.server.domain.potential.FlexPotentialEntity;
import pl.com.tt.flex.server.service.potential.dto.FlexPotentialImportDTO;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.*;

import static pl.com.tt.flex.server.web.rest.errors.ErrorConstants.CANNOT_IMPORT_FP_BECAUSE_WRONG_HEADERS;

@Component
@Slf4j
public class FlexPotentialImport implements DataImport<FlexPotentialImportDTO> {

    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT).withZone(ZoneId.systemDefault());
    private static final String TEMPLATE_FILENAME_PL = "templates/xlsx/export_flex_potentials_pl.xlsx";
    private static final String TEMPLATE_FILENAME_EN = "templates/xlsx/export_flex_potentials_en.xlsx";
    private static final int HEADER_INDEX = 0;

    @Override
    public List<FlexPotentialImportDTO> doImport(MultipartFile multipartFile, Locale locale) throws IOException, ImportDataException {
        Map<Integer, List<XSSFCell>> stringListMap = readFile(multipartFile.getInputStream(), locale);
        List<FlexPotentialImportDTO> importedFlexPotential = new ArrayList<>();
        for (Map.Entry<Integer, List<XSSFCell>> sheet : stringListMap.entrySet()) {
            importedFlexPotential.add(parseToDTO(sheet.getValue(), locale));
        }
        log.info("doImport() Find {} FlexPotential to import in file {}.", importedFlexPotential.size(), multipartFile.getOriginalFilename());
        return importedFlexPotential;
    }

    @Override
    public List<FlexPotentialImportDTO> doImport(FileDTO fileDTO, Locale locale) {
        throw new NotImplementedException("This method is currently not used");
    }

    @Override
    public boolean supportClass(Class clazz) {
        return clazz.equals(FlexPotentialEntity.class);
    }

    @Override
    public boolean supportFormat(DataImportFormat format) {
        return format.equals(DataImportFormat.XLSX);
    }

    private Map<Integer, List<XSSFCell>> readFile(InputStream inputStream, Locale locale) throws IOException, ImportDataException {
        XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
        XSSFSheet sheet = workbook.getSheetAt(0);
        Map<Integer, List<XSSFCell>> sheetData = new HashMap<>();
        Iterator rowIterator = sheet.rowIterator();
        if (rowIterator.hasNext()) {
            XSSFRow headers = (XSSFRow) rowIterator.next();
            checkHeadersInImportFile(locale, getCellsFromRaw(headers));
            while (rowIterator.hasNext()) {
                XSSFRow row = (XSSFRow) rowIterator.next();
                ArrayList<XSSFCell> rows = getCellsFromRaw(row);
                if (!rows.isEmpty()) {
                    sheetData.put(row.getRowNum(), rows);
                }
            }
        }
        return sheetData;
    }

    private ArrayList<XSSFCell> getCellsFromRaw(XSSFRow row) {
        ArrayList<XSSFCell> rows = new ArrayList<>();
        Iterator cells = row.cellIterator();
        while (cells.hasNext()) {
            XSSFCell cell = (XSSFCell) cells.next();
            cell.setCellType(XSSFCell.CELL_TYPE_STRING);
            rows.add(cell);
        }
        return rows;
    }

    private void checkHeadersInImportFile(Locale locale, List<XSSFCell> headers) throws IOException, ImportDataException {
        List<XSSFCell> correctHeaders = loadCorrectHeaders(locale);
        log.info("validHeaderInImportedFile() -- BEGIN");
        for (int i = 0; i < correctHeaders.size(); i++) {
            String correctHeader = correctHeaders.get(i).toString();
            String headerToValid = headers.get(i).toString();
            if (!correctHeader.equals(headerToValid)) {
                throw new ImportDataException(CANNOT_IMPORT_FP_BECAUSE_WRONG_HEADERS);
            }
        }
        log.info("validHeaderInImportedFile() -- END");
    }

    private List<XSSFCell> loadCorrectHeaders(Locale locale) throws IOException {
        String template = locale.getLanguage().equals("en") ? TEMPLATE_FILENAME_EN : TEMPLATE_FILENAME_PL;
        Resource templateFileResource = new ClassPathResource(template);
        InputStream fileWithHeaders = templateFileResource.getInputStream();
        XSSFWorkbook workbook = new XSSFWorkbook(fileWithHeaders);
        XSSFSheet sheet = workbook.getSheetAt(0);
        XSSFRow row = sheet.getRow(HEADER_INDEX);
        return getCellsFromRaw(row);
    }

    private FlexPotentialImportDTO parseToDTO(List<XSSFCell> data, Locale locale) {
        log.info("parseToDTO() -- BEGIN");
        FlexPotentialImportDTO flexPotentialImportDTO = new FlexPotentialImportDTO();
        flexPotentialImportDTO.setId(Long.parseLong(data.get(0).toString()));
        flexPotentialImportDTO.setProductShortName(data.get(1).toString());
        flexPotentialImportDTO.setFspCompanyName(data.get(2).toString());
        flexPotentialImportDTO.setUnitCode(data.get(3).toString());
        flexPotentialImportDTO.setVolume(BigDecimal.valueOf(Double.parseDouble(data.get(4).toString())));
        flexPotentialImportDTO.setVolumeUnit(ProductBidSizeUnit.valueOf(data.get(5).toString()));
        flexPotentialImportDTO.setValidFrom(LocalDateTime.parse(data.get(10).toString(), dateTimeFormatter).toInstant(ZoneOffset.UTC));
        if (!data.get(11).toString().isBlank()) {
            flexPotentialImportDTO.setValidTo(LocalDateTime.parse(data.get(11).toString(), dateTimeFormatter).toInstant(ZoneOffset.UTC));
        }
        flexPotentialImportDTO.setActivated(getBoolean(data.get(12).toString(), locale));
        flexPotentialImportDTO.setProductPrequalification(getBoolean(data.get(14).toString(), locale));
        flexPotentialImportDTO.setStaticGridPrequalification(getBoolean(data.get(16).toString(), locale));
        log.info("parseToDTO() - Imported Flex Potential data: \n {} \n", flexPotentialImportDTO);
        log.info("parseToDTO() -- END");
        return flexPotentialImportDTO;
    }

    private boolean getBoolean(String value, Locale locale) {
        boolean isEnglishLanguage = locale.getLanguage().equals("en");
        if (isEnglishLanguage) {
            return value.equalsIgnoreCase("Yes");
        } else {
            return value.equalsIgnoreCase("Tak");
        }
    }
}
