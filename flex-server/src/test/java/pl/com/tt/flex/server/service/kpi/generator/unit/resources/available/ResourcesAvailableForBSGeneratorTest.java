package pl.com.tt.flex.server.service.kpi.generator.unit.resources.available;

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

class ResourcesAvailableForBSGeneratorTest {

    private final ResourcesAvailableForBSDataFactory resourcesAvailableForBSDataFactory;
    private final ResourcesAvailableForBSGenerator resourcesAvailableForBSGenerator;

    ResourcesAvailableForBSGeneratorTest() {
        resourcesAvailableForBSDataFactory = Mockito.mock(ResourcesAvailableForBSDataFactory.class);
        resourcesAvailableForBSGenerator = new ResourcesAvailableForBSGenerator(resourcesAvailableForBSDataFactory);
    }

    @Test
    void givenSimpleData_expectedGenerateFile() throws KpiGenerateException, IOException {
        //before
        ResourcesAvailableForBSData data = new ResourcesAvailableForBSData(BigDecimal.valueOf(30), BigDecimal.valueOf(70));
        Mockito.doReturn(data).when(resourcesAvailableForBSDataFactory).create();

        //given
        KpiDTO kpiDTO = KpiDTO.builder()
                              .type(KpiType.PERCENTAGE_RESOURCES_AVAILABLE_FOR_BALANCING_SERVICES)
                              .id(0L)
                              .build();

        //when
        FileDTO generate = resourcesAvailableForBSGenerator.generate(kpiDTO);

        //then
        XSSFWorkbook expectedWorkbook = getExpectedWorkbook(new ClassPathResource("/templates/kpi/unit/PercentageResourceAvailable.xlsx"));
        verifyThatTwoWorkbookAreSame(expectedWorkbook, new XSSFWorkbook(new ByteArrayInputStream(generate.getBytesData())));
    }

    @NotNull
    private static XSSFWorkbook getExpectedWorkbook(Resource resource) throws IOException {
        InputStream expectedFile = resource.getInputStream();
        return new XSSFWorkbook(new ByteArrayInputStream(expectedFile.readAllBytes()));
    }
}
