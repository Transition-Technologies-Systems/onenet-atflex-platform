package pl.com.tt.flex.server.service.algorithm.agnoAlgorithm.eneregy;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import pl.com.tt.flex.model.service.dto.file.FileDTO;
import pl.com.tt.flex.server.service.algorithm.agnoAlgorithm.FileGeneratorAbstract;
import pl.com.tt.flex.server.service.algorithm.agnoAlgorithm.dto.AgnoCouplingPointDTO;
import pl.com.tt.flex.server.service.algorithm.agnoAlgorithm.dto.AgnoHourNumberDTO;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;

@Component
@Slf4j
public class AgnoBmFileGeneratorImpl extends FileGeneratorAbstract implements AgnoBmFileGenerator {

    private static final String TEMPLATE_FILE_PATH = "templates/xlsx/agno_file_template/agno_bm_template.xlsx";
    private static final String FILENAME_FORMAT = "input_bm_%s_%s_%s"; // input_bm_{coupling_point}_{delivery_date}_{hour_nr}

    @Override
    public FileDTO getBmFile(AgnoCouplingPointDTO couplingPoint, AgnoHourNumberDTO hourNumber, LocalDate deliveryDate) throws IOException {
        log.debug("getBmFile() START - generate PBCM file: couplingPointId: {}, deliveryDate{}, hourNumber: {}",
            couplingPoint.getCouplingPointId().getName(), deliveryDate, hourNumber.getHourNumber());
        XSSFWorkbook workbook = getWorkbook(TEMPLATE_FILE_PATH);
        fillOffersSheet(hourNumber, workbook);
        fillUnitsSheet(hourNumber, workbook);
        FileDTO pbcmFile = getFileDtoFromWorkbook(couplingPoint, hourNumber, deliveryDate, workbook);
        log.debug("getBmFile() END - generate BM file: couplingPointId: {}, deliveryDate{}, hourNumber: {}. RESULT FILENAME: {}",
            couplingPoint.getCouplingPointId().getName(), deliveryDate, hourNumber.getHourNumber(), pbcmFile.getFileName());
        return pbcmFile;
    }

    private FileDTO getFileDtoFromWorkbook(AgnoCouplingPointDTO couplingPoint, AgnoHourNumberDTO hourNumber, LocalDate deliveryDate, XSSFWorkbook workbook) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();
        String extension = ".xlsx";
        String filename = String.format(FILENAME_FORMAT, couplingPoint.getCouplingPointId().getName(), deliveryDate, hourNumber.getHourNumber()) + extension;
        return new FileDTO(filename, outputStream.toByteArray());
    }
}
