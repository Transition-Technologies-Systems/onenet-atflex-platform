package pl.com.tt.flex.server.web.rest.auction.da;

import io.github.jhipster.web.util.PaginationUtil;
import io.github.jhipster.web.util.ResponseUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pl.com.tt.flex.model.service.dto.auction.da.AuctionsSeriesDTO;
import pl.com.tt.flex.server.domain.auction.da.AuctionsSeriesEntity;
import pl.com.tt.flex.server.service.auction.da.series.AuctionsSeriesQueryService;
import pl.com.tt.flex.server.service.auction.da.series.AuctionsSeriesService;
import pl.com.tt.flex.server.service.auction.da.series.dto.AuctionsSeriesCriteria;
import pl.com.tt.flex.server.validator.auction.da.AuctionsSeriesValidator;

import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing {@link AuctionsSeriesEntity}.
 */
@RestController
@Slf4j
@RequestMapping("/api")
public class AuctionsSeriesResource {

    public static final String ENTITY_NAME = "auctionsSeries";

    @Value("${jhipster.clientApp.name}")
    protected String applicationName;

    protected final AuctionsSeriesService auctionsSeriesService;

    protected final AuctionsSeriesQueryService auctionsSeriesQueryService;

    protected final AuctionsSeriesValidator auctionsSeriesValidator;

    public AuctionsSeriesResource(AuctionsSeriesService auctionsSeriesService, AuctionsSeriesQueryService auctionsSeriesQueryService, AuctionsSeriesValidator auctionsSeriesValidator) {
        this.auctionsSeriesService = auctionsSeriesService;
        this.auctionsSeriesQueryService = auctionsSeriesQueryService;
        this.auctionsSeriesValidator = auctionsSeriesValidator;
    }

    protected ResponseEntity<List<AuctionsSeriesDTO>> getAllAuctionsSeries(AuctionsSeriesCriteria criteria, Pageable pageable) {
        Page<AuctionsSeriesDTO> page = auctionsSeriesQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    protected ResponseEntity<AuctionsSeriesDTO> getAuctionsSeries(Long id) {
        Optional<AuctionsSeriesDTO> auctionsSeriesDTO = auctionsSeriesService.findById(id);
        return ResponseUtil.wrapOrNotFound(auctionsSeriesDTO);
    }
}
