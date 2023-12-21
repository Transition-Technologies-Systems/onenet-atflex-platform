package pl.com.tt.flex.server.web.rest.auction.cmvc;

import static pl.com.tt.flex.server.web.rest.errors.ErrorConstants.AUCTION_NAME_NOT_UNIQUE;

import io.github.jhipster.web.util.HeaderUtil;
import io.github.jhipster.web.util.PaginationUtil;
import io.github.jhipster.web.util.ResponseUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pl.com.tt.flex.model.service.dto.auction.cmvc.AuctionCmvcDTO;
import pl.com.tt.flex.model.service.dto.auction.offer.cmvc.AuctionCmvcOfferDTO;
import pl.com.tt.flex.model.service.dto.auction.offer.AuctionOfferDTO;
import pl.com.tt.flex.model.service.dto.auction.type.AuctionStatus;
import pl.com.tt.flex.model.service.dto.potential.FlexPotentialMinDTO;
import pl.com.tt.flex.server.common.errors.ObjectValidationException;
import pl.com.tt.flex.server.domain.auction.cmvc.AuctionCmvcEntity;
import pl.com.tt.flex.server.service.auction.cmvc.AuctionCmvcService;
import pl.com.tt.flex.server.service.auction.cmvc.dto.AuctionCmvcViewCriteria;
import pl.com.tt.flex.server.service.auction.cmvc.view.AuctionCmvcViewQueryService;
import pl.com.tt.flex.server.service.auction.cmvc.AuctionCmvcOfferQueryService;
import pl.com.tt.flex.server.service.auction.offer.dto.AuctionOfferCriteria;
import pl.com.tt.flex.server.validator.auction.cmvc.AuctionsCmvcValidator;
import pl.com.tt.flex.server.web.rest.errors.BadRequestAlertException;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing {@link AuctionCmvcEntity}.
 */
@Slf4j
@RestController
@RequestMapping("/api")
public class AuctionCmvcResource {

    public static final String ENTITY_NAME = "auctionCmvc";
    public static final String OFFER_ENTITY_NAME = "auctionOffer";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    protected final AuctionCmvcService auctionCmvcService;
    protected final AuctionCmvcViewQueryService auctionCmvcViewQueryService;
    protected final AuctionsCmvcValidator auctionsCmvcValidator;
    protected final AuctionCmvcOfferQueryService offerQueryService;

    public AuctionCmvcResource(AuctionCmvcService auctionCmvcService, AuctionCmvcViewQueryService auctionCmvcViewQueryService, AuctionsCmvcValidator auctionsCmvcValidator, AuctionCmvcOfferQueryService offerQueryService) {
        this.auctionCmvcService = auctionCmvcService;
        this.auctionCmvcViewQueryService = auctionCmvcViewQueryService;
        this.auctionsCmvcValidator = auctionsCmvcValidator;
        this.offerQueryService = offerQueryService;
    }

    public ResponseEntity<AuctionCmvcDTO> createAuctionCmvc(AuctionCmvcDTO auctionCmvcDTO) throws URISyntaxException, ObjectValidationException {
        log.debug("REST request to save AuctionCmvc : {}", auctionCmvcDTO);
        if (auctionCmvcDTO.getId() != null) {
            throw new BadRequestAlertException("A new auctionCmvc cannot already have an ID", ENTITY_NAME, "idexists");
        }
        auctionsCmvcValidator.checkValid(auctionCmvcDTO);
        try {
            AuctionCmvcDTO result = auctionCmvcService.save(auctionCmvcDTO);
            return ResponseEntity.created(new URI("/api/auctions-cmvc/" + result.getId()))
                .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
                .body(result);
        } catch (DataIntegrityViolationException e){
            throw new ObjectValidationException("Detected simultaneous addition of the auction for the same product", AUCTION_NAME_NOT_UNIQUE);
        }
    }

    public ResponseEntity<AuctionCmvcDTO> updateAuctionCmvc(AuctionCmvcDTO auctionCmvcDTO) throws URISyntaxException, ObjectValidationException {
        log.debug("REST request to update AuctionCmvc : {}", auctionCmvcDTO);
        if (auctionCmvcDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        auctionsCmvcValidator.checkModifiable(auctionCmvcDTO);
        AuctionCmvcDTO result = auctionCmvcService.save(auctionCmvcDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, auctionCmvcDTO.getId().toString()))
            .body(result);
    }

    public ResponseEntity<List<AuctionCmvcDTO>> getAllAuctionsCmvc(AuctionCmvcViewCriteria criteria, Pageable pageable) {
        log.debug("REST request to get AuctionCmvc by criteria: {}", criteria);
        Page<AuctionCmvcDTO> page = auctionCmvcViewQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    public ResponseEntity<AuctionCmvcDTO> getAuctionCmvc(Long id) {
        log.debug("REST request to get AuctionCmvc : {}", id);
        Optional<AuctionCmvcDTO> auctionCmvcDTO = auctionCmvcService.findById(id);
        return ResponseUtil.wrapOrNotFound(auctionCmvcDTO);
    }


    public ResponseEntity<Void> deleteAuctionCmvc(Long id) throws ObjectValidationException {
        log.debug("REST request to delete AuctionCmvc : {}", id);
        auctionsCmvcValidator.checkDeletable(id);
        auctionCmvcService.delete(id);
        return ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString())).build();
    }

    //********************************************************************************** OFFERS ************************************************************************************
    protected ResponseEntity<List<AuctionOfferDTO>> getAllOffers(AuctionOfferCriteria criteria, Pageable pageable) {
        Page<AuctionOfferDTO> page = offerQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    protected ResponseEntity<AuctionCmvcOfferDTO> createOffer(AuctionCmvcOfferDTO offerDTO, AuctionStatus auctionStatus) throws URISyntaxException, ObjectValidationException {
        if (offerDTO.getId() != null) {
            throw new BadRequestAlertException("A new offer cannot already have an ID", ENTITY_NAME, "idexists");
        }
        AuctionCmvcOfferDTO result = auctionCmvcService.saveOffer(offerDTO, auctionStatus);
        return ResponseEntity.created(new URI("/api/auctions/offers/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, OFFER_ENTITY_NAME, result.getId().toString())).body(result);
    }

    protected ResponseEntity<AuctionCmvcOfferDTO> updateOffer(AuctionCmvcOfferDTO offerDTO, AuctionStatus auctionStatus) throws URISyntaxException, ObjectValidationException {
        if (offerDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", OFFER_ENTITY_NAME, "idnull");
        }
        AuctionCmvcOfferDTO result = auctionCmvcService.saveOffer(offerDTO, auctionStatus);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, OFFER_ENTITY_NAME, offerDTO.getId().toString())).body(result);
    }

    protected ResponseEntity<AuctionCmvcOfferDTO> getOffer(Long id) {
        Optional<AuctionCmvcOfferDTO> offerDTO = auctionCmvcService.findOfferById(id);
        return ResponseUtil.wrapOrNotFound(offerDTO);
    }

    protected ResponseEntity<Void> deleteOffer(Long id, AuctionStatus auctionStatus) throws ObjectValidationException {
        auctionCmvcService.deleteOffer(id, auctionStatus);
        return ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, OFFER_ENTITY_NAME, id.toString())).build();
    }

    protected ResponseEntity<List<FlexPotentialMinDTO>> getAllRegisteredFlexPotentialsForFspAndAuction(Long fspId, Long auctionCmvcId) {
        List<FlexPotentialMinDTO> result = auctionCmvcService.findAllRegisteredFlexPotentialsForFspAndAuction(fspId, auctionCmvcId);
        if (result.isEmpty()) {
            ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok().body(result);
    }
    //********************************************************************************** OFFERS ************************************************************************************

    protected static class AuctionCmvcResourceException extends RuntimeException {
        protected AuctionCmvcResourceException(String message) {
            super(message);
        }
    }
}
