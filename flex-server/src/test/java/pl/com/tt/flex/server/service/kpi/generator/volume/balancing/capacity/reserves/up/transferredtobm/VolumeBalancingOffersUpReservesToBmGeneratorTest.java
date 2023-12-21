package pl.com.tt.flex.server.service.kpi.generator.volume.balancing.capacity.reserves.up.transferredtobm;

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
import pl.com.tt.flex.server.service.kpi.generator.volume.balancing.capacity.reserves.VolumeBalancingOffersReservesData;

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

class VolumeBalancingOffersUpReservesToBmGeneratorTest {
    private final VolumeBalancingOffersUpReservesToBmDataFactory volumeBalancingOffersUpReservesToBmDataFactory;
    private final VolumeBalancingOffersUpReservesToBmGenerator volumeBalancingOffersUpReservesToBmGenerator;

    VolumeBalancingOffersUpReservesToBmGeneratorTest() {
        volumeBalancingOffersUpReservesToBmDataFactory = Mockito.mock(VolumeBalancingOffersUpReservesToBmDataFactory.class);
        volumeBalancingOffersUpReservesToBmGenerator = new VolumeBalancingOffersUpReservesToBmGenerator(volumeBalancingOffersUpReservesToBmDataFactory);
    }

    @Test
    void givenSimpleData_expectedGenerateFile() throws KpiGenerateException, IOException {
        //before
        VolumeBalancingOffersReservesData volumeBalancingOffersReservesData = getVolumeBalancingOffersUpReservesData();
        Mockito.doReturn(volumeBalancingOffersReservesData)
               .when(volumeBalancingOffersUpReservesToBmDataFactory)
               .create(Mockito.any(), Mockito.any());

        //given
        KpiDTO kpiDTO = KpiDTO.builder()
                              .type(KpiType.VOLUME_OF_BALANCING_SERVICE_OFFERS_UP_RESERVES_TRANSFERRED_TO_BM)
                              .dateFrom(Instant.parse("2022-10-03T22:00:00.00Z"))
                              .dateTo(Instant.parse("2022-11-02T22:00:00.00Z"))
                              .id(0L)
                              .build();

        //when
        FileDTO generate = volumeBalancingOffersUpReservesToBmGenerator.generate(kpiDTO);

        //then
        XSSFWorkbook expectedWorkbook = getExpectedWorkbook(
            new ClassPathResource("/templates/kpi/volume/balancing/capacity/reserves/up/VolumeBalancingOffersUpReservesTransferredToBM.xlsx")
        );
        verifyThatTwoWorkbookAreSame(expectedWorkbook, new XSSFWorkbook(new ByteArrayInputStream(generate.getBytesData())));
    }

    @NotNull
    private VolumeBalancingOffersReservesData getVolumeBalancingOffersUpReservesData() {
        final Map<Pair<String, LocalDate>, BigDecimal> numberOfVolumesGroupingByProductNameAndDeliveryDate = new LinkedHashMap<>();
        numberOfVolumesGroupingByProductNameAndDeliveryDate.put(Pair.of("A", LocalDate.of(2022, 10, 4)), BigDecimal.valueOf(20));
        numberOfVolumesGroupingByProductNameAndDeliveryDate.put(Pair.of("A", LocalDate.of(2022, 10, 5)), BigDecimal.valueOf(30));
        numberOfVolumesGroupingByProductNameAndDeliveryDate.put(Pair.of("A", LocalDate.of(2022, 10, 6)), BigDecimal.valueOf(20));
        numberOfVolumesGroupingByProductNameAndDeliveryDate.put(Pair.of("Up", LocalDate.of(2022, 10, 7)), BigDecimal.valueOf(10));
        numberOfVolumesGroupingByProductNameAndDeliveryDate.put(Pair.of("Up", LocalDate.of(2022, 10, 18)), BigDecimal.valueOf(15));
        final Map<String, BigDecimal> numberOfVolumesGroupingByProductName = new HashMap<>();
        numberOfVolumesGroupingByProductName.put("A", BigDecimal.valueOf(70));
        numberOfVolumesGroupingByProductName.put("Up", BigDecimal.valueOf(25));
        return new VolumeBalancingOffersReservesData(numberOfVolumesGroupingByProductNameAndDeliveryDate, numberOfVolumesGroupingByProductName);
    }

    @NotNull
    private static XSSFWorkbook getExpectedWorkbook(Resource resource) throws IOException {
        InputStream expectedFile = resource.getInputStream();
        return new XSSFWorkbook(new ByteArrayInputStream(expectedFile.readAllBytes()));
    }
}
