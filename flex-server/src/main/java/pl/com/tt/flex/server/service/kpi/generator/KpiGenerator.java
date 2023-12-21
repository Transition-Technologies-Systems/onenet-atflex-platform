package pl.com.tt.flex.server.service.kpi.generator;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import pl.com.tt.flex.model.service.dto.file.FileDTO;
import pl.com.tt.flex.model.service.dto.kpi.KpiDTO;
import pl.com.tt.flex.model.service.dto.kpi.KpiType;
import pl.com.tt.flex.server.common.errors.ObjectValidationException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

@Slf4j
public abstract class KpiGenerator {

    protected abstract void fillSheet(XSSFWorkbook workbook, KpiDTO kpiDTO);

    protected abstract String getTemplate();

    protected abstract String getFilename(KpiDTO kpiDTO);

    public abstract boolean isSupported(KpiType kpiType);

    public FileDTO generate(KpiDTO kpiDTO) throws KpiGenerateException {
        try {
            XSSFWorkbook workbook = readWorkbook();
            fillSheet(workbook, kpiDTO);
            return getFileDTO(kpiDTO, workbook);
        } catch (ObjectValidationException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Problem with generate KPI. Exception msg: {}", e.getMessage());
            throw new KpiGenerateException(String.format("Cannot generate KPI for kpiDTO: %s", kpiDTO));
        }
    }

    protected XSSFWorkbook readWorkbook() throws IOException {
        Resource templateFileResource = new ClassPathResource(getTemplate());
        InputStream template = templateFileResource.getInputStream();
        return new XSSFWorkbook(template);
    }

    private FileDTO getFileDTO(KpiDTO kpiDTO, XSSFWorkbook workbook) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();
        return new FileDTO(getFilename(kpiDTO), outputStream.toByteArray());
    }
}
