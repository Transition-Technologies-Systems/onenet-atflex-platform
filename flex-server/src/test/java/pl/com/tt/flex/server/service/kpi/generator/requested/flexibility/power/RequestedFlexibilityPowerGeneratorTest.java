package pl.com.tt.flex.server.service.kpi.generator.requested.flexibility.power;

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
import java.util.LinkedHashMap;
import java.util.Map;

import static pl.com.tt.flex.server.util.WorkbookUtils.verifyThatTwoWorkbookAreSame;

class RequestedFlexibilityPowerGeneratorTest {
    private final RequestedFlexibilityPowerDataFactory requestedFlexibilityPowerDataFactory;
    private final RequestedFlexibilityPowerGenerator requestedFlexibilityPowerGenerator;

    RequestedFlexibilityPowerGeneratorTest() {
        requestedFlexibilityPowerDataFactory = Mockito.mock(RequestedFlexibilityPowerDataFactory.class);
        requestedFlexibilityPowerGenerator = new RequestedFlexibilityPowerGenerator(requestedFlexibilityPowerDataFactory);
    }

    @Test
    void givenSimpleData_expectedGenerateFile() throws KpiGenerateException, IOException {
        //before
        RequestedFlexibilityPowerData requestedFlexibilityPowerData = getRequestedFlexibilityPowerData();
        Mockito.doReturn(requestedFlexibilityPowerData)
               .when(requestedFlexibilityPowerDataFactory)
               .create(Mockito.any(), Mockito.any());

        //given
        KpiDTO kpiDTO = KpiDTO.builder()
                              .type(KpiType.REQUEST_FLEXIBILITY_POWER)
                              .dateFrom(Instant.parse("2022-10-03T22:00:00.00Z"))
                              .dateTo(Instant.parse("2022-11-02T22:00:00.00Z"))
                              .id(0L)
                              .build();

        //when
        FileDTO generate = requestedFlexibilityPowerGenerator.generate(kpiDTO);

        //then
        XSSFWorkbook expectedWorkbook = getExpectedWorkbook(new ClassPathResource("/templates/kpi/requested/flexibility/power/RequestedFlexibilityPower.xlsx"));
        verifyThatTwoWorkbookAreSame(expectedWorkbook, new XSSFWorkbook(new ByteArrayInputStream(generate.getBytesData())));
    }

    @NotNull
    private RequestedFlexibilityPowerData getRequestedFlexibilityPowerData() {
        final Map<Pair<String, LocalDate>, BigDecimal> maxVolumeGroupingByProductAndDate = new LinkedHashMap<>();
        maxVolumeGroupingByProductAndDate.put(Pair.of("A", LocalDate.of(2022, 10, 4)), BigDecimal.valueOf(20));
        maxVolumeGroupingByProductAndDate.put(Pair.of("A", LocalDate.of(2022, 10, 5)), BigDecimal.valueOf(30));
        maxVolumeGroupingByProductAndDate.put(Pair.of("A", LocalDate.of(2022, 10, 6)), BigDecimal.valueOf(50));
        maxVolumeGroupingByProductAndDate.put(Pair.of("B", LocalDate.of(2022, 10, 4)), BigDecimal.valueOf(20));
        maxVolumeGroupingByProductAndDate.put(Pair.of("B", LocalDate.of(2022, 10, 5)), BigDecimal.valueOf(40));
        return new RequestedFlexibilityPowerData(maxVolumeGroupingByProductAndDate, BigDecimal.valueOf(5));
    }

    @NotNull
    private static XSSFWorkbook getExpectedWorkbook(Resource resource) throws IOException {
        InputStream expectedFile = resource.getInputStream();
        return new XSSFWorkbook(new ByteArrayInputStream(expectedFile.readAllBytes()));
    }
}
