package pl.com.tt.flex.server.dataexport.exporter.offer;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.MessageSource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import pl.com.tt.flex.model.service.dto.auction.da.AuctionDayAheadMinDTO;
import pl.com.tt.flex.model.service.dto.auction.offer.AuctionOfferViewDTO;
import pl.com.tt.flex.model.service.dto.auction.offer.da.AuctionDayAheadOfferDTO;
import pl.com.tt.flex.model.service.dto.auction.offer.da.AuctionOfferBandDataDTO;
import pl.com.tt.flex.model.service.dto.auction.offer.da.AuctionOfferDersDTO;
import pl.com.tt.flex.model.service.dto.auction.type.AuctionOfferType;
import pl.com.tt.flex.model.service.dto.der.DerMinDTO;
import pl.com.tt.flex.model.service.dto.product.ProductMinDTO;
import pl.com.tt.flex.model.service.dto.product.type.Direction;
import pl.com.tt.flex.model.service.dto.schedulingUnit.SchedulingUnitMinDTO;
import pl.com.tt.flex.server.FlexserverApp;
import pl.com.tt.flex.server.common.errors.ObjectValidationException;
import pl.com.tt.flex.server.dataexport.exporter.offer.detail.DaOfferDetailExporter;
import pl.com.tt.flex.server.service.auction.da.AuctionDayAheadService;
import pl.com.tt.flex.server.service.product.dto.ProductDTO;
import pl.com.tt.flex.server.service.unit.dto.UnitDTO;
import pl.com.tt.flex.server.web.rest.product.ProductResourceIT;
import pl.com.tt.flex.server.web.rest.unit.UnitResourceAdminIT;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.*;

import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static pl.com.tt.flex.server.util.WorkbookUtils.verifyThatTwoWorkbookAreSame;


@SpringBootTest(classes = FlexserverApp.class)
@Transactional
@Slf4j
class BidsEvaluationExporterTest {

    private final String EXPECTED_DAY_AHEAD_OFFERS_DETAILS_SHEET = "/templates/export/offers/daOffersDetails.xlsx";

    private final int OFFER_START_HOUR = 5;
    private final int OFFER_LAST_HOUR = 10;
    private final List<Integer> ENERGY_OFFER_BANDS = Arrays.asList(-2, -1, 0, 1, 2, 3);
    private final List<Integer> CAPACITY_OFFER_BANDS = Arrays.asList(-1, 0);

    private final BigDecimal BAND_PRICE = BigDecimal.valueOf(200.31);
    private final BigDecimal ACCEPTED_BAND_PRICE = BigDecimal.valueOf(190.31);
    private final BigDecimal BAND_VOLUME = BigDecimal.valueOf(30);
    private final BigDecimal BAND_ACCEPTED_VOLUME = BigDecimal.valueOf(20);

    @MockBean
    protected AuctionDayAheadService mockDayAheadService;
    @MockBean
    protected MessageSource messageSource;

    @Autowired
    EntityManager em;
    private DaOfferDetailExporter daOfferDetailExporter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        ProductDTO productDTO = ProductResourceIT.createDto(em);
        UnitDTO unitDTO = UnitResourceAdminIT.createUnitDto(em);

        this.daOfferDetailExporter = new DaOfferDetailExporter(mockDayAheadService, messageSource);

        long offerId = 1L;
        Set<AuctionDayAheadOfferDTO> energyOffers = createdDayAhedOffers(productDTO, unitDTO, AuctionOfferType.ENERGY, offerId++);
        Set<AuctionDayAheadOfferDTO> capacityOffers = createdDayAhedOffers(productDTO, unitDTO, AuctionOfferType.CAPACITY, offerId++);

        HashSet<AuctionDayAheadOfferDTO> allOffers = new HashSet<>();
        allOffers.addAll(energyOffers);
        allOffers.addAll(capacityOffers);

        Mockito.when(mockDayAheadService.findAllOffersById(anyList())).thenReturn(allOffers);
        Mockito.when(messageSource.getMessage(any(), any(), any())).thenReturn("test");
    }

    @NotNull
    private Set<AuctionDayAheadOfferDTO> createdDayAhedOffers(ProductDTO productDTO, UnitDTO unitDTO, AuctionOfferType offerType, Long offerId) {
        AuctionDayAheadOfferDTO auctionDayAheadOfferDTO = new AuctionDayAheadOfferDTO();
        auctionDayAheadOfferDTO.setId(offerId);
        auctionDayAheadOfferDTO.setType(offerType);
        setSchedulingUnitInOffer(auctionDayAheadOfferDTO);
        setAuctionInOffer(productDTO, offerType, auctionDayAheadOfferDTO);
        setDersInOffer(unitDTO, offerType, auctionDayAheadOfferDTO);
        return new HashSet<>(singletonList(auctionDayAheadOfferDTO));
    }

    private void setSchedulingUnitInOffer(AuctionDayAheadOfferDTO auctionDayAheadOfferDTO) {
        SchedulingUnitMinDTO schedulingUnitMinDTO = new SchedulingUnitMinDTO();
        schedulingUnitMinDTO.setId(1L);
        schedulingUnitMinDTO.setName("SU-NAME");
        auctionDayAheadOfferDTO.setSchedulingUnit(schedulingUnitMinDTO);
    }

    private void setDersInOffer(UnitDTO unitDTO, AuctionOfferType offerType, AuctionDayAheadOfferDTO auctionDayAheadOfferDTO) {
        AuctionOfferDersDTO auctionOfferDersDTO = new AuctionOfferDersDTO();

        DerMinDTO firstDer = new DerMinDTO();
        firstDer.setName("DER1");
        firstDer.setSourcePower(unitDTO.getSourcePower());
        auctionOfferDersDTO.setDer(firstDer);

        DerMinDTO secondDer = new DerMinDTO();
        secondDer.setName("DER2");
        secondDer.setSourcePower(unitDTO.getSourcePower());
        auctionOfferDersDTO.setDer(secondDer);

        List<AuctionOfferBandDataDTO> bandList = new ArrayList<>();
        List<Integer> offerBandList = offerType.equals(AuctionOfferType.ENERGY) ? ENERGY_OFFER_BANDS : CAPACITY_OFFER_BANDS;
        offerBandList.forEach(b -> bandList.addAll(getBandHours(b)));

        auctionOfferDersDTO.setBandData(bandList);
        auctionDayAheadOfferDTO.setDers(singletonList(auctionOfferDersDTO));
    }

    private void setAuctionInOffer(ProductDTO productDTO, AuctionOfferType offerType, AuctionDayAheadOfferDTO auctionDayAheadOfferDTO) {
        AuctionDayAheadMinDTO auctionDayAheadMinDTO = new AuctionDayAheadMinDTO();
        ProductMinDTO productMinDTO = new ProductMinDTO();
        Direction direction = offerType.equals(AuctionOfferType.ENERGY) ? Direction.UNDEFINED : Direction.DOWN;
        productMinDTO.setDirection(direction);
        productMinDTO.setShortName(productDTO.getShortName());
        auctionDayAheadMinDTO.setProduct(productMinDTO);
        auctionDayAheadOfferDTO.setAuctionDayAhead(auctionDayAheadMinDTO);
    }

    @NotNull
    private List<AuctionOfferBandDataDTO> getBandHours(int bandNumber) {
        List<AuctionOfferBandDataDTO> bandList = new ArrayList<>();
        for (int i = OFFER_START_HOUR; i <= OFFER_LAST_HOUR; i++) {
            AuctionOfferBandDataDTO auctionOfferBandDataDTO = new AuctionOfferBandDataDTO();
            auctionOfferBandDataDTO.setBandNumber(bandNumber);
            auctionOfferBandDataDTO.setHourNumber(String.valueOf(i));
            auctionOfferBandDataDTO.setPrice(bandNumber == 0 ? null : BAND_PRICE);
            auctionOfferBandDataDTO.setAcceptedPrice(bandNumber == 0 ? null : ACCEPTED_BAND_PRICE);
            auctionOfferBandDataDTO.setVolume(BAND_VOLUME);
            auctionOfferBandDataDTO.setAcceptedVolume(BAND_ACCEPTED_VOLUME);
            auctionOfferBandDataDTO.setEdited(true);
            bandList.add(auctionOfferBandDataDTO);
        }
        return bandList;
    }

    @Test
    void fillOfferDetailSheet_shouldReturnOfferDetailWithPrice() throws IOException, ObjectValidationException {
        byte[] generatedSheet = daOfferDetailExporter.fillOfferDetailSheet(new XSSFWorkbook(), singletonList(new AuctionOfferViewDTO()), Locale.forLanguageTag("pl"));
        Resource templateFileResource = new ClassPathResource(EXPECTED_DAY_AHEAD_OFFERS_DETAILS_SHEET);
        InputStream expectedSheet = templateFileResource.getInputStream();
        XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(generatedSheet));
        XSSFWorkbook expectedWorkbook = new XSSFWorkbook(new ByteArrayInputStream(expectedSheet.readAllBytes()));
        verifyThatTwoWorkbookAreSame(expectedWorkbook, workbook);
    }
}

