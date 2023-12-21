package pl.com.tt.flex.server.web.rest.product;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.com.tt.flex.model.service.dto.product.ProductMinDTO;
import pl.com.tt.flex.server.domain.product.ProductEntity;
import pl.com.tt.flex.server.domain.screen.enumeration.Screen;
import pl.com.tt.flex.model.service.dto.file.FileDTO;
import pl.com.tt.flex.server.service.product.ProductQueryService;
import pl.com.tt.flex.server.service.product.ProductService;
import pl.com.tt.flex.server.service.product.dto.ProductCriteria;
import pl.com.tt.flex.server.service.product.dto.ProductDTO;
import pl.com.tt.flex.server.service.product.mapper.ProductMapper;
import pl.com.tt.flex.server.service.user.UserService;
import pl.com.tt.flex.server.service.user.dto.UserMinDTO;
import pl.com.tt.flex.server.validator.product.ProductValidator;

import java.io.IOException;
import java.util.List;

import static pl.com.tt.flex.model.security.permission.Authority.*;
import static pl.com.tt.flex.model.security.permission.Authority.FLEX_USER_PRODUCT_VIEW;

/**
 * REST controller for managing {@link ProductEntity} for FLEX-USER web module.
 */
@Slf4j
@RestController
@RequestMapping("/api/user")
public class ProductResourceUser extends ProductResource {

    public ProductResourceUser(ProductService productService, ProductQueryService productQueryService, ProductMapper productMapper, ProductValidator productValidator,
        UserService userService) {
        super(productService, productQueryService, productMapper, productValidator, userService);
    }

    /**
     * {@code GET  /products} : get all the products.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of products in body.
     */
    @GetMapping("/products")
    @PreAuthorize("hasAuthority(\"" + FLEX_USER_PRODUCT_VIEW + "\")")
    public ResponseEntity<List<ProductDTO>> getAllProducts(ProductCriteria criteria, Pageable pageable) {
        log.debug("FLEX-USER - REST request to get Products by criteria: {}", criteria);
        return super.getAllProducts(criteria, pageable);
    }

    @GetMapping("/products/get-all")
    @PreAuthorize("hasAuthority(\"" + FLEX_USER_PRODUCT_VIEW + "\")")
    public ResponseEntity<List<ProductMinDTO>> getAllProductsMinimal(ProductCriteria criteria) {
        return super.getAllProductsMinimal(criteria);
    }

    /**
     * {@code GET  /products/:id} : get the "id" product.
     *
     * @param id the id of the productDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the productDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/products/{id}")
    @PreAuthorize("hasAuthority(\"" + FLEX_USER_PRODUCT_VIEW + "\")")
    public ResponseEntity<ProductDTO> getProduct(@PathVariable Long id) {
        log.debug("FLEX-USER - REST request to get Product : {}", id);
        return super.getProduct(id);
    }

    // Pobrane listy obiektow za pomoca metod findByCriteria(Criteria criteria, Sort sort) oraz findByCriteria(Criteria criteria, Pageable pageable)
    // roznia sie w kolejnosci sortowania obiektow. Z tego wzgledu do pobierania listy obiektow do eksportu uzyto metody findByCriteria(Criteria criteria, Pageable pageable)
    // tej samej co do pobrania listy obiektow na front (do widoku).

    /**
     * {@code GET  /user/products/export/all} : export all products's to file by flex user.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)}.
     */
    @PreAuthorize("hasAuthority(\"" + FLEX_USER_PRODUCT_VIEW + "\")")
    @GetMapping("/products/export/all")
    public ResponseEntity<FileDTO> exportAllProducts(ProductCriteria criteria, Pageable pageable) throws IOException {
        log.debug("FLEX-USER - REST request to export all products");
        return super.exportProductsToFile(criteria, pageable, Screen.USER_PRODUCTS, false);
    }

    /**
     * {@code GET /products/users/get-pso-sso} : get all users minimal data for pSo and sSo selects.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body all users.
     */
    @GetMapping("/products/users/get-pso-sso")
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_USER_VIEW + "\") or hasAuthority(\"" + FLEX_USER_VIEW + "\")")
    public ResponseEntity<List<UserMinDTO>> getUsersForPsoAndSso() {
        return super.getUsersForPsoAndSso();
    }

    /**
     * {@code GET  /user/products/export/displayed-data} : export displayed products's to file by flex user.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)}.
     */
    @PreAuthorize("hasAuthority(\"" + FLEX_USER_PRODUCT_VIEW + "\")")
    @GetMapping("/products/export/displayed-data")
    public ResponseEntity<FileDTO> exportDisplayedProducts(ProductCriteria criteria, Pageable pageable) throws IOException {
        log.debug("FLEX-USER - REST request to export displayed products");
        return super.exportProductsToFile(criteria, pageable, Screen.USER_PRODUCTS, true);
    }

    /**
     * {@code GET  /products/files/:fileId} : get file from product
     *
     * @param fileId the id of the file attached to product.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the fileDTO, or with status {@code 404 (Not Found)}.
     * @throws IOException {@code 500 (Internal Server Error)} if file could not be returned
     */
    @GetMapping("/products/files/{fileId}")
    @PreAuthorize("hasAuthority(\"" + FLEX_USER_PRODUCT_VIEW + "\")")
    public ResponseEntity<FileDTO> getProductFile(@PathVariable Long fileId) throws IOException {
        log.debug("FLEX-USER - REST request to get ProductFile [id: {}]", fileId);
        return super.getProductFile(fileId);
    }
}
