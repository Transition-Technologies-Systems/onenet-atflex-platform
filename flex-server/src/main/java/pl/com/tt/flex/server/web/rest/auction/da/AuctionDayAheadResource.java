package pl.com.tt.flex.server.web.rest.auction.da;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.Range;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import io.github.jhipster.service.filter.InstantFilter;
import io.github.jhipster.web.util.HeaderUtil;
import io.github.jhipster.web.util.PaginationUtil;
import io.github.jhipster.web.util.ResponseUtil;
import lombok.extern.slf4j.Slf4j;
import pl.com.tt.flex.model.service.dto.auction.da.AuctionDayAheadDTO;
import pl.com.tt.flex.model.service.dto.auction.offer.da.AuctionDayAheadOfferDTO;
import pl.com.tt.flex.model.service.dto.auction.type.AuctionDayAheadType;
import pl.com.tt.flex.model.service.dto.auction.type.AuctionStatus;
import pl.com.tt.flex.model.service.dto.file.FileDTO;
import pl.com.tt.flex.server.common.errors.ObjectValidationException;
import pl.com.tt.flex.server.domain.auction.da.AuctionDayAheadEntity;
import pl.com.tt.flex.server.service.auction.da.AuctionDayAheadOfferQueryService;
import pl.com.tt.flex.server.service.auction.da.AuctionDayAheadQueryService;
import pl.com.tt.flex.server.service.auction.da.AuctionDayAheadService;
import pl.com.tt.flex.server.service.auction.da.dto.AuctionDayAheadCriteria;
import pl.com.tt.flex.server.service.auction.da.file.AuctionDayAheadOfferFileService;
import pl.com.tt.flex.server.service.product.dto.ProductNameMinDTO;
import pl.com.tt.flex.server.service.schedulingUnit.SchedulingUnitService;
import pl.com.tt.flex.server.util.InstantUtil;
import pl.com.tt.flex.server.validator.auction.da.AuctionDayAheadOfferImportFileValidator;
import pl.com.tt.flex.server.validator.auction.da.AuctionDayAheadOfferUpdateFileValidator;
import pl.com.tt.flex.server.web.rest.errors.BadRequestAlertException;

/**
 * REST controller for managing {@link AuctionDayAheadEntity}.
 */
@Slf4j
@RestController
@RequestMapping("/api")
public class AuctionDayAheadResource {

    protected static final String ENTITY_NAME = "auctionDayAhead";
    public static final String OFFER_ENTITY_NAME = "auctionOffer";

    @Value("${jhipster.clientApp.name}")
    protected String applicationName;

    protected final AuctionDayAheadService auctionDayAheadService;
    protected final AuctionDayAheadQueryService auctionDayAheadQueryService;
    protected final AuctionDayAheadOfferQueryService offerQueryService;
    protected final AuctionDayAheadOfferFileService auctionDayAheadOfferFileService;
    protected final AuctionDayAheadOfferImportFileValidator auctionDayAheadOfferImportFileValidator;
    protected final AuctionDayAheadOfferUpdateFileValidator auctionDayAheadOfferUpdateFileValidator;
    private final SchedulingUnitService schedulingUnitService;

    public AuctionDayAheadResource(AuctionDayAheadService auctionDayAheadService,
                                   AuctionDayAheadQueryService auctionDayAheadQueryService,
                                   AuctionDayAheadOfferQueryService offerQueryService,
                                   AuctionDayAheadOfferFileService auctionDayAheadOfferFileService,
                                   AuctionDayAheadOfferImportFileValidator auctionDayAheadOfferImportFileValidator,
                                   AuctionDayAheadOfferUpdateFileValidator auctionDayAheadOfferUpdateFileValidator,
                                   SchedulingUnitService schedulingUnitService) {
        this.auctionDayAheadService = auctionDayAheadService;
        this.auctionDayAheadQueryService = auctionDayAheadQueryService;
        this.offerQueryService = offerQueryService;
        this.auctionDayAheadOfferFileService = auctionDayAheadOfferFileService;
        this.auctionDayAheadOfferImportFileValidator = auctionDayAheadOfferImportFileValidator;
        this.auctionDayAheadOfferUpdateFileValidator = auctionDayAheadOfferUpdateFileValidator;
        this.schedulingUnitService = schedulingUnitService;
    }

    protected ResponseEntity<List<AuctionDayAheadDTO>> getAllDayAheadAuctions(AuctionDayAheadCriteria criteria, Pageable pageable) {
        //nie wyswietlamy utworzonych aukcji z data na przyszle dni
        //dzien aukcji (auction_day) jest trzymany w bazie jako poczatek dnia lokalnego czasu przekonwertowany na UTC
        //np. 22/02/11 23:00:00,000000000 gdy aukcja zostala utworzona 12 lutego czasu polskiego
        criteria.setDay(new InstantFilter().setLessThan(InstantUtil.now()));
        Page<AuctionDayAheadDTO> page = auctionDayAheadQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    protected ResponseEntity<AuctionDayAheadDTO> getDayAheadAuction(Long id) {
        Optional<AuctionDayAheadDTO> auctionDTO = auctionDayAheadService.findById(id);
        return ResponseUtil.wrapOrNotFound(auctionDTO);
    }

    protected ResponseEntity<List<ProductNameMinDTO>> findAllProductsUsedInAuctionDayAheadByType(AuctionDayAheadType auctionDayAheadType) {
        List<ProductNameMinDTO> productsName = auctionDayAheadService.findAllProductsUsedInAuctionDayAheadByType(auctionDayAheadType);
        return ResponseEntity.ok().body(productsName);
    }

    //********************************************************************************** OFFERS ************************************************************************************
    protected ResponseEntity<AuctionDayAheadOfferDTO> createOffer(AuctionDayAheadOfferDTO offerDTO, AuctionStatus auctionStatus) throws URISyntaxException, ObjectValidationException {
        if (offerDTO.getId() != null) {
            throw new BadRequestAlertException("A new offer cannot already have an ID", ENTITY_NAME, "idexists");
        }
        AuctionDayAheadOfferDTO result = auctionDayAheadService.saveOffer(offerDTO, auctionStatus, true);
        return ResponseEntity.created(new URI("/api/auctions/offers/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, OFFER_ENTITY_NAME, result.getId().toString())).body(result);
    }

    protected ResponseEntity<AuctionDayAheadOfferDTO> updateOffer(AuctionDayAheadOfferDTO offerDTO, AuctionStatus auctionStatus) throws URISyntaxException, ObjectValidationException {
        if (offerDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", OFFER_ENTITY_NAME, "idnull");
        }
        AuctionDayAheadOfferDTO result = auctionDayAheadService.saveOffer(offerDTO, auctionStatus, true);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, OFFER_ENTITY_NAME, offerDTO.getId().toString())).body(result);
    }

    protected ResponseEntity<AuctionDayAheadOfferDTO> getOffer(Long id) {
        Optional<AuctionDayAheadOfferDTO> offerDTO = auctionDayAheadService.findOfferById(id);
        return ResponseUtil.wrapOrNotFound(offerDTO);
    }

    protected ResponseEntity<Void> deleteOffer(Long id, AuctionStatus auctionStatus) throws ObjectValidationException {
        auctionDayAheadService.deleteOffer(id, auctionStatus);
        return ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, OFFER_ENTITY_NAME, id.toString())).build();
    }

    protected ResponseEntity<FileDTO> getTemplate(Long auctionId, Long schedulingUnitId, Long fspId) throws IOException {
        return ResponseEntity.ok().body(auctionDayAheadOfferFileService.getOfferImportTemplate(auctionId, schedulingUnitId, fspId));
    }

    protected AuctionDayAheadOfferDTO importOffer(@RequestPart(value = "file") MultipartFile multipartFile, Long auctionId, Long schedulingUnitId, Long offerId, Long fspId, Range<Instant> deliveryPeriod) throws IOException, ObjectValidationException {
        var dbAction = auctionDayAheadService.findById(auctionId)
            .orElseThrow(() -> new RuntimeException("Cannot find day ahead auction with id: " + auctionId));
        var dbSchedulingUnit = Optional.ofNullable(fspId).map(id -> schedulingUnitService.findByIdAndBspId(schedulingUnitId, id))
            .orElse(schedulingUnitService.findById(schedulingUnitId))
            .orElseThrow(() -> new RuntimeException("Cannot find scheduling unit with id: " + schedulingUnitId));
        auctionDayAheadOfferImportFileValidator.checkOfferImportFileValid(multipartFile, dbAction, dbSchedulingUnit);
        return auctionDayAheadOfferFileService.importDayAheadOffer(multipartFile, dbAction, dbSchedulingUnit, offerId, deliveryPeriod);
    }
    //********************************************************************************** OFFERS ************************************************************************************

    protected static class AuctionDayAheadResourceException extends RuntimeException {
        protected AuctionDayAheadResourceException(String message) {
            super(message);
        }
    }
}
