package pl.com.tt.flex.server.service.algorithm.danoAlgorithm;

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
public class DanoFileGeneratorImpl extends FileGeneratorAbstract implements DanoFileGenerator {
    private static final String TEMPLATE_FILE_PATH = "templates/xlsx/agno_file_template/agno_dano_template.xlsx";
    private static final String FILENAME_FORMAT = "input_dano_%s_%s_%s"; // input_dano_{coupling_point}_{delivery_date}_{hour_nr}

    @Override
    public FileDTO getDanoFile(AgnoCouplingPointDTO couplingPoint, AgnoHourNumberDTO hourNumber, LocalDate deliveryDate) throws IOException {
        log.debug("getDanoFile() START - generate DANO file: couplingPointId: {}, deliveryDate{}, hourNumber: {}",
            couplingPoint.getCouplingPointId().getName(), deliveryDate, hourNumber.getHourNumber());
        XSSFWorkbook workbook = getWorkbook(TEMPLATE_FILE_PATH);
        fillOffersSheet(hourNumber, workbook);
        fillUnitsSheet(hourNumber, workbook);
        FileDTO danoFile = getFileDtoFromWorkbook(couplingPoint, hourNumber, deliveryDate, workbook);
        log.debug("getDanoFile() END - generate DANO file: couplingPointId: {}, deliveryDate{}, hourNumber: {}. RESULT FILENAME: {}",
            couplingPoint.getCouplingPointId().getName(), deliveryDate, hourNumber.getHourNumber(), danoFile.getFileName());
        return danoFile;
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
