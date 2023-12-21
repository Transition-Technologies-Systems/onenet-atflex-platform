package pl.com.tt.flex.server.web.rest.auction.cmvc;

import io.github.jhipster.web.util.PaginationUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pl.com.tt.flex.model.service.dto.auction.cmvc.AuctionCmvcDTO;
import pl.com.tt.flex.model.service.dto.auction.offer.cmvc.AuctionCmvcOfferDTO;
import pl.com.tt.flex.model.service.dto.auction.offer.AuctionOfferDTO;
import pl.com.tt.flex.model.service.dto.auction.type.AuctionStatus;
import pl.com.tt.flex.model.service.dto.fsp.FspCompanyMinDTO;
import pl.com.tt.flex.model.service.dto.potential.FlexPotentialMinDTO;
import pl.com.tt.flex.server.common.errors.ObjectValidationException;
import pl.com.tt.flex.server.domain.auction.cmvc.AuctionCmvcEntity;
import pl.com.tt.flex.server.service.auction.cmvc.AuctionCmvcService;
import pl.com.tt.flex.server.service.auction.cmvc.dto.AuctionCmvcViewCriteria;
import pl.com.tt.flex.server.service.auction.cmvc.view.AuctionCmvcViewQueryService;
import pl.com.tt.flex.server.service.auction.cmvc.AuctionCmvcOfferQueryService;
import pl.com.tt.flex.server.service.auction.offer.dto.AuctionOfferCriteria;
import pl.com.tt.flex.server.validator.auction.cmvc.AuctionsCmvcValidator;

import javax.validation.Valid;
import java.net.URISyntaxException;
import java.util.List;

import static pl.com.tt.flex.model.security.permission.Authority.*;
import static pl.com.tt.flex.server.config.Constants.FLEX_ADMIN_APP_NAME;

/**
 * REST controller for managing {@link AuctionCmvcEntity}.
 */
@Slf4j
@RestController
@RequestMapping("/api/admin")
public class AuctionCmvcResourceAdmin extends AuctionCmvcResource {

    private final AuctionCmvcOfferQueryService offerQueryService;

    public AuctionCmvcResourceAdmin(AuctionCmvcService auctionCmvcService, AuctionCmvcViewQueryService auctionCmvcViewQueryService, AuctionsCmvcValidator auctionsCmvcValidator, AuctionCmvcOfferQueryService offerQueryService) {
        super(auctionCmvcService, auctionCmvcViewQueryService, auctionsCmvcValidator, offerQueryService);
        this.offerQueryService = offerQueryService;
    }

    /**
     * {@code POST  /auctions-cmvc} : Create a new auctionCmvc.
     *
     * @param auctionCmvcDTO the auctionCmvcDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new auctionCmvcDTO, or with status {@code 400 (Bad Request)} if the auctionCmvc has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_AUCTIONS_CMVC_MANAGE + "\")")
    @PostMapping("/auctions-cmvc")
    public ResponseEntity<AuctionCmvcDTO> createAuctionCmvc(@Valid @RequestBody AuctionCmvcDTO auctionCmvcDTO) throws URISyntaxException, ObjectValidationException {
        log.debug("FLEX-ADMIN - REST request to save AuctionCmvc : {}", auctionCmvcDTO);
        return super.createAuctionCmvc(auctionCmvcDTO);
    }

    /**
     * {@code PUT  /auctions-cmvc} : Updates an existing auctionCmvc.
     *
     * @param auctionCmvcDTO the auctionCmvcDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated auctionCmvcDTO,
     * or with status {@code 400 (Bad Request)} if the auctionCmvcDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the auctionCmvcDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_AUCTIONS_CMVC_MANAGE + "\")")
    @PutMapping("/auctions-cmvc")
    public ResponseEntity<AuctionCmvcDTO> updateAuctionCmvc(@Valid @RequestBody AuctionCmvcDTO auctionCmvcDTO) throws URISyntaxException, ObjectValidationException {
        log.debug("FLEX-ADMIN - REST request to update AuctionCmvc : {}", auctionCmvcDTO);
        return super.updateAuctionCmvc(auctionCmvcDTO);
    }

    /**
     * {@code GET  /auctions-cmvc} : get all the auctionCmvc.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of auctionCmvc in body.
     */
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_AUCTIONS_CMVC_VIEW + "\")")
    @GetMapping("/auctions-cmvc")
    public ResponseEntity<List<AuctionCmvcDTO>> getAllAuctionsCmvc(AuctionCmvcViewCriteria criteria, Pageable pageable) {
        log.debug("FLEX-ADMIN - REST request to get AuctionCmvc by criteria: {}", criteria);
        return super.getAllAuctionsCmvc(criteria, pageable);
    }

    /**
     * {@code GET  /auctions-cmvc/:id} : get the "id" auctionCmvc.
     *
     * @param id the id of the auctionCmvcDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the auctionCmvcDTO, or with status {@code 404 (Not Found)}.
     */
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_AUCTIONS_CMVC_VIEW + "\")")
    @GetMapping("/auctions-cmvc/{id}")
    public ResponseEntity<AuctionCmvcDTO> getAuctionCmvc(@PathVariable Long id) {
        log.debug("FLEX-ADMIN - REST request to get AuctionCmvc : {}", id);
        return super.getAuctionCmvc(id);
    }

    /**
     * {@code DELETE  /auctions-cmvc/:id} : delete the "id" auctionCmvc.
     *
     * @param id the id of the auctionCmvcDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_AUCTIONS_CMVC_DELETE + "\")")
    @DeleteMapping("/auctions-cmvc/{id}")
    public ResponseEntity<Void> deleteAuctionCmvc(@PathVariable Long id) throws ObjectValidationException {
        log.debug("FLEX-ADMIN - REST request to delete AuctionCmvc : {}", id);
        return super.deleteAuctionCmvc(id);
    }

    //********************************************************************************** OFFERS ************************************************************************************
    /**
     * {@code GET  /auctions-cmvc/offers} : get all the offers.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of offers in body.
     */
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_AUCTIONS_OFFER_VIEW + "\")")
    @GetMapping("/auctions-cmvc/offers")
    public ResponseEntity<List<AuctionOfferDTO>> getAllOffers(AuctionOfferCriteria criteria, Pageable pageable) {
        log.debug("{} - REST request to get AuctionOffers by criteria: {}", FLEX_ADMIN_APP_NAME, criteria);
        Page<AuctionOfferDTO> page = offerQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code POST  /admin/auctions-cmvc/offers} : Create a new offer.
     *
     * @param offerDTO the offerDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new offerDTO, or with status {@code 400 (Bad Request)} if the offer has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_AUCTIONS_CMVC_OFFER_CREATE + "\")")
    @PostMapping("/auctions-cmvc/offers")
    public ResponseEntity<AuctionCmvcOfferDTO> createOffer(@RequestBody AuctionCmvcOfferDTO offerDTO) throws URISyntaxException, ObjectValidationException {
        log.debug("{} - REST request to save AuctionCmvcOffer : {}", FLEX_ADMIN_APP_NAME, offerDTO);
        AuctionStatus auctionStatus = auctionCmvcService.findAuctionStatusById(offerDTO.getAuctionCmvc().getId());
        return super.createOffer(offerDTO, auctionStatus);
    }

    /**
     * {@code PUT  /admin/auctions-cmvc/offers} : Updates an existing offer.
     *
     * @param offerDTO the offerDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated offerDTO,
     * or with status {@code 400 (Bad Request)} if the offerDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the offerDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_AUCTIONS_CMVC_OFFER_EDIT + "\")")
    @PutMapping("/auctions-cmvc/offers")
    public ResponseEntity<AuctionCmvcOfferDTO> updateOffer(@RequestBody AuctionCmvcOfferDTO offerDTO) throws URISyntaxException, ObjectValidationException {
        log.debug("{} - REST request to update AuctionCmvcOffer : {}", FLEX_ADMIN_APP_NAME, offerDTO);
        AuctionStatus auctionStatus = auctionCmvcService.findAuctionStatusById(offerDTO.getAuctionCmvc().getId());
        return super.updateOffer(offerDTO, auctionStatus);
    }

    /**
     * {@code GET  /admin/auctions/offers/:id} : get the "id" offer.
     *
     * @param id the id of the offerDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the offerDTO, or with status {@code 404 (Not Found)}.
     */
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_AUCTIONS_CMVC_OFFER_VIEW + "\")")
    @GetMapping("/auctions-cmvc/offers/{id}")
    public ResponseEntity<AuctionCmvcOfferDTO> getOffer(@PathVariable Long id) {
        log.debug("{} - REST request to get AuctionCmvcOffer : {}", FLEX_ADMIN_APP_NAME, id);
        return super.getOffer(id);
    }

    /**
     * {@code DELETE  /admin/auctions/offers/:id} : delete the "id" offer.
     *
     * @param id the id of the offerDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_AUCTIONS_CMVC_OFFER_DELETE + "\")")
    @DeleteMapping("/auctions-cmvc/offers/{id}")
    public ResponseEntity<Void> deleteOffer(@PathVariable Long id) throws ObjectValidationException {
        log.debug("{} - REST request to delete AuctionCmvcOffer : {}", FLEX_ADMIN_APP_NAME, id);
        AuctionCmvcOfferDTO offerDTO = auctionCmvcService.findOfferById(id).get();
        AuctionStatus auctionStatus = auctionCmvcService.findAuctionStatusById(offerDTO.getAuctionCmvc().getId());
        return super.deleteOffer(id, auctionStatus);
    }

    /**
     * {@code GET  /admin/auctions-cmvc/get-fsps-with-registered-potentials} : get all the fsps minimal data of FSP/FSPA having registered flex potentials for Auction CM&VC.
     *
     * @param auctionCmvcId id of the Auction CM&VC for which the offer is being submitted.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of fsps in body.
     */
    @GetMapping("/auctions-cmvc/get-fsps-with-registered-potentials-for-auction")
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_AUCTIONS_CMVC_OFFER_CREATE + "\")")
    public ResponseEntity<List<FspCompanyMinDTO>> getFspsForAuctionCMVC(@RequestParam(value = "auctionCmvcId") Long auctionCmvcId) {
        log.debug("{} - REST request to get all the fsps minimal data of FSP/FSPA having registered flex potentials for Auction: {}", FLEX_ADMIN_APP_NAME, auctionCmvcId);
        List<FspCompanyMinDTO> result = auctionCmvcService.findFspsWithRegisteredPotentialsForAuction(auctionCmvcId);
        if (result.isEmpty()) {
            ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok().body(result);
    }


    /**
     * {@code GET  /admin/auctions-cmvc/get-all-registered-fp-for-fsp-and-auction} : get FSP/A registered flex potentials for auction CM&VC.
     *
     * @param fspId     the id of Fsp the owner of Flex potentials
     * @param auctionCmvcId id of the Auction CM&VC for which the offer is being submitted.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body with the list of flexPotentialMinDTOs}.
     */
    @GetMapping("/auctions-cmvc/get-all-registered-fp-for-fsp-and-auction")
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_AUCTIONS_CMVC_OFFER_VIEW + "\")")
    public ResponseEntity<List<FlexPotentialMinDTO>> getAllRegisteredFlexPotentialsForFspAndAuction(@RequestParam("fspId") Long fspId,
                                                                                                    @RequestParam(value = "auctionCmvcId") Long auctionCmvcId) {
        log.debug("{} - REST request to get all registered FlexPotentials for FSP: {} and AuctionCMVC: {}", FLEX_ADMIN_APP_NAME, fspId, auctionCmvcId);
        return super.getAllRegisteredFlexPotentialsForFspAndAuction(fspId, auctionCmvcId);
    }
    //********************************************************************************** OFFERS ************************************************************************************

}
