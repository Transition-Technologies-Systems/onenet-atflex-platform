package pl.com.tt.flex.server.service.kpi.generator.unit.number.fp;

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

class NumberOfDerWithFlexPotentialGeneratorTest {

    private final NumberOfDerWithFlexPotentialDataFactory numberOfDerWithFlexPotentialDataFactory;
    private final NumberOfDerWithFlexPotentialGenerator numberOfDerWithFlexPotentialGenerator;

    NumberOfDerWithFlexPotentialGeneratorTest() {
        numberOfDerWithFlexPotentialDataFactory = Mockito.mock(NumberOfDerWithFlexPotentialDataFactory.class);
        numberOfDerWithFlexPotentialGenerator = new NumberOfDerWithFlexPotentialGenerator(numberOfDerWithFlexPotentialDataFactory);
    }

    @Test
    void givenSimpleData_expectedGenerateFile() throws KpiGenerateException, IOException {
        //before
        NumberOfDerWithFlexPotentialData data = new NumberOfDerWithFlexPotentialData(BigDecimal.valueOf(20));
        Mockito.doReturn(data).when(numberOfDerWithFlexPotentialDataFactory).create();

        //given
        KpiDTO kpiDTO = KpiDTO.builder()
                              .type(KpiType.NUMBER_OF_DERS_WITH_AT_LEAST_ONE_FP)
                              .id(0L)
                              .build();

        //when
        FileDTO generate = numberOfDerWithFlexPotentialGenerator.generate(kpiDTO);

        //then
        XSSFWorkbook expectedWorkbook = getExpectedWorkbook(new ClassPathResource("/templates/kpi/unit/NumberOfDersWithFlexPotential.xlsx"));
        verifyThatTwoWorkbookAreSame(expectedWorkbook, new XSSFWorkbook(new ByteArrayInputStream(generate.getBytesData())));
    }

    @NotNull
    private static XSSFWorkbook getExpectedWorkbook(Resource resource) throws IOException {
        InputStream expectedFile = resource.getInputStream();
        return new XSSFWorkbook(new ByteArrayInputStream(expectedFile.readAllBytes()));
    }
}
