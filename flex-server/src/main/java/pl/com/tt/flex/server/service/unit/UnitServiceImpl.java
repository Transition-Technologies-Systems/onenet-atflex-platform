package pl.com.tt.flex.server.service.unit;

import static pl.com.tt.flex.model.security.permission.Role.ROLE_DISTRIBUTION_SYSTEM_OPERATOR;
import static pl.com.tt.flex.server.dataexport.exporter.offer.detail.enumeration.LevelOfDetail.STANDARD_DETAIL_SHEET;

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
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;
import pl.com.tt.flex.model.service.dto.MinimalDTO;
import pl.com.tt.flex.model.service.dto.dictionary.DictionaryType;
import pl.com.tt.flex.model.service.dto.file.FileDTO;
import pl.com.tt.flex.model.service.dto.fsp.FspCompanyMinDTO;
import pl.com.tt.flex.server.common.errors.ConcurrencyFailureException;
import pl.com.tt.flex.server.dataexport.exporter.DataExporter;
import pl.com.tt.flex.server.dataexport.factory.DataExporterFactory;
import pl.com.tt.flex.server.dataexport.factory.DataExporterFormat;
import pl.com.tt.flex.server.domain.enumeration.NotificationEvent;
import pl.com.tt.flex.server.domain.enumeration.NotificationParam;
import pl.com.tt.flex.server.domain.screen.enumeration.Screen;
import pl.com.tt.flex.server.domain.unit.DerTypeEntity;
import pl.com.tt.flex.server.domain.unit.LocalizationTypeEntity;
import pl.com.tt.flex.server.domain.unit.UnitEntity;
import pl.com.tt.flex.server.domain.unit.UnitGeoLocationEntity;
import pl.com.tt.flex.server.domain.user.UserEntity;
import pl.com.tt.flex.server.repository.unit.UnitGeoLocationRepository;
import pl.com.tt.flex.server.repository.unit.UnitRepository;
import pl.com.tt.flex.server.service.common.AbstractServiceImpl;
import pl.com.tt.flex.server.service.fsp.FspService;
import pl.com.tt.flex.server.service.mail.unit.UnitMailService;
import pl.com.tt.flex.server.service.mapper.EntityMapper;
import pl.com.tt.flex.server.service.notification.dto.NotificationParamValue;
import pl.com.tt.flex.server.service.notification.factory.NotifierFactory;
import pl.com.tt.flex.server.service.notification.util.NotificationUtils;
import pl.com.tt.flex.server.service.unit.dto.UnitDTO;
import pl.com.tt.flex.server.service.unit.dto.UnitMinDTO;
import pl.com.tt.flex.server.service.unit.mapper.UnitGeoLocationMapper;
import pl.com.tt.flex.server.service.unit.mapper.UnitMapper;
import pl.com.tt.flex.server.service.user.UserService;
import pl.com.tt.flex.server.service.user.dto.UserDTO;
import pl.com.tt.flex.server.util.DictionaryUtils;


/**
 * Service Implementation for managing {@link UnitEntity}.
 */
@Slf4j
@Service
@Transactional
public class UnitServiceImpl extends AbstractServiceImpl<UnitEntity, UnitDTO, Long> implements UnitService {

    private final UnitRepository unitRepository;
    private final UnitMapper unitMapper;
    private final UnitGeoLocationRepository unitGeoLocationRepository;
    private final DataExporterFactory dataExporterFactory;
    private final UnitMailService mailService;
    private final UserService userService;
    private final NotifierFactory notifierFactory;
    private final UnitGeoLocationMapper unitGeoLocationMapper;
    private final FspService fspService;

    public UnitServiceImpl(final UnitRepository unitRepository, final UnitMapper unitMapper, final UnitGeoLocationRepository unitGeoLocationRepository,
                           final DataExporterFactory dataExporterFactory, final UnitMailService mailService, final UserService userService,
                           final NotifierFactory notifierFactory, final UnitGeoLocationMapper unitGeoLocationMapper, final FspService fspService) {
        this.unitRepository = unitRepository;
        this.unitGeoLocationRepository = unitGeoLocationRepository;
        this.unitMapper = unitMapper;
        this.dataExporterFactory = dataExporterFactory;
        this.mailService = mailService;
        this.userService = userService;
        this.notifierFactory = notifierFactory;
        this.unitGeoLocationMapper = unitGeoLocationMapper;
        this.fspService = fspService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UnitGeoLocationEntity> findGeoLocationsOfUnit(Long unitId) {
        return unitGeoLocationRepository.findAllByUnitId(unitId);
    }

    /**
     * For each save, the 'version' column is self incremented (starts at 0).
     *
     * @throws ConcurrencyFailureException if object has been modified by another user.
     */
    @Override
    @Transactional
    public UnitDTO save(UnitEntity unitEntityToSave) {
        if (unitEntityToSave.isNew()) {
            unitEntityToSave.setSchedulingUnit(null);
            unitEntityToSave.setSubportfolio(null);
        } else {
            UnitEntity unitEntityDB = unitRepository.getOne(unitEntityToSave.getId());
            unitEntityToSave.setSchedulingUnit(unitEntityDB.getSchedulingUnit());
            unitEntityToSave.setSubportfolio(unitEntityDB.getSubportfolio());
        }
        // usuwane sa nie potrzebne spacje podczas zapisu oraz edycji derow
        unitEntityToSave.setName(StringUtils.normalizeSpace(unitEntityToSave.getName()));
        unitEntityToSave = unitRepository.save(unitEntityToSave);
        UnitDTO result = unitMapper.toDto(unitEntityToSave);
        result.setGeoLocations(unitGeoLocationMapper.toDto(findGeoLocationsOfUnit(result.getId())));
        return result;
    }

    public FileDTO exportUnitsToFile(List<UnitDTO> units, boolean isOnlyDisplayedData, Screen screen) throws IOException {
        String langKey = userService.getLangKeyForCurrentLoggedUser();
        DataExporter<UnitDTO> dataExporter = dataExporterFactory.getDataExporter(DataExporterFormat.XLSX, UnitDTO.class, screen);
        return dataExporter.export(units, Locale.forLanguageTag(langKey), screen, isOnlyDisplayedData, STANDARD_DETAIL_SHEET);
    }

    @Override
    @Transactional
    public void deactivateUnitsByValidFromToDates() {
        List<UnitEntity> expiredUnits = unitRepository.findUnitsToDeactivateByValidFromToDates();
        expiredUnits.forEach(unit -> {
            log.debug("deactivateUnitsByValidFromToDates() Deactivating Unit [id: {}]", unit.getId());
            unit.setActive(false);
        });
    }

    @Override
    @Transactional
    public void activateUnitsByValidFromToDates() {
        List<UnitEntity> unitsToActivate = unitRepository.findUnitsToActivateByValidFromToDates();
        unitsToActivate.forEach(unit -> {
            log.debug("activateUnitsByValidFromToDates() Activating Unit [id: {}]", unit.getId());
            unit.setActive(true);
        });
    }

    @Override
    public void sendInformingAboutUnitCreation(UnitDTO unitRequest, UnitDTO unitResult) {
        UnitEntity unitEntity = unitRepository.findById(unitResult.getId()).get();
        if (unitRequest.getId() == null) {
            registerNewNotification(unitEntity, NotificationEvent.UNIT_CREATED);
            if (unitResult.isCertified()) {
                sendInformingAboutUnitCertification(unitEntity);
                registerNewNotification(unitEntity, NotificationEvent.UNIT_HAS_BEEN_CERTIFIED);
            }
        }
    }

    @Override
    public void sendInformingAboutUnitModification(UnitDTO oldUnit, UnitDTO modifiedUnit) {
        UnitEntity unitEntity = unitRepository.findById(modifiedUnit.getId()).get();
        oldUnit.setGeoLocations(unitGeoLocationMapper.toDto(findGeoLocationsOfUnit(oldUnit.getId())));
        registerNewNotificationForUnitEdition(unitMapper.toEntity(oldUnit), unitEntity);
        if (!oldUnit.isCertified() && modifiedUnit.isCertified()) {
            sendInformingAboutUnitCertification(unitEntity);
            registerNewNotification(unitEntity, NotificationEvent.UNIT_HAS_BEEN_CERTIFIED);
        }
        if (oldUnit.isCertified() && !modifiedUnit.isCertified()) {
            sendInformingAboutUnitCertification(unitEntity);
            registerNewNotification(unitEntity, NotificationEvent.UNIT_LOST_CERTIFICATION);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<UnitMinDTO> getAllForSubportfolioModalSelect(Long fspaId, Long subportfolioId) {
        List<UnitEntity> unitEntities = unitRepository.getAllForSubportfolioModalSelect(fspaId, subportfolioId);
        return unitMapper.toMinDto(unitEntities);
    }

    private void sendInformingAboutUnitCertification(UnitEntity unitEntity) {
        Set<UserEntity> users = unitEntity.getFsp().getUsers();
        if (unitEntity.isCertified()) {
            users.forEach(user -> mailService.informFspAboutUnitCertified(user, unitMapper.toDto(unitEntity)));
        } else {
            users.forEach(user -> mailService.informFspAboutUnitLostCertification(user, unitMapper.toDto(unitEntity)));
        }
    }

    private Set<MinimalDTO<Long, String>> getUsersToBeNotified(UnitEntity unitEntity, NotificationEvent event) {
        UserDTO creator = userService.getCurrentUserDTO().orElseThrow(() -> new RuntimeException("Current logged user not found"));
        Set<MinimalDTO<Long, String>> usersToBeNotified = new HashSet<>(userService.getUsersByLogin(NotificationUtils.getLoginsOfUsersToBeNotified(creator.getLogin(), unitEntity)));
        usersToBeNotified.addAll(fspService.findFspUsersMin(unitEntity.getFsp().getId()));
        if (NotificationEvent.UNIT_CREATED.equals(event)) {
            usersToBeNotified.addAll(userService.getUsersByRolesMinimal(Set.of(ROLE_DISTRIBUTION_SYSTEM_OPERATOR)));
        }
        return usersToBeNotified;
    }

    private void registerNewNotificationForUnitEdition(UnitEntity oldUnitEntity, UnitEntity unitEntity) {
        String couplingPointIdTypes = unitEntity.getCouplingPointIdTypes().stream().map(LocalizationTypeEntity::getName).collect(Collectors.joining(", "));
        String oldCouplingPointIdTypes = oldUnitEntity.getCouplingPointIdTypes().stream().map(LocalizationTypeEntity::getName).collect(Collectors.joining(", "));
        String powerStation = unitEntity.getPowerStationTypes().stream().map(LocalizationTypeEntity::getName).collect(Collectors.joining(", "));
        String oldPowerStation = oldUnitEntity.getPowerStationTypes().stream().map(LocalizationTypeEntity::getName).collect(Collectors.joining(", "));
        String pointOfConnectionWithLvTypes = unitEntity.getPointOfConnectionWithLvTypes().stream().map(LocalizationTypeEntity::getName).collect(Collectors.joining(", "));
        String oldPointOfConnectionWithLvTypes = oldUnitEntity.getPointOfConnectionWithLvTypes().stream().map(LocalizationTypeEntity::getName).collect(Collectors.joining(", "));
        Map<NotificationParam, NotificationParamValue> notificationParams = NotificationUtils.ParamsMapBuilder.create()
            .addParam(NotificationParam.ID, unitEntity.getId())
            .addParam(NotificationParam.UNIT_NAME, unitEntity.getName())
            .addModificationParam(NotificationParam.UNIT_CODE, oldUnitEntity.getCode(), unitEntity.getCode())
            .addModificationParam(NotificationParam.POWER, oldUnitEntity.getSourcePower(), unitEntity.getSourcePower())
            .addModificationParam(NotificationParam.CONNECTION_POWER, oldUnitEntity.getConnectionPower(), unitEntity.getConnectionPower())
            .addModificationParam(NotificationParam.DIRECTION_OF_DEVIATION, oldUnitEntity.getDirectionOfDeviation(), unitEntity.getDirectionOfDeviation())
            .addModificationParam(NotificationParam.VALID_FROM, oldUnitEntity.getValidFrom(), unitEntity.getValidFrom())
            .addModificationParam(NotificationParam.VALID_TO, oldUnitEntity.getValidTo(), unitEntity.getValidTo())
            .addModificationParam(NotificationParam.PPE, oldUnitEntity.getPpe(), unitEntity.getPpe())
            .addModificationParam(NotificationParam.COUPLING_POINT_ID, oldCouplingPointIdTypes, couplingPointIdTypes)
            .addModificationParam(NotificationParam.POWER_STATION, oldPowerStation, powerStation)
            .addModificationParam(NotificationParam.MRID, oldUnitEntity.getMridTso(), unitEntity.getMridTso())
            .addModificationParam(NotificationParam.MRID_DSO, oldUnitEntity.getMridDso(), unitEntity.getMridDso())
            .addModificationParam(NotificationParam.DER_TYPE_RECEPTION, getDerNlsCodeOrNull(oldUnitEntity.getDerTypeReception()), getDerNlsCodeOrNull(unitEntity.getDerTypeReception()))
            .addModificationParam(NotificationParam.DER_TYPE_ENERGY_STORAGE, getDerNlsCodeOrNull(oldUnitEntity.getDerTypeEnergyStorage()), getDerNlsCodeOrNull(unitEntity.getDerTypeEnergyStorage()))
            .addModificationParam(NotificationParam.DER_TYPE_GENERATION, getDerNlsCodeOrNull(oldUnitEntity.getDerTypeGeneration()), getDerNlsCodeOrNull(unitEntity.getDerTypeGeneration()))
            .addModificationParam(NotificationParam.ACTIVE, oldUnitEntity.isActive(), unitEntity.isActive())
            .addModificationParam(NotificationParam.AGGREGATED, oldUnitEntity.isAggregated(), unitEntity.isAggregated())
            .addModificationParam(NotificationParam.CERTIFIED, oldUnitEntity.isCertified(), unitEntity.isCertified())
            .addModificationParam(NotificationParam.COMPANY, oldUnitEntity.getFsp().getCompanyName(), unitEntity.getFsp().getCompanyName())
            .addModificationParam(NotificationParam.P_MIN, oldUnitEntity.getPMin(), unitEntity.getPMin())
            .addModificationParam(NotificationParam.Q_MIN, oldUnitEntity.getQMin(), unitEntity.getQMin())
            .addModificationParam(NotificationParam.Q_MAX, oldUnitEntity.getQMax(), unitEntity.getQMax())
            .addModificationParam(NotificationParam.POINT_OF_CONNECTION_WITH_LV, oldPointOfConnectionWithLvTypes, pointOfConnectionWithLvTypes)
            .build();
        NotificationEvent event = NotificationEvent.UNIT_UPDATED;
        Set<MinimalDTO<Long, String>> usersToBeNotified = getUsersToBeNotified(unitEntity, event);
        NotificationUtils.registerNewNotificationForSpecifiedUsers(notifierFactory, event, notificationParams, new ArrayList<>(usersToBeNotified));
        usersToBeNotified.forEach(user -> mailService.informFspAboutUnitModification(findUserMinimalById(user.getId()), unitMapper.toDto(oldUnitEntity), unitMapper.toDto(unitEntity)));
    }

    private void registerNewNotification(UnitEntity unitEntity, NotificationEvent event) {
        Set<MinimalDTO<Long, String>> usersToBeNotified = getUsersToBeNotified(unitEntity, event);
        Set<LocalizationTypeEntity> couplingPointTypes = unitEntity.getCouplingPointIdTypes();
        Set<LocalizationTypeEntity> powerStationTypes = unitEntity.getPowerStationTypes();
        Set<LocalizationTypeEntity> pointOfConnectionWithLvTypes = unitEntity.getPointOfConnectionWithLvTypes();
        Map<NotificationParam, NotificationParamValue> notificationParams = NotificationUtils.ParamsMapBuilder.create()
            .addParam(NotificationParam.ID, unitEntity.getId())
            .addParam(NotificationParam.UNIT_CODE, unitEntity.getCode())
            .addParam(NotificationParam.POWER, unitEntity.getSourcePower())
            .addParam(NotificationParam.CONNECTION_POWER, unitEntity.getConnectionPower())
            .addParam(NotificationParam.DIRECTION_OF_DEVIATION, unitEntity.getDirectionOfDeviation())
            .addParam(NotificationParam.VALID_FROM, unitEntity.getValidFrom())
            .addParam(NotificationParam.VALID_TO, unitEntity.getValidTo())
            .addParam(NotificationParam.PPE, unitEntity.getPpe())
            .addParam(NotificationParam.COUPLING_POINT_ID, CollectionUtils.isNotEmpty(couplingPointTypes) ? couplingPointTypes.stream().map(LocalizationTypeEntity::getName).collect(Collectors.joining(", ")) : null)
            .addParam(NotificationParam.POWER_STATION, CollectionUtils.isNotEmpty(powerStationTypes) ? powerStationTypes.stream().map(LocalizationTypeEntity::getName).collect(Collectors.joining(", ")) : null)
            .addParam(NotificationParam.MRID, unitEntity.getMridTso())
            .addParam(NotificationParam.MRID_DSO, unitEntity.getMridDso())
            .addParam(NotificationParam.ACTIVE, unitEntity.isActive())
            .addParam(NotificationParam.AGGREGATED, unitEntity.isAggregated())
            .addParam(NotificationParam.CERTIFIED, unitEntity.isCertified())
            .addParam(NotificationParam.UNIT_NAME, unitEntity.getName())
            .addParam(NotificationParam.DER_TYPE_RECEPTION, getDerNlsCodeOrNull(unitEntity.getDerTypeReception()))
            .addParam(NotificationParam.DER_TYPE_ENERGY_STORAGE, getDerNlsCodeOrNull(unitEntity.getDerTypeEnergyStorage()))
            .addParam(NotificationParam.DER_TYPE_GENERATION, getDerNlsCodeOrNull(unitEntity.getDerTypeGeneration()))
            .addParam(NotificationParam.COMPANY, unitEntity.getFsp().getCompanyName())
            .addParam(NotificationParam.P_MIN, unitEntity.getPMin())
            .addParam(NotificationParam.Q_MIN, unitEntity.getQMin())
            .addParam(NotificationParam.Q_MAX, unitEntity.getQMax())
            .addParam(NotificationParam.POINT_OF_CONNECTION_WITH_LV, CollectionUtils.isNotEmpty(pointOfConnectionWithLvTypes) ? pointOfConnectionWithLvTypes.stream().map(LocalizationTypeEntity::getName).collect(Collectors.joining(", ")) : null)
            .build();
        if (event == NotificationEvent.UNIT_CREATED) {
            NotificationUtils.registerNewNotificationForSpecifiedUsers(notifierFactory, event, notificationParams, new ArrayList<>(usersToBeNotified));
            usersToBeNotified.forEach(user -> mailService.informFspAboutNewUnitCreation(findUserMinimalById(user.getId()), unitMapper.toDto(unitEntity)));
        }
    }

    private UserEntity findUserMinimalById(Long id) {
        return userService.findOne(id).orElseThrow(() -> new RuntimeException("Could not find user with ID" + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<UnitMinDTO> getSchedulingUnitDers(Long schedulingUnitId) {
        return unitRepository.findBySchedulingUnitIdOrderByIdDesc(schedulingUnitId).stream().map(unitMapper::toMinDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UnitMinDTO> getFspSchedulingUnitDers(Long schedulingUnitId, Long fspId) {
        return unitRepository.findBySchedulingUnitIdAndFspIdOrderByIdDesc(schedulingUnitId, fspId).stream().map(unitMapper::toMinDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByNameLowerCaseAndIdNot(String name, Long id) {
        return unitRepository.existsByNameIgnoreCaseAndIdNot(name, id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByNameLowerCase(String name) {
        return unitRepository.existsByNameIgnoreCase(name);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UnitMinDTO> getAllByFspId(Long fspId) {
        return unitMapper.toMinDto(unitRepository.findAllByFspIdAndCertifiedTrue(fspId));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByFspIdAndSchedulingUnitBspId(Long fspId, Long bspId) {
        return unitRepository.existsByFspIdAndSchedulingUnitBspId(fspId, bspId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByFspIdAndSchedulingUnitBspIdNot(Long fspId, Long bspId) {
        return unitRepository.existsByFspIdAndSchedulingUnitBspIdNot(fspId, bspId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UnitMinDTO> findAllBySubportfolioIdAndSchedulingUnitIsNull(Long subportfolioId) {
        return unitRepository.findAllBySubportfolioIdAndSchedulingUnitIsNull(subportfolioId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsBySubportfolioIdAndSchedulingUnitBspIdNot(Long subportfolioId, Long bspId) {
        return unitRepository.existsBySubportfolioIdAndSchedulingUnitBspIdNot(subportfolioId, bspId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UnitMinDTO> findDersNameAndFsp(List<Long> dersToRemove) {
        return unitRepository.findDersNameAndFsp(dersToRemove);
    }


    @Override
    @Transactional(readOnly = true)
    public Optional<UnitMinDTO> findUnitByNameIgnoreCase(String derName) {
        return unitRepository.findUnitByName(derName.strip());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> findDerRegisteredPotentialsProductsIds(Long derId) {
        return unitRepository.findDerRegisteredPotentialsProductsIds(derId);
    }

    @Override
    public FspCompanyMinDTO getDerFspMin(Long derId) {
        return unitRepository.getDerFspMin(derId);
    }

    @Override
    public UnitEntity getById(Long id) {
        return unitRepository.findById(id).orElseThrow(() -> new RuntimeException("Could not find unit with ID " + id));
    }

    @Override
    public UnitEntity getByName(String name) {
        return unitRepository.findByName(name).orElseThrow(() -> new RuntimeException("Could not find unit with name " + name));
    }

    @Override
    public Optional<UnitMinDTO> findByCodeAndFspCompanyName(String unitCode, String fspCompanyName) {
        return unitRepository.findByCodeAndFspName(unitCode, fspCompanyName);
    }

    @Override
    public List<UnitMinDTO> findAllWithoutSubportfolioByFspId(Long fspaId) {
        return unitRepository.findAllByFspIdAndSubportfolioIdNullAndCertifiedTrue(fspaId);
    }

    @Override
    public boolean existsByFspIdAndNoSubportfolioAndNoSchedulingUnit(Long fspId) {
        return unitRepository.existsByFspIdAndSubportfolioNullAndSchedulingUnitNullAndCertifiedTrueAndActiveTrue(fspId);
    }

    @Override
    public UnitRepository getRepository() {
        return unitRepository;
    }

    @Override
    public EntityMapper<UnitDTO, UnitEntity> getMapper() {
        return unitMapper;
    }

    private String getDerNlsCodeOrNull(DerTypeEntity typeEntity) {
        return Optional.ofNullable(typeEntity)
            .map(DerTypeEntity::getDescriptionEn)
            .map(descriptionEn -> DictionaryUtils.getNlsCode(DictionaryType.DER_TYPE, descriptionEn))
            .orElse(null);
    }
}
