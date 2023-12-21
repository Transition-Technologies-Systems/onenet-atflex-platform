package pl.com.tt.flex.server.service.algorithm.agnoAlgorithm.energy;

import static java.util.Collections.singletonList;
import static pl.com.tt.flex.server.service.common.XlsxUtil.getWorkbook;
import static pl.com.tt.flex.server.util.WorkbookUtils.verifyThatTwoWorkbookAreSame;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.transaction.Transactional;

import org.apache.commons.lang3.tuple.Triple;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import lombok.extern.slf4j.Slf4j;
import pl.com.tt.flex.model.service.dto.file.FileDTO;
import pl.com.tt.flex.server.FlexserverApp;
import pl.com.tt.flex.server.domain.algorithm.AlgorithmEvaluationEntity;
import pl.com.tt.flex.server.domain.auction.offer.da.AuctionDayAheadOfferEntity;
import pl.com.tt.flex.server.domain.auction.offer.da.AuctionOfferBandDataEntity;
import pl.com.tt.flex.server.domain.auction.offer.da.AuctionOfferDersEntity;
import pl.com.tt.flex.server.domain.unit.LocalizationTypeEntity;
import pl.com.tt.flex.server.domain.unit.UnitEntity;
import pl.com.tt.flex.server.service.algorithm.agnoAlgorithm.eneregy.AgnoResultsFileGenerator;
import pl.com.tt.flex.server.service.unit.UnitService;
import pl.com.tt.flex.server.service.user.UserService;

@SpringBootTest(classes = FlexserverApp.class)
@Transactional
@Slf4j
public class AgnoResultsFileGeneratorTest {

    private final String ENERGY_OUTPUT_ZIP_TEST_FILE = "/templates/agnoAlgorithm/output/pure_bm.zip";
    private final String ENERGY_EXPECTED_RESULTS_XLSX_TEST_FILE = "/templates/agnoAlgorithm/results/agno_results_2022-09-28.xlsx";
    private final Instant DELIVERY_DATE = Instant.parse("2022-09-27T22:00:00Z");
    private final String PRIMARY_COUPLING_POINT_NAME = "test";
    private final Long DB_OFFER_ID = 1L;
    private final List<Triple<String, BigDecimal, BigDecimal>> BANDS = List.of(
        Triple.of("1", BigDecimal.valueOf(0.04), BigDecimal.valueOf(445)),
        Triple.of("-1", BigDecimal.valueOf(0.07), BigDecimal.valueOf(265)),
        Triple.of("-2", BigDecimal.valueOf(0.06), BigDecimal.valueOf(280)),
        Triple.of("-3", BigDecimal.valueOf(0.41), BigDecimal.valueOf(400)),
        Triple.of("-4", BigDecimal.valueOf(0.05), BigDecimal.valueOf(615))
    );
    private final String TEST_TIMESTAMP = "1";

    @MockBean
    protected UnitService mockUnitService;

    @MockBean
    protected UserService userService;

    @Autowired
    AgnoResultsFileGenerator agnoResultsFileGenerator;

    @Test
    void getComparisonFile_shouldReturnAgnoResultComparisonFile() throws IOException {
        Mockito.when(userService.getLangKeyForCurrentLoggedUser()).thenReturn("pl");
        AlgorithmEvaluationEntity algorithmEvaluation = createAlgorithmEvaluationEntity();
        FileDTO comparisonFile = agnoResultsFileGenerator.getResultsFile(algorithmEvaluation);
        XSSFWorkbook actualWorkbook = getWorkbook(comparisonFile);
        XSSFWorkbook expectedWorkbook = getWorkbook(ENERGY_EXPECTED_RESULTS_XLSX_TEST_FILE);
        verifyThatTwoWorkbookAreSame(expectedWorkbook, actualWorkbook);
    }

    @NotNull
    private AlgorithmEvaluationEntity createAlgorithmEvaluationEntity() throws IOException {
        Resource templateFileResource = new ClassPathResource(ENERGY_OUTPUT_ZIP_TEST_FILE);
        InputStream expectedZipFile = templateFileResource.getInputStream();
        Set<AuctionDayAheadOfferEntity> daOffers = createDayAheadOffers();
        return AlgorithmEvaluationEntity.builder()
            .outputFilesZip(expectedZipFile.readAllBytes())
            .daOffers(daOffers)
            .deliveryDate(DELIVERY_DATE)
            .build();
    }

    @NotNull
    private Set<AuctionDayAheadOfferEntity> createDayAheadOffers() {
        AuctionDayAheadOfferEntity offer = AuctionDayAheadOfferEntity.builder()
            .id(DB_OFFER_ID)
            .units(createOfferDer())
            .build();
        return new HashSet<>(singletonList(offer));
    }

    @NotNull
    private List<AuctionOfferDersEntity> createOfferDer() {
        AuctionOfferDersEntity offerDer = AuctionOfferDersEntity.builder()
            .bandData(createBandData())
            .unit(createUnit())
            .build();
        return new ArrayList<>(singletonList(offerDer));
    }

    @NotNull
    private List<AuctionOfferBandDataEntity> createBandData() {
        List<AuctionOfferBandDataEntity> bands = new ArrayList<>();
        for (Triple<String, BigDecimal, BigDecimal> band : BANDS) {
            bands.add(AuctionOfferBandDataEntity.builder()
                .bandNumber(band.getLeft())
                .hourNumber(TEST_TIMESTAMP)
                .acceptedVolume(band.getMiddle())
                .acceptedPrice(band.getRight())
                .build());
        }
        return bands;
    }

    @NotNull
    private UnitEntity createUnit() {
        UnitEntity unit = new UnitEntity();
        unit.setCouplingPointIdTypes(Set.of(createCouplingPoint()));
        return unit;
    }

    @NotNull
    private LocalizationTypeEntity createCouplingPoint() {
        return LocalizationTypeEntity.builder()
            .name(PRIMARY_COUPLING_POINT_NAME)
            .build();
    }

}
