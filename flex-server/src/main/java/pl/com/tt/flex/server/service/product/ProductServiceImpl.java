package pl.com.tt.flex.server.service.product;

import static pl.com.tt.flex.server.dataexport.exporter.offer.detail.enumeration.LevelOfDetail.STANDARD_DETAIL_SHEET;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.com.tt.flex.model.service.dto.product.ProductMinDTO;
import pl.com.tt.flex.server.common.errors.ConcurrencyFailureException;
import pl.com.tt.flex.server.dataexport.exporter.DataExporter;
import pl.com.tt.flex.server.dataexport.factory.DataExporterFactory;
import pl.com.tt.flex.server.dataexport.factory.DataExporterFormat;
import pl.com.tt.flex.server.domain.auction.cmvc.AuctionCmvcViewEntity;
import pl.com.tt.flex.server.domain.auction.da.AuctionDayAheadViewEntity;
import pl.com.tt.flex.server.domain.auction.da.AuctionsSeriesEntity;
import pl.com.tt.flex.server.domain.enumeration.NotificationEvent;
import pl.com.tt.flex.server.domain.enumeration.NotificationParam;
import pl.com.tt.flex.server.domain.product.ProductEntity;
import pl.com.tt.flex.server.domain.product.ProductFileEntity;
import pl.com.tt.flex.server.domain.screen.enumeration.Screen;
import pl.com.tt.flex.server.domain.user.UserEntity;
import pl.com.tt.flex.server.repository.AbstractJpaRepository;
import pl.com.tt.flex.server.repository.product.ProductFileRepository;
import pl.com.tt.flex.server.repository.product.ProductRepository;
import pl.com.tt.flex.server.service.auction.cmvc.AuctionCmvcService;
import pl.com.tt.flex.server.service.auction.cmvc.view.AuctionCmvcViewQueryService;
import pl.com.tt.flex.server.service.auction.da.AuctionDayAheadQueryService;
import pl.com.tt.flex.server.service.auction.da.AuctionDayAheadService;
import pl.com.tt.flex.server.service.auction.da.series.AuctionsSeriesService;
import pl.com.tt.flex.server.service.common.AbstractServiceImpl;
import pl.com.tt.flex.model.service.dto.file.FileDTO;
import pl.com.tt.flex.model.service.dto.MinimalDTO;
import pl.com.tt.flex.server.service.mail.product.ProductMailService;
import pl.com.tt.flex.server.service.mapper.EntityMapper;
import pl.com.tt.flex.server.service.notification.dto.NotificationParamValue;
import pl.com.tt.flex.server.service.notification.factory.NotifierFactory;
import pl.com.tt.flex.server.service.notification.util.NotificationUtils;
import pl.com.tt.flex.server.service.product.dto.ProductDTO;
import pl.com.tt.flex.server.service.product.dto.ProductMailDTO;
import pl.com.tt.flex.server.service.product.dto.ProductNameMinDTO;
import pl.com.tt.flex.server.service.product.mapper.ProductMapper;
import pl.com.tt.flex.server.service.user.UserService;
import pl.com.tt.flex.server.service.user.dto.UserMinDTO;
import pl.com.tt.flex.server.util.AuctionCmvcDataUtil;
import pl.com.tt.flex.server.util.AuctionDayAheadDataUtil;
import pl.com.tt.flex.server.util.ZipUtil;

import java.io.IOException;
import java.util.*;

/**
 * Service Implementation for managing {@link ProductEntity}.
 */
@Slf4j
@Service
@Transactional
public class ProductServiceImpl extends AbstractServiceImpl<ProductEntity, ProductDTO, Long> implements ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final ProductFileRepository productFileRepository;
    private final DataExporterFactory dataExporterFactory;
    private final AuctionCmvcViewQueryService auctionCmvcViewQueryService;
    private final AuctionCmvcService auctionCmvcService;
    private final AuctionDayAheadQueryService auctionDayAheadQueryService;
    private final AuctionDayAheadService auctionDayAheadService;
    private final AuctionsSeriesService auctionsSeriesService;
    private final NotifierFactory notifierFactory;
    private final UserService userService;
    private final ProductMailService productMailService;

    public ProductServiceImpl(ProductRepository productRepository, ProductMapper productMapper, ProductFileRepository productFileRepository,
                              DataExporterFactory dataExporterFactory, AuctionCmvcViewQueryService auctionCmvcViewQueryService,
                              AuctionDayAheadQueryService auctionDayAheadQueryService, AuctionCmvcService auctionCmvcService,
                              AuctionDayAheadService auctionDayAheadService, AuctionsSeriesService auctionsSeriesService,
                              NotifierFactory notifierFactory, UserService userService, ProductMailService productMailService) {
        this.productRepository = productRepository;
        this.productMapper = productMapper;
        this.productFileRepository = productFileRepository;
        this.dataExporterFactory = dataExporterFactory;
        this.auctionCmvcViewQueryService = auctionCmvcViewQueryService;
        this.auctionDayAheadQueryService = auctionDayAheadQueryService;
        this.auctionCmvcService = auctionCmvcService;
        this.auctionDayAheadService = auctionDayAheadService;
        this.auctionsSeriesService = auctionsSeriesService;
        this.notifierFactory = notifierFactory;
        this.userService = userService;
        this.productMailService = productMailService;
    }

    /**
     * Get one product by id with pso and sso users ids.
     *
     * @param productId the id of the ProductEntity.
     * @return the ProductEntity.
     */
    @Transactional(readOnly = true)
    public Optional<ProductDTO> findOneWithUsers(Long productId) {
        log.debug("Request to get Product : {} with pso and sso users ids", productId);
        return productRepository.findOneWithUsers(productId).map(productMapper::toDto);
    }

    /**
     * For each save, the 'version' column is self incremented (starts at 0).
     *
     * @param productEntityToSave Entity to save (with only new files).
     * @param filesToRemove       Optional ids of files to remove from existing entity.
     * @throws ConcurrencyFailureException if object has been modified by another user.
     */
    @Override
    @Transactional
    public ProductDTO save(ProductEntity productEntityToSave, List<Long> filesToRemove) {
        if (!productEntityToSave.isNew()) {
            ProductEntity productEntityFromDb = productRepository.findById(productEntityToSave.getId()).get();
            updateFiles(productEntityFromDb, productEntityToSave, filesToRemove);
            updateCmvcAuctionsNames(productEntityToSave, productEntityFromDb);
            updateDayAheadAuctionsNames(productEntityToSave, productEntityFromDb);
            updateSeriesAuctionsNames(productEntityToSave, productEntityFromDb);
        }
        productEntityToSave = productRepository.save(productEntityToSave);
        return productMapper.toDto(productEntityToSave);
    }

    private void updateFiles(ProductEntity from, ProductEntity to, List<Long> filesToRemove) {
        for (ProductFileEntity dbFile : from.getFiles()) {
            if (!filesToRemove.contains(dbFile.getId())) {
                to.getFiles().add(dbFile);
            } else {
                to.getFiles().remove(dbFile);
            }
        }
    }

    private void updateCmvcAuctionsNames(ProductEntity productEntityToSave, ProductEntity productEntityFromDb) {
        if (!productEntityToSave.getShortName().equals(productEntityFromDb.getShortName())) {
            List<AuctionCmvcViewEntity> auctionCmvcViews = auctionCmvcViewQueryService.findAllByStatusNewAndProductId(productEntityFromDb.getId());
            auctionCmvcViews.forEach(auctionCmvc -> {
                String generatedName = generateAuctionCmvcName(productEntityToSave, auctionCmvc);
                auctionCmvc.setName(generatedName);
                auctionCmvcService.updateAuctionName(auctionCmvc.getName(), auctionCmvc.getId());
            });
        }
    }

    /**
     * Przy edycji nazwy produktu należy edytować nazwę auckji cmvc ("CM/VC_{produkt}_{data dostawy}_suffix")
     * Suffix z numerem porządkowym w nazwie jest dodawany do każdej aukcji cmvc, dlatego, że na daną datę dostawy
     * i na dany produkt może wystąpić więcej niż 1 aukcja.
     * Przy edycji produktu należy zachować stary suffix i edytować jedynie nazwę produktu w aukcji.
     */
    private String generateAuctionCmvcName(ProductEntity productEntityToSave, AuctionCmvcViewEntity auctionCmvc) {
        String auctionName = auctionCmvc.getName();
        String suffix = auctionName.substring(Math.max(auctionName.length() - 3, 0));
        String newAuctionName = AuctionCmvcDataUtil.generateAuctionCmvcName(productEntityToSave.getShortName(), auctionCmvc.getDeliveryDateFrom(), 0L);
        return newAuctionName.substring(0, newAuctionName.length() - 3) + suffix;

    }

    private void updateDayAheadAuctionsNames(ProductEntity productEntityToSave, ProductEntity productEntityFromDb) {
        if (!productEntityToSave.getShortName().equals(productEntityFromDb.getShortName())) {
            List<AuctionDayAheadViewEntity> auctionDayAheadViews = auctionDayAheadQueryService.findAllByStatusNewAndProductId(productEntityFromDb.getId());
            auctionDayAheadViews.forEach(auctionDayAhead -> {
                String generatedName = AuctionDayAheadDataUtil.generateAuctionDayAheadName(productEntityToSave.getShortName(), auctionDayAhead.getDay());
                auctionDayAheadService.updateAuctionName(generatedName, auctionDayAhead.getId());
            });
        }
    }

    private void updateSeriesAuctionsNames(ProductEntity productEntityToSave, ProductEntity productEntityFromDb) {
        if (!productEntityToSave.getShortName().equals(productEntityFromDb.getShortName())) {
            List<AuctionsSeriesEntity> auctionsSeries = auctionsSeriesService.findAllByProductId(productEntityFromDb.getId());
            auctionsSeries.forEach(series -> {
                String generatedName = AuctionDayAheadDataUtil.generateAuctionSeriesName(productEntityToSave.getShortName(), series.getCreatedDate());
                auctionsSeriesService.updateAuctionName(generatedName, series.getId());
            });
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ProductFileEntity> getProductFileByFileId(Long fileId) {
        return productFileRepository.findById(fileId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FileDTO> getZipWithAllFilesOfProduct(Long productId) {
        List<ProductFileEntity> fileEntities = productFileRepository.findAllByProductId(productId);
        List<FileDTO> fileDTOS = Lists.newArrayList();
        fileEntities.forEach(entity -> fileDTOS.add(new FileDTO(entity.getFileName(), ZipUtil.zipToFiles(entity.getFileZipData()).get(0).getBytesData())));
        return fileDTOS;
    }

    @Override
    public void sendMailInformingAboutProductCreation(ProductEntity productEntity, List<MinimalDTO<Long, String>> usersToBeNotified, String psoUser, String ssoUsers) {
        usersToBeNotified.forEach(user -> {
            UserEntity userEntity = userService.findOne(user.getId()).get();
            productMailService.informUserAboutNewProductCreation(userEntity, productMapper.toDto(productEntity), psoUser, ssoUsers);
        });
    }

    @Override
    public void sendMailInformingAboutProductEdition(ProductMailDTO oldProductMailDTO, ProductMailDTO productMailDTO, List<MinimalDTO<Long, String>> usersToBeNotified, Map<NotificationParam, NotificationParamValue> notificationParams) {
        usersToBeNotified.forEach(user -> {
            UserEntity userEntity = userService.findOne(user.getId()).get();
            productMailService.informUserAboutProductEdition(userEntity, oldProductMailDTO, productMailDTO, notificationParams);
        });
    }

    public void registerNewNotificationForProductCreation(ProductDTO productDTO) {
        ProductEntity productEntity = productRepository.findById(productDTO.getId()).get();
        UserEntity pso = userService.findOne(productDTO.getPsoUserId()).get();
        List<UserMinDTO> users = getSsoUsersIds(new HashSet<>(productDTO.getSsoUserIds()));
        String ssoUsers = getSsoString(users);
        Map<NotificationParam, NotificationParamValue> notificationParams = NotificationUtils.ParamsMapBuilder.create()
                .addParam(NotificationParam.ID, productDTO.getId())
                .addParam(NotificationParam.PRODUCT, productDTO.getFullName())
                .addParam(NotificationParam.PRODUCT_LOCATIONAL, productDTO.isLocational())
                .addParam(NotificationParam.PRODUCT_MIN_BID_SIZE, productDTO.getMinBidSize())
                .addParam(NotificationParam.PRODUCT_MAX_BID_SIZE, productDTO.getMaxBidSize())
                .addParam(NotificationParam.PRODUCT_BID_SIZE_UNIT, productDTO.getBidSizeUnit())
                .addParam(NotificationParam.MAXIMUM_FULL_ACTIVATION_TIME, productDTO.getMaxFullActivationTime())
                .addParam(NotificationParam.MINIMUM_REQUIRED_DURATION_OF_DELIVERY, productDTO.getMinRequiredDeliveryDuration())
                .addParam(NotificationParam.ACTIVE, productDTO.isActive())
                .addParam(NotificationParam.BALANCING, productDTO.isBalancing())
                .addParam(NotificationParam.CMVC, productDTO.isCmvc())
                .addParam(NotificationParam.VALID_FROM, productDTO.getValidFrom())
                .addParam(NotificationParam.VALID_TO, productDTO.getValidTo())
                .addParam(NotificationParam.PSO, pso.getLogin())
                .addParam(NotificationParam.SSO, ssoUsers)
                .addParam(NotificationParam.CREATED_BY, productEntity.getCreatedBy()).build();
        List<MinimalDTO<Long, String>> usersToBeNotified = userService.getUsersByLogin(Set.of(productEntity.getCreatedBy()));
        NotificationUtils.registerNewNotificationForSpecifiedUsers(notifierFactory, NotificationEvent.PRODUCT_CREATED, notificationParams, usersToBeNotified);
        sendMailInformingAboutProductCreation(productEntity, usersToBeNotified, pso.getLogin(), ssoUsers);
    }

    public void registerNewNotificationForProductEdition(ProductDTO productDTO, ProductDTO oldProductDTO) {
        UserEntity pso = userService.findOne(productDTO.getPsoUserId()).get();
        UserEntity oldPso = userService.findOne(oldProductDTO.getPsoUserId()).get();
        List<UserMinDTO> users = getSsoUsersIds(new HashSet<>(productDTO.getSsoUserIds()));
        List<UserMinDTO> oldUsers = getSsoUsersIds(new HashSet<>(oldProductDTO.getSsoUserIds()));
        String ssoUsers = getSsoString(users);
        String oldSsoUsers = getSsoString(oldUsers);
        Map<NotificationParam, NotificationParamValue> notificationParams = NotificationUtils.ParamsMapBuilder.create()
            .addParam(NotificationParam.ID, productDTO.getId())
            .addParam(NotificationParam.OLD_PRODUCT, oldProductDTO.getId())
            .addParam(NotificationParam.PRODUCT, productDTO.getFullName())
            .addModificationParam(NotificationParam.PRODUCT_LOCATIONAL, oldProductDTO.isLocational(), productDTO.isLocational())
            .addModificationParam(NotificationParam.PRODUCT_MIN_BID_SIZE, oldProductDTO.getMinBidSize(), productDTO.getMinBidSize())
            .addModificationParam(NotificationParam.PRODUCT_MAX_BID_SIZE, oldProductDTO.getMaxBidSize(), productDTO.getMaxBidSize())
            .addModificationParam(NotificationParam.PRODUCT_BID_SIZE_UNIT, oldProductDTO.getBidSizeUnit(), productDTO.getBidSizeUnit())
            .addModificationParam(NotificationParam.MAXIMUM_FULL_ACTIVATION_TIME, oldProductDTO.getMaxFullActivationTime(), productDTO.getMaxFullActivationTime())
            .addModificationParam(NotificationParam.MINIMUM_REQUIRED_DURATION_OF_DELIVERY, oldProductDTO.getMinRequiredDeliveryDuration(), productDTO.getMinRequiredDeliveryDuration())
            .addModificationParam(NotificationParam.ACTIVE, oldProductDTO.isActive(), productDTO.isActive())
            .addModificationParam(NotificationParam.BALANCING, oldProductDTO.isBalancing(), productDTO.isBalancing())
            .addModificationParam(NotificationParam.CMVC, oldProductDTO.isCmvc(), productDTO.isCmvc())
            .addModificationParam(NotificationParam.VALID_FROM, oldProductDTO.getValidFrom(), productDTO.getValidFrom())
            .addModificationParam(NotificationParam.VALID_TO, oldProductDTO.getValidTo(), productDTO.getValidTo())
            .addModificationParam(NotificationParam.PSO, oldPso.getLogin(), pso.getLogin())
            .addModificationParam(NotificationParam.SSO, oldSsoUsers, ssoUsers)
            .build();

        List<MinimalDTO<Long, String>> usersToBeNotified = getUsersToBeNotified(oldProductDTO);
        NotificationUtils.registerNewNotificationForSpecifiedUsers(notifierFactory, NotificationEvent.PRODUCT_UPDATED, notificationParams, new ArrayList<>(usersToBeNotified));
        ProductMailDTO oldProductMailDTO = new ProductMailDTO(oldProductDTO, oldPso.getLogin(), oldSsoUsers);
        ProductMailDTO productMailDTO = new ProductMailDTO(productDTO, pso.getLogin(), ssoUsers);
        sendMailInformingAboutProductEdition(oldProductMailDTO, productMailDTO, usersToBeNotified, notificationParams);
    }

    private List<MinimalDTO<Long, String>> getUsersToBeNotified(ProductDTO productDTO) {
        List<MinimalDTO<Long, String>> usersToBeNotified;
        String currentUserLogin = userService.getCurrentUser().getLogin();

        if (!productDTO.getCreatedBy().equals(productDTO.getLastModifiedBy()) && !productDTO.getLastModifiedBy().equals(currentUserLogin) &&
            !productDTO.getCreatedBy().equals(currentUserLogin)) {
            // trzech użytkowników dostaje komunikat (każdy użytkownik jest inny, w kolejności: twórca, poprzedni modyfikujący i obecny modyfikujący różny od poprzedniego)
            // (np. admin -> flex-dso -> flex-tso)
            usersToBeNotified = userService.getUsersByLogin(Set.of(productDTO.getCreatedBy(), productDTO.getLastModifiedBy(), currentUserLogin));
        } else if (!productDTO.getCreatedBy().equals(productDTO.getLastModifiedBy()) && productDTO.getLastModifiedBy().equals(currentUserLogin)) {
            // dwóch użytkowników dostaje komunikat (poprzedni modyfikujący jest taki sam co obecny) (np. admin -> flex-dso -> flex-dso)
            usersToBeNotified = userService.getUsersByLogin(Set.of(productDTO.getCreatedBy(), productDTO.getLastModifiedBy()));
        } else if (!productDTO.getCreatedBy().equals(productDTO.getLastModifiedBy()) && productDTO.getCreatedBy().equals(currentUserLogin)) {
            // dwóch użytkowników dostaje komunikat (obecny modyfikujący jest jego twórcą) (np. admin -> flex-dso -> admin)
            usersToBeNotified = userService.getUsersByLogin(Set.of(productDTO.getCreatedBy(), productDTO.getLastModifiedBy()));
        } else if (productDTO.getCreatedBy().equals(productDTO.getLastModifiedBy()) && !productDTO.getCreatedBy().equals(currentUserLogin)) {
            // dwóch użytkowników dostaje komunikat (obecny modyfikujący jest inny niż poprzedni a zarazem jego twórca) (np. admin -> admin -> flex-dso)
            usersToBeNotified = userService.getUsersByLogin(Set.of(productDTO.getCreatedBy(), currentUserLogin));
        } else {
            // jeden użytkownik dostaje komunikat (twórca jest jego poprzednim i obecnym modyfikującym) (np. admin -> admin -> admin)
            usersToBeNotified = userService.getUsersByLogin(Set.of(productDTO.getCreatedBy()));
        }

        return usersToBeNotified;
    }

    private List<UserMinDTO> getSsoUsersIds(Set<Long> ssoUsers) {
        return userService.getUsersByIds(ssoUsers);
    }

    private String getSsoString(List<UserMinDTO> ssoUsers) {
        StringBuilder ssoString = new StringBuilder();
        ssoUsers.forEach(user -> ssoString.append(user.getLogin()).append(", "));

        return ssoString.substring(0, ssoString.length() - 2);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isPsoUserOfAnyProduct(String login) {
        return productRepository.existsByPsoUser_LoginAndActiveIsTrue(login);
    }

    @Override
    public FileDTO exportProductsToFile(List<ProductDTO> productToExport, String langKey, boolean isOnlyDisplayedData, Screen screen) throws IOException {
        DataExporter<ProductDTO> dataExporter = dataExporterFactory.getDataExporter(DataExporterFormat.XLSX, ProductDTO.class, screen);
        return dataExporter.export(productToExport, Locale.forLanguageTag(langKey), screen, isOnlyDisplayedData, STANDARD_DETAIL_SHEET);
    }


    @Override
    @Transactional(readOnly = true)
    public Optional<ProductMinDTO> findByShortName(String shortName) {
        return productRepository.findByShortName(shortName).map(productMapper::toMinDto);
    }

    @Override
    @Transactional
    public void deactivateProductsByValidFromToDates() {
        List<ProductEntity> expiredProducts = productRepository.findProductsToDeactivateByValidFromToDates();
        expiredProducts.forEach(product -> {
            log.debug("deactivateProductsByValidFromToDates() Deactivating Product [id: {}]", product.getId());
            product.setActive(false);
        });
    }

    @Override
    @Transactional
    public void activateProductsByValidFromToDates() {
        List<ProductEntity> productsToActivate = productRepository.findProductsToActivateByValidFromToDates();
        productsToActivate.forEach(product -> {
            log.debug("activateProductsByValidFromToDates() Activating Product [id: {}]", product.getId());
            product.setActive(true);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByShortName(String shortName) {
        return productRepository.existsByShortNameIgnoreCase(shortName);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByShortNameAndIdNot(String shortName, Long id) {
        return productRepository.existsByShortNameIgnoreCaseAndIdNot(shortName, id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByFullName(String fullName) {
        return productRepository.existsByFullNameIgnoreCase(fullName);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByFullNameAndIdNot(String fullName, Long id) {
        return productRepository.existsByFullNameIgnoreCaseAndIdNot(fullName, id);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductNameMinDTO getProductShortName(Long productId) {
        return productRepository.getProductShortName(productId);
    }

    @Override
    @Transactional(readOnly = true)
    public Long findByFullNameOrShortName(String productName) {
        return productRepository.findByFullNameOrShortName(productName);
    }

    @Override
    @Transactional(readOnly = true)
    public Long findByFullName(String productName) {
        return productRepository.findByFullName(productName);
    }

    @Override
    public AbstractJpaRepository<ProductEntity, Long> getRepository() {
        return this.productRepository;
    }

    @Override
    public EntityMapper<ProductDTO, ProductEntity> getMapper() {
        return this.productMapper;
    }
}
