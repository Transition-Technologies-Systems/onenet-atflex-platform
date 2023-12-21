package pl.com.tt.flex.server.service.kpi.generator.requested.flexibility.power.fillfactor;

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

class PowerFillFactorGeneratorTest {
    private final PowerFillFactorDataFactory powerFillFactorDataFactory;
    private final PowerFillFactorGenerator powerFillFactorGenerator;

    PowerFillFactorGeneratorTest() {
        powerFillFactorDataFactory = Mockito.mock(PowerFillFactorDataFactory.class);
        powerFillFactorGenerator = new PowerFillFactorGenerator(powerFillFactorDataFactory);
    }

    @Test
    void givenSimpleData_expectedGenerateFile() throws KpiGenerateException, IOException {
        //before
        PowerFillFactorData powerFillFactorData = getPowerFillFactorData();
        Mockito.doReturn(powerFillFactorData)
               .when(powerFillFactorDataFactory)
               .create(Mockito.any(), Mockito.any());

        //given
        KpiDTO kpiDTO = KpiDTO.builder()
                              .type(KpiType.FLEX_VOLUME_OFFERED_VS_FLEX_REQUESTED_BY_DSO)
                              .dateFrom(Instant.parse("2022-10-03T22:00:00.00Z"))
                              .dateTo(Instant.parse("2022-11-02T22:00:00.00Z"))
                              .id(0L)
                              .build();

        //when
        FileDTO generate = powerFillFactorGenerator.generate(kpiDTO);

        //then
        XSSFWorkbook expectedWorkbook = getExpectedWorkbook(new ClassPathResource("/templates/kpi/requested/flexibility/power/fill_factor/PowerFillFactor.xlsx"));
        verifyThatTwoWorkbookAreSame(expectedWorkbook, new XSSFWorkbook(new ByteArrayInputStream(generate.getBytesData())));
    }

    @NotNull
    private PowerFillFactorData getPowerFillFactorData() {
        final Map<Pair<String, LocalDate>, RequestedAndOfferedVolume> maxVolumeGroupingByProductAndDate = new LinkedHashMap<>();
        maxVolumeGroupingByProductAndDate.put(Pair.of("A", LocalDate.of(2022, 10, 4)), new RequestedAndOfferedVolume(BigDecimal.valueOf(20), BigDecimal.valueOf(30)));
        maxVolumeGroupingByProductAndDate.put(Pair.of("A", LocalDate.of(2022, 10, 5)), new RequestedAndOfferedVolume(BigDecimal.valueOf(30), BigDecimal.valueOf(40)));
        maxVolumeGroupingByProductAndDate.put(Pair.of("A", LocalDate.of(2022, 10, 6)), new RequestedAndOfferedVolume(BigDecimal.valueOf(40), BigDecimal.valueOf(30)));
        maxVolumeGroupingByProductAndDate.put(Pair.of("B", LocalDate.of(2022, 10, 7)), new RequestedAndOfferedVolume(BigDecimal.valueOf(20), BigDecimal.valueOf(30)));
        maxVolumeGroupingByProductAndDate.put(Pair.of("B", LocalDate.of(2022, 10, 8)), new RequestedAndOfferedVolume(BigDecimal.valueOf(30), BigDecimal.valueOf(40)));
        maxVolumeGroupingByProductAndDate.put(Pair.of("B", LocalDate.of(2022, 10, 9)), new RequestedAndOfferedVolume(BigDecimal.valueOf(40), BigDecimal.valueOf(60)));
        return new PowerFillFactorData(maxVolumeGroupingByProductAndDate, BigDecimal.valueOf(6));
    }

    @NotNull
    private static XSSFWorkbook getExpectedWorkbook(Resource resource) throws IOException {
        InputStream expectedFile = resource.getInputStream();
        return new XSSFWorkbook(new ByteArrayInputStream(expectedFile.readAllBytes()));
    }
}
