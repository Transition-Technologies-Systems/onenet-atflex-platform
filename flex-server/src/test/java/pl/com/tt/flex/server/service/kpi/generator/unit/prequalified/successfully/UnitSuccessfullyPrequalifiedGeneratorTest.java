package pl.com.tt.flex.server.service.kpi.generator.unit.prequalified.successfully;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import pl.com.tt.flex.model.service.dto.file.FileDTO;
import pl.com.tt.flex.model.service.dto.kpi.KpiDTO;
import pl.com.tt.flex.model.service.dto.kpi.KpiType;
import pl.com.tt.flex.server.service.kpi.generator.KpiGenerateException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;

import static pl.com.tt.flex.server.util.WorkbookUtils.verifyThatTwoWorkbookAreSame;

class UnitSuccessfullyPrequalifiedGeneratorTest {

    private final UnitSuccessfullyPrequalifiedDataFactory unitSuccessfullyPrequalifiedDataFactory;
    private final UnitSuccessfullyPrequalifiedGenerator unitSuccessfullyPrequalifiedGenerator;

    UnitSuccessfullyPrequalifiedGeneratorTest() {
        unitSuccessfullyPrequalifiedDataFactory = Mockito.mock(UnitSuccessfullyPrequalifiedDataFactory.class);
        unitSuccessfullyPrequalifiedGenerator = new UnitSuccessfullyPrequalifiedGenerator(unitSuccessfullyPrequalifiedDataFactory);
    }

    @Test
    void givenSimpleData_expectedGenerateFile() throws KpiGenerateException, IOException {
        //before
        UnitSuccessfullyPrequalifiedData data = new UnitSuccessfullyPrequalifiedData(BigDecimal.valueOf(50), BigDecimal.valueOf(100));
        Mockito.doReturn(data).when(unitSuccessfullyPrequalifiedDataFactory).create();

        //given
        KpiDTO kpiDTO = KpiDTO.builder()
                              .type(KpiType.PERCENTAGE_OF_SUCCESSFULLY_PREQUALIFIED_DERS)
                              .id(0L)
                              .build();

        //when
        FileDTO generate = unitSuccessfullyPrequalifiedGenerator.generate(kpiDTO);

        //then
        XSSFWorkbook expectedWorkbook = getExpectedWorkbook(new ClassPathResource("/templates/kpi/unit/PercentageSuccessfullyPrequalifiedDers.xlsx"));
        verifyThatTwoWorkbookAreSame(expectedWorkbook, new XSSFWorkbook(new ByteArrayInputStream(generate.getBytesData())));
    }

    @NotNull
    private static XSSFWorkbook getExpectedWorkbook(Resource resource) throws IOException {
        InputStream expectedFile = resource.getInputStream();
        return new XSSFWorkbook(new ByteArrayInputStream(expectedFile.readAllBytes()));
    }
}
