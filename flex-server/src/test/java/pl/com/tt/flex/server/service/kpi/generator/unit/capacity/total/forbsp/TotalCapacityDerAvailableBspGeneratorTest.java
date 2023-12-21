package pl.com.tt.flex.server.service.kpi.generator.unit.capacity.total.forbsp;

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

class TotalCapacityDerAvailableBspGeneratorTest {

    private final TotalCapacityDerAvailableBspDataFactory totalCapacityDerAvailableBspDataFactory;
    private final TotalCapacityDerAvailableBspGenerator totalCapacityDerAvailableBspGenerator;

    TotalCapacityDerAvailableBspGeneratorTest() {
        totalCapacityDerAvailableBspDataFactory = Mockito.mock(TotalCapacityDerAvailableBspDataFactory.class);
        totalCapacityDerAvailableBspGenerator = new TotalCapacityDerAvailableBspGenerator(totalCapacityDerAvailableBspDataFactory);
    }

    @Test
    void givenSimpleData_expectedGenerateFile() throws KpiGenerateException, IOException {
        //before
        TotalCapacityDerAvailableBspData data = new TotalCapacityDerAvailableBspData(BigDecimal.valueOf(12345));
        Mockito.doReturn(data).when(totalCapacityDerAvailableBspDataFactory).create();

        //given
        KpiDTO kpiDTO = KpiDTO.builder()
                              .type(KpiType.TOTAL_CAPACITY_OF_DERS_AVAILABLE_FOR_BSP)
                              .id(0L)
                              .build();

        //when
        FileDTO generate = totalCapacityDerAvailableBspGenerator.generate(kpiDTO);

        //then
        XSSFWorkbook expectedWorkbook = getExpectedWorkbook(new ClassPathResource("/templates/kpi/unit/TotalCapacityOfDerAvailableForBsp.xlsx"));
        verifyThatTwoWorkbookAreSame(expectedWorkbook, new XSSFWorkbook(new ByteArrayInputStream(generate.getBytesData())));
    }

    @NotNull
    private static XSSFWorkbook getExpectedWorkbook(Resource resource) throws IOException {
        InputStream expectedFile = resource.getInputStream();
        return new XSSFWorkbook(new ByteArrayInputStream(expectedFile.readAllBytes()));
    }
}
