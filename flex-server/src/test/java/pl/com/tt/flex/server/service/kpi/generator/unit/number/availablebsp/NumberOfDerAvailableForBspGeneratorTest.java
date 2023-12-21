package pl.com.tt.flex.server.service.kpi.generator.unit.number.availablebsp;

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

class NumberOfDerAvailableForBspGeneratorTest {

    private final NumberOfDerAvailableForBspDataFactory numberOfDerAvailableForBspDataFactory;
    private final NumberOfDerAvailableForBspGenerator numberOfDerAvailableForBspGenerator;

    NumberOfDerAvailableForBspGeneratorTest() {
        numberOfDerAvailableForBspDataFactory = Mockito.mock(NumberOfDerAvailableForBspDataFactory.class);
        numberOfDerAvailableForBspGenerator = new NumberOfDerAvailableForBspGenerator(numberOfDerAvailableForBspDataFactory);
    }

    @Test
    void givenSimpleData_expectedGenerateFile() throws KpiGenerateException, IOException {
        //before
        NumberOfDerAvailableForBspData data = new NumberOfDerAvailableForBspData(BigDecimal.valueOf(20));
        Mockito.doReturn(data).when(numberOfDerAvailableForBspDataFactory).create();

        //given
        KpiDTO kpiDTO = KpiDTO.builder()
                              .type(KpiType.NUMBER_OF_DER_AVAILABLE_FOR_BSP)
                              .id(0L)
                              .build();

        //when
        FileDTO generate = numberOfDerAvailableForBspGenerator.generate(kpiDTO);

        //then
        XSSFWorkbook expectedWorkbook = getExpectedWorkbook(new ClassPathResource("/templates/kpi/unit/NumberOfDerAvailableForBsp.xlsx"));
        verifyThatTwoWorkbookAreSame(expectedWorkbook, new XSSFWorkbook(new ByteArrayInputStream(generate.getBytesData())));
    }

    @NotNull
    private static XSSFWorkbook getExpectedWorkbook(Resource resource) throws IOException {
        InputStream expectedFile = resource.getInputStream();
        return new XSSFWorkbook(new ByteArrayInputStream(expectedFile.readAllBytes()));
    }
}
