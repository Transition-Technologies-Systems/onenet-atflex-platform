package pl.com.tt.flex.server.web.rest.product;

import io.github.jhipster.web.util.HeaderUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pl.com.tt.flex.model.service.dto.product.ProductMinDTO;
import pl.com.tt.flex.server.common.errors.ObjectValidationException;
import pl.com.tt.flex.server.domain.product.ProductEntity;
import pl.com.tt.flex.server.domain.screen.enumeration.Screen;
import pl.com.tt.flex.model.service.dto.file.FileDTO;
import pl.com.tt.flex.server.service.common.dto.FileDTOUtil;
import pl.com.tt.flex.server.service.product.ProductQueryService;
import pl.com.tt.flex.server.service.product.ProductService;
import pl.com.tt.flex.server.service.product.dto.ProductCriteria;
import pl.com.tt.flex.server.service.product.dto.ProductDTO;
import pl.com.tt.flex.server.service.product.dto.ProductFileDTO;
import pl.com.tt.flex.server.service.product.mapper.ProductMapper;
import pl.com.tt.flex.server.service.user.UserService;
import pl.com.tt.flex.server.service.user.dto.UserMinDTO;
import pl.com.tt.flex.server.validator.product.ProductValidator;
import pl.com.tt.flex.server.web.rest.errors.BadRequestAlertException;

import javax.validation.Valid;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static pl.com.tt.flex.model.security.permission.Authority.*;

/**
 * REST controller for managing {@link ProductEntity} for FLEX-ADMIN web module.
 */
@Slf4j
@RestController
@RequestMapping("/api/admin")
public class ProductResourceAdmin extends ProductResource {

    public ProductResourceAdmin(ProductService productService, ProductQueryService productQueryService, ProductMapper productMapper, ProductValidator productValidator,
        UserService userService) {
        super(productService, productQueryService, productMapper, productValidator, userService);
    }

    /**
     * {@code POST  /products} : Create a new product
     *
     * @param productDTO            the productDTO to create.
     * @param regularMultipartFiles attached files
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new productDTO, or with status {@code 400 (Bad Request)} if the product has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_PRODUCT_MANAGE + "\")")
    @PostMapping(value = "/products")
    public ResponseEntity<ProductDTO> createProduct(@Valid @RequestPart ProductDTO productDTO,
        @RequestPart(value = "regularFiles", required = false) MultipartFile[] regularMultipartFiles)
        throws URISyntaxException, ObjectValidationException {

        log.debug("FLEX-ADMIN - REST request to save Product : {}", productDTO);
        if (productDTO.getId() != null) {
            throw new BadRequestAlertException("A new product cannot already have an ID", ENTITY_NAME, "idexists");
        }
        productValidator.checkValid(productDTO);
        addFilesForNewProduct(regularMultipartFiles, productDTO);
        ProductDTO result = productService.save(productMapper.toEntity(productDTO), Lists.newArrayList());
        productService.registerNewNotificationForProductCreation(result);
        return ResponseEntity.created(new URI("/api/products/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString())).body(result);
    }

    private void addFilesForNewProduct(MultipartFile[] regularMultipartFiles, ProductDTO productDTO) {
        List<ProductFileDTO> regularFileDTOs = Arrays.stream(regularMultipartFiles)
            .map(multipartFile -> new ProductFileDTO(FileDTOUtil.parseMultipartFile(multipartFile)))
            .collect(Collectors.toList());

        productDTO.getFiles().addAll(regularFileDTOs);
    }

    @PostMapping(value = "/products/update")
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_PRODUCT_MANAGE + "\")")
    public ResponseEntity<ProductDTO> updateProduct(@Valid @RequestPart ProductDTO productDTO,
        @RequestPart(value = "regularFiles", required = false) MultipartFile[] regularMultipartFiles)
        throws ObjectValidationException {
        log.debug("FLEX-ADMIN - REST request to update Product : {}", productDTO);
        if (productDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        ProductDTO oldProductDTO = productService.findById(productDTO.getId()).get();
        addFilesForNewProduct(regularMultipartFiles, productDTO);
        productValidator.checkModifiable(productDTO);
        ProductDTO result = (productService.save(productMapper.toEntity(productDTO), productDTO.getRemoveFiles()));
        productService.registerNewNotificationForProductEdition(productDTO, oldProductDTO);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, productDTO.getId().toString())).body(result);
    }

    /**
     * {@code GET  /products} : get all the products.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of products in body.
     */
    @GetMapping("/products")
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_PRODUCT_VIEW + "\")")
    public ResponseEntity<List<ProductDTO>> getAllProducts(ProductCriteria criteria, Pageable pageable) {
        log.debug("FLEX-ADMIN - REST request to get Products by criteria: {}", criteria);
        return super.getAllProducts(criteria, pageable);
    }


    @GetMapping("/products/get-all")
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_PRODUCT_VIEW + "\")")
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
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_PRODUCT_VIEW + "\")")
    public ResponseEntity<ProductDTO> getProduct(@PathVariable Long id) {
        log.debug("FLEX-ADMIN - REST request to get Product : {}", id);
        return super.getProduct(id);
    }

    /**
     * {@code DELETE  /products/:id} : delete the "id" product.
     *
     * @param id the id of the productDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/products/{id}")
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_PRODUCT_DELETE + "\")")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) throws ObjectValidationException {
        log.debug("FLEX-ADMIN - REST request to delete Product : {}", id);
        return super.deleteProduct(id);
    }

    // Pobrane listy obiektow za pomoca metod findByCriteria(Criteria criteria, Sort sort) oraz findByCriteria(Criteria criteria, Pageable pageable)
    // roznia sie w kolejnosci sortowania obiektow. Z tego wzgledu do pobierania listy obiektow do eksportu uzyto metody findByCriteria(Criteria criteria, Pageable pageable)
    // tej samej co do pobrania listy obiektow na front (do widoku).

    /**
     * {@code GET  /admin/products/export/all} : export all products's to file by flex admin.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)}.
     */
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_PRODUCT_VIEW + "\")")
    @GetMapping("/products/export/all")
    public ResponseEntity<FileDTO> exportAllProducts(ProductCriteria criteria, Pageable pageable) throws IOException {
        log.debug("FLEX-ADMIN - REST request to export all products");
        return super.exportProductsToFile(criteria, pageable, Screen.ADMIN_PRODUCTS, false);
    }

    /**
     * {@code GET  /admin/products/export/displayed-data} : export displayed products's to file by flex admin.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)}.
     */
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_PRODUCT_VIEW + "\")")
    @GetMapping("/products/export/displayed-data")
    public ResponseEntity<FileDTO> exportDisplayedProducts(ProductCriteria criteria, Pageable pageable) throws IOException {
        log.debug("FLEX-ADMIN - REST request to export displayed products");
        return super.exportProductsToFile(criteria, pageable, Screen.ADMIN_PRODUCTS, true);
    }

    /**
     * {@code GET /products/users/get-pso-sso} : get all users minimal data for pSo and sSo selects.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body all users.
     */
    @GetMapping("/products/users/get-pso-sso")
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_PRODUCT_VIEW + "\")")
    public ResponseEntity<List<UserMinDTO>> getUsersForPsoAndSso() {
        return super.getUsersForPsoAndSso();
    }

    /**
     * {@code GET  /products/files/:fileId} : get file from product
     *
     * @param fileId the id of the file attached to product.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the fileDTO, or with status {@code 404 (Not Found)}.
     * @throws IOException {@code 500 (Internal Server Error)} if file could not be returned
     */
    @GetMapping("/products/files/{fileId}")
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_PRODUCT_VIEW + "\")")
    public ResponseEntity<FileDTO> getProductFile(@PathVariable Long fileId) throws IOException {
        log.debug("FLEX-ADMIN - REST request to get ProductFile [id: {}]", fileId);
        return super.getProductFile(fileId);
    }
}
