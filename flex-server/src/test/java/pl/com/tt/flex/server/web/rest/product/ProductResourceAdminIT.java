package pl.com.tt.flex.server.web.rest.product;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;
import pl.com.tt.flex.server.FlexserverApp;
import pl.com.tt.flex.server.domain.product.ProductEntity;
import pl.com.tt.flex.server.repository.auction.da.AuctionDayAheadRepository;
import pl.com.tt.flex.server.repository.product.ProductRepository;
import pl.com.tt.flex.server.service.product.dto.ProductDTO;
import pl.com.tt.flex.server.service.product.mapper.ProductMapper;
import pl.com.tt.flex.server.web.rest.TestUtil;
import pl.com.tt.flex.server.web.rest.errors.ErrorConstants;

import javax.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static pl.com.tt.flex.model.security.permission.Authority.*;

@SpringBootTest(classes = FlexserverApp.class)
@AutoConfigureMockMvc
@WithMockUser(authorities = {FLEX_ADMIN_PRODUCT_VIEW, FLEX_ADMIN_PRODUCT_MANAGE, FLEX_ADMIN_PRODUCT_DELETE})
public class ProductResourceAdminIT extends ProductResourceIT {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final EntityManager em;
    private final MockMvc restProductMockMvc;
    private final static String requestUri = "/api/admin/products";

    @Autowired
    public ProductResourceAdminIT(ProductRepository productRepository, ProductMapper productMapper, EntityManager em, MockMvc restProductMockMvc) {
        super(productRepository, em, restProductMockMvc, requestUri);
        this.productRepository = productRepository;
        this.productMapper = productMapper;
        this.restProductMockMvc = restProductMockMvc;
        this.em = em;
    }

    @Test
    @Transactional
    public void createProduct() throws Exception {
        int databaseSizeBeforeCreate = productRepository.findAll().size();

        MockMultipartFile multipartProduct = new MockMultipartFile("productDTO", "productDTO", MediaType.APPLICATION_JSON_VALUE, TestUtil.convertObjectToJsonBytes(productDTO));
        restProductMockMvc.perform(MockMvcRequestBuilders.multipart(requestUri)
            .file(multipartProduct))
            .andExpect(status().isCreated());

        // Validate the Product in the database
        List<ProductEntity> productList = productRepository.findAll();
        assertThat(productList).hasSize(databaseSizeBeforeCreate + 1);
        ProductEntity testProduct = productList.get(productList.size() - 1);
        assertThat(testProduct.getFullName()).isEqualTo(DEFAULT_FULL_NAME);
        assertThat(testProduct.getShortName()).isEqualTo(DEFAULT_SHORT_NAME);
        assertThat(testProduct.isLocational()).isEqualTo(DEFAULT_LOCATIONAL);
        assertThat(testProduct.getMinBidSize()).isEqualTo(DEFAULT_MIN_BID_SIZE);
        assertThat(testProduct.getMaxBidSize()).isEqualTo(DEFAULT_MAX_BID_SIZE);
        assertThat(testProduct.getMaxFullActivationTime()).isEqualTo(DEFAULT_MAX_FULL_ACTIVATION_TIME);
        assertThat(testProduct.getMinRequiredDeliveryDuration()).isEqualTo(DEFAULT_MIN_REQUIRED_DELIVERY_DURATION);
        assertThat(testProduct.isActive()).isEqualTo(DEFAULT_ACTIVE);
        assertThat(testProduct.isBalancing()).isEqualTo(DEFAULT_BALANCING);
        assertThat(testProduct.getValidFrom()).isEqualTo(DEFAULT_VALID_FROM);
        assertThat(testProduct.getValidTo()).isEqualTo(DEFAULT_VALID_TO);
        assertThat(testProduct.getVersion()).isEqualTo(DEFAULT_VERSION.intValue() + 1);
    }


    @Test
    @Transactional
    public void updateProduct() throws Exception {
        // Initialize the database
        productRepository.saveAndFlush(productEntity);

        int databaseSizeBeforeUpdate = productRepository.findAll().size();

        // Update the product
        ProductEntity updatedProductEntity = productRepository.findById(productEntity.getId()).get();
        // Disconnect from session so that the updates on updatedProductEntity are not directly saved in db
        em.detach(updatedProductEntity);
        em.flush();
        updatedProductEntity.setFullName(UPDATED_FULL_NAME);
        updatedProductEntity.setShortName(UPDATED_SHORT_NAME);
        updatedProductEntity.setLocational(UPDATED_LOCATIONAL);
        updatedProductEntity.setMinBidSize(UPDATED_MIN_BID_SIZE);
        updatedProductEntity.setMaxBidSize(UPDATED_MAX_BID_SIZE);
        updatedProductEntity.setMaxFullActivationTime(UPDATED_MAX_FULL_ACTIVATION_TIME);
        updatedProductEntity.setMinRequiredDeliveryDuration(UPDATED_MIN_REQUIRED_DELIVERY_DURATION);
        updatedProductEntity.setActive(DEFAULT_ACTIVE);
        updatedProductEntity.setValidFrom(UPDATED_VALID_FROM);
        updatedProductEntity.setValidTo(UPDATED_VALID_TO);
        updatedProductEntity.setBalancing(UPDATE_BALANCING);
        ProductDTO productDTO = productMapper.toDto(updatedProductEntity);

        MockMultipartFile multipartProduct = new MockMultipartFile("productDTO", "productDTO", MediaType.APPLICATION_JSON_VALUE, TestUtil.convertObjectToJsonBytes(productDTO));
        restProductMockMvc.perform(MockMvcRequestBuilders.multipart(requestUri + "/update")
            .file(multipartProduct))
            .andExpect(status().isOk());

        // Validate the Product in the database
        List<ProductEntity> productList = productRepository.findAll();
        assertThat(productList).hasSize(databaseSizeBeforeUpdate);
        ProductEntity testProduct = productList.get(productList.size() - 1);
        assertThat(testProduct.getFullName()).isEqualTo(UPDATED_FULL_NAME);
        assertThat(testProduct.getShortName()).isEqualTo(UPDATED_SHORT_NAME);
        assertThat(testProduct.isLocational()).isEqualTo(UPDATED_LOCATIONAL);
        assertThat(testProduct.getMinBidSize()).isEqualTo(UPDATED_MIN_BID_SIZE);
        assertThat(testProduct.getMaxBidSize()).isEqualTo(UPDATED_MAX_BID_SIZE);
        assertThat(testProduct.getMaxFullActivationTime()).isEqualTo(UPDATED_MAX_FULL_ACTIVATION_TIME);
        assertThat(testProduct.getMinRequiredDeliveryDuration()).isEqualTo(UPDATED_MIN_REQUIRED_DELIVERY_DURATION);
        assertThat(testProduct.isActive()).isEqualTo(DEFAULT_ACTIVE);
        assertThat(testProduct.getValidFrom()).isEqualTo(UPDATED_VALID_FROM);
        assertThat(testProduct.getValidTo()).isEqualTo(UPDATED_VALID_TO);
        assertThat(testProduct.getVersion()).isEqualTo(UPDATED_VERSION);
    }

    @Test
    @Transactional
    public void updateNonExistingProduct() throws Exception {
        int databaseSizeBeforeUpdate = productRepository.findAll().size();

        // Create the Product
        ProductDTO productDTO = productMapper.toDto(productEntity);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        MockMultipartFile multipartProduct = new MockMultipartFile("productDTO", "productDTO", MediaType.APPLICATION_JSON_VALUE, TestUtil.convertObjectToJsonBytes(productDTO));
        restProductMockMvc.perform(MockMvcRequestBuilders.multipart(requestUri + "/update")
            .file(multipartProduct))
            .andExpect(status().isBadRequest());

        // Validate the Product in the database
        List<ProductEntity> productList = productRepository.findAll();
        assertThat(productList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    public void deleteProduct() throws Exception {
        // Initialize the database
        productRepository.saveAndFlush(productEntity);

        int databaseSizeBeforeDelete = productRepository.findAll().size();

        // Delete the product
        restProductMockMvc.perform(delete(requestUri + "/{id}", productEntity.getId())
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<ProductEntity> productList = productRepository.findAll();
        assertThat(productList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void checkMinBidSizeIsRequired() throws Exception {
        int databaseSizeBeforeTest = productRepository.findAll().size();
        // Create the Product, which fails.
        // set the field null
        productDTO.setMinBidSize(null);

        MockMultipartFile multipartProduct = new MockMultipartFile("productDTO", "productDTO", MediaType.APPLICATION_JSON_VALUE, TestUtil.convertObjectToJsonBytes(productDTO));
        restProductMockMvc.perform(MockMvcRequestBuilders.multipart(requestUri)
            .file(multipartProduct))
            .andExpect(status().isBadRequest());

        List<ProductEntity> productList = productRepository.findAll();
        assertThat(productList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkMaxBidSizeIsRequired() throws Exception {
        int databaseSizeBeforeTest = productRepository.findAll().size();
        // Create the Product, which fails.
        // set the field null
        productDTO.setMaxBidSize(null);

        MockMultipartFile multipartProduct = new MockMultipartFile("productDTO", "productDTO", MediaType.APPLICATION_JSON_VALUE, TestUtil.convertObjectToJsonBytes(productDTO));
        restProductMockMvc.perform(MockMvcRequestBuilders.multipart(requestUri)
            .file(multipartProduct))
            .andExpect(status().isBadRequest());

        List<ProductEntity> productList = productRepository.findAll();
        assertThat(productList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkValidFromIsRequired() throws Exception {
        int databaseSizeBeforeTest = productRepository.findAll().size();
        // Create the Product, which fails.
        // set the field null
        productDTO.setValidFrom(null);

        MockMultipartFile multipartProduct = new MockMultipartFile("productDTO", "productDTO", MediaType.APPLICATION_JSON_VALUE, TestUtil.convertObjectToJsonBytes(productDTO));
        restProductMockMvc.perform(MockMvcRequestBuilders.multipart(requestUri)
            .file(multipartProduct))
            .andExpect(status().isBadRequest());

        List<ProductEntity> productList = productRepository.findAll();
        assertThat(productList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkValidToIsRequired() throws Exception {
        int databaseSizeBeforeTest = productRepository.findAll().size();
        // Create the Product, which fails.
        // set the field null
        productDTO.setValidTo(null);

        MockMultipartFile multipartProduct = new MockMultipartFile("productDTO", "productDTO", MediaType.APPLICATION_JSON_VALUE, TestUtil.convertObjectToJsonBytes(productDTO));
        restProductMockMvc.perform(MockMvcRequestBuilders.multipart(requestUri)
            .file(multipartProduct))
            .andExpect(status().isBadRequest());

        List<ProductEntity> productList = productRepository.findAll();
        assertThat(productList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void createProductWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = productRepository.findAll().size();

        // Create the Product with an existing ID
        productDTO.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        MockMultipartFile multipartProduct = new MockMultipartFile("productDTO", "productDTO", MediaType.APPLICATION_JSON_VALUE, TestUtil.convertObjectToJsonBytes(productDTO));
        restProductMockMvc.perform(MockMvcRequestBuilders.multipart(requestUri)
            .file(multipartProduct))
            .andExpect(status().isBadRequest());

        // Validate the Product in the database
        List<ProductEntity> productList = productRepository.findAll();
        assertThat(productList).hasSize(databaseSizeBeforeCreate);
    }


    @Test
    @Transactional
    public void checkFullNameIsRequired() throws Exception {
        int databaseSizeBeforeTest = productRepository.findAll().size();
        // Create the Product, which fails.
        // set the field null
        productDTO.setFullName(null);

        MockMultipartFile multipartProduct = new MockMultipartFile("productDTO", "productDTO", MediaType.APPLICATION_JSON_VALUE, TestUtil.convertObjectToJsonBytes(productDTO));
        restProductMockMvc.perform(MockMvcRequestBuilders.multipart(requestUri)
            .file(multipartProduct))
            .andExpect(status().isBadRequest());

        List<ProductEntity> productList = productRepository.findAll();
        assertThat(productList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkShortNameIsRequired() throws Exception {
        int databaseSizeBeforeTest = productRepository.findAll().size();
        // Create the Product, which fails.
        // set the field null
        productDTO.setShortName(null);

        MockMultipartFile multipartProduct = new MockMultipartFile("productDTO", "productDTO", MediaType.APPLICATION_JSON_VALUE, TestUtil.convertObjectToJsonBytes(productDTO));
        restProductMockMvc.perform(MockMvcRequestBuilders.multipart(requestUri)
            .file(multipartProduct))
            .andExpect(status().isBadRequest());

        List<ProductEntity> productList = productRepository.findAll();
        assertThat(productList).hasSize(databaseSizeBeforeTest);
    }

    //    @Test
//    public void checkProductDtoVersionIncrementingSequentially() {
//        ProductDTO save1 = productMapper.toDto(productService.save(productMapper.toEntity(productDTO)));
//        assertThat(save1.getVersion()).isEqualTo(DEFAULT_VERSION);
//        ProductDTO save2 = productMapper.toDto(productService.save(productMapper.toEntity(save1)));
//        assertThat(save2.getVersion()).isEqualTo(DEFAULT_VERSION + 1);
//        ProductDTO save3 = productMapper.toDto(productService.save(productMapper.toEntity(save2)));
//        assertThat(save3.getVersion()).isEqualTo(DEFAULT_VERSION + 2);
//    }

    @Test
    @Transactional
    public void createProduct_shouldNotCreatedBecauseMinBidSizeIsGreaterThanMaxBidSize() throws Exception {
        int databaseSizeBeforeCreate = productRepository.findAll().size();

        productDTO.setMinBidSize(BigDecimal.valueOf(20));
        productDTO.setMaxBidSize(BigDecimal.valueOf(10));

        MockMultipartFile multipartProduct = new MockMultipartFile("productDTO", "productDTO", MediaType.APPLICATION_JSON_VALUE, TestUtil.convertObjectToJsonBytes(productDTO));
        restProductMockMvc.perform(MockMvcRequestBuilders.multipart(requestUri)
            .file(multipartProduct))
            .andExpect(jsonPath("$.errorKey").value(ErrorConstants.MIN_NUMBER_GREATER_THAN_MAX_NUMBER))
            .andExpect(status().isBadRequest());

        // Validate the AuctionsSeries is not created
        int databaseSizeAfterCreate = productRepository.findAll().size();
        assertThat(databaseSizeAfterCreate).isEqualTo(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void createProduct_shouldNotCreatedBecauseCurrentDateIsNotBetweenValidFromAndTo() throws Exception {
        int databaseSizeBeforeCreate = productRepository.findAll().size();

        productDTO.setValidFrom(Instant.now().plus(1, ChronoUnit.DAYS).truncatedTo(ChronoUnit.SECONDS));
        productDTO.setValidTo(Instant.now().plus(2, ChronoUnit.DAYS).truncatedTo(ChronoUnit.SECONDS));
        productDTO.setActive(true);

        MockMultipartFile multipartProduct = new MockMultipartFile("productDTO", "productDTO", MediaType.APPLICATION_JSON_VALUE, TestUtil.convertObjectToJsonBytes(productDTO));
        restProductMockMvc.perform(MockMvcRequestBuilders.multipart(requestUri)
            .file(multipartProduct))
            .andExpect(jsonPath("$.errorKey").value(ErrorConstants.PRODUCT_CANNOT_BE_ACTIVE_BECAUSE_DATE_NOW_IS_NOT_BETWEEN_VALID_FROM_TO_DATES))
            .andExpect(status().isBadRequest());

        // Validate the AuctionsSeries is not created
        int databaseSizeAfterCreate = productRepository.findAll().size();
        assertThat(databaseSizeAfterCreate).isEqualTo(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void createProduct_shouldNotCreatedBecauseNotMarkedBalancingOrCmvc() throws Exception {
        int databaseSizeBeforeCreate = productRepository.findAll().size();

        productDTO.setBalancing(false);
        productDTO.setCmvc(false);

        MockMultipartFile multipartProduct = new MockMultipartFile("productDTO", "productDTO", MediaType.APPLICATION_JSON_VALUE, TestUtil.convertObjectToJsonBytes(productDTO));
        restProductMockMvc.perform(MockMvcRequestBuilders.multipart(requestUri)
            .file(multipartProduct))
            .andExpect(jsonPath("$.errorKey").value(ErrorConstants.ONE_OF_BALANCING_AND_CMVC_SHOULD_BE_MARKED))
            .andExpect(status().isBadRequest());

        // Validate the AuctionsSeries is not created
        int databaseSizeAfterCreate = productRepository.findAll().size();
        assertThat(databaseSizeAfterCreate).isEqualTo(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void createProduct_shouldNotModifyProductNameBecauseAuctionIsStarted() throws Exception {
        AuctionDayAheadRepository mockAuctionDayAheadRepository = Mockito.mock(AuctionDayAheadRepository.class);
        Mockito.when(mockAuctionDayAheadRepository.existsOpenAuctionsWithProductId(any())).thenReturn(true);

        productRepository.saveAndFlush(productEntity);

        int databaseSizeBeforeUpdate = productRepository.findAll().size();

        // Update the product
        ProductEntity updatedProductEntity = productRepository.findById(productEntity.getId()).get();
        em.detach(updatedProductEntity);

        updatedProductEntity.setFullName(UPDATED_FULL_NAME);
        ProductDTO productDTO = productMapper.toDto(updatedProductEntity);

        MockMultipartFile multipartProduct = new MockMultipartFile("productDTO", "productDTO", MediaType.APPLICATION_JSON_VALUE, TestUtil.convertObjectToJsonBytes(productDTO));
        restProductMockMvc.perform(MockMvcRequestBuilders.multipart(requestUri + "/update")
            .file(multipartProduct))
            .andExpect(jsonPath("$.errorKey").value(ErrorConstants.ONE_OF_BALANCING_AND_CMVC_SHOULD_BE_MARKED))
            .andExpect(status().isBadRequest());


        // Validate the AuctionsSeries is not created
        int databaseSizeAfterUpdate = productRepository.findAll().size();
        assertThat(databaseSizeAfterUpdate).isEqualTo(databaseSizeBeforeUpdate);
    }
}
