package pl.com.tt.flex.server.web.rest.auction.da;

import static pl.com.tt.flex.model.security.permission.Authority.FLEX_USER_AUCTIONS_DAY_AHEAD_OFFER_CREATE;
import static pl.com.tt.flex.model.security.permission.Authority.FLEX_USER_AUCTIONS_DAY_AHEAD_OFFER_DELETE;
import static pl.com.tt.flex.model.security.permission.Authority.FLEX_USER_AUCTIONS_DAY_AHEAD_OFFER_EDIT;
import static pl.com.tt.flex.model.security.permission.Authority.FLEX_USER_AUCTIONS_DAY_AHEAD_OFFER_VIEW;
import static pl.com.tt.flex.model.security.permission.Authority.FLEX_USER_AUCTIONS_DAY_AHEAD_VIEW;
import static pl.com.tt.flex.server.config.Constants.FLEX_USER_APP_NAME;
import static pl.com.tt.flex.server.web.rest.errors.ErrorConstants.AUCTION_DA_OFFER_IMPORT_DELIVERY_PERIOD_NOT_SET;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.List;

import javax.validation.constraints.NotNull;

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

import io.github.jhipster.service.filter.LongFilter;
import io.github.jhipster.web.util.PaginationUtil;
import lombok.extern.slf4j.Slf4j;
import pl.com.tt.flex.model.service.dto.auction.da.AuctionDayAheadDTO;
import pl.com.tt.flex.model.service.dto.auction.offer.AuctionOfferDTO;
import pl.com.tt.flex.model.service.dto.auction.offer.da.AuctionDayAheadOfferDTO;
import pl.com.tt.flex.model.service.dto.auction.type.AuctionDayAheadType;
import pl.com.tt.flex.model.service.dto.auction.type.AuctionStatus;
import pl.com.tt.flex.model.service.dto.file.FileDTO;
import pl.com.tt.flex.server.common.errors.ObjectValidationException;
import pl.com.tt.flex.server.domain.auction.da.AuctionDayAheadEntity;
import pl.com.tt.flex.server.domain.fsp.FspEntity;
import pl.com.tt.flex.server.domain.user.UserEntity;
import pl.com.tt.flex.server.service.auction.da.AuctionDayAheadOfferQueryService;
import pl.com.tt.flex.server.service.auction.da.AuctionDayAheadQueryService;
import pl.com.tt.flex.server.service.auction.da.AuctionDayAheadService;
import pl.com.tt.flex.server.service.auction.da.dto.AuctionDayAheadCriteria;
import pl.com.tt.flex.server.service.auction.da.file.AuctionDayAheadOfferFileService;
import pl.com.tt.flex.server.service.auction.offer.dto.AuctionOfferCriteria;
import pl.com.tt.flex.server.service.auction.offer.dto.AuctionReminderDTO;
import pl.com.tt.flex.server.service.fsp.FspService;
import pl.com.tt.flex.server.service.product.dto.ProductNameMinDTO;
import pl.com.tt.flex.server.service.schedulingUnit.SchedulingUnitService;
import pl.com.tt.flex.server.service.user.UserService;
import pl.com.tt.flex.server.validator.auction.da.AuctionDayAheadOfferImportFileValidator;
import pl.com.tt.flex.server.validator.auction.da.AuctionDayAheadOfferUpdateFileValidator;

/**
 * REST controller for managing {@link AuctionDayAheadEntity} for FLEX-USER web module.
 */
@Slf4j
@RestController
@RequestMapping("/api/user")
public class AuctionDayAheadUserResource extends AuctionDayAheadResource {

    private final FspService fspService;
    private final UserService userService;

    public AuctionDayAheadUserResource(AuctionDayAheadService auctionDayAheadService,
                                       AuctionDayAheadQueryService auctionDayAheadQueryService,
                                       AuctionDayAheadOfferQueryService offerQueryService,
                                       FspService fspService,
                                       UserService userService,
                                       AuctionDayAheadOfferFileService auctionDayAheadOfferFileService,
                                       AuctionDayAheadOfferImportFileValidator auctionDayAheadOfferImportFileValidator,
                                       SchedulingUnitService schedulingUnitService,
                                       AuctionDayAheadOfferUpdateFileValidator auctionDayAheadOfferUpdateFileValidator) {
        super(auctionDayAheadService, auctionDayAheadQueryService, offerQueryService, auctionDayAheadOfferFileService, auctionDayAheadOfferImportFileValidator, auctionDayAheadOfferUpdateFileValidator, schedulingUnitService);
        this.fspService = fspService;
        this.userService = userService;
    }

    /**
     * {@code GET  /auctions-day-ahead} : get all the auctions.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of auctions in body.
     */
    @GetMapping("/auctions-day-ahead")
    @PreAuthorize("hasAuthority(\"" + FLEX_USER_AUCTIONS_DAY_AHEAD_VIEW + "\")")
    public ResponseEntity<List<AuctionDayAheadDTO>> getAllDayAheadAuctions(AuctionDayAheadCriteria criteria, Pageable pageable) {
        log.debug("FLEX-USER - REST request to get Auctions by criteria: {}", criteria);
        return super.getAllDayAheadAuctions(criteria, pageable);
    }

    /**
     * {@code GET  /auctions-day-ahead/:id} : get the "id" auction.
     *
     * @param id the id of the auctionDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the auctionDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/auctions-day-ahead/{id}")
    @PreAuthorize("hasAuthority(\"" + FLEX_USER_AUCTIONS_DAY_AHEAD_VIEW + "\")")
    public ResponseEntity<AuctionDayAheadDTO> getDayAheadAuction(@PathVariable Long id) {
        log.debug("FLEX-USER - REST request to get Auction : {}", id);
        return super.getDayAheadAuction(id);
    }

    /**
     * {@code GET  /auctions-day-ahead/get-products-used-in-auction} : get all products used id auction DayAhed by auction type.
     *
     * @param auctionDayAheadType the type of the DayAhed Auction.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the productNameMinDTO}.
     */
    @GetMapping("/auctions-day-ahead/get-products-used-in-auction")
    @PreAuthorize("hasAuthority(\"" + FLEX_USER_AUCTIONS_DAY_AHEAD_VIEW + "\")")
    public ResponseEntity<List<ProductNameMinDTO>> findAllProductsUsedInAuctionDayAheadByType(@RequestParam("type") AuctionDayAheadType auctionDayAheadType) {
        log.debug("FLEX-USER - REST request to get all products used id auction type : {}", auctionDayAheadType);
        return super.findAllProductsUsedInAuctionDayAheadByType(auctionDayAheadType);
    }
    //********************************************************************************** OFFERS ************************************************************************************

    /**
     * {@code GET  /user/auctions-day-ahead/offers} : get all the offers.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of offers in body.
     */
    @PreAuthorize("hasAuthority(\"" + FLEX_USER_AUCTIONS_DAY_AHEAD_OFFER_VIEW + "\")")
    @GetMapping("/auctions-day-ahead/offers")
    public ResponseEntity<List<AuctionOfferDTO>> getAllOffers(AuctionOfferCriteria criteria, Pageable pageable) {
        log.debug("{} - REST request to get AuctionOffers by criteria: {}", FLEX_USER_APP_NAME, criteria);
        FspEntity fspEntity = getCurrentLoggedFsp();
        criteria.setBspId((LongFilter) new LongFilter().setEquals(fspEntity.getId()));
        Page<AuctionOfferDTO> page = offerQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code POST  /user/auctions-day-ahead/offers} : Create a new offer.
     *
     * @param offerDTO the offerDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new offerDTO, or with status {@code 400 (Bad Request)} if the offer has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PreAuthorize("hasAuthority(\"" + FLEX_USER_AUCTIONS_DAY_AHEAD_OFFER_CREATE + "\")")
    @PostMapping("/auctions-day-ahead/offers")
    public ResponseEntity<AuctionDayAheadOfferDTO> createOffer(@RequestBody AuctionDayAheadOfferDTO offerDTO) throws URISyntaxException, ObjectValidationException {
        log.debug("{} - REST request to save AuctionDayAheadOffer : {}", FLEX_USER_APP_NAME, offerDTO);
        AuctionStatus auctionStatus = auctionDayAheadService.findAuctionStatusById(offerDTO.getAuctionDayAhead().getId());
        return super.createOffer(offerDTO, auctionStatus);
    }

    /**
     * {@code PUT  /user/auctions-day-ahead/offers} : Updates an existing offer.
     *
     * @param offerDTO the offerDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated offerDTO,
     * or with status {@code 400 (Bad Request)} if the offerDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the offerDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PreAuthorize("hasAuthority(\"" + FLEX_USER_AUCTIONS_DAY_AHEAD_OFFER_EDIT + "\")")
    @PutMapping("/auctions-day-ahead/offers")
    public ResponseEntity<AuctionDayAheadOfferDTO> updateOffer(@RequestBody AuctionDayAheadOfferDTO offerDTO) throws URISyntaxException, ObjectValidationException {
        log.debug("{} - REST request to update AuctionDayAheadOffer : {}", FLEX_USER_APP_NAME, offerDTO);
        AuctionStatus auctionStatus = auctionDayAheadService.findAuctionStatusById(offerDTO.getAuctionDayAhead().getId());
        return super.updateOffer(offerDTO, auctionStatus);
    }

    /**
     * {@code GET  /user/auctions-day-ahead/offers/:id} : get the "id" offer.
     *
     * @param id the id of the offerDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the offerDTO, or with status {@code 404 (Not Found)}.
     */
    @PreAuthorize("hasAuthority(\"" + FLEX_USER_AUCTIONS_DAY_AHEAD_OFFER_VIEW + "\")")
    @GetMapping("/auctions-day-ahead/offers/{id}")
    public ResponseEntity<AuctionDayAheadOfferDTO> getOffer(@PathVariable Long id) {
        log.debug("{} - REST request to get AuctionDayAheadOffer : {}", FLEX_USER_APP_NAME, id);
        return super.getOffer(id);
    }

    /**
     * {@code GET  /user/auctions-day-ahead/offers/reminder} : get reminder for current logged user to take part in Energy auction .
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the AuctionReminderDTO.
     */
    @PreAuthorize("hasAuthority(\"" + FLEX_USER_AUCTIONS_DAY_AHEAD_OFFER_CREATE + "\")")
    @GetMapping("/auctions-day-ahead/offers/reminder")
    public ResponseEntity<AuctionReminderDTO> getOfferReminder() {
        FspEntity fsp = getCurrentLoggedFsp();
        log.debug("{} - REST request to get reminder informing about the obligation to take part in balancing energy auction", FLEX_USER_APP_NAME);
        return ResponseEntity.ok().body(auctionDayAheadService.getAuctionOfferReminderForBsp(fsp.getId()));
    }

    /**
     * {@code DELETE  /user/auctions-day-ahead/offers/:id} : delete the "id" offer.
     *
     * @param id the id of the offerDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @PreAuthorize("hasAuthority(\"" + FLEX_USER_AUCTIONS_DAY_AHEAD_OFFER_DELETE + "\")")
    @DeleteMapping("/auctions-day-ahead/offers/{id}")
    public ResponseEntity<Void> deleteOffer(@PathVariable Long id) throws ObjectValidationException {
        log.debug("{} - REST request to delete AuctionDayAheadOffer : {}", FLEX_USER_APP_NAME, id);
        AuctionDayAheadOfferDTO offerDTO = auctionDayAheadService.findOfferById(id).get();
        AuctionStatus auctionStatus = auctionDayAheadService.findAuctionStatusById(offerDTO.getAuctionDayAhead().getId());
        return super.deleteOffer(id, auctionStatus);
    }

    @GetMapping("/auctions-day-ahead/{auctionId}/offer-template")
    @PreAuthorize("hasAuthority(\"" + FLEX_USER_AUCTIONS_DAY_AHEAD_OFFER_VIEW + "\")")
    public ResponseEntity<FileDTO> getTemplate(@PathVariable Long auctionId, Long schedulingUnitId) throws IOException {
        log.debug("FLEX_USER - REST request to get DA offer import template");
        FspEntity fsp = getCurrentLoggedFsp();
        return super.getTemplate(auctionId, schedulingUnitId, fsp.getId());
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority(\"" + FLEX_USER_AUCTIONS_DAY_AHEAD_OFFER_VIEW + "\")")
    @PostMapping("/auctions-day-ahead/{auctionId}/offer-import")
    public AuctionDayAheadOfferDTO importSelfScheduleDer(@PathVariable Long auctionId, @RequestPart(value = "file") MultipartFile multipartFile, Long offerId, Long schedulingUnitId,
                                                         @NotNull Instant deliveryPeriodFrom, @NotNull Instant deliveryPeriodTo) throws ObjectValidationException, IOException {
        log.debug("FLEX_USER - REST request to import day ahead offer file");
        FspEntity fsp = getCurrentLoggedFsp();
        Range<Instant> deliveryPeriod;
        try {
            deliveryPeriod = Range.between(deliveryPeriodFrom, deliveryPeriodTo);
        } catch (IllegalArgumentException e) {
            throw new ObjectValidationException("Delivery period not set", AUCTION_DA_OFFER_IMPORT_DELIVERY_PERIOD_NOT_SET);
        }
        return super.importOffer(multipartFile, auctionId, schedulingUnitId, offerId, fsp.getId(), deliveryPeriod);
    }

    private FspEntity getCurrentLoggedFsp() {
        UserEntity maybefspUser = userService.getCurrentUser();
        return fspService.findFspOfUser(maybefspUser.getId(), maybefspUser.getLogin())
            .orElseThrow(() -> new AuctionDayAheadResourceException("Cannot find Fsp by user id: " + maybefspUser.getId()));
    }
    //********************************************************************************** OFFERS ************************************************************************************
}
