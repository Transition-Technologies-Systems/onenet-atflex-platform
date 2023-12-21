package pl.com.tt.flex.server.web.rest.auction.da;

import static pl.com.tt.flex.model.security.permission.Authority.FLEX_ADMIN_AUCTIONS_DAY_AHEAD_OFFER_CREATE;
import static pl.com.tt.flex.model.security.permission.Authority.FLEX_ADMIN_AUCTIONS_DAY_AHEAD_OFFER_DELETE;
import static pl.com.tt.flex.model.security.permission.Authority.FLEX_ADMIN_AUCTIONS_DAY_AHEAD_OFFER_EDIT;
import static pl.com.tt.flex.model.security.permission.Authority.FLEX_ADMIN_AUCTIONS_DAY_AHEAD_OFFER_VIEW;
import static pl.com.tt.flex.model.security.permission.Authority.FLEX_ADMIN_AUCTIONS_DAY_AHEAD_VIEW;
import static pl.com.tt.flex.model.security.permission.Authority.FLEX_ADMIN_AUCTIONS_OFFER_IMPORT;
import static pl.com.tt.flex.model.security.permission.Authority.FLEX_ADMIN_AUCTIONS_OFFER_VIEW;
import static pl.com.tt.flex.server.config.Constants.FLEX_ADMIN_APP_NAME;
import static pl.com.tt.flex.server.web.rest.errors.ErrorConstants.AUCTION_DA_OFFER_IMPORT_DELIVERY_PERIOD_NOT_SET;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.List;

import org.apache.commons.lang3.Range;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import io.github.jhipster.web.util.PaginationUtil;
import lombok.extern.slf4j.Slf4j;
import pl.com.tt.flex.model.service.dto.auction.da.AuctionDayAheadDTO;
import pl.com.tt.flex.model.service.dto.auction.offer.AuctionOfferDTO;
import pl.com.tt.flex.model.service.dto.auction.offer.da.AuctionDayAheadOfferDTO;
import pl.com.tt.flex.model.service.dto.auction.type.AuctionDayAheadType;
import pl.com.tt.flex.model.service.dto.auction.type.AuctionStatus;
import pl.com.tt.flex.model.service.dto.file.FileDTO;
import pl.com.tt.flex.model.service.dto.fsp.FspCompanyMinDTO;
import pl.com.tt.flex.server.common.errors.ObjectValidationException;
import pl.com.tt.flex.server.domain.auction.da.AuctionDayAheadEntity;
import pl.com.tt.flex.server.service.algorithm.disaggregationAlgorithm.DisaggregationAlgorithmService;
import pl.com.tt.flex.server.service.auction.da.AuctionDayAheadOfferQueryService;
import pl.com.tt.flex.server.service.auction.da.AuctionDayAheadQueryService;
import pl.com.tt.flex.server.service.auction.da.AuctionDayAheadService;
import pl.com.tt.flex.server.service.auction.da.dto.AuctionDayAheadCriteria;
import pl.com.tt.flex.server.service.auction.da.file.AuctionDayAheadOfferFileService;
import pl.com.tt.flex.server.service.auction.offer.dto.AuctionOfferCriteria;
import pl.com.tt.flex.server.service.product.dto.ProductNameMinDTO;
import pl.com.tt.flex.server.service.schedulingUnit.SchedulingUnitService;
import pl.com.tt.flex.server.validator.auction.da.AuctionDayAheadOfferImportFileValidator;
import pl.com.tt.flex.server.validator.auction.da.AuctionDayAheadOfferUpdateFileValidator;

/**
 * REST controller for managing {@link AuctionDayAheadEntity} for FLEX-ADMIN web module.
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/auctions-day-ahead")
public class AuctionDayAheadAdminResource extends AuctionDayAheadResource {

    private final DisaggregationAlgorithmService disaggregationAlgorithmService;


    public AuctionDayAheadAdminResource(AuctionDayAheadService auctionDayAheadService,
                                        AuctionDayAheadQueryService auctionDayAheadQueryService,
                                        AuctionDayAheadOfferQueryService offerQueryService,
                                        AuctionDayAheadOfferFileService auctionDayAheadOfferFileService,
                                        AuctionDayAheadOfferImportFileValidator auctionDayAheadOfferImportFileValidator,
                                        SchedulingUnitService schedulingUnitService,
                                        DisaggregationAlgorithmService disaggregationAlgorithmService,
                                        AuctionDayAheadOfferUpdateFileValidator auctionDayAheadOfferUpdateFileValidator) {
        super(auctionDayAheadService, auctionDayAheadQueryService, offerQueryService, auctionDayAheadOfferFileService, auctionDayAheadOfferImportFileValidator, auctionDayAheadOfferUpdateFileValidator, schedulingUnitService);
        this.disaggregationAlgorithmService = disaggregationAlgorithmService;
    }

    /**
     * {@code GET  /auctions-day-ahead} : get all the auctions.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of auctions in body.
     */
    @GetMapping
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_AUCTIONS_DAY_AHEAD_VIEW + "\")")
    public ResponseEntity<List<AuctionDayAheadDTO>> getAllDayAheadAuctions(AuctionDayAheadCriteria criteria, Pageable pageable) {
        log.debug("FLEX-ADMIN - REST request to get Auctions by criteria: {}", criteria);
        return super.getAllDayAheadAuctions(criteria, pageable);
    }

    /**
     * {@code GET  /auctions-day-ahead/:id} : get the "id" auction.
     *
     * @param id the id of the auctionDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the auctionDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_AUCTIONS_DAY_AHEAD_VIEW + "\")")
    public ResponseEntity<AuctionDayAheadDTO> getDayAheadAuction(@PathVariable Long id) {
        log.debug("FLEX-ADMIN - REST request to get Auction : {}", id);
        return super.getDayAheadAuction(id);
    }

    /**
     * {@code GET  /auctions-day-ahead/get-products-used-in-auction} : get all products used id auction DayAhed by auction type.
     *
     * @param auctionDayAheadType the type of the DayAhed Auction.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the productNameMinDTO}.
     */
    @GetMapping("/get-products-used-in-auction")
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_AUCTIONS_DAY_AHEAD_VIEW + "\")")
    public ResponseEntity<List<ProductNameMinDTO>> findAllProductsUsedInAuctionDayAheadByType(@RequestParam("type") AuctionDayAheadType auctionDayAheadType) {
        log.debug("FLEX-ADMIN - REST request to get all products used id auction type : {}", auctionDayAheadType);
        return super.findAllProductsUsedInAuctionDayAheadByType(auctionDayAheadType);
    }

    //********************************************************************************** OFFERS ************************************************************************************

    /**
     * {@code GET  /admin/auctions-day-ahead/offers} : get all the offers.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of offers in body.
     */
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_AUCTIONS_OFFER_VIEW + "\")")
    @GetMapping("/offers")
    public ResponseEntity<List<AuctionOfferDTO>> getAllOffers(AuctionOfferCriteria criteria, Pageable pageable) {
        log.debug("{} - REST request to get AuctionOffers by criteria: {}", FLEX_ADMIN_APP_NAME, criteria);
        Page<AuctionOfferDTO> page = offerQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code POST  /admin/auctions-day-ahead/offers} : Create a new offer.
     *
     * @param offerDTO the offerDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new offerDTO, or with status {@code 400 (Bad Request)} if the offer has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_AUCTIONS_DAY_AHEAD_OFFER_CREATE + "\")")
    @PostMapping("/offers")
    public ResponseEntity<AuctionDayAheadOfferDTO> createOffer(@RequestBody AuctionDayAheadOfferDTO offerDTO) throws URISyntaxException, ObjectValidationException {
        log.debug("{} - REST request to save AuctionDayAheadOffer : {}", FLEX_ADMIN_APP_NAME, offerDTO);
        AuctionStatus auctionStatus = auctionDayAheadService.findAuctionStatusById(offerDTO.getAuctionDayAhead().getId());
        return super.createOffer(offerDTO, auctionStatus);
    }

    /**
     * {@code PUT  /admin/auctions-day-ahead/offers} : Updates an existing offer.
     *
     * @param offerDTO the offerDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated offerDTO,
     * or with status {@code 400 (Bad Request)} if the offerDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the offerDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_AUCTIONS_DAY_AHEAD_OFFER_EDIT + "\")")
    @PutMapping("/offers")
    public ResponseEntity<AuctionDayAheadOfferDTO> updateOffer(@RequestBody AuctionDayAheadOfferDTO offerDTO) throws URISyntaxException, ObjectValidationException {
        log.debug("{} - REST request to update AuctionDayAheadOffer : {}", FLEX_ADMIN_APP_NAME, offerDTO);
        AuctionStatus auctionStatus = auctionDayAheadService.findAuctionStatusById(offerDTO.getAuctionDayAhead().getId());
        return super.updateOffer(offerDTO, auctionStatus);
    }

    /**
     * {@code GET  /admin/auctions/offers/:id} : get the "id" offer.
     *
     * @param id the id of the offerDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the offerDTO, or with status {@code 404 (Not Found)}.
     */
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_AUCTIONS_DAY_AHEAD_OFFER_VIEW + "\")")
    @GetMapping("/offers/{id}")
    public ResponseEntity<AuctionDayAheadOfferDTO> getOffer(@PathVariable Long id) {
        log.debug("{} - REST request to get AuctionDayAheadOffer : {}", FLEX_ADMIN_APP_NAME, id);
        return super.getOffer(id);
    }

    /**
     * {@code DELETE  /admin/auctions/offers/:id} : delete the "id" offer.
     *
     * @param id the id of the offerDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_AUCTIONS_DAY_AHEAD_OFFER_DELETE + "\")")
    @DeleteMapping("/offers/{id}")
    public ResponseEntity<Void> deleteOffer(@PathVariable Long id) throws ObjectValidationException {
        log.debug("{} - REST request to delete AuctionDayAheadOffer : {}", FLEX_ADMIN_APP_NAME, id);
        AuctionDayAheadOfferDTO offerDTO = auctionDayAheadService.findOfferById(id).get();
        AuctionStatus auctionStatus = auctionDayAheadService.findAuctionStatusById(offerDTO.getAuctionDayAhead().getId());
        return super.deleteOffer(id, auctionStatus);
    }

    /**
     * {@code GET  /admin/auctions-day-ahead/offers/get-reg-su-for-auction-da-offer} : get all the fsps minimal data of BSP having registered scheduling units for DA auction.
     *
     * @param productId the Product id of Scheduling unit to find
     * @param auctionId the Auction id of AuctionDayAhead to find
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of fsps in body.
     */
    @GetMapping("/offers/get-reg-su-for-auction-da-offer")
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_AUCTIONS_DAY_AHEAD_OFFER_CREATE + "\") or hasAuthority(\"" + FLEX_ADMIN_AUCTIONS_DAY_AHEAD_OFFER_EDIT + "\")")
    public ResponseEntity<List<FspCompanyMinDTO>> getBspsForAuctionDayAheadOffer(@RequestParam(value = "productId") Long productId,
                                                                                 @RequestParam(value = "auctionId") Long auctionId) {
        log.debug("REST request to get all the fsps minimal data of BSP having registered Scheduling units for Product: {} and Auction: {}", productId, auctionId);
        List<FspCompanyMinDTO> result = auctionDayAheadService.findBspsWithRegisteredSchedulingUnitsForProductAndAuction(productId, auctionId);
        if (result.isEmpty()) {
            ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok().body(result);
    }

    @GetMapping("/{auctionId}/offer-template")
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_AUCTIONS_DAY_AHEAD_OFFER_VIEW + "\")")
    public ResponseEntity<FileDTO> getTemplate(@PathVariable Long auctionId, Long schedulingUnitId) throws IOException {
        log.debug("FLEX_ADMIN - REST request to get DA offer import template");
        return super.getTemplate(auctionId, schedulingUnitId, null);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_AUCTIONS_DAY_AHEAD_OFFER_VIEW + "\")")
    @PostMapping("/{auctionId}/offer-import")
    public AuctionDayAheadOfferDTO importOffer(@PathVariable Long auctionId, @RequestPart(value = "file") MultipartFile multipartFile, Long schedulingUnitId,
                                               Long offerId, Instant deliveryPeriodFrom, Instant deliveryPeriodTo) throws ObjectValidationException, IOException {
        log.debug("FLEX_ADMIN - REST request to import day ahead offer file");
        Range<Instant> deliveryPeriod;
        try {
            deliveryPeriod = Range.between(deliveryPeriodFrom, deliveryPeriodTo);
        } catch (IllegalArgumentException e) {
            throw new ObjectValidationException("Delivery period not set", AUCTION_DA_OFFER_IMPORT_DELIVERY_PERIOD_NOT_SET);
        }
        return super.importOffer(multipartFile, auctionId, schedulingUnitId, offerId, null, deliveryPeriod);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_AUCTIONS_OFFER_IMPORT + "\")")
    @PostMapping("/offer-update-import")
    public void importBalancingMarketOfferUpdate(@RequestPart(value = "file") MultipartFile multipartFile) throws ObjectValidationException, IOException {
        log.debug("FLEX_ADMIN - REST request to import day ahead offer updates from agno comparison file");
        auctionDayAheadOfferUpdateFileValidator.checkOfferUpdateFileValid(multipartFile);
        disaggregationAlgorithmService.startOfferUpdateImport(multipartFile);
    }
    //********************************************************************************** OFFERS ************************************************************************************
}
