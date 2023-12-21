package pl.com.tt.flex.server.service.algorithm.agnoAlgorithm;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import pl.com.tt.flex.model.service.dto.MinimalDTO;
import pl.com.tt.flex.model.service.dto.algorithm.AlgorithmEvaluationConfigDTO;
import pl.com.tt.flex.model.service.dto.algorithm.AlgorithmType;
import pl.com.tt.flex.model.service.dto.auction.da.AuctionDayAheadMinDTO;
import pl.com.tt.flex.model.service.dto.auction.offer.da.AuctionDayAheadOfferDTO;
import pl.com.tt.flex.model.service.dto.auction.offer.da.AuctionOfferBandDataDTO;
import pl.com.tt.flex.model.service.dto.auction.offer.da.AuctionOfferDersDTO;
import pl.com.tt.flex.model.service.dto.auction.type.AuctionDayAheadType;
import pl.com.tt.flex.model.service.dto.auction.type.AuctionOfferType;
import pl.com.tt.flex.model.service.dto.der.DerMinDTO;
import pl.com.tt.flex.model.service.dto.file.FileDTO;
import pl.com.tt.flex.model.service.dto.kdm_model.KdmModelMinimalDTO;
import pl.com.tt.flex.model.service.dto.kdm_model.KdmModelTimestampsMinimalDTO;
import pl.com.tt.flex.model.service.dto.product.ProductMinDTO;
import pl.com.tt.flex.model.service.dto.product.type.Direction;
import pl.com.tt.flex.server.FlexserverApp;
import pl.com.tt.flex.server.common.errors.ObjectValidationException;
import pl.com.tt.flex.server.service.algorithm.agnoAlgorithm.capacity.CapacityAgnoAlgorithmService;
import pl.com.tt.flex.server.service.algorithm.agnoAlgorithm.eneregy.EnergyAgnoAlgorithmService;
import pl.com.tt.flex.server.service.algorithm.danoAlgorithm.EnergyDanoAlgorithmService;
import pl.com.tt.flex.server.service.auction.da.AuctionDayAheadService;
import pl.com.tt.flex.server.service.product.ProductService;
import pl.com.tt.flex.server.service.product.dto.ProductDTO;
import pl.com.tt.flex.server.service.product.forecastedPrices.ForecastedPricesService;
import pl.com.tt.flex.server.service.product.forecastedPrices.dto.ForecastedPricesDTO;
import pl.com.tt.flex.server.service.unit.UnitService;
import pl.com.tt.flex.server.service.unit.dto.UnitDTO;
import pl.com.tt.flex.server.service.unit.selfSchedule.UnitSelfScheduleService;
import pl.com.tt.flex.server.util.ZipUtil;
import pl.com.tt.flex.server.web.rest.algorithm.resource.FlexAgnoAlgorithmResource;
import pl.com.tt.flex.server.web.rest.product.ProductResourceIT;
import pl.com.tt.flex.server.web.rest.unit.UnitResourceAdminIT;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.any;
import static pl.com.tt.flex.server.util.DateUtil.sortedHourNumbers;
import static pl.com.tt.flex.server.util.WorkbookUtils.verifyThatTwoWorkbookAreSame;

@SpringBootTest(classes = FlexserverApp.class)
@Transactional
@Slf4j
class AlgorithmAbstractTest {

    private final String ENERGY_EXPECTED_ZIP_TEST_FILE = "/templates/agnoAlgorithm/input/energyAgnoBmFiles.zip";
    private final String CAPACITY_EXPECTED_ZIP_TEST_FILE = "/templates/agnoAlgorithm/input/capacityAgnoPbcmFiles.zip";
    private final String DANO_EXPECTED_ZIP_TEST_FILE = "/templates/agnoAlgorithm/input/energyDgiaFiles.zip";

    private final Long KDM_MODEL_ID = 1L;
    private final Instant DELIVERY_DATE = Instant.parse("2022-07-01T22:00:00Z");
    private final int OFFER_START_HOUR = 5;
    private final int OFFER_LAST_HOUR = 10;
    private final List<Integer> ENERGY_OFFER_BANDS = Arrays.asList(-1, 0, 1);
    private final List<Integer> CAPACITY_OFFER_BANDS = Arrays.asList(-1, 0);

    private final BigDecimal BAND_PRICE = BigDecimal.valueOf(200.31);
    private final BigDecimal BAND_VOLUME = BigDecimal.valueOf(30);
    private final BigDecimal BAND_ACCEPTED_VOLUME = BigDecimal.valueOf(40);

    private final BigDecimal FORECASTED_PRICE = BigDecimal.valueOf(100);
    private final BigDecimal SELF_SCHEDULE_VOLUME = BigDecimal.valueOf(100);

    @MockBean
    protected AuctionDayAheadService mockDayAheadService;
    @MockBean
    protected ProductService mockProductService;
    @MockBean
    protected ForecastedPricesService mockForecastedPricesService;
    @MockBean
    protected UnitService mockUnitService;
    @MockBean
    protected UnitSelfScheduleService mockScheduleFileService;
    @MockBean(name = "flexAgnoAlgorithmResource")
    protected FlexAgnoAlgorithmResource mockFlexAgnoAlgorithmResource;

    @Autowired
    EntityManager em;
    @Autowired
    EnergyAgnoAlgorithmService energyAgnoAlgorithmService;
    @Autowired
    CapacityAgnoAlgorithmService capacityAgnoAlgorithmService;
    @Autowired
    EnergyDanoAlgorithmService energyDanoAlgorithmService;

    @BeforeEach
    void setUp() throws IOException {
        MockitoAnnotations.initMocks(this);

        KdmModelMinimalDTO kdmModelMinimalDTO = getKdmModelMinimalDTO();
        Mockito.when(mockFlexAgnoAlgorithmResource.getKdmFileMinimal(any())).thenReturn(Optional.of(kdmModelMinimalDTO));

        ProductDTO productDTO = ProductResourceIT.createDto(em);
        Mockito.when(mockProductService.findById(any())).thenReturn(Optional.of(productDTO));

        UnitDTO unitDTO = UnitResourceAdminIT.createUnitDto(em);
        Mockito.when(mockUnitService.findById(any())).thenReturn(Optional.of(unitDTO));

        ForecastedPricesDTO forecastedPricesFileDTO = new ForecastedPricesDTO();
        List<MinimalDTO<String, BigDecimal>> forecastPrices = new ArrayList<>();
        sortedHourNumbers.forEach(hourNumber -> forecastPrices.add(new MinimalDTO<>(hourNumber, FORECASTED_PRICE)));
        forecastedPricesFileDTO.setPrices(forecastPrices);
        Mockito.when(mockForecastedPricesService.findByProductAndForecastedPriceDate(productDTO.getId(), DELIVERY_DATE)).thenReturn(Optional.of(forecastedPricesFileDTO));
        Mockito.when(mockForecastedPricesService.existForecastedPricesForProductAndDeliveryDate(productDTO.getId(), DELIVERY_DATE)).thenReturn(true);

        List<MinimalDTO<String, BigDecimal>> schedulingUnits = new ArrayList<>();
        sortedHourNumbers.forEach(hourNumber -> schedulingUnits.add(new MinimalDTO<>(hourNumber, SELF_SCHEDULE_VOLUME)));
        Mockito.when(mockScheduleFileService.findVolumesForDerAndSelfScheduleDate(unitDTO.getId(), DELIVERY_DATE)).thenReturn(schedulingUnits);

        Set<AuctionDayAheadOfferDTO> energyOffers = createdDayAhedOffers(productDTO, unitDTO, AuctionOfferType.ENERGY);
        Mockito.when(mockDayAheadService.findAllOfferByAuctionTypeAndDeliveryDate(AuctionDayAheadType.ENERGY, DELIVERY_DATE)).thenReturn(energyOffers);

        Set<AuctionDayAheadOfferDTO> capacityOffers = createdDayAhedOffers(productDTO, unitDTO, AuctionOfferType.CAPACITY);
        Mockito.when(mockDayAheadService.findAllOfferByAuctionTypeAndDeliveryDate(AuctionDayAheadType.CAPACITY, DELIVERY_DATE)).thenReturn(capacityOffers);
    }

    @NotNull
    private KdmModelMinimalDTO getKdmModelMinimalDTO() {
        KdmModelMinimalDTO kdmModelMinimalDTO = new KdmModelMinimalDTO();
        kdmModelMinimalDTO.setLvModel(true);
        kdmModelMinimalDTO.setId(KDM_MODEL_ID);
        List<KdmModelTimestampsMinimalDTO> kdmModelTimestampMinimal = createKdmModelTimestampMinimal(OFFER_START_HOUR, OFFER_LAST_HOUR);
        kdmModelMinimalDTO.setTimestamps(kdmModelTimestampMinimal);
        return kdmModelMinimalDTO;
    }

    private List<KdmModelTimestampsMinimalDTO> createKdmModelTimestampMinimal(int startHour, int lastHour) {
        List<KdmModelTimestampsMinimalDTO> kdmModelTimestampMinimalList = new ArrayList<>();
        for (int i = startHour; i <= lastHour; i++) {
            KdmModelTimestampsMinimalDTO kdmModelTimestampsMinimalDTO = new KdmModelTimestampsMinimalDTO();
            kdmModelTimestampsMinimalDTO.setTimestamp(String.valueOf(i));
            kdmModelTimestampsMinimalDTO.setStations(Arrays.asList("Z7601197", "COUPLING_POINT_ID"));
            kdmModelTimestampMinimalList.add(kdmModelTimestampsMinimalDTO);
        }
        return kdmModelTimestampMinimalList;

    }

    @NotNull
    private Set<AuctionDayAheadOfferDTO> createdDayAhedOffers(ProductDTO productDTO, UnitDTO unitDTO, AuctionOfferType offerType) {
        AuctionDayAheadOfferDTO auctionDayAheadOfferDTO = new AuctionDayAheadOfferDTO();
        auctionDayAheadOfferDTO.setType(offerType);
        AuctionDayAheadMinDTO auctionDayAheadMinDTO = new AuctionDayAheadMinDTO();
        ProductMinDTO productMinDTO = new ProductMinDTO();
        Direction direction = offerType.equals(AuctionOfferType.ENERGY) ? Direction.UNDEFINED : Direction.DOWN;
        productMinDTO.setDirection(direction);
        productMinDTO.setShortName(productDTO.getShortName());
        auctionDayAheadMinDTO.setProduct(productMinDTO);
        auctionDayAheadOfferDTO.setAuctionDayAhead(auctionDayAheadMinDTO);
        AuctionOfferDersDTO auctionOfferDersDTO = new AuctionOfferDersDTO();
        DerMinDTO derMinDTO = new DerMinDTO();
        derMinDTO.setName(unitDTO.getName());
        derMinDTO.setSourcePower(unitDTO.getSourcePower());
        auctionOfferDersDTO.setDer(derMinDTO);
        List<AuctionOfferBandDataDTO> bandList = new ArrayList<>();
        List<Integer> offerBandList = offerType.equals(AuctionOfferType.ENERGY) ? ENERGY_OFFER_BANDS : CAPACITY_OFFER_BANDS;
        offerBandList.forEach(b -> bandList.addAll(getBandHours(b)));
        auctionOfferDersDTO.setBandData(bandList);
        auctionDayAheadOfferDTO.setDers(singletonList(auctionOfferDersDTO));
        return new HashSet<>(singletonList(auctionDayAheadOfferDTO));
    }

    @NotNull
    private List<AuctionOfferBandDataDTO> getBandHours(int bandNumber) {
        List<AuctionOfferBandDataDTO> bandList = new ArrayList<>();
        for (int i = OFFER_START_HOUR; i <= OFFER_LAST_HOUR; i++) {
            AuctionOfferBandDataDTO auctionOfferBandDataDTO = new AuctionOfferBandDataDTO();
            auctionOfferBandDataDTO.setBandNumber(bandNumber);
            auctionOfferBandDataDTO.setHourNumber(String.valueOf(i));
            auctionOfferBandDataDTO.setPrice(BAND_PRICE);
            auctionOfferBandDataDTO.setVolume(BAND_VOLUME);
            auctionOfferBandDataDTO.setAcceptedVolume(BAND_ACCEPTED_VOLUME);
            bandList.add(auctionOfferBandDataDTO);
        }
        return bandList;
    }

    @Test
    void prepareDataToCreateAgnoFile_shouldReturnBmAgnoFiles() throws IOException, ObjectValidationException {
        AlgorithmEvaluationConfigDTO algorithmEvaluationConfigDTO = new AlgorithmEvaluationConfigDTO();
        algorithmEvaluationConfigDTO.setDeliveryDate(DELIVERY_DATE);
        algorithmEvaluationConfigDTO.setAlgorithmType(AlgorithmType.BM);
        algorithmEvaluationConfigDTO.setKdmModelId(KDM_MODEL_ID);
        ProductDTO productDTO = ProductResourceIT.createDto(em);
        UnitDTO unitDTO = UnitResourceAdminIT.createUnitDto(em);
        Set<AuctionDayAheadOfferDTO> energyOffers = createdDayAhedOffers(productDTO, unitDTO, AuctionOfferType.ENERGY);
        FileDTO bmAlgorithmFile = energyAgnoAlgorithmService.getAlgorithmInputFiles(algorithmEvaluationConfigDTO, energyOffers);
        Resource templateFileResource = new ClassPathResource(ENERGY_EXPECTED_ZIP_TEST_FILE);
        InputStream expectedZipFile = templateFileResource.getInputStream();
        compareTwoZipFilesWithWorkbooks(expectedZipFile, bmAlgorithmFile);
    }

    @Test
    void prepareDataToCreateAgnoFile_shouldReturnPbcmAgnoFiles() throws IOException, ObjectValidationException {
        AlgorithmEvaluationConfigDTO algorithmEvaluationConfigDTO = new AlgorithmEvaluationConfigDTO();
        algorithmEvaluationConfigDTO.setDeliveryDate(DELIVERY_DATE);
        algorithmEvaluationConfigDTO.setAlgorithmType(AlgorithmType.PBCM);
        algorithmEvaluationConfigDTO.setKdmModelId(KDM_MODEL_ID);
        ProductDTO productDTO = ProductResourceIT.createDto(em);
        UnitDTO unitDTO = UnitResourceAdminIT.createUnitDto(em);
        Set<AuctionDayAheadOfferDTO> energyOffers = createdDayAhedOffers(productDTO, unitDTO, AuctionOfferType.CAPACITY);
        FileDTO pbcmAlgorithmFile = capacityAgnoAlgorithmService.getAlgorithmInputFiles(algorithmEvaluationConfigDTO, energyOffers);
        Resource templateFileResource = new ClassPathResource(CAPACITY_EXPECTED_ZIP_TEST_FILE);
        InputStream expectedZipFIle = templateFileResource.getInputStream();
        compareTwoZipFilesWithWorkbooks(expectedZipFIle, pbcmAlgorithmFile);
    }

    @Test
    void prepareDataToCreateDanoFile_shouldReturnDanoFiles() throws IOException, ObjectValidationException {
        AlgorithmEvaluationConfigDTO algorithmEvaluationConfigDTO = new AlgorithmEvaluationConfigDTO();
        algorithmEvaluationConfigDTO.setDeliveryDate(DELIVERY_DATE);
        algorithmEvaluationConfigDTO.setKdmModelId(KDM_MODEL_ID);
        ProductDTO productDTO = ProductResourceIT.createDto(em);
        UnitDTO unitDTO = UnitResourceAdminIT.createUnitDto(em);
        Set<AuctionDayAheadOfferDTO> capacityOffers = createdDayAhedOffers(productDTO, unitDTO, AuctionOfferType.ENERGY);
        FileDTO danoAlgorithmFile = energyDanoAlgorithmService.getAlgorithmInputFiles(algorithmEvaluationConfigDTO, capacityOffers);
        Resource templateFileResource = new ClassPathResource(DANO_EXPECTED_ZIP_TEST_FILE);
        InputStream expectedZipFile = templateFileResource.getInputStream();
        compareTwoZipFilesWithWorkbooks(expectedZipFile, danoAlgorithmFile);
    }

    private void compareTwoZipFilesWithWorkbooks(InputStream expectedZip, FileDTO fileDTO) throws IOException {
        List<FileDTO> expectedFileDTOs = ZipUtil.zipToFiles(expectedZip.readAllBytes());
        List<FileDTO> fileDTOs = ZipUtil.zipToFiles(fileDTO.getBytesData());
        Assertions.assertEquals(expectedFileDTOs.size(), fileDTOs.size());
        for (FileDTO expectedFile : expectedFileDTOs) {
            String expectedFilename = expectedFile.getFileName();
            FileDTO fileDto = fileDTOs.stream().filter(f -> f.getFileName().equals(expectedFilename)).findFirst().get();
            Assertions.assertNotNull(fileDto);
            XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(fileDto.getBytesData()));
            XSSFWorkbook expectedWorkbook = new XSSFWorkbook(new ByteArrayInputStream(expectedFile.getBytesData()));
            log.info("Start compare workbook with name {}: ", expectedFilename);
            verifyThatTwoWorkbookAreSame(expectedWorkbook, workbook);
        }
    }
}
