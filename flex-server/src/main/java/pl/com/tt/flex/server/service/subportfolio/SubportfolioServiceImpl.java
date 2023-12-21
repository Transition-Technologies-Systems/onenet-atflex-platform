package pl.com.tt.flex.server.service.subportfolio;

import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.compress.utils.Lists;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.com.tt.flex.model.security.permission.Role;
import pl.com.tt.flex.model.service.dto.localization.LocalizationTypeDTO;
import pl.com.tt.flex.server.dataexport.exporter.DataExporter;
import pl.com.tt.flex.server.dataexport.factory.DataExporterFactory;
import pl.com.tt.flex.server.dataexport.factory.DataExporterFormat;
import pl.com.tt.flex.server.domain.enumeration.NotificationEvent;
import pl.com.tt.flex.server.domain.enumeration.NotificationParam;
import pl.com.tt.flex.server.domain.screen.enumeration.Screen;
import pl.com.tt.flex.server.domain.subportfolio.SubportfolioEntity;
import pl.com.tt.flex.server.domain.subportfolio.SubportfolioFileEntity;
import pl.com.tt.flex.server.domain.unit.LocalizationTypeEntity;
import pl.com.tt.flex.server.domain.user.UserEntity;
import pl.com.tt.flex.server.repository.subportfolio.SubportfolioFileRepository;
import pl.com.tt.flex.server.repository.subportfolio.SubportfolioRepository;
import pl.com.tt.flex.server.repository.unit.UnitRepository;
import pl.com.tt.flex.server.service.common.AbstractServiceImpl;
import pl.com.tt.flex.model.service.dto.file.FileDTO;
import pl.com.tt.flex.model.service.dto.MinimalDTO;
import pl.com.tt.flex.server.service.dictionary.localizationType.mapper.LocalizationTypeMapper;
import pl.com.tt.flex.server.service.fsp.FspService;
import pl.com.tt.flex.server.service.mail.subportfolio.SubportfolioMailService;
import pl.com.tt.flex.server.service.mapper.EntityMapper;
import pl.com.tt.flex.server.service.notification.dto.NotificationParamValue;
import pl.com.tt.flex.server.service.notification.factory.NotifierFactory;
import pl.com.tt.flex.server.service.notification.util.NotificationUtils;
import pl.com.tt.flex.server.service.subportfolio.dto.SubportfolioDTO;
import pl.com.tt.flex.server.service.subportfolio.dto.SubportfolioMinDTO;
import pl.com.tt.flex.server.service.subportfolio.mapper.SubportfolioMapper;
import pl.com.tt.flex.server.service.unit.dto.UnitDTO;
import pl.com.tt.flex.server.service.unit.dto.UnitMinDTO;
import pl.com.tt.flex.server.service.user.UserService;
import pl.com.tt.flex.server.service.user.dto.UserDTO;
import pl.com.tt.flex.server.util.ZipUtil;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static pl.com.tt.flex.model.security.permission.Role.ROLE_DISTRIBUTION_SYSTEM_OPERATOR;
import static pl.com.tt.flex.server.dataexport.exporter.offer.detail.enumeration.LevelOfDetail.STANDARD_DETAIL_SHEET;
import static pl.com.tt.flex.server.domain.enumeration.NotificationParam.*;

/**
 * Service Implementation for managing {@link SubportfolioEntity}.
 */
@Slf4j
@Service
@Transactional
public class SubportfolioServiceImpl extends AbstractServiceImpl<SubportfolioEntity, SubportfolioDTO, Long> implements SubportfolioService {

    private final SubportfolioRepository subportfolioRepository;
    private final UnitRepository unitRepository;
    private final SubportfolioMapper subportfolioMapper;
    private final SubportfolioFileRepository subportfolioFileRepository;
    private final DataExporterFactory dataExporterFactory;
    private final UserService userService;
    private final LocalizationTypeMapper localizationTypeMapper;
    private final NotifierFactory notifierFactory;
    private final SubportfolioMailService subportfolioMailService;
    private final FspService fspService;


    public SubportfolioServiceImpl(SubportfolioRepository subportfolioRepository, UnitRepository unitRepository, SubportfolioMapper subportfolioMapper,
                                   SubportfolioFileRepository subportfolioFileRepository, DataExporterFactory dataExporterFactory, UserService userService, LocalizationTypeMapper localizationTypeMapper, NotifierFactory notifierFactory, SubportfolioMailService subportfolioMailService, FspService fspService) {
        this.subportfolioRepository = subportfolioRepository;
        this.unitRepository = unitRepository;
        this.subportfolioMapper = subportfolioMapper;
        this.subportfolioFileRepository = subportfolioFileRepository;
        this.dataExporterFactory = dataExporterFactory;
        this.userService = userService;
        this.localizationTypeMapper = localizationTypeMapper;
        this.notifierFactory = notifierFactory;
        this.subportfolioMailService = subportfolioMailService;
        this.fspService = fspService;
    }

    @Override
    @Transactional
    public SubportfolioDTO save(SubportfolioDTO dtoToSave, List<Long> filesToRemove) {
        SubportfolioEntity entityToSave = subportfolioMapper.toEntity(dtoToSave);
        if (!entityToSave.isNew()) {
            SubportfolioEntity subportfolioEntityFromDb = subportfolioRepository.getOne(entityToSave.getId());
            UserDTO currentUser = userService.getCurrentUserDTO().get();
            if (currentUser.hasAnyRole(Sets.newHashSet(Role.ROLE_TRANSMISSION_SYSTEM_OPERATOR))) {
                subportfolioEntityFromDb.setCertified(dtoToSave.isCertified());
                return saveSubportfolio(subportfolioEntityFromDb);
            } else if (currentUser.hasAnyRole(Sets.newHashSet(Role.ROLE_DISTRIBUTION_SYSTEM_OPERATOR))) {
                subportfolioEntityFromDb.setCertified(dtoToSave.isCertified());
                Set<LocalizationTypeEntity> couplingPointTypes = Sets.newHashSet(localizationTypeMapper.toEntity(dtoToSave.getCouplingPointIdTypes()));
                subportfolioEntityFromDb.setCouplingPointIdTypes(couplingPointTypes);
                subportfolioEntityFromDb.setMrid(dtoToSave.getMrid());
                return saveSubportfolio(subportfolioEntityFromDb);
            }
            updateFiles(subportfolioEntityFromDb, entityToSave, filesToRemove);
        }
        linkDERs(entityToSave, dtoToSave.getUnitIds());
        return saveSubportfolio(entityToSave);
    }

    public SubportfolioDTO saveSubportfolio(SubportfolioEntity entityToSave) {
        SubportfolioEntity subportfolioEntity = subportfolioRepository.save(entityToSave);
        return subportfolioMapper.toDto(subportfolioEntity);
    }

    /**
     * Do Subportfolio maja byc podpiete po prostu DERy wybrane w formularzu edycji tego Subportfolio (dto.unitIds).
     */
    private void linkDERs(SubportfolioEntity entityToSave, List<Long> unitIds) {
        if (!entityToSave.isNew()) {
            detachAllDersFromSubportfolio(entityToSave.getId());
        }
        //zapis zmapowanej listy DERow z SubportfolioMapper (dto.unitIds -> entity.Units) powoduje blad constraint violations np. interpolatedMessage='must not be null', propertyPath=connectionPower,
        //dlatego pobieramy DERy z bazy danych na podstawie listy przekazanej z frontu
        entityToSave.setUnits(new HashSet<>(unitRepository.findAllById(unitIds)));
        entityToSave.getUnits().forEach(unitEntity -> unitEntity.setSubportfolio(entityToSave));
    }

    private void detachAllDersFromSubportfolio(Long subportfolioId) {
        subportfolioRepository.detachAllDersFromSubportfolio(subportfolioId);
    }

    private void updateFiles(SubportfolioEntity from, SubportfolioEntity to, List<Long> filesToRemove) {
        for (SubportfolioFileEntity dbFile : from.getFiles()) {
            if (!filesToRemove.contains(dbFile.getId())) {
                to.getFiles().add(dbFile);
            } else {
                to.getFiles().remove(dbFile);
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SubportfolioFileEntity> getSubportfolioFileByFileId(Long fileId) {
        return subportfolioFileRepository.findById(fileId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FileDTO> getZipWithAllFilesOfSubportfolio(Long sunportfolioId) {
        List<SubportfolioFileEntity> fileEntities = subportfolioFileRepository.findAllBySubportfolioId(sunportfolioId);
        List<FileDTO> fileDTOS = Lists.newArrayList();
        fileEntities.forEach(entity -> fileDTOS.add(new FileDTO(entity.getFileName(), ZipUtil.zipToFiles(entity.getFileZipData()).get(0).getBytesData())));
        return fileDTOS;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SubportfolioDTO> findByIdAndFspaId(Long id, Long fspId) {
        return subportfolioRepository.findByIdAndFspaId(id, fspId).map(subportfolioEntity -> getMapper().toDto(subportfolioEntity));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SubportfolioFileEntity> getSubportfolioFileByFileIdAndFspaId(Long fileId, Long fspId) {
        return subportfolioFileRepository.findByIdAndSubportfolioFspaId(fileId, fspId);
    }

    @Override
    @Transactional(readOnly = true)
    public FileDTO exportSubportfoliosToFile(List<SubportfolioDTO> subportfolioToExport, String langKey, boolean isOnlyDisplayedData, Screen screen) throws IOException {
        DataExporter<SubportfolioDTO> dataExporter = dataExporterFactory.getDataExporter(DataExporterFormat.XLSX, SubportfolioDTO.class, screen);
        return dataExporter.export(subportfolioToExport, Locale.forLanguageTag(langKey), screen, isOnlyDisplayedData, STANDARD_DETAIL_SHEET);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> findByUnit(UnitDTO unitDTO) {
        return subportfolioRepository.findByUnit(unitDTO.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubportfolioEntity> findByFspaId(Long fspaId) {
        return subportfolioRepository.findAllByFspaId(fspaId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubportfolioEntity> findAllCertifiedByFspaId(Long fspaId) {
        return subportfolioRepository.findAllCertifiedByFspaId(fspaId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsBySubportfolioIdAndFspaId(Long subportfolioId, Long fspaId) {
        return subportfolioRepository.existsByIdAndFspaId(subportfolioId, fspaId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> findAllSubportfolioNames() {
        return subportfolioRepository.findNames();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubportfolioMinDTO> findAllFspaCertifiedSubportfoliosMin(Long fspaId) {
        return subportfolioRepository.findAllFspaCertifiedSubportfoliosMin(fspaId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> findAllDerIdsFromDerSubportfolio(Long derId) {
        return subportfolioRepository.findAllDerIdsFromDerSubportfolio(derId);
    }

    //********************************************************************************** NOTIFICATION ************************************************************************************
    @Override
    @Transactional
    public void registerNewNotificationForSubportfolioCreation(SubportfolioDTO subportfolioDTO) {
        SubportfolioDTO dbSubportfolio = findById(subportfolioDTO.getId()).orElseThrow(() -> new RuntimeException("Cannot find Suportfolio with id: " + subportfolioDTO.getId()));
        Map<NotificationParam, NotificationParamValue> notificationParams = NotificationUtils.ParamsMapBuilder.create()
            .addParam(ID, dbSubportfolio.getId())
            .addParam(FSP_NAME, dbSubportfolio.getFspa().getCompanyName())
            .addParam(SUBPORTFOLIO_NAME, dbSubportfolio.getName())
            .addParam(SUBPORTFOLIO_MRID, dbSubportfolio.getMrid())
            .addParam(VALID_FROM, dbSubportfolio.getValidFrom())
            .addParam(VALID_TO, dbSubportfolio.getValidTo())
            .addParam(ACTIVE, dbSubportfolio.getActive())
            .addParam(CERTIFIED, dbSubportfolio.isCertified())
            .build();

        addDersParam(subportfolioDTO, notificationParams);
        addCouplingPointsParam(subportfolioDTO, notificationParams);
        Set<MinimalDTO<Long, String>> usersToBeNotified = getUsersToBeNotifiedWithFsp(dbSubportfolio, NotificationEvent.SUBPORTFOLIO_CREATED);
        NotificationUtils.registerNewNotificationForSpecifiedUsers(notifierFactory, NotificationEvent.SUBPORTFOLIO_CREATED, notificationParams, new ArrayList<>(usersToBeNotified));
    }


    private void addDersParam(SubportfolioDTO subportfolioDTO, Map<NotificationParam, NotificationParamValue> notificationParams) {
        List<UnitMinDTO> units = subportfolioDTO.getUnits();
        if (!CollectionUtils.isEmpty(units)) {
            notificationParams.put(SUBPORTFOLIO_DERS, NotificationParamValue.ParamValueBuilder
                .create()
                .addParam(units.stream().map(UnitMinDTO::getName).collect(Collectors.joining(", ")))
                .build());
        }
    }

    private void addCouplingPointsParam(SubportfolioDTO subportfolioDTO, Map<NotificationParam, NotificationParamValue> notificationParams) {
        List<LocalizationTypeDTO> couplingPoints = subportfolioDTO.getCouplingPointIdTypes();
        if (!CollectionUtils.isEmpty(couplingPoints)) {
            notificationParams.put(SUBPORTFOLIO_COUPLING_POINS_ID, NotificationParamValue.ParamValueBuilder
                .create()
                .addParam(couplingPoints.stream().map(LocalizationTypeDTO::getName).collect(Collectors.joining(", ")))
                .build());
        }
    }

    @Override
    @Transactional
    public void sendMailInformingAboutCreation(SubportfolioDTO subportfolioDTO) {
        SubportfolioDTO dbSubportfolioDTO = getMapper().toDto(subportfolioRepository.findById(subportfolioDTO.getId())
            .orElseThrow(() -> new RuntimeException("Subportfolio not found with id: " + subportfolioDTO.getId())));
        Set<MinimalDTO<Long, String>> usersToBeNotified = getUsersToBeNotifiedWithFsp(dbSubportfolioDTO, NotificationEvent.SUBPORTFOLIO_CREATED);
        usersToBeNotified.forEach(user -> subportfolioMailService.informUserAboutSubportfolioCreation(findUserByLogin(user.getValue()), dbSubportfolioDTO));
    }

    @Override
    @Transactional
    public void registerNewNotificationForSubportfolioEdition(SubportfolioDTO modifiedSubportolio, SubportfolioDTO oldSubportfolio) {
        SubportfolioDTO dbSubportfolio = findById(modifiedSubportolio.getId()).orElseThrow(() -> new RuntimeException("Cannot find Suportfolio with id: " + modifiedSubportolio.getId()));
        Map<NotificationParam, NotificationParamValue> notificationParams = NotificationUtils.ParamsMapBuilder.create()
            .addParam(ID, dbSubportfolio.getId())
            .addParam(SUBPORTFOLIO_NAME, dbSubportfolio.getName())
            .addModificationParam(SUBPORTFOLIO_MRID, oldSubportfolio.getMrid(), dbSubportfolio.getMrid())
            .addModificationParam(VALID_FROM, oldSubportfolio.getValidFrom(), dbSubportfolio.getValidFrom())
            .addModificationParam(VALID_TO, oldSubportfolio.getValidTo(), dbSubportfolio.getValidTo())
            .addModificationParam(ACTIVE, oldSubportfolio.getActive(), dbSubportfolio.getActive())
            .addModificationParam(CERTIFIED, oldSubportfolio.isCertified(), dbSubportfolio.isCertified())
            .build();

        addDersParamIfModified(oldSubportfolio, dbSubportfolio, notificationParams);
        addCouplingPointsIdParamIfModified(oldSubportfolio, dbSubportfolio, notificationParams);
        Set<MinimalDTO<Long, String>> usersToBeNotified = getUsersToBeNotifiedWithFsp(oldSubportfolio, NotificationEvent.SUBPORTFOLIO_UPDATED);
        NotificationUtils.registerNewNotificationForSpecifiedUsers(notifierFactory, NotificationEvent.SUBPORTFOLIO_UPDATED, notificationParams, new ArrayList<>(usersToBeNotified));
    }

    private void addDersParamIfModified(SubportfolioDTO oldSubportfolioDTO, SubportfolioDTO modifySubportfolioDTO, Map<NotificationParam, NotificationParamValue> notificationParams) {
        // gdy lista z DERami po edycji nie zawiera tych samych elementow co przed modyfikacja, zostaje dodany parametr z DERami
        boolean isDersChange = !CollectionUtils.isEqualCollection(modifySubportfolioDTO.getUnits().stream().map(UnitMinDTO::getId).collect(Collectors.toList()),
            oldSubportfolioDTO.getUnits().stream().map(UnitMinDTO::getId).collect(Collectors.toList()));
        if (isDersChange) {
            notificationParams.put(SUBPORTFOLIO_DERS, NotificationParamValue.ParamValueBuilder.create()
                .addParam(modifySubportfolioDTO.getUnits().stream().map(UnitMinDTO::getName).collect(Collectors.joining(","))).build());
        }
    }

    private void addCouplingPointsIdParamIfModified(SubportfolioDTO oldSubportfolioDTO, SubportfolioDTO modifySubportfolioDTO, Map<NotificationParam, NotificationParamValue> notificationParams) {
        // gdy lista z CouplingPointsId po edycji nie zawiera tych samych elementow co przed modyfikacja, zostaje dodany parametr z CouplingPointsId
        boolean isCouplingPointsChange = !CollectionUtils.isEqualCollection(modifySubportfolioDTO.getCouplingPointIdTypes().stream().map(LocalizationTypeDTO::getId).collect(Collectors.toList()),
            oldSubportfolioDTO.getCouplingPointIdTypes().stream().map(LocalizationTypeDTO::getId).collect(Collectors.toList()));
        if (isCouplingPointsChange) {
            notificationParams.put(SUBPORTFOLIO_COUPLING_POINS_ID, NotificationParamValue.ParamValueBuilder.create()
                .addParam(modifySubportfolioDTO.getCouplingPointIdTypes().stream().map(LocalizationTypeDTO::getName).collect(Collectors.joining(","))).build());
        }
    }

    @Override
    @Transactional
    public void sendMailInformingAboutModification(SubportfolioDTO subportfolioDTO, SubportfolioDTO oldSubportfolio) {
        SubportfolioDTO dbSubportfolioDTO = getMapper().toDto(subportfolioRepository.findById(subportfolioDTO.getId())
            .orElseThrow(() -> new RuntimeException("Subportfolio not found with id: " + subportfolioDTO.getId())));
        Set<MinimalDTO<Long, String>> usersToBeNotifiedWithFsp = getUsersToBeNotifiedWithFsp(subportfolioDTO, NotificationEvent.SUBPORTFOLIO_UPDATED);
        usersToBeNotifiedWithFsp.forEach(user -> subportfolioMailService.informUserAboutSubportfolioEdition(findUserByLogin(user.getValue()), dbSubportfolioDTO, oldSubportfolio));
    }

    // Komunikat wyswietlany jest dla:
    // - 1: uzytkownika ktory stworzyl, ostatnio zmodyfikowal i aktualnie modyfikuje danego Subportfolio,
    // - 2: uzytkownikow Subportfolio
    // - 3: wszyscy DSO jeśli powiadomienie dotyczy utworzenia nowego subportfolio
    private Set<MinimalDTO<Long, String>> getUsersToBeNotifiedWithFsp(SubportfolioDTO subportfolioDTO, NotificationEvent event) {
        //Ad. 1
        UserDTO creatorUser = userService.getCurrentUserDTO().orElseThrow(() -> new RuntimeException("Current logged user not found"));
        Set<MinimalDTO<Long, String>> usersToBeNotified =
            new HashSet<>(userService.getUsersByLogin(NotificationUtils.getLoginsOfUsersToBeNotified(creatorUser.getLogin(), subportfolioDTO)));
        //Ad. 2
        usersToBeNotified.addAll(fspService.findFspUsersMin(subportfolioDTO.getFspa().getId()));
        //Ad. 3
        if (NotificationEvent.SUBPORTFOLIO_CREATED.equals(event)) {
            usersToBeNotified.addAll(userService.getUsersByRolesMinimal(Set.of(ROLE_DISTRIBUTION_SYSTEM_OPERATOR)));
        }
        return usersToBeNotified;
    }

    private UserEntity findUserByLogin(String login) {
        return userService.findOneByLogin(login).orElseThrow(() -> new RuntimeException("User not found with login: " + login));
    }

    //********************************************************************************** NOTIFICATION ************************************************************************************


    @Override
    public SubportfolioRepository getRepository() {
        return this.subportfolioRepository;
    }

    @Override
    public EntityMapper<SubportfolioDTO, SubportfolioEntity> getMapper() {
        return this.subportfolioMapper;
    }
}
