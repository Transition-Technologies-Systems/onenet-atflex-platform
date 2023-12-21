package pl.com.tt.flex.server.service.kpi.generator.available.flexibility;

import org.apache.commons.lang3.tuple.Pair;
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
import java.time.Instant;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static pl.com.tt.flex.server.util.WorkbookUtils.verifyThatTwoWorkbookAreSame;

class AvailableFlexibilityGeneratorTest {
    private final AvailableFlexibilityDataFactory availableFlexibilityDataFactory;
    private final AvailableFlexibilityGenerator availableFlexibilityGenerator;

    AvailableFlexibilityGeneratorTest() {
        availableFlexibilityDataFactory = Mockito.mock(AvailableFlexibilityDataFactory.class);
        availableFlexibilityGenerator = new AvailableFlexibilityGenerator(availableFlexibilityDataFactory);
    }

    @Test
    void givenSimpleData_expectedGenerateFile() throws KpiGenerateException, IOException {
        //before
        AvailableFlexibilityData availableFlexibilityData = getTransactionVolumeData();
        Mockito.doReturn(availableFlexibilityData).when(availableFlexibilityDataFactory).create(Mockito.any(), Mockito.any());

        //given
        KpiDTO kpiDTO = KpiDTO.builder()
                              .type(KpiType.AVAILABLE_FLEXIBILITY)
                              .dateFrom(Instant.parse("2022-06-05T22:00:00.00Z"))
                              .dateTo(Instant.parse("2023-03-04T22:00:00.00Z"))
                              .id(0L)
                              .build();

        //when
        FileDTO generate = availableFlexibilityGenerator.generate(kpiDTO);

        //then
        XSSFWorkbook expectedWorkbook = getExpectedWorkbook(new ClassPathResource("/templates/kpi/available/flexibility/AvailableFlexibility_2022_06_05-2023_04_03.xlsx"));
        verifyThatTwoWorkbookAreSame(expectedWorkbook, new XSSFWorkbook(new ByteArrayInputStream(generate.getBytesData())));
    }

    @NotNull
    private AvailableFlexibilityData getTransactionVolumeData() {
        final Map<Pair<String, LocalDate>, FlexibilityData> availableFlexibilityDateGroupingByProductAndDeliveryDate = new LinkedHashMap<>();
        availableFlexibilityDateGroupingByProductAndDeliveryDate.put(
            Pair.of("Down", LocalDate.of(2022, 6, 9)),
            new FlexibilityData(BigDecimal.valueOf(2000), BigDecimal.valueOf(1000))
        );
        availableFlexibilityDateGroupingByProductAndDeliveryDate.put(
            Pair.of("Down", LocalDate.of(2022, 7, 26)),
            new FlexibilityData(BigDecimal.valueOf(1000), BigDecimal.valueOf(1000)
            )
        );
        availableFlexibilityDateGroupingByProductAndDeliveryDate.put(
            Pair.of("Up", LocalDate.of(2023, 2, 28)),
            new FlexibilityData(BigDecimal.valueOf(2000), BigDecimal.valueOf(1000)
            )
        );
        availableFlexibilityDateGroupingByProductAndDeliveryDate.put(
            Pair.of("Up", LocalDate.of(2023, 3, 4)),
            new FlexibilityData(BigDecimal.valueOf(400), BigDecimal.valueOf(100)
            )
        );
        final Map<String, FlexibilityData> availableFlexibilityDateGroupingByProduct = new HashMap<>();
        availableFlexibilityDateGroupingByProduct.put("Down", new FlexibilityData(BigDecimal.valueOf(3000), BigDecimal.valueOf(2000)));
        availableFlexibilityDateGroupingByProduct.put("Up", new FlexibilityData(BigDecimal.valueOf(2400), BigDecimal.valueOf(1100)));
        return new AvailableFlexibilityData(availableFlexibilityDateGroupingByProductAndDeliveryDate, availableFlexibilityDateGroupingByProduct);
    }

    @NotNull
    private static XSSFWorkbook getExpectedWorkbook(Resource resource) throws IOException {
        InputStream expectedFile = resource.getInputStream();
        return new XSSFWorkbook(new ByteArrayInputStream(expectedFile.readAllBytes()));
    }
}
