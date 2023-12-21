package pl.com.tt.flex.server.service.kpi.generator.number.avoided.technical.restrictions.voltageviolations;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import pl.com.tt.flex.model.service.dto.file.FileDTO;
import pl.com.tt.flex.model.service.dto.kpi.KpiDTO;
import pl.com.tt.flex.model.service.dto.kpi.KpiType;
import pl.com.tt.flex.server.service.kpi.generator.KpiGenerateException;
import pl.com.tt.flex.server.service.kpi.generator.number.avoided.technical.restrictions.AvoidedTechnicalRestrictionsGenerator;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static pl.com.tt.flex.server.util.WorkbookUtils.verifyThatTwoWorkbookAreSame;

class AvoidedTechRestrictionsVolViolationsGeneratorTest {

    private final AvoidedTechnicalRestrictionsGenerator avoidedTechnicalRestrictionsGenerator;

    AvoidedTechRestrictionsVolViolationsGeneratorTest() {
        this.avoidedTechnicalRestrictionsGenerator = new AvoidedTechnicalRestrictionsGenerator();
    }

    @Test
    void givenSimpleData_expectedGenerateFile() throws KpiGenerateException, IOException {
        //given
        KpiDTO kpiDTO = KpiDTO.builder()
                              .type(KpiType.NUMBER_AVOIDED_TECHNICAL_RESTRICTIONS)
                              .id(0L)
                              .build();

        //when
        FileDTO generate = avoidedTechnicalRestrictionsGenerator.generate(kpiDTO);

        //then
        XSSFWorkbook expectedWorkbook = getExpectedWorkbook(new ClassPathResource("/templates/kpi/number/avoided/technical/restrictions/NumberAvoidedTechRestrictions.xlsx"));
        verifyThatTwoWorkbookAreSame(expectedWorkbook, new XSSFWorkbook(new ByteArrayInputStream(generate.getBytesData())));
    }

    @NotNull
    private static XSSFWorkbook getExpectedWorkbook(Resource resource) throws IOException {
        InputStream expectedFile = resource.getInputStream();
        return new XSSFWorkbook(new ByteArrayInputStream(expectedFile.readAllBytes()));
    }
}
