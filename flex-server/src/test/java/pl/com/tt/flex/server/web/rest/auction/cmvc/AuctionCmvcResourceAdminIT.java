package pl.com.tt.flex.server.web.rest.auction.cmvc;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import pl.com.tt.flex.model.service.dto.auction.cmvc.AuctionCmvcDTO;
import pl.com.tt.flex.model.service.dto.auction.type.AuctionType;
import pl.com.tt.flex.server.FlexserverApp;
import pl.com.tt.flex.server.domain.auction.cmvc.AuctionCmvcEntity;
import pl.com.tt.flex.server.domain.product.ProductEntity;
import pl.com.tt.flex.server.repository.auction.cmvc.AuctionCmvcRepository;
import pl.com.tt.flex.server.service.auction.cmvc.mapper.AuctionCmvcMapper;
import pl.com.tt.flex.server.web.rest.TestUtil;
import pl.com.tt.flex.server.web.rest.errors.ErrorConstants;

import javax.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static java.time.temporal.ChronoUnit.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static pl.com.tt.flex.model.security.permission.Authority.*;
import static pl.com.tt.flex.server.web.rest.InstantTestUtil.getInstantWithSpecifiedHourAndMinute;

/**
 * Integration tests for the {@link AuctionCmvcResourceAdmin} REST controller.
 */
@SpringBootTest(classes = FlexserverApp.class)
@AutoConfigureMockMvc
@WithMockUser(authorities = {FLEX_ADMIN_AUCTIONS_CMVC_MANAGE, FLEX_ADMIN_AUCTIONS_CMVC_VIEW, FLEX_ADMIN_AUCTIONS_CMVC_DELETE})
public class AuctionCmvcResourceAdminIT extends AuctionCmvcResourceIT {


    private final AuctionCmvcRepository auctionCmvcRepository;
    private final MockMvc restAuctionCmvcMockMvc;
    private final AuctionCmvcMapper auctionCmvcMapper;
    private final EntityManager em;
    private final static String requestUri = "/api/admin/auctions-cmvc";


    @Autowired
    public AuctionCmvcResourceAdminIT(AuctionCmvcRepository auctionCmvcRepository, AuctionCmvcMapper auctionCmvcMapper,
                                      EntityManager em, MockMvc restAuctionCmvcMockMvc) {
        super(auctionCmvcRepository, auctionCmvcMapper, em, restAuctionCmvcMockMvc, requestUri);
        this.restAuctionCmvcMockMvc = restAuctionCmvcMockMvc;
        this.auctionCmvcRepository = auctionCmvcRepository;
        this.auctionCmvcMapper = auctionCmvcMapper;
        this.em = em;
    }

    @Test
    @Transactional
    public void createAuctionCmvc_shouldNotCreateBecauseDeliveryDateToIsBeforeDeliveryDateFrom() throws Exception {
        int databaseSizeBeforeCreate = auctionCmvcRepository.findAll().size();
        // Create the AuctionCmvc
        AuctionCmvcDTO auctionCmvcDTO = auctionCmvcMapper.toDto(auctionCmvcEntity);
        auctionCmvcDTO.setDeliveryDateFrom(getInstantWithSpecifiedHourAndMinute(Instant.now(), 10, 0));
        auctionCmvcDTO.setDeliveryDateTo(getInstantWithSpecifiedHourAndMinute(Instant.now(), 5, 0));

        restAuctionCmvcMockMvc.perform(post(requestUri)
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(auctionCmvcDTO)))
            .andExpect(jsonPath("$.errorKey").value(ErrorConstants.DELIVERY_DATE_TO_BEFORE_DELIVERY_DATE_FROM))
            .andExpect(status().isBadRequest());

        // Validate the AuctionCmvc is not created
        List<AuctionCmvcEntity> auctionCmvcList = auctionCmvcRepository.findAll();
        assertThat(auctionCmvcList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void createAuctionCmvc_shouldNotCreateBecauseGateClosureTimeIsBeforeGateOpeningTime() throws Exception {
        int databaseSizeBeforeCreate = auctionCmvcRepository.findAll().size();
        // Create the AuctionCmvc
        AuctionCmvcDTO auctionCmvcDTO = auctionCmvcMapper.toDto(auctionCmvcEntity);
        auctionCmvcDTO.setGateOpeningTime(getInstantWithSpecifiedHourAndMinute(Instant.now(), 10, 0));
        auctionCmvcDTO.setGateClosureTime(getInstantWithSpecifiedHourAndMinute(Instant.now(), 5, 0));

        restAuctionCmvcMockMvc.perform(post(requestUri)
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(auctionCmvcDTO)))
            .andExpect(jsonPath("$.errorKey").value(ErrorConstants.GATE_CLOSURE_TIME_BEFORE_GATE_OPENING_TIME))
            .andExpect(status().isBadRequest());

        // Validate the AuctionCmvc is not created
        List<AuctionCmvcEntity> auctionCmvcList = auctionCmvcRepository.findAll();
        assertThat(auctionCmvcList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void createAuctionCmvc_shouldNotCreateBecauseGateClosureTimeIsAfterDeliveryDateFrom() throws Exception {
        int databaseSizeBeforeCreate = auctionCmvcRepository.findAll().size();
        // Create the AuctionCmvc
        AuctionCmvcDTO auctionCmvcDTO = auctionCmvcMapper.toDto(auctionCmvcEntity);
        auctionCmvcDTO.setGateClosureTime(getInstantWithSpecifiedHourAndMinute(Instant.now(), 10, 0));
        auctionCmvcDTO.setDeliveryDateFrom(getInstantWithSpecifiedHourAndMinute(Instant.now(), 5, 0));

        restAuctionCmvcMockMvc.perform(post(requestUri)
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(auctionCmvcDTO)))
            .andExpect(jsonPath("$.errorKey").value(ErrorConstants.GATE_CLOSURE_TIME_IS_AFTER_DELIVERY_DATE_FROM))
            .andExpect(status().isBadRequest());

        // Validate the AuctionCmvc is not created
        List<AuctionCmvcEntity> auctionCmvcList = auctionCmvcRepository.findAll();
        assertThat(auctionCmvcList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void createAuctionCmvc_shouldNotCreateBecauseMinDesiredPowerIsBiggerThanMaxDesiredPower() throws Exception {
        int databaseSizeBeforeCreate = auctionCmvcRepository.findAll().size();
        // Create the AuctionCmvc
        AuctionCmvcDTO auctionCmvcDTO = auctionCmvcMapper.toDto(auctionCmvcEntity);
        auctionCmvcDTO.setMinDesiredPower(BigDecimal.TEN);
        auctionCmvcDTO.setMaxDesiredPower(BigDecimal.ONE);

        restAuctionCmvcMockMvc.perform(post(requestUri)
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(auctionCmvcDTO)))
            .andExpect(jsonPath("$.errorKey").value(ErrorConstants.MAX_DESIRED_POWER_BIGGER_THAN_MIN_DESIRED_POWER))
            .andExpect(status().isBadRequest());

        // Validate the AuctionCmvc is not created
        List<AuctionCmvcEntity> auctionCmvcList = auctionCmvcRepository.findAll();
        assertThat(auctionCmvcList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void createAuctionCmvc_shouldNotCreateBecauseProductIsNotActive() throws Exception {
        int databaseSizeBeforeCreate = auctionCmvcRepository.findAll().size();

        // Create no active product
        ProductEntity product = TestUtil.findAll(em, ProductEntity.class).get(0);
        product.setActive(false);
        em.persist(product);
        em.flush();
        auctionCmvcEntity.setProduct(product);
        AuctionCmvcDTO auctionCmvcDTO = auctionCmvcMapper.toDto(auctionCmvcEntity);

        restAuctionCmvcMockMvc.perform(post(requestUri)
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(auctionCmvcDTO)))
            .andExpect(jsonPath("$.errorKey").value(ErrorConstants.PRODUCT_IS_INACTIVE_OR_DATE_VALID_TO_EXPIRED))
            .andExpect(status().isBadRequest());

        // Validate the AuctionCmvc is not created
        List<AuctionCmvcEntity> auctionCmvcList = auctionCmvcRepository.findAll();
        assertThat(auctionCmvcList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void createAuctionCmvc_shouldNotCreateBecauseGateClosureTimeIsAfterProductValidTo() throws Exception {
        int databaseSizeBeforeCreate = auctionCmvcRepository.findAll().size();

        // Create Product with ValidTo and AuctionCmvc GateClosureTime
        // gateClosureTime > productValidTo
        Instant productValidTo = getInstantWithSpecifiedHourAndMinute(Instant.now().plus(1, DAYS), 10, 0);
        Instant gateClosureTime = getInstantWithSpecifiedHourAndMinute(Instant.now().plus(1, DAYS), 15, 0);

        ProductEntity product = TestUtil.findAll(em, ProductEntity.class).get(0);
        product.setValidTo(productValidTo);
        em.persist(product);
        em.flush();
        auctionCmvcEntity.setProduct(product);
        AuctionCmvcDTO auctionCmvcDTO = auctionCmvcMapper.toDto(auctionCmvcEntity);
        auctionCmvcDTO.setGateClosureTime(gateClosureTime);

        restAuctionCmvcMockMvc.perform(post(requestUri)
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(auctionCmvcDTO)))
            .andExpect(jsonPath("$.errorKey").value(ErrorConstants.GATE_CLOSURE_TIME_IS_AFTER_PRODUCT_VALID_TO))
            .andExpect(status().isBadRequest());

        // Validate the AuctionCmvc is not created
        List<AuctionCmvcEntity> auctionCmvcList = auctionCmvcRepository.findAll();
        assertThat(auctionCmvcList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void createAuctionCmvc() throws Exception {
        int databaseSizeBeforeCreate = auctionCmvcRepository.findAll().size();
        // Create the AuctionCmvc
        AuctionCmvcDTO auctionCmvcDTO = auctionCmvcMapper.toDto(auctionCmvcEntity);
        restAuctionCmvcMockMvc.perform(post(requestUri)
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(auctionCmvcDTO)))
            .andExpect(status().isCreated());

        // Validate the AuctionCmvc in the database
        List<AuctionCmvcEntity> auctionCmvcList = auctionCmvcRepository.findAll();
        assertThat(auctionCmvcList).hasSize(databaseSizeBeforeCreate + 1);
        AuctionCmvcEntity testAuctionCmvc = auctionCmvcList.get(auctionCmvcList.size() - 1);
        assertThat(testAuctionCmvc.getName()).isNotNull();
        assertThat(testAuctionCmvc.getLocalization().size()).isEqualTo(1);
        assertThat(testAuctionCmvc.getDeliveryDateFrom()).isEqualTo(DEFAULT_DELIVERY_DATE_FROM);
        assertThat(testAuctionCmvc.getDeliveryDateTo()).isEqualTo(DEFAULT_DELIVERY_DATE_TO);
        assertThat(testAuctionCmvc.getGateOpeningTime()).isEqualTo(DEFAULT_GATE_OPENING_TIME);
        assertThat(testAuctionCmvc.getGateClosureTime()).isEqualTo(DEFAULT_GATE_CLOSURE_TIME);
        assertThat(testAuctionCmvc.getMinDesiredPower()).isEqualTo(DEFAULT_MIN_DESIRED_POWER);
        assertThat(testAuctionCmvc.getMaxDesiredPower()).isEqualTo(DEFAULT_MAX_DESIRED_POWER);
        assertThat(testAuctionCmvc.getAuctionType()).isEqualTo(AuctionType.CAPACITY);
    }

    @Test
    @Transactional
    public void createAuctionCmvcWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = auctionCmvcRepository.findAll().size();

        // Create the AuctionCmvc with an existing ID
        auctionCmvcEntity.setId(1L);
        AuctionCmvcDTO auctionCmvcDTO = auctionCmvcMapper.toDto(auctionCmvcEntity);

        // An entity with an existing ID cannot be created, so this API call must fail
        restAuctionCmvcMockMvc.perform(post(requestUri)
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(auctionCmvcDTO)))
            .andExpect(status().isBadRequest());

        // Validate the AuctionCmvc in the database
        List<AuctionCmvcEntity> auctionCmvcList = auctionCmvcRepository.findAll();
        assertThat(auctionCmvcList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void checkDeliveryDateFromIsRequired() throws Exception {
        int databaseSizeBeforeTest = auctionCmvcRepository.findAll().size();
        // set the field null
        auctionCmvcEntity.setDeliveryDateFrom(null);

        // Create the AuctionCmvc, which fails.
        AuctionCmvcDTO auctionCmvcDTO = auctionCmvcMapper.toDto(auctionCmvcEntity);


        restAuctionCmvcMockMvc.perform(post(requestUri)
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(auctionCmvcDTO)))
            .andExpect(status().isBadRequest());

        List<AuctionCmvcEntity> auctionCmvcList = auctionCmvcRepository.findAll();
        assertThat(auctionCmvcList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkDeliveryDateToIsRequired() throws Exception {
        int databaseSizeBeforeTest = auctionCmvcRepository.findAll().size();
        // set the field null
        auctionCmvcEntity.setDeliveryDateTo(null);

        // Create the AuctionCmvc, which fails.
        AuctionCmvcDTO auctionCmvcDTO = auctionCmvcMapper.toDto(auctionCmvcEntity);


        restAuctionCmvcMockMvc.perform(post(requestUri)
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(auctionCmvcDTO)))
            .andExpect(status().isBadRequest());

        List<AuctionCmvcEntity> auctionCmvcList = auctionCmvcRepository.findAll();
        assertThat(auctionCmvcList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkGateOpeningTimeIsRequired() throws Exception {
        int databaseSizeBeforeTest = auctionCmvcRepository.findAll().size();
        // set the field null
        auctionCmvcEntity.setGateOpeningTime(null);

        // Create the AuctionCmvc, which fails.
        AuctionCmvcDTO auctionCmvcDTO = auctionCmvcMapper.toDto(auctionCmvcEntity);


        restAuctionCmvcMockMvc.perform(post(requestUri)
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(auctionCmvcDTO)))
            .andExpect(status().isBadRequest());

        List<AuctionCmvcEntity> auctionCmvcList = auctionCmvcRepository.findAll();
        assertThat(auctionCmvcList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkGateClosureTimeIsRequired() throws Exception {
        int databaseSizeBeforeTest = auctionCmvcRepository.findAll().size();
        // set the field null
        auctionCmvcEntity.setGateClosureTime(null);

        // Create the AuctionCmvc, which fails.
        AuctionCmvcDTO auctionCmvcDTO = auctionCmvcMapper.toDto(auctionCmvcEntity);


        restAuctionCmvcMockMvc.perform(post(requestUri)
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(auctionCmvcDTO)))
            .andExpect(status().isBadRequest());

        List<AuctionCmvcEntity> auctionCmvcList = auctionCmvcRepository.findAll();
        assertThat(auctionCmvcList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void updateAuctionCmvc() throws Exception {
        // Initialize the database
        auctionCmvcRepository.saveAndFlush(auctionCmvcEntity);

        int databaseSizeBeforeUpdate = auctionCmvcRepository.findAll().size();

        // Update the auctionCmvc
        AuctionCmvcEntity updatedAuctionCmvcEntity = auctionCmvcRepository.findById(auctionCmvcEntity.getId()).get();
        // Disconnect from session so that the updates on updatedAuctionCmvcEntity are not directly saved in db
        em.detach(updatedAuctionCmvcEntity);
        updatedAuctionCmvcEntity.setDeliveryDateFrom(UPDATED_DELIVERY_DATE_FROM);
        updatedAuctionCmvcEntity.setDeliveryDateTo(UPDATED_DELIVERY_DATE_TO);
        updatedAuctionCmvcEntity.setGateOpeningTime(UPDATED_GATE_OPENING_TIME);
        updatedAuctionCmvcEntity.setGateClosureTime(UPDATED_GATE_CLOSURE_TIME);
        updatedAuctionCmvcEntity.setMinDesiredPower(UPDATED_MIN_DESIRED_POWER);
        updatedAuctionCmvcEntity.setMaxDesiredPower(UPDATED_MAX_DESIRED_POWER);
        AuctionCmvcDTO auctionCmvcDTO = auctionCmvcMapper.toDto(updatedAuctionCmvcEntity);

        restAuctionCmvcMockMvc.perform(put(requestUri + "")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(auctionCmvcDTO)))
            .andExpect(status().isOk());

        // Validate the AuctionCmvc in the database
        List<AuctionCmvcEntity> auctionCmvcList = auctionCmvcRepository.findAll();
        assertThat(auctionCmvcList).hasSize(databaseSizeBeforeUpdate);
        AuctionCmvcEntity testAuctionCmvc = auctionCmvcList.get(auctionCmvcList.size() - 1);
        assertThat(testAuctionCmvc.getName()).isNotNull();
        assertThat(testAuctionCmvc.getLocalization().size()).isEqualTo(1);
        assertThat(testAuctionCmvc.getDeliveryDateFrom()).isEqualTo(UPDATED_DELIVERY_DATE_FROM);
        assertThat(testAuctionCmvc.getDeliveryDateTo()).isEqualTo(UPDATED_DELIVERY_DATE_TO);
        assertThat(testAuctionCmvc.getGateOpeningTime()).isEqualTo(UPDATED_GATE_OPENING_TIME);
        assertThat(testAuctionCmvc.getGateClosureTime()).isEqualTo(UPDATED_GATE_CLOSURE_TIME);
        assertThat(testAuctionCmvc.getMinDesiredPower()).isEqualTo(UPDATED_MIN_DESIRED_POWER);
        assertThat(testAuctionCmvc.getMaxDesiredPower()).isEqualTo(UPDATED_MAX_DESIRED_POWER);
        assertThat(testAuctionCmvc.getAuctionType()).isEqualTo(AuctionType.CAPACITY);
    }

    @Test
    @Transactional
    public void updateNonExistingAuctionCmvc() throws Exception {
        int databaseSizeBeforeUpdate = auctionCmvcRepository.findAll().size();

        // Create the AuctionCmvc
        AuctionCmvcDTO auctionCmvcDTO = auctionCmvcMapper.toDto(auctionCmvcEntity);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restAuctionCmvcMockMvc.perform(put(requestUri + "")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(auctionCmvcDTO)))
            .andExpect(status().isBadRequest());

        // Validate the AuctionCmvc in the database
        List<AuctionCmvcEntity> auctionCmvcList = auctionCmvcRepository.findAll();
        assertThat(auctionCmvcList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    public void deleteAuctionCmvc() throws Exception {
        // Initialize the database

        // Create auction with status NEW
        Instant GATE_OPENING_TIME = Instant.now().plus(6, HOURS).truncatedTo(SECONDS);
        Instant GATE_CLOSURE_TIME = Instant.now().plus(10, HOURS).truncatedTo(SECONDS);

        auctionCmvcEntity.setGateOpeningTime(GATE_OPENING_TIME);
        auctionCmvcEntity.setGateClosureTime(GATE_CLOSURE_TIME);
        AuctionCmvcEntity auctionCmvcEntity = auctionCmvcRepository.saveAndFlush(this.auctionCmvcEntity);

        int databaseSizeBeforeDelete = auctionCmvcRepository.findAll().size();

        // Delete the auctionCmvc
        restAuctionCmvcMockMvc.perform(delete(requestUri + "/{id}", auctionCmvcEntity.getId())
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<AuctionCmvcEntity> auctionCmvcList = auctionCmvcRepository.findAll();
        assertThat(auctionCmvcList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void deleteAuctionCmvc_shouldNotDeleteBecauseAuctionIsOpen() throws Exception {
        //Create OPEN AuctionCmvc
        Instant GATE_OPENING_TIME = Instant.now().minus(2, HOURS).truncatedTo(SECONDS);
        Instant GATE_CLOSURE_TIME = Instant.now().plus(10, HOURS).truncatedTo(SECONDS);

        auctionCmvcEntity.setGateOpeningTime(GATE_OPENING_TIME);
        auctionCmvcEntity.setGateClosureTime(GATE_CLOSURE_TIME);
        AuctionCmvcEntity auctionCmvcEntity = auctionCmvcRepository.saveAndFlush(this.auctionCmvcEntity);

        int databaseSizeBeforeCreate = auctionCmvcRepository.findAll().size();
        AuctionCmvcDTO auctionCmvcDTO = auctionCmvcMapper.toDto(auctionCmvcEntity);

        restAuctionCmvcMockMvc.perform(delete(requestUri + "/{id}", auctionCmvcEntity.getId())
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.errorKey").value(ErrorConstants.CANNOT_DELETE_OPEN_AUCTION))
            .andExpect(status().isBadRequest());

        // Validate the AuctionCmvc is not created
        List<AuctionCmvcEntity> auctionCmvcList = auctionCmvcRepository.findAll();
        assertThat(auctionCmvcList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void deleteAuctionCmvc_shouldNotDeleteBecauseAuctionIsClose() throws Exception {
        //Create CLOSE AuctionCmvc
        Instant GATE_OPENING_TIME = Instant.now().minus(10, HOURS).truncatedTo(SECONDS);
        Instant GATE_CLOSURE_TIME = Instant.now().minus(8, HOURS).truncatedTo(SECONDS);

        auctionCmvcEntity.setGateOpeningTime(GATE_OPENING_TIME);
        auctionCmvcEntity.setGateClosureTime(GATE_CLOSURE_TIME);
        AuctionCmvcEntity auctionCmvcEntity = auctionCmvcRepository.saveAndFlush(this.auctionCmvcEntity);

        int databaseSizeBeforeCreate = auctionCmvcRepository.findAll().size();
        AuctionCmvcDTO auctionCmvcDTO = auctionCmvcMapper.toDto(auctionCmvcEntity);

        restAuctionCmvcMockMvc.perform(delete(requestUri + "/{id}", auctionCmvcEntity.getId())
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.errorKey").value(ErrorConstants.CANNOT_DELETE_CLOSED_AUCTION))
            .andExpect(status().isBadRequest());

        // Validate the AuctionCmvc is not created
        List<AuctionCmvcEntity> auctionCmvcList = auctionCmvcRepository.findAll();
        assertThat(auctionCmvcList).hasSize(databaseSizeBeforeCreate);
    }
}
