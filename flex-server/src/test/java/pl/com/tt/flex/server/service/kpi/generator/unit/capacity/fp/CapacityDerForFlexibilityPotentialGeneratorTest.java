package pl.com.tt.flex.server.service.kpi.generator.unit.capacity.fp;

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
import java.util.HashMap;
import java.util.Map;

import static pl.com.tt.flex.server.util.WorkbookUtils.verifyThatTwoWorkbookAreSame;

class CapacityDerForFlexibilityPotentialGeneratorTest {
    private final CapacityDerForFlexibilityPotentialDataFactory capacityDerForFlexibilityPotentialDataFactory;
    private final CapacityDerForFlexibilityPotentialGenerator capacityDerForFlexibilityPotentialGenerator;

    CapacityDerForFlexibilityPotentialGeneratorTest() {
        capacityDerForFlexibilityPotentialDataFactory = Mockito.mock(CapacityDerForFlexibilityPotentialDataFactory.class);
        capacityDerForFlexibilityPotentialGenerator = new CapacityDerForFlexibilityPotentialGenerator(capacityDerForFlexibilityPotentialDataFactory);
    }

    @Test
    void givenSimpleData_expectedGenerateFile() throws KpiGenerateException, IOException {
        //before
        CapacityDerForFlexibilityPotentialData data = getCapacityCertifiedDerForFLexPotentialData();
        Mockito.doReturn(data).when(capacityDerForFlexibilityPotentialDataFactory).create();

        //given
        KpiDTO kpiDTO = KpiDTO.builder()
                              .type(KpiType.CAPACITY_OF_CERTIFIED_DERS_WITH_AT_LEAST_ONE_FP)
                              .id(0L)
                              .build();

        //when
        FileDTO generate = capacityDerForFlexibilityPotentialGenerator.generate(kpiDTO);

        //then
        XSSFWorkbook expectedWorkbook = getExpectedWorkbook(new ClassPathResource("/templates/kpi/unit/CapacityOfCertifiedDersForFlexibilityPotential.xlsx"));
        verifyThatTwoWorkbookAreSame(expectedWorkbook, new XSSFWorkbook(new ByteArrayInputStream(generate.getBytesData())));
    }

    @NotNull
    private CapacityDerForFlexibilityPotentialData getCapacityCertifiedDerForFLexPotentialData() {
        final Map<String, BigDecimal> fpVolumeSumGroupingByProductName = new HashMap<>();
        fpVolumeSumGroupingByProductName.put("Up", BigDecimal.valueOf(30));
        fpVolumeSumGroupingByProductName.put("Down", BigDecimal.valueOf(20));
        return new CapacityDerForFlexibilityPotentialData(fpVolumeSumGroupingByProductName);
    }

    @NotNull
    private static XSSFWorkbook getExpectedWorkbook(Resource resource) throws IOException {
        InputStream expectedFile = resource.getInputStream();
        return new XSSFWorkbook(new ByteArrayInputStream(expectedFile.readAllBytes()));
    }
}
