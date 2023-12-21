package pl.com.tt.flex.server.web.rest.auction.da;

import io.github.jhipster.web.util.HeaderUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pl.com.tt.flex.model.service.dto.auction.da.AuctionsSeriesDTO;
import pl.com.tt.flex.server.common.errors.ObjectValidationException;
import pl.com.tt.flex.server.domain.auction.da.AuctionsSeriesEntity;
import pl.com.tt.flex.server.service.auction.da.series.AuctionsSeriesQueryService;
import pl.com.tt.flex.server.service.auction.da.series.AuctionsSeriesService;
import pl.com.tt.flex.server.service.auction.da.series.dto.AuctionsSeriesCriteria;
import pl.com.tt.flex.server.validator.auction.da.AuctionsSeriesValidator;
import pl.com.tt.flex.server.web.rest.errors.BadRequestAlertException;

import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import static pl.com.tt.flex.model.security.permission.Authority.*;

/**
 * REST controller for managing {@link AuctionsSeriesEntity} for FLEX-ADMIN web module.
 * Przy tworzeniu nowej aukcji Series, z frontu w AuctionsSeriesDTO przekazywane sa daty do wygenerowania pierwszej aukcji DayAhead:
 * Auction series utworzona 22/01/28
 * FirstAuctionDate:        22/01/28 23:00:00
 * LastAuctionDate:         22/02/07 23:00:00
 * OpeningTime:             22/01/29 00:15:00
 * ClosureTime:             22/01/29 22:45:00
 * AvailabilityFrom:        22/01/30 05:00:00
 * AvailabilityTo:          22/01/30 17:00:00
 */
@Slf4j
@RestController
@RequestMapping("/api/admin")
public class AuctionsSeriesAdminResource extends AuctionsSeriesResource {

    public AuctionsSeriesAdminResource(AuctionsSeriesService auctionsSeriesService, AuctionsSeriesQueryService auctionsSeriesQueryService, AuctionsSeriesValidator auctionsSeriesValidator) {
        super(auctionsSeriesService, auctionsSeriesQueryService, auctionsSeriesValidator);
    }

    /**
     * {@code POST  /auctions-series} : Create a new auctionsSeries.
     *
     * @param auctionsSeriesDTO the auctionsSeriesDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new auctionsSeriesDTO, or with status {@code 400 (Bad Request)} if the auctionsSeries has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/auctions-series")
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_AUCTIONS_SERIES_MANAGE + "\")")
    public ResponseEntity<AuctionsSeriesDTO> createAuctionsSeries(@Valid @RequestBody AuctionsSeriesDTO auctionsSeriesDTO) throws URISyntaxException, ObjectValidationException {
        log.debug("FLEX-ADMIN - REST request to save AuctionsSeries : {}", auctionsSeriesDTO);
        if (auctionsSeriesDTO.getId() != null) {
            throw new BadRequestAlertException("A new auctionsSeries cannot already have an ID", ENTITY_NAME, "idexists");
        }
        auctionsSeriesValidator.checkValid(auctionsSeriesDTO);
        AuctionsSeriesDTO result = auctionsSeriesService.save(auctionsSeriesDTO);
        return ResponseEntity.created(new URI("/api/auctions-series/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /auctions-series} : Updates an existing auctionsSeries.
     *
     * @param auctionsSeriesDTO the auctionsSeriesDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated auctionsSeriesDTO,
     * or with status {@code 400 (Bad Request)} if the auctionsSeriesDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the auctionsSeriesDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/auctions-series")
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_AUCTIONS_SERIES_MANAGE + "\")")
    public ResponseEntity<AuctionsSeriesDTO> updateAuctionsSeries(@Valid @RequestBody AuctionsSeriesDTO auctionsSeriesDTO) throws URISyntaxException, ObjectValidationException {
        log.debug("FLEX-ADMIN - REST request to update AuctionsSeries : {}", auctionsSeriesDTO);
        if (auctionsSeriesDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        auctionsSeriesValidator.checkModifiable(auctionsSeriesDTO);
        AuctionsSeriesDTO result = auctionsSeriesService.save(auctionsSeriesDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, auctionsSeriesDTO.getId().toString()))
            .body(result);
    }

    /**
     * {@code GET  /auctions-series} : get all the auctionsSeries.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of auctionsSeries in body.
     */
    @GetMapping("/auctions-series")
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_AUCTIONS_SERIES_VIEW + "\")")
    public ResponseEntity<List<AuctionsSeriesDTO>> getAllAuctionsSeries(AuctionsSeriesCriteria criteria, Pageable pageable) {
        log.debug("FLEX-ADMIN - REST request to get AuctionsSeries by criteria: {}", criteria);
        return super.getAllAuctionsSeries(criteria, pageable);
    }

    /**
     * {@code GET  /auctions-series/:id} : get the "id" auctionsSeries.
     *
     * @param id the id of the auctionsSeriesDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the auctionsSeriesDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/auctions-series/{id}")
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_AUCTIONS_SERIES_VIEW + "\")")
    public ResponseEntity<AuctionsSeriesDTO> getAuctionsSeries(@PathVariable Long id) {
        log.debug("FLEX-ADMIN - REST request to get AuctionsSeries : {}", id);
        return super.getAuctionsSeries(id);
    }

    /**
     * {@code DELETE  /auctions-series/:id} : delete the "id" auctionsSeries.
     *
     * @param id the id of the auctionsSeriesDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/auctions-series/{id}")
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_AUCTIONS_SERIES_DELETE + "\")")
    public ResponseEntity<Void> deleteAuctionsSeries(@PathVariable Long id) throws ObjectValidationException {
        log.debug("FLEX-ADMIN - REST request to delete AuctionsSeries : {}", id);
        auctionsSeriesValidator.checkDeletable(id);
        auctionsSeriesService.delete(id);
        return ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString())).build();
    }
}
