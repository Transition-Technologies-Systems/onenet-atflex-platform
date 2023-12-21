package pl.com.tt.flex.server.service.potential;

import static pl.com.tt.flex.server.dataexport.exporter.offer.detail.enumeration.LevelOfDetail.STANDARD_DETAIL_SHEET;
import static pl.com.tt.flex.server.domain.enumeration.NotificationParam.ACTIVE;
import static pl.com.tt.flex.server.domain.enumeration.NotificationParam.COMPANY;
import static pl.com.tt.flex.server.domain.enumeration.NotificationParam.DERS;
import static pl.com.tt.flex.server.domain.enumeration.NotificationParam.DIVISIBLE;
import static pl.com.tt.flex.server.domain.enumeration.NotificationParam.ID;
import static pl.com.tt.flex.server.domain.enumeration.NotificationParam.MAXIMUM_FULL_ACTIVATION_TIME;
import static pl.com.tt.flex.server.domain.enumeration.NotificationParam.MINIMUM_REQUIRED_DURATION_OF_DELIVERY;
import static pl.com.tt.flex.server.domain.enumeration.NotificationParam.PREQUALIFICATION;
import static pl.com.tt.flex.server.domain.enumeration.NotificationParam.PRODUCT;
import static pl.com.tt.flex.server.domain.enumeration.NotificationParam.STATIC_GRID_PREQUALIFICATION;
import static pl.com.tt.flex.server.domain.enumeration.NotificationParam.VALID_FROM;
import static pl.com.tt.flex.server.domain.enumeration.NotificationParam.VALID_TO;
import static pl.com.tt.flex.server.domain.enumeration.NotificationParam.VOLUME;
import static pl.com.tt.flex.server.domain.enumeration.NotificationParam.VOLUME_UNIT;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.compress.utils.Sets;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;
import pl.com.tt.flex.model.security.permission.Role;
import pl.com.tt.flex.model.service.dto.MinimalDTO;
import pl.com.tt.flex.model.service.dto.file.FileDTO;
import pl.com.tt.flex.model.service.dto.potential.FlexPotentialMinDTO;
import pl.com.tt.flex.server.common.errors.ConcurrencyFailureException;
import pl.com.tt.flex.server.dataexport.exporter.DataExporter;
import pl.com.tt.flex.server.dataexport.factory.DataExporterFactory;
import pl.com.tt.flex.server.dataexport.factory.DataExporterFormat;
import pl.com.tt.flex.server.domain.enumeration.NotificationEvent;
import pl.com.tt.flex.server.domain.enumeration.NotificationParam;
import pl.com.tt.flex.server.domain.potential.FlexPotentialEntity;
import pl.com.tt.flex.server.domain.potential.FlexPotentialFileEntity;
import pl.com.tt.flex.server.domain.screen.enumeration.Screen;
import pl.com.tt.flex.server.domain.unit.UnitEntity;
import pl.com.tt.flex.server.domain.user.UserEntity;
import pl.com.tt.flex.server.repository.AbstractJpaRepository;
import pl.com.tt.flex.server.repository.potential.FlexPotentialFileRepository;
import pl.com.tt.flex.server.repository.potential.FlexPotentialRepository;
import pl.com.tt.flex.server.service.common.AbstractServiceImpl;
import pl.com.tt.flex.server.service.fsp.FspService;
import pl.com.tt.flex.server.service.fsp.dto.FspDTO;
import pl.com.tt.flex.server.service.mail.flexPotential.FlexPotentialMailService;
import pl.com.tt.flex.server.service.mapper.EntityMapper;
import pl.com.tt.flex.server.service.notification.dto.NotificationParamValue;
import pl.com.tt.flex.server.service.notification.factory.NotifierFactory;
import pl.com.tt.flex.server.service.notification.util.NotificationUtils;
import pl.com.tt.flex.server.service.potential.dto.FlexPotentialDTO;
import pl.com.tt.flex.server.service.potential.mapper.FlexPotentialMapper;
import pl.com.tt.flex.server.service.unit.dto.UnitDTO;
import pl.com.tt.flex.server.service.unit.dto.UnitMinDTO;
import pl.com.tt.flex.server.service.user.UserService;
import pl.com.tt.flex.server.service.user.dto.UserDTO;
import pl.com.tt.flex.server.util.ZipUtil;

/**
 * Service Implementation for managing {@link pl.com.tt.flex.server.domain.potential.FlexPotentialEntity}.
 */
@Slf4j
@Service
@Transactional
public class FlexPotentialServiceImpl extends AbstractServiceImpl<FlexPotentialEntity, FlexPotentialDTO, Long> implements FlexPotentialService {

    private final FlexPotentialRepository flexPotentialRepository;

    private final FlexPotentialMapper flexPotentialMapper;

    private final FlexPotentialFileRepository flexPotentialFileRepository;

    private final UserService userService;

    private final DataExporterFactory dataExporterFactory;

    private final FspService fspService;

    private final FlexPotentialMailService flexPotentialMailService;

    private final NotifierFactory notifierFactory;

    public FlexPotentialServiceImpl(final FlexPotentialRepository flexPotentialRepository, final FlexPotentialMapper flexPotentialMapper,
                                    final FlexPotentialFileRepository flexPotentialFileRepository, final UserService userService,
                                    final DataExporterFactory dataExporterFactory, final NotifierFactory notifierFactory,
                                    final FlexPotentialMailService flexPotentialMailService, final FspService fspService) {
        this.flexPotentialRepository = flexPotentialRepository;
        this.flexPotentialMapper = flexPotentialMapper;
        this.flexPotentialFileRepository = flexPotentialFileRepository;
        this.userService = userService;
        this.dataExporterFactory = dataExporterFactory;
        this.fspService = fspService;
        this.flexPotentialMailService = flexPotentialMailService;
        this.notifierFactory = notifierFactory;
    }

    /**
     * For each save, the 'version' column is self incremented (starts at 0).
     *
     * @param flexPotentialDTO DTO to save (with only new files).
     * @param filesToRemove    Optional ids of files to remove from existing entity.
     * @throws ConcurrencyFailureException if object has been modified by another user.
     */
    @Override
    @Transactional
    public FlexPotentialDTO save(FlexPotentialDTO flexPotentialDTO, List<Long> filesToRemove) {
        FlexPotentialEntity flexPotentialEntity = flexPotentialMapper.toEntity(flexPotentialDTO);
        fillAuditRoles(flexPotentialEntity);
        if (!flexPotentialEntity.isNew()) {
            FlexPotentialEntity flexPotentialEntityFromDb = flexPotentialRepository.getOne(flexPotentialEntity.getId());
            UserDTO currentUser = userService.getCurrentUserDTO().get();
            if (currentUser.hasAnyRole(Sets.newHashSet(Role.ROLE_TRANSMISSION_SYSTEM_OPERATOR, Role.ROLE_DISTRIBUTION_SYSTEM_OPERATOR))) {
                flexPotentialEntityFromDb.setProductPrequalification(flexPotentialDTO.isProductPrequalification());
                flexPotentialEntityFromDb.setStaticGridPrequalification(flexPotentialDTO.isStaticGridPrequalification());
                flexPotentialEntity = flexPotentialRepository.save(flexPotentialEntity);
                return flexPotentialMapper.toDto(flexPotentialEntity);
            }
            updateFiles(flexPotentialEntityFromDb, flexPotentialEntity, filesToRemove);
        }
        flexPotentialEntity = flexPotentialRepository.save(flexPotentialEntity);
        return flexPotentialMapper.toDto(flexPotentialEntity);
    }

    private void updateFiles(FlexPotentialEntity from, FlexPotentialEntity to, List<Long> filesToRemove) {
        for (FlexPotentialFileEntity dbFile : from.getFiles()) {
            if (!filesToRemove.contains(dbFile.getId())) {
                to.getFiles().add(dbFile);
            } else {
                to.getFiles().remove(dbFile);
            }
        }
    }

    private void fillAuditRoles(FlexPotentialEntity flexPotentialEntityToSave) {
        UserEntity currentUser = userService.getCurrentUser();
        String currentUserRole = currentUser.getRoles().contains(Role.ROLE_ADMIN) ? Role.ROLE_ADMIN.name() : currentUser.getRoles().stream().findFirst().get().name();
        if (flexPotentialEntityToSave.isNew()) {
            flexPotentialEntityToSave.setCreatedByRole(currentUserRole);
        }
        flexPotentialEntityToSave.setLastModifiedByRole(currentUserRole);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<FlexPotentialFileEntity> getFlexPotentialFileByFileId(Long fileId) {
        return flexPotentialFileRepository.findById(fileId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FileDTO> getZipWithAllFilesOfFlexPotential(Long flexPotentialId) {
        List<FlexPotentialFileEntity> fileEntities = flexPotentialFileRepository.findAllByFlexPotentialId(flexPotentialId);
        List<FileDTO> fileDTOS = Lists.newArrayList();
        fileEntities.forEach(entity -> fileDTOS.add(new FileDTO(entity.getFileName(), ZipUtil.zipToFiles(entity.getFileZipData()).get(0).getBytesData())));
        return fileDTOS;
    }

    @Override
    @Transactional(readOnly = true)
    public FileDTO exportFlexPotentialToFile(List<FlexPotentialDTO> flexPotentials, String langKey, boolean isOnlyDisplayedData, Screen screen) throws IOException {
        DataExporter<FlexPotentialDTO> dataExporter = dataExporterFactory.getDataExporter(DataExporterFormat.XLSX, FlexPotentialDTO.class, screen);
        return dataExporter.export(flexPotentials, Locale.forLanguageTag(langKey), screen, isOnlyDisplayedData, STANDARD_DETAIL_SHEET);
    }

    public boolean isUserHasPermissionToFlexPotential(Long id, String fspCompanyName) {
        return flexPotentialRepository.existsByIdAndFspCompanyName(id, fspCompanyName);
    }

    @Override
    @Transactional(readOnly = true)
    public MinimalDTO<Long, String> findFlexPotentialMinWithFspCompanyName(Long id) {
        return flexPotentialRepository.findFlexPotentialMinWithFspCompanyName(id);
    }

    @Override
    @Transactional
    public void deactivateFlexPotentialsByValidFromToDates() {
        List<FlexPotentialEntity> expiredFlexPotentials = flexPotentialRepository.findFlexPotentialsToDeactivateByValidFromToDates();
        expiredFlexPotentials.forEach(flexPotential -> {
            log.debug("deactivateFlexPotentialsByValidFromToDates() Deactivating FlexPotential [id: {}]", flexPotential.getId());
            flexPotential.setActive(false);
        });
    }

    @Override
    @Transactional
    public void activateFlexPotentialsByValidFromToDates() {
        List<FlexPotentialEntity> flexPotentialsToActivate = flexPotentialRepository.findFlexPotentialsToActivateByValidFromToDates();
        flexPotentialsToActivate.forEach(flexPotential -> {
            log.debug("activateFlexPotentialsByValidFromToDates() Activating FlexPotential [id: {}]", flexPotential.getId());
            flexPotential.setActive(true);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getAllDerNameJoinedToFP() {
        return flexPotentialRepository.findAllDerNamesJoinedToFP();
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getAllDerNameJoinedToFPByFspId(Long fspId) {
        return flexPotentialRepository.findAllDerNamesJoinedToFPByFspId(fspId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getAllDerNameJoinedToFlexRegister() {
        return flexPotentialRepository.findAllDerNamesJoinedToFlexRegister();
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getAllDerNameJoinedToFlexRegisterByFspId(Long fspId) {
        return flexPotentialRepository.findAllDerNamesJoinedToFlexRegisterByFspId(fspId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> findActiveByUnit(UnitDTO unitDTO) {
        return flexPotentialRepository.findActiveByUnit(unitDTO.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> findByUnit(UnitDTO unitDTO) {
        return flexPotentialRepository.findByUnit(unitDTO.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<FlexPotentialMinDTO> findAllRegisteredFlexPotentialsForFspAndProduct(Long fspId, Long productId) {
        return flexPotentialRepository.findAllByFspIdAndProductIdAndRegisteredIsTrueAndActiveIsTrue(fspId, productId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByFlexPotentialIdAndFspId(Long flexPotentialId, Long fspId) {
        return flexPotentialRepository.existsByIdAndFspId(flexPotentialId, fspId);
    }

    /**
     * Aby FSP mógł wysłać propozycję DERa do jednostki grafikowej musi najpierw uzyskać "Flex Register" na produkt bilansujący,
     * czyli taki, który posiada flagę "Balancing" ustawioną na "Yes". Czyli zanim FSP będzie mógł wysłać DERa, to na ten DER
     * powinno istnieć Flexibility Potential z atrybutami "Prod. preq." i "Stat. grid preq." ustawionymi na "Yes", czyli jeżeli
     * ma status Flex Register(jest w oknie "Registered flexibility potentials"). Dopiero wtedy pojawiają się ikony "plusa" w tabelach
     * w oknach "BSP" oraz "Scheduling units" oraz przycisk "Propose DER" w oknie szczegółów "Scheduling units".
     */
    @Override
    @Transactional(readOnly = true)
    public boolean isAtLeastOneDerOfFspBalancedByRegisteredFlexPotentialProduct(Long fspId) {
        return flexPotentialRepository.existsByFspIdAndRegisteredIsTrueAndActiveIsTrueAndProductBalancingIsTrue(fspId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isDerOfFspBalancedByRegisteredFlexPotentialProduct(Long derId) {
        //Aby BSP mógł zaprosić DERa do jednostki grafikowej, to ten DER musi posiadać "Flex register" na produkt z flagą "Balancing".
        return flexPotentialRepository.existsByRegisteredIsTrueAndActiveIsTrueAndProductBalancingIsTrueAndUnitsId(derId);
    }


    //********************************************************************************** NOTIFICATION ************************************************************************************

    @Override
    public void registerCreatedNotification(FlexPotentialDTO flexPotentialDTO) {
        FlexPotentialEntity flexPotential = flexPotentialRepository.findById(flexPotentialDTO.getId())
            .orElseThrow(() -> new RuntimeException("FlexPotential not found with id: " + flexPotentialDTO.getId()));
        FspDTO fsp = fspService.findById(flexPotentialDTO.getFsp().getId())
            .orElseThrow(() -> new RuntimeException("FSP not found with id: " + flexPotentialDTO.getFsp().getId()));
        Map<NotificationParam, NotificationParamValue> notificationParams = NotificationUtils.ParamsMapBuilder.create()
            .addParam(ID, flexPotentialDTO.getId()) //uzywac company??
            .addParam(PRODUCT, flexPotential.getProduct().getShortName())
            .addParam(COMPANY, fsp.getCompanyName())
            .addParam(DERS, flexPotential.getUnits().stream().map(UnitEntity::getName).collect(Collectors.joining(",")))
            .addParam(VOLUME, flexPotential.getVolume())
            .addParam(VOLUME_UNIT, flexPotential.getVolumeUnit())
            .addParam(DIVISIBLE, flexPotential.isDivisibility())
            .addParam(MAXIMUM_FULL_ACTIVATION_TIME, flexPotential.getFullActivationTime())
            .addParam(MINIMUM_REQUIRED_DURATION_OF_DELIVERY, flexPotential.getMinDeliveryDuration())
            .addParam(VALID_FROM, flexPotential.getValidFrom())
            .addParam(VALID_TO, flexPotential.getValidTo())
            .addParam(ACTIVE, flexPotential.isActive())
            .addParam(PREQUALIFICATION, flexPotential.isProductPrequalification())
            .addParam(STATIC_GRID_PREQUALIFICATION, flexPotential.isStaticGridPrequalification())
            .build();

        Set<MinimalDTO<Long, String>> usersToBeNotified = getUsersToBeNotified(flexPotentialDTO);
        NotificationUtils.registerNewNotificationForSpecifiedUsers(notifierFactory, NotificationEvent.FP_CREATED,
            notificationParams, new ArrayList<>(usersToBeNotified));
    }

    @Override
    public void registerUpdatedNotification(FlexPotentialDTO oldFlexPotentialDTO, FlexPotentialDTO modifyFlexPotentialDTO) {
        Map<NotificationParam, NotificationParamValue> notificationParams = NotificationUtils.ParamsMapBuilder.create()
            .addParam(ID, modifyFlexPotentialDTO.getId())
            .addModificationParam(PRODUCT, oldFlexPotentialDTO.getProduct().getShortName(), modifyFlexPotentialDTO.getProduct().getShortName())
            .addModificationParam(COMPANY, oldFlexPotentialDTO.getFsp().getCompanyName(), modifyFlexPotentialDTO.getFsp().getCompanyName())
            .addModificationParam(VOLUME, oldFlexPotentialDTO.getVolume(), modifyFlexPotentialDTO.getVolume())
            .addModificationParam(VOLUME_UNIT, oldFlexPotentialDTO.getVolumeUnit(), modifyFlexPotentialDTO.getVolumeUnit())
            .addModificationParam(DIVISIBLE, oldFlexPotentialDTO.isDivisibility(), modifyFlexPotentialDTO.isDivisibility())
            .addModificationParam(MAXIMUM_FULL_ACTIVATION_TIME, oldFlexPotentialDTO.getFullActivationTime(), modifyFlexPotentialDTO.getFullActivationTime())
            .addModificationParam(MINIMUM_REQUIRED_DURATION_OF_DELIVERY, oldFlexPotentialDTO.getMinDeliveryDuration(), modifyFlexPotentialDTO.getMinDeliveryDuration())
            .addModificationParam(VALID_FROM, oldFlexPotentialDTO.getValidFrom(), modifyFlexPotentialDTO.getValidFrom())
            .addModificationParam(VALID_TO, oldFlexPotentialDTO.getValidTo(), modifyFlexPotentialDTO.getValidTo())
            .addModificationParam(ACTIVE, oldFlexPotentialDTO.isActive(), modifyFlexPotentialDTO.isActive())
            .addModificationParam(PREQUALIFICATION, oldFlexPotentialDTO.isProductPrequalification(), modifyFlexPotentialDTO.isProductPrequalification())
            .addModificationParam(STATIC_GRID_PREQUALIFICATION, oldFlexPotentialDTO.isStaticGridPrequalification(), modifyFlexPotentialDTO.isStaticGridPrequalification())
            .build();

        addDersParamIfModified(oldFlexPotentialDTO, modifyFlexPotentialDTO, notificationParams);

        Set<MinimalDTO<Long, String>> usersToBeNotified = getUsersToBeNotified(oldFlexPotentialDTO);
        NotificationUtils.registerNewNotificationForSpecifiedUsers(notifierFactory, NotificationEvent.FP_UPDATED,
            notificationParams, new ArrayList<>(usersToBeNotified));
    }

    private void addDersParamIfModified(FlexPotentialDTO oldFlexPotentialDTO, FlexPotentialDTO modifyFlexPotentialDTO, Map<NotificationParam, NotificationParamValue> notificationParams) {
        // gdy lista z DERami po edycji nie zawiera tych samych elementow co przed modyfikacja, zostaje dodany parametr z DERami
        boolean isDersChange = !CollectionUtils.isEqualCollection(modifyFlexPotentialDTO.getUnits().stream().map(UnitMinDTO::getId).collect(Collectors.toList()),
            oldFlexPotentialDTO.getUnits().stream().map(UnitMinDTO::getId).collect(Collectors.toList()));
        if (isDersChange) {
            notificationParams.put(DERS, NotificationParamValue.ParamValueBuilder.create().addParam(modifyFlexPotentialDTO.getUnits().stream().map(UnitMinDTO::getName).collect(Collectors.joining(","))).build());
        }
    }

    @Override
    public void sendMailInformingAboutModification(FlexPotentialDTO oldFlexPotentialDTO, FlexPotentialDTO modifyFlexPotentialDTO) {
        Set<MinimalDTO<Long, String>> usersToBeNotified = getUsersToBeNotified(oldFlexPotentialDTO);
        usersToBeNotified.forEach(user -> flexPotentialMailService.informUserAboutNewFlexPotentialEdition(findUserMinimalById(user.getId()), oldFlexPotentialDTO, modifyFlexPotentialDTO));
    }

    @Override
    public void sendMailInformingAboutCreation(FlexPotentialDTO flexPotentialDTO) {
        FspDTO fsp = fspService.findById(flexPotentialDTO.getFsp().getId())
            .orElseThrow(() -> new RuntimeException("FSP not found with id: " + flexPotentialDTO.getFsp().getId()));
        FlexPotentialDTO flexPotential = getMapper().toDto(flexPotentialRepository.findById(flexPotentialDTO.getId())
            .orElseThrow(() -> new RuntimeException("FlexPotential not found with id: " + flexPotentialDTO.getId())));
        Set<MinimalDTO<Long, String>> usersToBeNotified = getUsersToBeNotified(flexPotentialDTO);
        usersToBeNotified.forEach(user -> flexPotentialMailService.informUserAboutNewFlexPotentialCreation(findUserMinimalById(user.getId()), flexPotential, fsp));
    }

    @Override
    public void sendNotificationInformingAboutRegistered(FlexPotentialDTO modifyFlexPotentialDTO) {
        FlexPotentialEntity flexPotential = flexPotentialRepository.findById(modifyFlexPotentialDTO.getId())
            .orElseThrow(() -> new RuntimeException("FlexPotential not found with id: " + modifyFlexPotentialDTO.getId()));
        FspDTO fsp = fspService.findById(modifyFlexPotentialDTO.getFsp().getId())
            .orElseThrow(() -> new RuntimeException("FSP not found with id: " + modifyFlexPotentialDTO.getFsp().getId()));
        Map<NotificationParam, NotificationParamValue> notificationParams = NotificationUtils.ParamsMapBuilder.create()
            .addParam(ID, modifyFlexPotentialDTO.getId())
            .addParam(PRODUCT, flexPotential.getProduct().getShortName())
            .addParam(COMPANY, fsp.getCompanyName())
            .addParam(DERS, flexPotential.getUnits().stream().map(UnitEntity::getName).collect(Collectors.joining(",")))
            .addParam(VOLUME, flexPotential.getVolume())
            .addParam(VOLUME_UNIT, flexPotential.getVolumeUnit())
            .addParam(DIVISIBLE, flexPotential.isDivisibility())
            .addParam(MAXIMUM_FULL_ACTIVATION_TIME, flexPotential.getFullActivationTime())
            .addParam(MINIMUM_REQUIRED_DURATION_OF_DELIVERY, flexPotential.getMinDeliveryDuration())
            .addParam(VALID_FROM, flexPotential.getValidFrom())
            .addParam(VALID_TO, flexPotential.getValidTo())
            .addParam(ACTIVE, flexPotential.isActive())
            .addParam(PREQUALIFICATION, flexPotential.isProductPrequalification())
            .addParam(STATIC_GRID_PREQUALIFICATION, flexPotential.isStaticGridPrequalification())
            .build();

        Set<MinimalDTO<Long, String>> usersToBeNotified = getUsersToBeNotified(modifyFlexPotentialDTO);
        NotificationUtils.registerNewNotificationForSpecifiedUsers(notifierFactory, NotificationEvent.FP_MOVED_TO_FLEX_REGISTER,
            notificationParams, new ArrayList<>(usersToBeNotified));
    }

    @Override
    public void sendMailInformingAboutRegistered(FlexPotentialDTO modifyFlexPotentialDTO) {
        FspDTO fsp = fspService.findById(modifyFlexPotentialDTO.getFsp().getId())
            .orElseThrow(() -> new RuntimeException("FSP not found with id: " + modifyFlexPotentialDTO.getFsp().getId()));
        FlexPotentialDTO flexPotential = getMapper().toDto(flexPotentialRepository.findById(modifyFlexPotentialDTO.getId())
            .orElseThrow(() -> new RuntimeException("FlexPotential not found with id: " + modifyFlexPotentialDTO.getId())));
        Set<MinimalDTO<Long, String>> usersToBeNotified = getUsersToBeNotified(modifyFlexPotentialDTO);
        usersToBeNotified.forEach(user -> flexPotentialMailService.informUserAboutTransferOfFlexPotentialToFlexRegister(findUserMinimalById(user.getId()), flexPotential, fsp));
    }

    @Override
    public void sendNotificationAboutDeleted(FlexPotentialDTO modifyFlexPotentialDTO) {
        FspDTO fsp = fspService.findById(modifyFlexPotentialDTO.getFsp().getId())
            .orElseThrow(() -> new RuntimeException("FSP not found with id: " + modifyFlexPotentialDTO.getFsp().getId()));
        Map<NotificationParam, NotificationParamValue> notificationParams = NotificationUtils.ParamsMapBuilder.create()
            .addParam(ID, modifyFlexPotentialDTO.getId())
            .addParam(COMPANY, fsp.getCompanyName())
            .build();
        Set<MinimalDTO<Long, String>> usersToBeNotified = getUsersToBeNotified(modifyFlexPotentialDTO);
        NotificationUtils.registerNewNotificationForSpecifiedUsers(notifierFactory, NotificationEvent.FP_DELETED,
            notificationParams, new ArrayList<>(usersToBeNotified));
    }

    // Komunikat wyswietlany jest dla:
    // - 1: uzytkownika ktory stworzyl, ostatnio zmodyfikowal i aktualnie modyfikuje danego FP,
    // - 2: uzytkownikow FSP
    private Set<MinimalDTO<Long, String>> getUsersToBeNotified(FlexPotentialDTO flexPotentialDTO) {
        //Ad. 1
        UserDTO creatorUser = userService.getCurrentUserDTO().orElseThrow(() -> new RuntimeException("Current logged user not found"));
        Set<MinimalDTO<Long, String>> usersToBeNotified =
            new HashSet<>(userService.getUsersByLogin(NotificationUtils.getLoginsOfUsersToBeNotified(creatorUser.getLogin(), flexPotentialDTO)));
        //Ad. 2
        usersToBeNotified.addAll(fspService.findFspUsersMin(flexPotentialDTO.getFsp().getId()));
        return usersToBeNotified;
    }

    private UserEntity findUserMinimalById(Long id) {
        return userService.findOne(id).orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    //********************************************************************************** NOTIFICATION ************************************************************************************

    @Override
    public AbstractJpaRepository<FlexPotentialEntity, Long> getRepository() {
        return flexPotentialRepository;
    }

    @Override
    public EntityMapper<FlexPotentialDTO, FlexPotentialEntity> getMapper() {
        return flexPotentialMapper;
    }
}
