package pl.com.tt.flex.server.service.kpi.generator.fsp.number;

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
import pl.com.tt.flex.server.service.kpi.generator.active.participation.ActiveParticipationData;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;

import static pl.com.tt.flex.server.util.WorkbookUtils.verifyThatTwoWorkbookAreSame;

class NumberOfFspGeneratorTest {
    private final NumberOfFspDataFactory numberOfFspDataFactory;
    private final NumberOfFspGenerator numberOfFspGenerator;

    NumberOfFspGeneratorTest() {
        numberOfFspDataFactory = Mockito.mock(NumberOfFspDataFactory.class);
        numberOfFspGenerator = new NumberOfFspGenerator(numberOfFspDataFactory);
    }

    @Test
    void givenSimpleData_expectedGenerateFile() throws KpiGenerateException, IOException {
        //before
        NumberOfFspData numberOfFspData = new NumberOfFspData(BigDecimal.TEN);
        Mockito.doReturn(numberOfFspData).when(numberOfFspDataFactory).create();

        //given
        KpiDTO kpiDTO = KpiDTO.builder()
            .type(KpiType.NUMBER_OF_FSPS)
            .id(0L)
            .build();

        //when
        FileDTO generate = numberOfFspGenerator.generate(kpiDTO);

        //then
        XSSFWorkbook expectedWorkbook = getExpectedWorkbook(new ClassPathResource("/templates/kpi/fsp/number/NumberOfFSPs.xlsx"));
        verifyThatTwoWorkbookAreSame(expectedWorkbook, new XSSFWorkbook(new ByteArrayInputStream(generate.getBytesData())));
    }

    @NotNull
    private static XSSFWorkbook getExpectedWorkbook(Resource resource) throws IOException {
        InputStream expectedFile = resource.getInputStream();
        return new XSSFWorkbook(new ByteArrayInputStream(expectedFile.readAllBytes()));
    }
}
