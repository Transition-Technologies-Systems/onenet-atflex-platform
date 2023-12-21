package pl.com.tt.flex.server.service.auction.cmvc;

import static java.util.Objects.isNull;
import static java.util.stream.Collectors.toSet;
import static pl.com.tt.flex.model.security.permission.Role.FSP_ORGANISATIONS_ROLES;
import static pl.com.tt.flex.model.security.permission.Role.ROLE_ADMIN;
import static pl.com.tt.flex.server.web.rest.errors.ErrorConstants.ERR_USER_WITH_NO_COMPANY;

import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import lombok.extern.slf4j.Slf4j;
import pl.com.tt.flex.model.service.dto.auction.cmvc.AuctionCmvcDTO;
import pl.com.tt.flex.model.service.dto.auction.offer.cmvc.AuctionCmvcOfferDTO;
import pl.com.tt.flex.model.service.dto.auction.type.AuctionOfferStatus;
import pl.com.tt.flex.model.service.dto.auction.type.AuctionOfferType;
import pl.com.tt.flex.model.service.dto.auction.type.AuctionStatus;
import pl.com.tt.flex.model.service.dto.fsp.FspCompanyMinDTO;
import pl.com.tt.flex.model.service.dto.localization.LocalizationTypeDTO;
import pl.com.tt.flex.model.service.dto.potential.FlexPotentialMinDTO;
import pl.com.tt.flex.server.common.errors.ObjectValidationException;
import pl.com.tt.flex.server.domain.auction.cmvc.AuctionCmvcEntity;
import pl.com.tt.flex.server.domain.auction.offer.cmvc.AuctionCmvcOfferEntity;
import pl.com.tt.flex.server.domain.fsp.FspEntity;
import pl.com.tt.flex.server.domain.potential.FlexPotentialEntity;
import pl.com.tt.flex.server.domain.product.ProductEntity;
import pl.com.tt.flex.server.domain.unit.LocalizationTypeEntity;
import pl.com.tt.flex.server.domain.unit.UnitEntity;
import pl.com.tt.flex.server.domain.user.UserEntity;
import pl.com.tt.flex.server.repository.AbstractJpaRepository;
import pl.com.tt.flex.server.repository.auction.cmvc.AuctionCmvcRepository;
import pl.com.tt.flex.server.repository.auction.cmvc.AuctionCmvcViewRepository;
import pl.com.tt.flex.server.repository.auction.offer.AuctionCmvcOfferRepository;
import pl.com.tt.flex.server.repository.potential.FlexPotentialRepository;
import pl.com.tt.flex.server.repository.product.ProductRepository;
import pl.com.tt.flex.server.service.auction.cmvc.mapper.AuctionCmvcMapper;
import pl.com.tt.flex.server.service.auction.cmvc.mapper.AuctionCmvcOfferMapper;
import pl.com.tt.flex.server.service.common.AbstractServiceImpl;
import pl.com.tt.flex.server.service.fsp.FspService;
import pl.com.tt.flex.server.service.mapper.EntityMapper;
import pl.com.tt.flex.server.service.user.UserService;
import pl.com.tt.flex.server.service.user.dto.UserDTO;
import pl.com.tt.flex.server.util.AuctionCmvcDataUtil;
import pl.com.tt.flex.server.validator.auction.cmvc.AuctionCmvcOfferValidator;
import pl.com.tt.flex.server.validator.auction.cmvc.AuctionCmvcOfferValidatorUtil;

/**
 * Service Implementation for managing {@link AuctionCmvcEntity}.
 */
@Slf4j
@Service
@Transactional
public class AuctionCmvcServiceImpl extends AbstractServiceImpl<AuctionCmvcEntity, AuctionCmvcDTO, Long> implements AuctionCmvcService {

    private final AuctionCmvcRepository auctionCmvcRepository;
    private final AuctionCmvcViewRepository auctionCmvcViewRepository;
    private final AuctionCmvcMapper auctionCmvcMapper;
    private final AuctionCmvcOfferRepository auctionOfferRepository;
    private final AuctionCmvcOfferMapper auctionOfferMapper;
    private final AuctionCmvcOfferValidator auctionCmvcOfferValidator;
    private final ProductRepository productRepository;
    private final UserService userService;
    private final FspService fspService;
    private final FlexPotentialRepository flexPotentialRepository;

    public AuctionCmvcServiceImpl(final AuctionCmvcRepository auctionCmvcRepository, final AuctionCmvcViewRepository auctionCmvcViewRepository,
                                  final AuctionCmvcMapper auctionCmvcMapper, final AuctionCmvcOfferRepository auctionOfferRepository,
                                  final AuctionCmvcOfferMapper auctionOfferMapper, final AuctionCmvcOfferValidator auctionCmvcOfferValidator,
                                  final ProductRepository productRepository, final UserService userService, final FspService fspService,
                                  final FlexPotentialRepository flexPotentialRepository) {
        this.auctionCmvcRepository = auctionCmvcRepository;
        this.auctionCmvcViewRepository = auctionCmvcViewRepository;
        this.auctionCmvcMapper = auctionCmvcMapper;
        this.auctionOfferRepository = auctionOfferRepository;
        this.auctionOfferMapper = auctionOfferMapper;
        this.auctionCmvcOfferValidator = auctionCmvcOfferValidator;
        this.productRepository = productRepository;
        this.userService = userService;
        this.fspService = fspService;
        this.flexPotentialRepository = flexPotentialRepository;
    }


    @Override
    @Transactional
    public AuctionCmvcDTO save(AuctionCmvcDTO auctionCmvcDTO) {
        log.debug("Request to save AuctionCmvc : {}", auctionCmvcDTO);
        auctionCmvcDTO.setName(generateAuctionName(auctionCmvcDTO));
        AuctionCmvcEntity auctionCmvcEntity = auctionCmvcMapper.toEntity(auctionCmvcDTO);
        auctionCmvcEntity = auctionCmvcRepository.save(auctionCmvcEntity);
        return auctionCmvcMapper.toDto(auctionCmvcEntity);
    }

    /**
     * Nazwa aukcji jest generowana z skroconej nazwy produktu i daty dostawy "CM/VC_{produkt}_{data dostawy}".
     * Gdy na dany dzień występuje więcej niż jedna aukcja, w suffixie wygenerowanej nazwy ma pojawić się numer porządkowy
     * Dla każdej kolejnej aukcji dodajemy kolejny suffix (_02, _03, _04, ...)
     *
     * @param auctionCmvcDTO
     * @return
     */
    private String generateAuctionName(AuctionCmvcDTO auctionCmvcDTO) {
        ProductEntity productEntity = productRepository.getOne(auctionCmvcDTO.getProduct().getId());
        Long auctionsCount = countByDeliveryDateAndProductName(auctionCmvcDTO.getDeliveryDateFrom(), productEntity.getShortName());
        return AuctionCmvcDataUtil.generateAuctionCmvcName(productEntity.getShortName(),
            auctionCmvcDTO.getDeliveryDateFrom(), auctionsCount);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AuctionCmvcDTO> findById(Long id) {
        Optional<AuctionCmvcDTO> maybeAuctionCmvcDTO = super.findById(id);
        if (maybeAuctionCmvcDTO.isPresent()) {
            AuctionCmvcDTO auctionCmvcDTO = maybeAuctionCmvcDTO.get();
            auctionCmvcDTO.setCanAddBid(canCurrentLoggedUserAddNewBid(auctionCmvcDTO, auctionCmvcDTO.getProduct().getId()));
            maybeAuctionCmvcDTO = Optional.of(auctionCmvcDTO);
        }
        return maybeAuctionCmvcDTO;
    }

    @Override
    @Transactional
    public void updateAuctionName(String auctionName, Long auctionId) {
        auctionCmvcRepository.updateAuctionName(auctionName, auctionId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canCurrentLoggedUserAddNewBid(AuctionCmvcDTO auctionCmvcDTO, Long productId) {
        UserEntity currentUser = userService.getCurrentUser();
        if (isFspOrganisationUser(currentUser)) {
            FspEntity fspEntity = fspService.findFspOfUser(currentUser.getId(), currentUser.getLogin())
                .orElseThrow(() -> new ObjectValidationException("Cannot find Fsp by user id: " + currentUser.getId(), ERR_USER_WITH_NO_COMPANY));
            List<FlexPotentialEntity> registeredFp = flexPotentialRepository.findAllEntitiesByFspIdAndProductIdAndRegisteredIsTrueAndActiveIsTrue(fspEntity.getId(), productId);
            return isThereAnyRegisteredFpWithAuctionLocalization(auctionCmvcDTO, registeredFp);
        } else {
            List<FlexPotentialEntity> registeredFp = flexPotentialRepository.findAllByProductIdAndRegisteredIsTrueAndActiveIsTrueAndFspRole(productId);
            return isThereAnyRegisteredFpWithAuctionLocalization(auctionCmvcDTO, registeredFp);
        }


    }

    /**
     * W poniższej metodzie sprawdzane jest czy w przekazanej liście registeredFP są jakiekolwiek dery zawierające chociaż jedną
     * lokalizację z aukcji. Sprawdzamy po typie lokalizacji. Jeśli chociaż jeden der analizowanego flex register zawiera
     * chociaż jedną lokalizację aukcji można dodać ofertę na tę aukcję.
     */
    public boolean isThereAnyRegisteredFpWithAuctionLocalization(AuctionCmvcDTO auctionCmvcDTO, List<FlexPotentialEntity> registeredFp) {
        if (!registeredFp.isEmpty()) {
            for (FlexPotentialEntity flexPotentialEntity : registeredFp) {
                if (isThereAnyDerWithAuctionLocalization(auctionCmvcDTO, flexPotentialEntity.getUnits())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * W poniższej metodzie sprawdzane jest czy w przekazanej liście drow  są jakiekolwiek dery zawierające chociaż jedną
     * lokalizację z aukcji. Sprawdzamy po typie lokalizacji. Jeśli chociaż jeden der zawiera
     * chociaż jedną lokalizację aukcji można dodać ofertę na tę aukcję.
     */
    public static boolean isThereAnyDerWithAuctionLocalization(AuctionCmvcDTO auctionCmvcDTO, Set<UnitEntity> unitEntities) {
        for (UnitEntity unit : unitEntities) {
            if (!CollectionUtils.isEmpty(auctionCmvcDTO.getLocalization())) {
                // Localizacje uzywane w Unicie
                Set<String> unitLocalizationNames = unit.getCouplingPointIdTypes().stream().map(LocalizationTypeEntity::getName).collect(toSet());
                unitLocalizationNames.addAll(unit.getPowerStationTypes().stream().map(LocalizationTypeEntity::getName).collect(toSet()));
                // Lokalizacje uzywane w aukcji CMVC
                Set<String> auctionLocalizationNames = auctionCmvcDTO.getLocalization().stream().map(LocalizationTypeDTO::getName).collect(toSet());
                if (unitLocalizationNames.stream().anyMatch(auctionLocalizationNames::contains)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isFspOrganisationUser(UserEntity maybefspUser) {
        return CollectionUtils.containsAny(maybefspUser.getRoles(), FSP_ORGANISATIONS_ROLES) && !maybefspUser.getRoles().contains(ROLE_ADMIN);
    }

    @Override
    @Transactional(readOnly = true)
    public AuctionStatus findAuctionStatusById(Long auctionId) {
        return auctionCmvcViewRepository.findStatusById(auctionId);
    }

    @Transactional(readOnly = true)
    public Long countByDeliveryDateAndProductName(Instant deliveryDateFrom, String productName) {
        Instant dateFrom = deliveryDateFrom.atZone(ZoneId.systemDefault())
            .truncatedTo(ChronoUnit.DAYS).toInstant();
        // W obecnym zapisie godzina 23 stanowi początek dnia dostawy, więc, żeby nie zliczać aukcji z kolejnego dnia dateTo musi być ustawionana na godzinę nie 23:00:00 a na 22:59:59
        Instant dateTo = dateFrom.plus(1, ChronoUnit.DAYS).minus(1, ChronoUnit.SECONDS);
        return auctionCmvcViewRepository.countAllByDeliveryDateFromBetweenAndProductName(dateFrom, dateTo, productName);
    }

    //********************************************************************************** OFFERS ************************************************************************************

    /**
     * @param auctionStatus status aukcji w momencie klikniecia przycisku utworz/edytuj/usun oferte
     */
    @Override
    @Transactional
    public AuctionCmvcOfferDTO saveOffer(AuctionCmvcOfferDTO offerDTO, AuctionStatus auctionStatus) throws ObjectValidationException {
        offerDTO.setType(AuctionOfferType.CAPACITY);
        offerDTO.setStatus(AuctionOfferStatus.PENDING); //do zmiany statusu jest oddzielny ep
        if (isNull(offerDTO.getId())) {
            offerDTO.setAcceptedVolume(offerDTO.getVolume());
            offerDTO.setAcceptedDeliveryPeriodFrom(offerDTO.getDeliveryPeriodFrom());
            offerDTO.setAcceptedDeliveryPeriodTo(offerDTO.getDeliveryPeriodTo());
            auctionCmvcOfferValidator.checkValid(offerDTO, auctionStatus);
        } else {
            UserDTO currentUser = userService.getCurrentUserDTO().get();
            AuctionCmvcOfferDTO dbOffer = findOfferById(offerDTO.getId()).get();
            offerDTO = AuctionCmvcOfferValidatorUtil.overwriteOnlyAllowedOfferDtoFieldsForCurrentUser(offerDTO, auctionStatus, dbOffer, currentUser);
            auctionCmvcOfferValidator.checkModifiable(offerDTO, auctionStatus);
        }
        AuctionCmvcOfferEntity auctionOfferEntity = auctionOfferMapper.toEntityFromCmvc(offerDTO);
        auctionOfferEntity = auctionOfferRepository.save(auctionOfferEntity);
        return auctionOfferMapper.toCmvcDto(auctionOfferEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AuctionCmvcOfferDTO> findOfferById(Long id) {
        return auctionOfferRepository.findById(id).map(auctionOfferMapper::toCmvcDto);
    }

    /**
     * @param auctionStatus status aukcji w momencie klikniecia przycisku utworz/edytuj/usun oferte
     */
    @Override
    @Transactional
    public void deleteOffer(Long offerId, AuctionStatus auctionStatus) throws ObjectValidationException {
        auctionCmvcOfferValidator.checkDeletable(offerId, auctionStatus);
        auctionOfferRepository.deleteById(offerId);
    }

    @Override
    public List<FspCompanyMinDTO> findFspsWithRegisteredPotentialsForAuction(Long auctionCmvcId) {
        AuctionCmvcDTO auctionCmvcDTO = findById(auctionCmvcId).get();
        List<FlexPotentialEntity> registeredFp = flexPotentialRepository.findAllByProductIdAndRegisteredIsTrueAndActiveIsTrueAndFspRole(auctionCmvcDTO.getProduct().getId());
        return registeredFp.stream().filter(fp -> isThereAnyDerWithAuctionLocalization(auctionCmvcDTO, fp.getUnits()))
            .map(fp -> {
                FspCompanyMinDTO fspCompanyMinDTO = new FspCompanyMinDTO();
                fspCompanyMinDTO.setId(fp.getFsp().getId());
                fspCompanyMinDTO.setCompanyName(fp.getFsp().getCompanyName());
                fspCompanyMinDTO.setRole(fp.getFsp().getRole());
                return fspCompanyMinDTO;
            }).distinct().collect(Collectors.toList());
    }

    @Override
    public List<FlexPotentialMinDTO> findAllRegisteredFlexPotentialsForFspAndAuction(Long fspId, Long auctionCmvcId) {
        AuctionCmvcDTO auctionCmvcDTO = findById(auctionCmvcId).get();
        List<FlexPotentialEntity> registeredFp = flexPotentialRepository.findAllByProductIdAndRegisteredIsTrueAndActiveIsTrueAndFspRole(auctionCmvcDTO.getProduct().getId());
        return registeredFp.stream().filter(fp -> fp.getFsp().getId().equals(fspId) && isThereAnyDerWithAuctionLocalization(auctionCmvcDTO, fp.getUnits()))
            .map(fp -> {
                FlexPotentialMinDTO flexPotentialMinDTO = new FlexPotentialMinDTO();
                flexPotentialMinDTO.setId(fp.getId());
                flexPotentialMinDTO.setFsp(new FspCompanyMinDTO(fp.getFsp().getId(), fp.getFsp().getCompanyName(), fp.getFsp().getRole()));
                flexPotentialMinDTO.setProductName(fp.getProduct().getShortName());
                flexPotentialMinDTO.setMinDeliveryDuration(fp.getMinDeliveryDuration());
                flexPotentialMinDTO.setFullActivationTime(fp.getFullActivationTime());
                flexPotentialMinDTO.setVolumeUnit(fp.getVolumeUnit());
                flexPotentialMinDTO.setVolume(fp.getVolume());
                return flexPotentialMinDTO;
            }).distinct().collect(Collectors.toList());
    }
    //********************************************************************************** OFFERS ************************************************************************************

    @Override
    public AbstractJpaRepository<AuctionCmvcEntity, Long> getRepository() {
        return auctionCmvcRepository;
    }

    @Override
    public EntityMapper<AuctionCmvcDTO, AuctionCmvcEntity> getMapper() {
        return auctionCmvcMapper;
    }
}
