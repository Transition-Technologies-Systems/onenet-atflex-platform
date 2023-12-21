package pl.com.tt.flex.server.web.rest.auction.offer;

import io.github.jhipster.service.filter.LongFilter;
import io.github.jhipster.web.util.PaginationUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pl.com.tt.flex.model.service.dto.auction.offer.AuctionOfferViewDTO;
import pl.com.tt.flex.server.domain.auction.offer.cmvc.AuctionCmvcOfferEntity;
import pl.com.tt.flex.server.domain.fsp.FspEntity;
import pl.com.tt.flex.server.service.auction.offer.AuctionOfferService;
import pl.com.tt.flex.server.service.auction.offer.AuctionOfferViewQueryService;
import pl.com.tt.flex.server.service.auction.offer.dto.AuctionOfferViewCriteria;
import pl.com.tt.flex.server.service.fsp.FspService;
import pl.com.tt.flex.server.service.user.UserService;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static pl.com.tt.flex.model.security.permission.Authority.FLEX_USER_AUCTIONS_CMVC_OFFER_VIEW;
import static pl.com.tt.flex.model.security.permission.Authority.FLEX_USER_AUCTIONS_DAY_AHEAD_OFFER_VIEW;
import static pl.com.tt.flex.server.config.Constants.FLEX_USER_APP_NAME;

/**
 * FLEX-USER REST controller for managing {@link AuctionCmvcOfferEntity}.
 */
@Slf4j
@RestController
@RequestMapping("/api/user/auctions")
public class AuctionOfferResourceUser extends AuctionOfferResource {

    public AuctionOfferResourceUser(AuctionOfferService offerService, AuctionOfferViewQueryService auctionOfferViewQueryService,
                                    UserService userService, FspService fspService) {
        super(offerService, auctionOfferViewQueryService, userService, fspService);
    }

    @GetMapping("/offers/view")
    @PreAuthorize("hasAuthority(\"" + FLEX_USER_AUCTIONS_CMVC_OFFER_VIEW + "\") or hasAuthority(\"" + FLEX_USER_AUCTIONS_DAY_AHEAD_OFFER_VIEW + "\")")
    public ResponseEntity<List<AuctionOfferViewDTO>> getAllViewOffers(AuctionOfferViewCriteria criteria, Pageable pageable) {
        var fspId = Optional.ofNullable(userService.getCurrentUser().getFsp())
            .map(FspEntity::getId)
            .orElse(null);
        if(Objects.isNull(fspId)){
            return ResponseEntity.ok().build();
        }
        LongFilter fspIdFilter = (LongFilter) new LongFilter().setEquals(fspId);
        criteria.setFspId(fspIdFilter);
        log.debug("{} - REST request to get AuctionOffers by criteria: {}", FLEX_USER_APP_NAME, criteria);
        Page<AuctionOfferViewDTO> page = offerViewQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }
}
