package pl.com.tt.flex.server.web.rest.product;

import static pl.com.tt.flex.server.web.rest.errors.ErrorConstants.PRODUCT_NOTHING_TO_EXPORT;

import io.github.jhipster.service.filter.InstantFilter;
import io.github.jhipster.web.util.HeaderUtil;
import io.github.jhipster.web.util.PaginationUtil;
import io.github.jhipster.web.util.ResponseUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pl.com.tt.flex.model.service.dto.product.ProductMinDTO;
import pl.com.tt.flex.server.common.errors.ObjectValidationException;
import pl.com.tt.flex.server.domain.product.ProductEntity;
import pl.com.tt.flex.server.domain.product.ProductFileEntity;
import pl.com.tt.flex.server.domain.screen.enumeration.Screen;
import pl.com.tt.flex.model.service.dto.file.FileDTO;
import pl.com.tt.flex.server.service.product.ProductQueryService;
import pl.com.tt.flex.server.service.product.ProductService;
import pl.com.tt.flex.server.service.product.dto.ProductCriteria;
import pl.com.tt.flex.server.service.product.dto.ProductDTO;
import pl.com.tt.flex.server.service.product.mapper.ProductMapper;
import pl.com.tt.flex.server.service.user.UserService;
import pl.com.tt.flex.server.service.user.dto.UserMinDTO;
import pl.com.tt.flex.server.util.InstantUtil;
import pl.com.tt.flex.server.util.ZipUtil;
import pl.com.tt.flex.server.validator.product.ProductValidator;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Common REST controller for managing {@link ProductEntity} for all web modules.
 */
@Slf4j
@RestController
@RequestMapping("/api")
public class ProductResource {

    public static final String ENTITY_NAME = "product";
    public static final String FILE_ENTITY_NAME = "productFile";

    @Value("${jhipster.clientApp.name}")
    protected String applicationName;

    protected final ProductService productService;
    protected final ProductQueryService productQueryService;
    protected final ProductMapper productMapper;
    protected final ProductValidator productValidator;
    protected final UserService userService;

    public ProductResource(ProductService productService, ProductQueryService productQueryService, ProductMapper productMapper, ProductValidator productValidator,
        UserService userService) {
        this.productService = productService;
        this.productQueryService = productQueryService;
        this.productMapper = productMapper;
        this.productValidator = productValidator;
        this.userService = userService;
    }

    protected ResponseEntity<List<ProductDTO>> getAllProducts(ProductCriteria criteria, Pageable pageable) {
        Page<ProductDTO> page = productQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    protected ResponseEntity<List<ProductMinDTO>> getAllProductsMinimal(ProductCriteria productCriteria) {
        productCriteria.setValidTo(new InstantFilter().setGreaterThan(InstantUtil.now()));
        return ResponseEntity.ok(productQueryService.findMinByCriteria(productCriteria));
    }

    public ResponseEntity<List<UserMinDTO>> getUsersForPsoAndSso() {
        return ResponseEntity.ok(userService.getUsersForPsoAndSso());
    }

    protected ResponseEntity<ProductDTO> getProduct(Long id) {
        Optional<ProductDTO> productDTO = productService.findOneWithUsers(id);
        return ResponseUtil.wrapOrNotFound(productDTO);
    }

    protected ResponseEntity<Void> deleteProduct(Long id) throws ObjectValidationException {
        productValidator.checkDeletable(id);
        productService.delete(id);
        return ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString())).build();
    }

    // Pobrane listy obiektow za pomoca metod findByCriteria(Criteria criteria, Sort sort) oraz findByCriteria(Criteria criteria, Pageable pageable)
    // roznia sie w kolejnosci sortowania obiektow. Z tego wzgledu do pobierania listy obiektow do eksportu uzyto metody findByCriteria(Criteria criteria, Pageable pageable)
    // tej samej co do pobrania listy obiektow na front (do widoku).
    protected ResponseEntity<FileDTO> exportProductsToFile(ProductCriteria criteria, Pageable pageable, Screen screen, boolean isOnlyDisplayedData) throws IOException {
        String langKey = userService.getLangKeyForCurrentLoggedUser();
        int size = (int) productQueryService.countByCriteria(criteria);
        if (size == 0) {
            throw new ObjectValidationException("Nothing to export", PRODUCT_NOTHING_TO_EXPORT);
        }
        PageRequest pageRequest = PageRequest.of(0, size, pageable.getSort());
        Page<ProductDTO> productsPage = productQueryService.findByCriteria(criteria, pageRequest);
        return ResponseEntity.ok().body(productService.exportProductsToFile(productsPage.getContent(), langKey, isOnlyDisplayedData, screen));
    }

    protected ResponseEntity<FileDTO> getProductFile(Long fileId) throws IOException {
        Optional<FileDTO> fileDTO = Optional.empty();
        Optional<ProductFileEntity> fileEntity = productService.getProductFileByFileId(fileId);
        if (fileEntity.isPresent()) {
            fileDTO = Optional.ofNullable(ZipUtil.zipToFiles(fileEntity.get().getFileZipData()).get(0));
        }
        return ResponseUtil.wrapOrNotFound(fileDTO);
    }

    protected static class ProductResourceException extends RuntimeException {
        private ProductResourceException(String message) {
            super(message);
        }
    }
}
