package pl.com.tt.flex.server.web.rest.product.forecastedPrices;

import io.github.jhipster.web.util.HeaderUtil;
import io.github.jhipster.web.util.PaginationUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pl.com.tt.flex.server.common.errors.ObjectValidationException;
import pl.com.tt.flex.server.domain.product.ForecastedPricesEntity;
import pl.com.tt.flex.model.service.dto.file.FileDTO;
import pl.com.tt.flex.server.service.product.forecastedPrices.ForecastedPricesQueryService;
import pl.com.tt.flex.server.service.product.forecastedPrices.ForecastedPricesService;
import pl.com.tt.flex.server.service.product.forecastedPrices.dto.ForecastedPricesDTO;
import pl.com.tt.flex.server.service.product.forecastedPrices.dto.ForecastedPricesCriteria;
import pl.com.tt.flex.server.validator.forecastedPrices.ForecastedPricesFileValidator;

import java.io.IOException;
import java.util.List;

import static pl.com.tt.flex.model.security.permission.Authority.*;

/**
 * REST controller for managing {@link ForecastedPricesEntity} for FLEX-ADMIN web module.
 */
@Slf4j
@RestController
@RequestMapping("/api/admin")
public class ForecastedPricesResourceAdmin {

    public static final String ENTITY_NAME = "forecastedPrices";

    @Value("${jhipster.clientApp.name}")
    protected String applicationName;

    private final ForecastedPricesService forecastedPricesService;
    private final ForecastedPricesQueryService forecastedPricesQueryService;
    private final ForecastedPricesFileValidator forecastedPricesFileValidator;

    public ForecastedPricesResourceAdmin(ForecastedPricesService forecastedPricesService, ForecastedPricesQueryService forecastedPricesQueryService, ForecastedPricesFileValidator forecastedPricesFileValidator) {
        this.forecastedPricesService = forecastedPricesService;
        this.forecastedPricesQueryService = forecastedPricesQueryService;
        this.forecastedPricesFileValidator = forecastedPricesFileValidator;
    }

    @GetMapping("/forecasted-prices/template")
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_FORECASTED_PRICES_VIEW + "\")")
    public ResponseEntity<FileDTO> getTemplate() throws IOException {
        log.debug("FLEX_ADMIN - REST request to get Forecasted Prices Template");
        return ResponseEntity.ok().body(forecastedPricesService.getTemplate());
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_FORECASTED_PRICES_MANAGE + "\")")
    @PostMapping(value = "/forecasted-prices")
    public void importForecastedPrices(@RequestPart(value = "file") MultipartFile[] multipartFile, @RequestParam boolean force)
        throws ObjectValidationException, IOException {
        log.debug("FLEX_ADMIN - REST request to import Forecasted Prices file");
        forecastedPricesFileValidator.checkValid(multipartFile);
        if (!force) {
            forecastedPricesService.throwExceptionIfExistForecastedPrices(multipartFile);
        }
        forecastedPricesService.save(multipartFile);
    }

    @GetMapping("/forecasted-prices")
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_FORECASTED_PRICES_VIEW + "\")")
    public ResponseEntity<List<ForecastedPricesDTO>> getAllForecastedPrices(ForecastedPricesCriteria criteria, Pageable pageable) {
        log.debug("FLEX_ADMIN - REST request to get Forecasted Prices by criteria: {}", criteria);
        Page<ForecastedPricesDTO> page = forecastedPricesQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    @GetMapping("/forecasted-prices/detail/{id}")
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_FORECASTED_PRICES_VIEW + "\")")
    public ResponseEntity<ForecastedPricesDTO> getDetail(@PathVariable Long id) throws IOException {
        log.debug("FLEX_ADMIN - REST request to get detail of Forecasted Prices: {}", id);
        return ResponseEntity.ok().body(forecastedPricesService.getDetail(id));
    }

    @DeleteMapping("/forecasted-prices/{id}")
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_FORECASTED_PRICES_DELETE + "\")")
    public ResponseEntity<Void> deleteForecastedPrices(@PathVariable Long id) throws ObjectValidationException {
        log.debug("FLEX_ADMIN - REST request to delete Forecasted Prices : {}", id);
        forecastedPricesFileValidator.checkDeletable(id);
        forecastedPricesService.delete(id);
        return ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(applicationName, true,
            ENTITY_NAME, id.toString())).build();
    }
}
