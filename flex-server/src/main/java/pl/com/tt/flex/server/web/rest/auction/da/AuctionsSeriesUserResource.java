package pl.com.tt.flex.server.web.rest.auction.da;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.com.tt.flex.model.service.dto.auction.da.AuctionsSeriesDTO;
import pl.com.tt.flex.server.domain.auction.da.AuctionsSeriesEntity;
import pl.com.tt.flex.server.service.auction.da.series.AuctionsSeriesQueryService;
import pl.com.tt.flex.server.service.auction.da.series.AuctionsSeriesService;
import pl.com.tt.flex.server.service.auction.da.series.dto.AuctionsSeriesCriteria;
import pl.com.tt.flex.server.validator.auction.da.AuctionsSeriesValidator;

import java.util.List;

import static pl.com.tt.flex.model.security.permission.Authority.FLEX_USER_AUCTIONS_SERIES_VIEW;

/**
 * REST controller for managing {@link AuctionsSeriesEntity} for FLEX-USER web module.
 */
@Slf4j
@RestController
@RequestMapping("/api/user")
public class AuctionsSeriesUserResource extends AuctionsSeriesResource {

    public AuctionsSeriesUserResource(AuctionsSeriesService auctionsSeriesService, AuctionsSeriesQueryService auctionsSeriesQueryService, AuctionsSeriesValidator auctionsSeriesValidator) {
        super(auctionsSeriesService, auctionsSeriesQueryService, auctionsSeriesValidator);
    }

    /**
     * {@code GET  /auctions-series} : get all the auctionsSeries.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of auctionsSeries in body.
     */
    @GetMapping("/auctions-series")
    @PreAuthorize("hasAuthority(\"" + FLEX_USER_AUCTIONS_SERIES_VIEW + "\")")
    public ResponseEntity<List<AuctionsSeriesDTO>> getAllAuctionsSeries(AuctionsSeriesCriteria criteria, Pageable pageable) {
        log.debug("FLEX-USER - REST request to get AuctionsSeries by criteria: {}", criteria);
        return super.getAllAuctionsSeries(criteria, pageable);
    }

    /**
     * {@code GET  /auctions-series/:id} : get the "id" auctionsSeries.
     *
     * @param id the id of the auctionsSeriesDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the auctionsSeriesDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/auctions-series/{id}")
    @PreAuthorize("hasAuthority(\"" + FLEX_USER_AUCTIONS_SERIES_VIEW + "\")")
    public ResponseEntity<AuctionsSeriesDTO> getAuctionsSeries(@PathVariable Long id) {
        log.debug("FLEX-USER - REST request to get AuctionsSeries : {}", id);
        return super.getAuctionsSeries(id);
    }
}
