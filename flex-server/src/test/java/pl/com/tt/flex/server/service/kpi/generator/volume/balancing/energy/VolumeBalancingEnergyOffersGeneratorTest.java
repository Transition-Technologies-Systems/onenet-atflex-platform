package pl.com.tt.flex.server.service.kpi.generator.volume.balancing.energy;

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

class VolumeBalancingEnergyOffersGeneratorTest {
    private final VolumeBalancingEnergyOffersDataFactory volumeBalancingEnergyOffersDataFactory;
    private final VolumeBalancingEnergyOffersGenerator volumeBalancingEnergyOffersGenerator;

    VolumeBalancingEnergyOffersGeneratorTest() {
        volumeBalancingEnergyOffersDataFactory = Mockito.mock(VolumeBalancingEnergyOffersDataFactory.class);
        volumeBalancingEnergyOffersGenerator = new VolumeBalancingEnergyOffersGenerator(volumeBalancingEnergyOffersDataFactory);
    }

    @Test
    void givenSimpleData_expectedGenerateFile() throws KpiGenerateException, IOException {
        //before
        VolumeBalancingEnergyOffersData volumeBalancingEnergyOffersData = getVolumeBalancingOffersData();
        Mockito.doReturn(volumeBalancingEnergyOffersData)
               .when(volumeBalancingEnergyOffersDataFactory)
               .create(Mockito.any(), Mockito.any());

        //given
        KpiDTO kpiDTO = KpiDTO.builder()
                              .type(KpiType.VOLUME_OF_BALANCING_ENERGY_OFFERS)
                              .dateFrom(Instant.parse("2022-10-03T22:00:00.00Z"))
                              .dateTo(Instant.parse("2022-11-02T22:00:00.00Z"))
                              .id(0L)
                              .build();

        //when
        FileDTO generate = volumeBalancingEnergyOffersGenerator.generate(kpiDTO);

        //then
        XSSFWorkbook expectedWorkbook = getExpectedWorkbook(new ClassPathResource("/templates/kpi/volume/balancing/energy/VolumeBalancingEnergyOffers.xlsx"));
        verifyThatTwoWorkbookAreSame(expectedWorkbook, new XSSFWorkbook(new ByteArrayInputStream(generate.getBytesData())));
    }

    @NotNull
    private VolumeBalancingEnergyOffersData getVolumeBalancingOffersData() {
        final Map<LocalDate, BigDecimal> numberOfVolumesGroupingByDeliveryDate = new LinkedHashMap<>();
        numberOfVolumesGroupingByDeliveryDate.put(LocalDate.of(2022, 10, 4), BigDecimal.valueOf(20));
        numberOfVolumesGroupingByDeliveryDate.put(LocalDate.of(2022, 10, 5), BigDecimal.valueOf(30));
        numberOfVolumesGroupingByDeliveryDate.put(LocalDate.of(2022, 10, 6), BigDecimal.valueOf(20));
        numberOfVolumesGroupingByDeliveryDate.put(LocalDate.of(2022, 10, 7), BigDecimal.valueOf(10));
        numberOfVolumesGroupingByDeliveryDate.put(LocalDate.of(2022, 10, 8), BigDecimal.valueOf(15));
        return new VolumeBalancingEnergyOffersData(numberOfVolumesGroupingByDeliveryDate);
    }

    @NotNull
    private static XSSFWorkbook getExpectedWorkbook(Resource resource) throws IOException {
        InputStream expectedFile = resource.getInputStream();
        return new XSSFWorkbook(new ByteArrayInputStream(expectedFile.readAllBytes()));
    }
}
