package pl.com.tt.flex.server.service.schedulingUnit;

import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.compress.utils.Lists;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.com.tt.flex.model.security.permission.Role;
import pl.com.tt.flex.model.service.dto.fsp.FspCompanyMinDTO;
import pl.com.tt.flex.model.service.dto.localization.LocalizationTypeDTO;
import pl.com.tt.flex.model.service.dto.schedulingUnit.SchedulingUnitMinDTO;
import pl.com.tt.flex.server.common.errors.ConcurrencyFailureException;
import pl.com.tt.flex.server.common.errors.ObjectValidationException;
import pl.com.tt.flex.server.dataexport.exporter.DataExporter;
import pl.com.tt.flex.server.dataexport.factory.DataExporterFactory;
import pl.com.tt.flex.server.dataexport.factory.DataExporterFormat;
import pl.com.tt.flex.server.domain.enumeration.NotificationEvent;
import pl.com.tt.flex.server.domain.enumeration.NotificationParam;
import pl.com.tt.flex.server.domain.fsp.FspEntity;
import pl.com.tt.flex.server.domain.product.ProductEntity;
import pl.com.tt.flex.server.domain.schedulingUnit.SchedulingUnitEntity;
import pl.com.tt.flex.server.domain.schedulingUnit.SchedulingUnitFileEntity;
import pl.com.tt.flex.server.domain.schedulingUnit.SchedulingUnitProposalEntity;
import pl.com.tt.flex.server.domain.schedulingUnit.SchedulingUnitTypeEntity;
import pl.com.tt.flex.server.domain.schedulingUnit.enumeration.SchedulingUnitProposalStatus;
import pl.com.tt.flex.server.domain.schedulingUnit.enumeration.SchedulingUnitProposalType;
import pl.com.tt.flex.server.domain.screen.enumeration.Screen;
import pl.com.tt.flex.server.domain.subportfolio.SubportfolioEntity;
import pl.com.tt.flex.server.domain.unit.UnitEntity;
import pl.com.tt.flex.server.domain.user.UserEntity;
import pl.com.tt.flex.server.repository.AbstractJpaRepository;
import pl.com.tt.flex.server.repository.schedulingUnit.SchedulingUnitFileRepository;
import pl.com.tt.flex.server.repository.schedulingUnit.SchedulingUnitProposalRepository;
import pl.com.tt.flex.server.repository.schedulingUnit.SchedulingUnitRepository;
import pl.com.tt.flex.server.service.common.AbstractServiceImpl;
import pl.com.tt.flex.model.service.dto.file.FileDTO;
import pl.com.tt.flex.model.service.dto.MinimalDTO;
import pl.com.tt.flex.server.service.dictionary.schedulingUnitType.SchedulingUnitTypeService;
import pl.com.tt.flex.server.service.fsp.FspService;
import pl.com.tt.flex.server.service.fsp.dto.FspDTO;
import pl.com.tt.flex.server.service.mail.schedulingUnit.SchedulingUnitMailService;
import pl.com.tt.flex.server.service.mapper.EntityMapper;
import pl.com.tt.flex.server.service.notification.dto.NotificationParamValue;
import pl.com.tt.flex.server.service.notification.factory.NotifierFactory;
import pl.com.tt.flex.server.service.notification.util.NotificationUtils;
import pl.com.tt.flex.server.service.potential.FlexPotentialService;
import pl.com.tt.flex.server.service.schedulingUnit.dto.SchedulingUnitDTO;
import pl.com.tt.flex.server.service.schedulingUnit.dto.SchedulingUnitDropdownSelectDTO;
import pl.com.tt.flex.server.service.schedulingUnit.dto.SchedulingUnitProposalDTO;
import pl.com.tt.flex.server.service.schedulingUnit.mapper.SchedulingUnitMapper;
import pl.com.tt.flex.server.service.schedulingUnit.mapper.SchedulingUnitProposalMapper;
import pl.com.tt.flex.server.service.subportfolio.SubportfolioService;
import pl.com.tt.flex.server.service.unit.UnitService;
import pl.com.tt.flex.server.service.unit.dto.UnitMinDTO;
import pl.com.tt.flex.server.service.unit.mapper.UnitMapper;
import pl.com.tt.flex.server.service.user.UserService;
import pl.com.tt.flex.server.service.user.dto.UserDTO;
import pl.com.tt.flex.server.util.DictionaryUtils;
import pl.com.tt.flex.server.util.InstantUtil;
import pl.com.tt.flex.server.util.ZipUtil;
import pl.com.tt.flex.server.validator.schedulingUnit.SchedulingUnitProposalValidator;
import pl.com.tt.flex.server.validator.schedulingUnit.SchedulingUnitValidator;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static pl.com.tt.flex.server.dataexport.exporter.offer.detail.enumeration.LevelOfDetail.STANDARD_DETAIL_SHEET;
import static pl.com.tt.flex.server.domain.schedulingUnit.enumeration.SchedulingUnitProposalStatus.NEW;
import static pl.com.tt.flex.server.domain.schedulingUnit.enumeration.SchedulingUnitProposalStatus.getSortOrder;
import static pl.com.tt.flex.server.validator.schedulingUnit.SchedulingUnitProposalValidator.STATUSES_ALLOWING_RESEND;
import static pl.com.tt.flex.server.web.rest.errors.ErrorConstants.UNEXPECTED_ERROR;

/**
 * Service Implementation for managing {@link SchedulingUnitEntity}.
 */
@Slf4j
@Service
@Transactional
public class SchedulingUnitServiceImpl extends AbstractServiceImpl<SchedulingUnitEntity, SchedulingUnitDTO, Long> implements SchedulingUnitService {

    private final SchedulingUnitRepository schedulingUnitRepository;
    private final SchedulingUnitMapper schedulingUnitMapper;
    private final SchedulingUnitFileRepository schedulingUnitFileRepository;
    private final SchedulingUnitProposalRepository schedulingUnitProposalRepository;
    private final SchedulingUnitProposalMapper schedulingUnitProposalMapper;
    private final UserService userService;
    private final DataExporterFactory dataExporterFactory;
    private final UnitService unitService;
    private final SchedulingUnitMailService schedulingUnitMailService;
    private final NotifierFactory notifierFactory;
    private final UnitMapper unitMapper;
    private final SchedulingUnitProposalValidator schedulingUnitProposalValidator;
    private final SubportfolioService subportfolioService;
    private final FspService fspService;
    private final SchedulingUnitTypeService schedulingUnitTypeService;
    private final FlexPotentialService flexPotentialService;

    public SchedulingUnitServiceImpl(SchedulingUnitRepository schedulingUnitRepository, SchedulingUnitMapper schedulingUnitMapper,
                                     SchedulingUnitFileRepository schedulingUnitFileRepository, SchedulingUnitProposalRepository schedulingUnitProposalRepository,
                                     SchedulingUnitProposalMapper schedulingUnitProposalMapper, UserService userService, DataExporterFactory dataExporterFactory, UnitService unitService,
                                     SchedulingUnitMailService schedulingUnitMailService, NotifierFactory notifierFactory, UnitMapper unitMapper,
                                     SchedulingUnitProposalValidator schedulingUnitProposalValidator, SubportfolioService subportfolioService, FspService fspService,
                                     SchedulingUnitTypeService schedulingUnitTypeService, FlexPotentialService flexPotentialService) {
        this.schedulingUnitRepository = schedulingUnitRepository;
        this.schedulingUnitMapper = schedulingUnitMapper;
        this.schedulingUnitFileRepository = schedulingUnitFileRepository;
        this.schedulingUnitProposalRepository = schedulingUnitProposalRepository;
        this.schedulingUnitProposalMapper = schedulingUnitProposalMapper;
        this.userService = userService;
        this.dataExporterFactory = dataExporterFactory;
        this.unitService = unitService;
        this.schedulingUnitMailService = schedulingUnitMailService;
        this.notifierFactory = notifierFactory;
        this.unitMapper = unitMapper;
        this.schedulingUnitProposalValidator = schedulingUnitProposalValidator;
        this.subportfolioService = subportfolioService;
        this.fspService = fspService;
        this.schedulingUnitTypeService = schedulingUnitTypeService;
        this.flexPotentialService = flexPotentialService;
    }

    /**
     * @param schedulingUnitDTO Zapisywany obiekt SchedulingUnit.
     * @param dersToRemove      Lista DERow do usuniecia z SchedulingUnit.
     *                          Dery sa usuwane z SchedulingUnit w formularzu edycji SchedulingUnit, natomiast dodawane sa poprzez
     *                          zaproszenia, a nie przez bezposredni wybor DERow w formularzu tworzenia/edycji SchedulingUnit.
     *                          Patrz metody acceptSchedulingUnitProposalByFsp, acceptSchedulingUnitProposalByBsp.
     */
    @Override
    @Transactional
    public SchedulingUnitDTO update(SchedulingUnitDTO schedulingUnitDTO, List<Long> dersToRemove, List<Long> filesToRemove) throws ObjectValidationException {
        SchedulingUnitEntity schedulingUnitToSave = schedulingUnitMapper.toEntity(schedulingUnitDTO);
        SchedulingUnitEntity schedulingUnitEntityFromDb = schedulingUnitRepository.getOne(schedulingUnitToSave.getId());
        if (CollectionUtils.isNotEmpty(dersToRemove)) {
            removeDersFromSchedulingUnit(schedulingUnitEntityFromDb, dersToRemove);
        }
        updateFiles(schedulingUnitEntityFromDb, schedulingUnitToSave, filesToRemove);
        schedulingUnitToSave = schedulingUnitRepository.save(schedulingUnitToSave);
        return schedulingUnitMapper.toDto(schedulingUnitToSave);
    }

    /**
     * Unlinks DERs from SchedulingUnit
     */
    private void removeDersFromSchedulingUnit(SchedulingUnitEntity schedulingUnit, List<Long> derIdsToRemove) throws ObjectValidationException {
        log.debug("removeDersFromSchedulingUnit() Unlinking Ders {} from SchedulingUnit {}",
            derIdsToRemove.stream().map(String::valueOf).collect(Collectors.joining(", ", "[", "]")), schedulingUnit.getId());
        SchedulingUnitValidator.checkIfAnyDersCanBeRemovedFromSchedulingUnit(schedulingUnit);
        schedulingUnitRepository.removeDersFromSchedulingUnit(derIdsToRemove, schedulingUnit.getId());
        notifyRemovingDersFromSchedulingUnit(schedulingUnit, derIdsToRemove);
    }

    private void notifyRemovingDersFromSchedulingUnit(SchedulingUnitEntity schedulingUnit, List<Long> derIdsToRemove) {
        List<UnitMinDTO> dersToRemove = unitService.findDersNameAndFsp(derIdsToRemove);
        dersToRemove.forEach(der -> {
            Map<NotificationParam, NotificationParamValue> fspNotificationParams = NotificationUtils.ParamsMapBuilder.create()
                .addParam(NotificationParam.SCHEDULING_UNIT_NAME, schedulingUnit.getName())
                .addParam(NotificationParam.UNIT_NAME, der.getName()).build();
            List<MinimalDTO<Long, String>> fspNotificationUsers = fspService.findFspUsersMin(der.getFspId());
            NotificationUtils.registerNewNotificationForSpecifiedUsers(notifierFactory, NotificationEvent.SCHEDULING_UNIT_YOUR_DER_REMOVED_FROM_SU, fspNotificationParams,
                fspNotificationUsers);
        });
        Map<NotificationParam, NotificationParamValue> bspNotificationParams = NotificationUtils.ParamsMapBuilder.create()
            .addParam(NotificationParam.SCHEDULING_UNIT_NAME, schedulingUnit.getName())
            .addParam(NotificationParam.UNIT_NAME, dersToRemove.stream().map(UnitMinDTO::getName).collect(Collectors.joining(", "))).build();
        List<MinimalDTO<Long, String>> bspNotificationUsers = fspService.findFspUsersMin(schedulingUnit.getBsp().getId());
        NotificationUtils.registerNewNotificationForSpecifiedUsers(notifierFactory, NotificationEvent.SCHEDULING_UNIT_DER_REMOVED_FROM_YOUR_SU, bspNotificationParams,
            bspNotificationUsers);
    }

    private void updateFiles(SchedulingUnitEntity from, SchedulingUnitEntity to, List<Long> filesToRemove) {
        for (SchedulingUnitFileEntity dbFile : from.getFiles()) {
            if (!filesToRemove.contains(dbFile.getId())) {
                to.getFiles().add(dbFile);
            } else {
                to.getFiles().remove(dbFile);
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SchedulingUnitFileEntity> getSchedulingUnitFileByFileId(Long fileId) {
        return schedulingUnitFileRepository.findById(fileId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FileDTO> getZipWithAllFilesOfSchedulingUnit(Long schedulingUnitId) {
        List<SchedulingUnitFileEntity> fileEntities = schedulingUnitFileRepository.findAllBySchedulingUnitId(schedulingUnitId);
        List<FileDTO> fileDTOS = Lists.newArrayList();
        fileEntities.forEach(entity -> fileDTOS.add(new FileDTO(entity.getFileName(), ZipUtil.zipToFiles(entity.getFileZipData()).get(0).getBytesData())));
        return fileDTOS;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SchedulingUnitDTO> findByIdAndBspId(Long id, Long fspId) {
        return schedulingUnitRepository.findByIdAndBspId(id, fspId).map(schedulingUnitEntity -> getMapper().toDto(schedulingUnitEntity));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SchedulingUnitFileEntity> getSchedulingUnitFileByFileIdAndSchedulingUnitBspId(Long fileId, Long fspId) {
        return schedulingUnitFileRepository.findByIdAndSchedulingUnitBspId(fileId, fspId);
    }

    @Override
    @Transactional(readOnly = true)
    public FileDTO exportSchedulingUnitToFile(List<SchedulingUnitDTO> schedulingUnits, boolean isOnlyDisplayedData, Screen screen) throws IOException {
        String langKey = userService.getLangKeyForCurrentLoggedUser();
        DataExporter<SchedulingUnitDTO> dataExporter = dataExporterFactory.getDataExporter(DataExporterFormat.XLSX, SchedulingUnitDTO.class, screen);
        return dataExporter.export(schedulingUnits, Locale.forLanguageTag(langKey), screen, isOnlyDisplayedData, STANDARD_DETAIL_SHEET);
    }

    /**
     * Tworzenie nowego zaproszenia/propozycji dolaczenia DERa do SchedulingUnit. W przypadku gdy identyczna propozycja juz istnieje (findExistingProposal())
     * to zostanie wyslane ponowne powiadomienie do jej adresatow (nie jest tworzony nowy wpis w bazie danych).
     * Mozna wysylac ponownie wszystkie zaproszenia/propozycje oprocz juz zaakceptowanych (status ACCEPTED, CONNECTED_WITH_OTHER).
     *
     * @see SchedulingUnitProposalStatus#ACCEPTED
     * @see SchedulingUnitProposalStatus#CONNECTED_WITH_OTHER
     */
    @Override
    @Transactional
    public SchedulingUnitProposalDTO createOrResendSchedulingUnitProposal(SchedulingUnitProposalDTO proposalDTO) throws ObjectValidationException {
        Optional<SchedulingUnitProposalEntity> maybeExistingProposal = findExistingProposal(proposalDTO);
        if (maybeExistingProposal.isPresent()) {
            SchedulingUnitProposalEntity existingProposalEntity = maybeExistingProposal.get();
            log.debug("createOrResendSchedulingUnitProposal() resends proposal [id: {}]", existingProposalEntity.getId());
            return resendSchedulingUnitProposal(existingProposalEntity);
        } else {
            log.debug("createOrResendSchedulingUnitProposal() creates new proposal");
            return createSchedulingUnitProposal(proposalDTO);
        }
    }

    /**
     * Propozycje mozna wysylac ponownie na dwa sposoby:
     * 1 - Klikniecie przycisku resend w oknie propozycji
     * 2 - Wybranie tego samego DERa przy tworzeniu nowej propozycji
     */
    public Optional<SchedulingUnitProposalEntity> findExistingProposal(SchedulingUnitProposalDTO proposalDTO) throws ObjectValidationException {
        // opcja 1 (front ustawia id istniejacej propozycji)
        if (nonNull(proposalDTO.getId())) {
            return schedulingUnitProposalRepository.findById(proposalDTO.getId());
        }
        // opcja 2 (brak id istniejacej propozycji)
        if (SchedulingUnitProposalType.INVITATION.equals(proposalDTO.getProposalType())) {
            //BSP zaprasza bezposrednio do swojego Scheduling, wiec szukamy po schedulingUnitId.
            return schedulingUnitProposalRepository.findOneByUnitIdAndSchedulingUnitIdAndProposalTypeAndStatusIn(
                proposalDTO.getUnitId(), proposalDTO.getSchedulingUnitId(), proposalDTO.getProposalType(), STATUSES_ALLOWING_RESEND);
        } else if (SchedulingUnitProposalType.REQUEST.equals(proposalDTO.getProposalType())) {
            //FSP wysla prosbe o dolaczenie do BSP wiec szukamy po bspId. Dopiero potem, przy akceptaji propozycji, BSP wybiera Scheduling.
            return schedulingUnitProposalRepository.findOneByUnitIdAndBspIdAndProposalTypeAndStatusIn(
                proposalDTO.getUnitId(), proposalDTO.getBspId(), proposalDTO.getProposalType(), STATUSES_ALLOWING_RESEND);
        }
        throw new ObjectValidationException("Field proposalType is not present in SchedulingUnitProposalDTO", UNEXPECTED_ERROR);
    }

    private SchedulingUnitProposalDTO createSchedulingUnitProposal(SchedulingUnitProposalDTO proposalDTO) throws ObjectValidationException {
        if (proposalDTO.getProposalType().equals(SchedulingUnitProposalType.INVITATION)) {
            //uzupelnienie pola o id BSP ktory zaprasza DERa FSP bezposrednio do swojego SchedulingUnit
            proposalDTO.setBspId(schedulingUnitRepository.getBspIdOfScheduling(proposalDTO.getSchedulingUnitId()));
        }
        SchedulingUnitProposalEntity proposalEntity = schedulingUnitProposalMapper.toEntity(proposalDTO);
        schedulingUnitProposalValidator.validIfProposalCanBeSend(schedulingUnitProposalMapper.toDto(proposalEntity));
        proposalEntity.setSenderRole(getSchedulingUnitProposalSenderRole(userService.getCurrentUser()));
        proposalEntity.setSentDate(InstantUtil.now());
        proposalEntity.setStatus(NEW);
        proposalEntity.setStatusSortOrder(getSortOrder(proposalEntity.getStatus()));
        proposalEntity = schedulingUnitProposalRepository.save(proposalEntity);
        sendMailAndNotificationWithNewProposal(proposalEntity);
        return schedulingUnitProposalMapper.toDto(proposalEntity);
    }

    private SchedulingUnitProposalDTO resendSchedulingUnitProposal(SchedulingUnitProposalEntity existingProposalEntity) throws ObjectValidationException {
        SchedulingUnitProposalDTO existingProposalDTO = schedulingUnitProposalMapper.toDto(existingProposalEntity);
        schedulingUnitProposalValidator.validIfProposalCanBeSend(existingProposalDTO);
        existingProposalEntity.setStatus(NEW);
        existingProposalEntity.setStatusSortOrder(getSortOrder(existingProposalEntity.getStatus()));
        existingProposalEntity.setLastModifiedDate(InstantUtil.now());
        existingProposalEntity.setSentDate(InstantUtil.now());
        sendMailAndNotificationWithNewProposal(existingProposalEntity);
        return existingProposalDTO;
    }

    private Role getSchedulingUnitProposalSenderRole(UserEntity currentUser) {
        if (currentUser.hasRole(Role.ROLE_ADMIN)) {
            // only user with ROLE_ADMIN has many roles
            return Role.ROLE_ADMIN;
        } else {
            return currentUser.getRoles().stream().findAny().get();
        }
    }

    private void sendMailAndNotificationWithNewProposal(SchedulingUnitProposalEntity proposalEntity) {
        if (nonNull(proposalEntity.getUnit()) && isNull(proposalEntity.getUnit().getName())) {
            proposalEntity.setUnit(unitService.getRepository().getOne(proposalEntity.getUnit().getId()));
        }
        if (nonNull(proposalEntity.getBsp()) && isNull(proposalEntity.getBsp().getCompanyName())) {
            proposalEntity.setBsp(fspService.getRepository().getOne(proposalEntity.getBsp().getId()));
        }
        if (proposalEntity.getProposalType().equals(SchedulingUnitProposalType.REQUEST)) {
            sendMailAndNotificationWithNewProposalToBsp(proposalEntity);
        } else if (proposalEntity.getProposalType().equals(SchedulingUnitProposalType.INVITATION)) {
            sendMailAndNotificationWithNewProposalToFsp(proposalEntity);
        }
    }

    private void sendMailAndNotificationWithNewProposalToBsp(SchedulingUnitProposalEntity proposalEntity) {
        FspEntity bsp = proposalEntity.getBsp();
        UserEntity bspOwner = bsp.getOwner();
        UnitEntity unitEntity = proposalEntity.getUnit();
        schedulingUnitMailService.sendSchedulingUnitProposalLinkForBsp(bspOwner, schedulingUnitProposalMapper.toDto(proposalEntity));
        Map<NotificationParam, NotificationParamValue> notificationParams = NotificationUtils.ParamsMapBuilder.create()
            .addParam(NotificationParam.SCHEDULING_UNIT_PROPOSAL_ID, proposalEntity.getId())
            .addParam(NotificationParam.UNIT_NAME, unitEntity.getName())
            .addParam(NotificationParam.BSP_NAME, bsp.getCompanyName()).build();
        List<MinimalDTO<Long, String>> notificationUsers = Collections.singletonList(new MinimalDTO<>(bspOwner.getId(), bspOwner.getLogin()));
        NotificationUtils.registerNewNotificationForSpecifiedUsers(notifierFactory, NotificationEvent.SCHEDULING_UNIT_PROPOSAL_TO_BSP, notificationParams, notificationUsers);
    }

    private void sendMailAndNotificationWithNewProposalToFsp(SchedulingUnitProposalEntity proposalEntity) {
        if (nonNull(proposalEntity.getSchedulingUnit()) && isNull(proposalEntity.getSchedulingUnit().getName())) {
            proposalEntity.setSchedulingUnit(schedulingUnitRepository.getOne(proposalEntity.getSchedulingUnit().getId()));
        }
        SchedulingUnitEntity schedulingUnitEntity = proposalEntity.getSchedulingUnit();
        UnitEntity unitEntity = proposalEntity.getUnit();
        UserEntity ownerOfUnit = unitEntity.getFsp().getOwner();
        schedulingUnitMailService.sendSchedulingUnitProposalLinkForFsp(ownerOfUnit, schedulingUnitProposalMapper.toDto(proposalEntity));
        Map<NotificationParam, NotificationParamValue> notificationParams = NotificationUtils.ParamsMapBuilder.create()
            .addParam(NotificationParam.UNIT_NAME, unitEntity.getName())
            .addParam(NotificationParam.SCHEDULING_UNIT_NAME, schedulingUnitEntity.getName())
            .addParam(NotificationParam.SCHEDULING_UNIT_PROPOSAL_ID, proposalEntity.getId()).build();
        List<MinimalDTO<Long, String>> notificationUsers = Collections.singletonList(new MinimalDTO<>(ownerOfUnit.getId(), ownerOfUnit.getLogin()));
        NotificationUtils.registerNewNotificationForSpecifiedUsers(notifierFactory, NotificationEvent.SCHEDULING_UNIT_PROPOSAL_TO_FSP, notificationParams, notificationUsers);
    }

    @Override
    @Transactional
    public void notifyUsersThatSchedulingUnitIsReadyForTests(SchedulingUnitDTO schedulingUnitDTO, List<UserEntity> recipients) {
        for (UserEntity user : recipients) {
            schedulingUnitMailService.sendSchedulingUnitReadyForTestsLink(user, schedulingUnitDTO);
        }
        Map<NotificationParam, NotificationParamValue> notificationParams = NotificationUtils.ParamsMapBuilder.create()
            .addParam(NotificationParam.SCHEDULING_UNIT_NAME, schedulingUnitDTO.getName())
            .addParam(NotificationParam.ID, schedulingUnitDTO.getId())
            .addParam(NotificationParam.BSP_NAME, schedulingUnitDTO.getBsp().getCompanyName()).build();
        List<MinimalDTO<Long, String>> notificationUsers = recipients.stream().map(userEntity -> new MinimalDTO<>(userEntity.getId(), userEntity.getLogin())).collect(Collectors.toList());
        NotificationUtils.registerNewNotificationForSpecifiedUsers(notifierFactory, NotificationEvent.SCHEDULING_UNIT_READY_FOR_TESTS, notificationParams, notificationUsers);
    }

    @Override
    @Transactional
    public void acceptSchedulingUnitProposalByFsp(Long proposalId) throws ObjectValidationException {
        SchedulingUnitProposalEntity proposalEntity = schedulingUnitProposalRepository.getOne(proposalId);
        prepareProposalToBeAccepted(proposalEntity);
        proposalEntity.getSchedulingUnit().addUnit(proposalEntity.getUnit());
        notifyUserThatProposalStatusIsChanged(proposalEntity, SchedulingUnitProposalStatus.ACCEPTED,
            NotificationEvent.SCHEDULING_UNIT_PROPOSAL_TO_BSP_ACCEPTED, proposalEntity.getBsp().getOwner());
    }

    @Override
    @Transactional
    public void acceptSchedulingUnitProposalByBsp(Long proposalId, Long schedulingUnitId) throws ObjectValidationException {
        SchedulingUnitProposalEntity proposalEntity = schedulingUnitProposalRepository.getOne(proposalId);
        prepareProposalToBeAccepted(proposalEntity);
        SchedulingUnitEntity schedulingUnitEntity = schedulingUnitRepository.findById(schedulingUnitId).get();
        schedulingUnitEntity.addUnit(proposalEntity.getUnit());
        proposalEntity.setSchedulingUnit(schedulingUnitEntity);
        notifyUserThatProposalStatusIsChanged(proposalEntity, SchedulingUnitProposalStatus.ACCEPTED,
            NotificationEvent.SCHEDULING_UNIT_PROPOSAL_TO_FSP_ACCEPTED, proposalEntity.getUnit().getFsp().getOwner());
    }

    //czesc wspolna akceptacji propozycji dla BSP i FSP/A
    private void prepareProposalToBeAccepted(SchedulingUnitProposalEntity proposalEntity) throws ObjectValidationException {
        schedulingUnitProposalValidator.validIfProposalCanBeAccepted(schedulingUnitProposalMapper.toDto(proposalEntity));
        //returns number of updated entities
        if (schedulingUnitProposalRepository.acceptProposal(proposalEntity.getId()) > 0) {
            //sprawdzenie czy update sie wykonal w celu zapobiegniecia akceptacji zaproszen przez dwoch roznych uzytkownikow w tym samym czasie
            proposalEntity.setStatus(SchedulingUnitProposalStatus.ACCEPTED); //nadpisanie statusu gdyz przy wyjsciu z metody wykonuje sie zapis encji z starym statusem
            proposalEntity.setStatusSortOrder(getSortOrder(proposalEntity.getStatus()));
            schedulingUnitProposalRepository.rejectProposalsForUnit(proposalEntity.getUnit().getId());
        } else {
            throw new ConcurrencyFailureException("SchedulingUnitProposal is already accepted by another company");
        }
        markProposalNotificationAsRead(proposalEntity.getId());
    }

    @Override
    @Transactional
    public void rejectSchedulingUnitProposal(Long proposalId) {
        schedulingUnitProposalRepository.rejectProposal(proposalId);
        markProposalNotificationAsRead(proposalId);
        SchedulingUnitProposalEntity proposalEntity = schedulingUnitProposalRepository.getOne(proposalId);
        if (proposalEntity.getProposalType().equals(SchedulingUnitProposalType.REQUEST)) {
            notifyUserThatProposalStatusIsChanged(proposalEntity, SchedulingUnitProposalStatus.REJECTED,
                NotificationEvent.SCHEDULING_UNIT_PROPOSAL_TO_BSP_REJECTED_BY_BSP, proposalEntity.getUnit().getFsp().getOwner());
        }
        if (proposalEntity.getProposalType().equals(SchedulingUnitProposalType.INVITATION)) {
            notifyUserThatProposalStatusIsChanged(proposalEntity, SchedulingUnitProposalStatus.REJECTED,
                NotificationEvent.SCHEDULING_UNIT_PROPOSAL_TO_FSP_REJECTED_BY_FSP, proposalEntity.getBsp().getOwner());
        }
    }

    @Override
    @Transactional
    public void cancelSchedulingUnitProposal(Long proposalId) {
        schedulingUnitProposalRepository.cancelProposal(proposalId);
        markProposalNotificationAsRead(proposalId);
        SchedulingUnitProposalEntity proposalEntity = schedulingUnitProposalRepository.getOne(proposalId);
        if (proposalEntity.getProposalType().equals(SchedulingUnitProposalType.REQUEST)) {
            notifyUserThatProposalStatusIsChanged(proposalEntity, SchedulingUnitProposalStatus.CANCELLED,
                NotificationEvent.SCHEDULING_UNIT_PROPOSAL_TO_BSP_CANCELLED_BY_FSP, proposalEntity.getBsp().getOwner());
        }
        if (proposalEntity.getProposalType().equals(SchedulingUnitProposalType.INVITATION)) {
            notifyUserThatProposalStatusIsChanged(proposalEntity, SchedulingUnitProposalStatus.CANCELLED,
                NotificationEvent.SCHEDULING_UNIT_PROPOSAL_TO_FSP_CANCELLED_BY_BSP, proposalEntity.getUnit().getFsp().getOwner());
        }
    }

    private void notifyUserThatProposalStatusIsChanged(SchedulingUnitProposalEntity proposalEntity, SchedulingUnitProposalStatus status, NotificationEvent event, UserEntity userToNotify) {
        Map<NotificationParam, NotificationParamValue> notificationParams = NotificationUtils.ParamsMapBuilder.create()
            .addParam(NotificationParam.UNIT_NAME, proposalEntity.getUnit().getName())
            .addParam(NotificationParam.FSP_NAME, proposalEntity.getUnit().getFsp().getCompanyName())
            .addParam(NotificationParam.BSP_NAME, proposalEntity.getBsp().getCompanyName())
            .addParam(NotificationParam.SCHEDULING_UNIT_PROPOSAL_ID, proposalEntity.getId()).build();
        if (nonNull(proposalEntity.getSchedulingUnit())) {
            notificationParams.put(NotificationParam.SCHEDULING_UNIT_NAME, NotificationParamValue.ParamValueBuilder.create().addParam(proposalEntity.getSchedulingUnit().getName()).build());
        }
        List<MinimalDTO<Long, String>> notificationUsers = Collections.singletonList(new MinimalDTO<>(userToNotify.getId(), userToNotify.getLogin()));
        NotificationUtils.registerNewNotificationForSpecifiedUsers(notifierFactory, event, notificationParams, notificationUsers);
        SchedulingUnitProposalDTO proposalDTO = schedulingUnitProposalMapper.toDto(proposalEntity);
        proposalDTO.setStatus(status);
        schedulingUnitMailService.notifyUserThatProposalStatusIsChanged(proposalDTO, userToNotify);
    }

    /**
     * Oznaczenie powiadomiania jako przeczytane przy akceptacji/rezygnacji z oferty poprzez link z maila a nie link z powiadomienia
     */
    private void markProposalNotificationAsRead(Long proposalId) {
        schedulingUnitProposalRepository.markProposalNotificationAsRead(String.valueOf(proposalId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<UnitMinDTO> getSchedulingUnitDers(Long schedulingUnitId) {
        return unitService.getSchedulingUnitDers(schedulingUnitId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UnitMinDTO> getSchedulingUnitDersForFsp(Long schedulingUnitId, Long fspId) {
        return unitService.getFspSchedulingUnitDers(schedulingUnitId, fspId);
    }

    /**
     * FSP może się zarejestrować tylko u jednego BSP i może połączyć swojego DER'a tylko z jedną jednostką grafikową,
     * dopuszcza się jednak zarejestrowanie wielu swoich DER'ów do jednej jednostki grafikowej, ale wtedy nie będą one
     * dostępne(jeżeli zostaną zaakceptowane) do zarejestrowania na inne jednostki grafikowe danego BSP.
     * Czyli jeżeli FSP zarejestruje się już do jakiegoś BSP, to nie powinien mieć możliwości rejestracji do jednostki grafikowej innego BSP.
     */
    @Override
    @Transactional(readOnly = true)
    public List<UnitMinDTO> getAvailableFspDersForNewSchedulingUnitProposal(Long fspId, Long bspId) {
        //W przypadku FSP przycisk 'propose der' powinien byc zablokowany, jesli ma on juz dolaczony jakis DER do Jednostki grafikowej innego BSP.
        if (isFspJoinedWithOtherBspBySchedulingUnit(fspId, bspId)) {
            return Lists.newArrayList();
        }
        List<UnitMinDTO> result = schedulingUnitProposalRepository.getFspDersNotJoinedToAnySchedulingUnit(fspId).stream().map(unitMapper::toMinDto).collect(Collectors.toList());
        //w okienku proponowania DERa w selekcie "FSP" dostępne wszystkie FSP oraz FSPA, a po wyborze FSP
        // w selekcie "DER" wyświetlane tylko te DERy, które posiadają "Flex register" na produkt z "Balancing"
        return result.stream().filter(unit -> flexPotentialService.isDerOfFspBalancedByRegisteredFlexPotentialProduct(unit.getId())).collect(Collectors.toList());
    }

    /**
     * Zwracane jest true jesli jakis DER wskazanego FSP jest polaczony z SchedulingUnit innego BSP niz ten wskazany.
     */
    @Override
    @Transactional(readOnly = true)
    public boolean isFspJoinedWithOtherBspBySchedulingUnit(Long fspId, Long bspId) {
        return unitService.existsByFspIdAndSchedulingUnitBspIdNot(fspId, bspId);
    }

    /**
     * Zwracane jest true jesli wszystkie Subportfolio (jakikolwiek DER z tego Subportfolio) wskazanego FSPA sa polaczone z SchedulingUnits innych BSP niz wskazany.
     */
    @Override
    @Transactional(readOnly = true)
    public boolean isAllFspaSubportfoliosJoinedWithOtherBspBySchedulingUnit(Long fspId, Long bspId) {
        List<SubportfolioEntity> fspaSubs = subportfolioService.findAllCertifiedByFspaId(fspId);
        Set<Long> subsJoinedWithOtherBsp = Sets.newHashSet();
        for (SubportfolioEntity sub : fspaSubs) {
            if (sub.getUnits().stream().anyMatch(unit -> nonNull(unit.getSchedulingUnit()) && !unit.getSchedulingUnit().getBsp().getId().equals(bspId)) && sub.isCertified()) {
                subsJoinedWithOtherBsp.add(sub.getId());
            }
        }
        return fspaSubs.size() == subsJoinedWithOtherBsp.size();
    }

    /**
     * Zwracane sa DERy wskazanego Subportfolio niepodpiete pod SchedulingUnit (1),
     * które posiadają "Flex register" na produkt z flagą "Balancing" (2).
     * Jeżeli już jakaś SchedulingUnit nalezace do inngeo BSP niz wskazany, ma
     * połączenie z DERem FSPA należącym do wybranego subportfolio, to inny DER
     * tego FSPA z tego samego subportfolio może być połączony tylko z tą SU,
     * więc nie może być połączony z wybranym BSP (3).
     * W przypadku nie podania id subportfolio zwrócone zostaną wszystkie certyfikowane dery
     * bez subportfolio przypisane do fsp o podanym id
     */
    @Override
    @Transactional(readOnly = true)
    public List<UnitMinDTO> findAvailableFspaSubportfolioDersForNewSchedulingUnitProposal(Long subportfolioId, Long bspId, Long fspaId) {
        if(Objects.isNull(subportfolioId) && Objects.nonNull(fspaId)) {
            return unitService.findAllWithoutSubportfolioByFspId(fspaId);
        }
        //Ad 1.
        List<UnitMinDTO> subDersNotJoinedWithAnySchedulingUnit = unitService.findAllBySubportfolioIdAndSchedulingUnitIsNull(subportfolioId);
        //Ad 2.
        List<UnitMinDTO> result = subDersNotJoinedWithAnySchedulingUnit.stream().filter(unit ->
            flexPotentialService.isDerOfFspBalancedByRegisteredFlexPotentialProduct(unit.getId())).collect(Collectors.toList());
        //Ad 3.
        if (unitService.existsBySubportfolioIdAndSchedulingUnitBspIdNot(subportfolioId, bspId)) {
            return Lists.newArrayList();
        }
        return result;
    }

    /**
     * Sprawdzenie czy mozna dodac wskazanego DERa (derId) do SchedulingUnits nalezacych do wskazanego BSP (bspId).
     */
    @Override
    @Transactional(readOnly = true)
    public boolean canDerBeAddedToBspSchedulingUnits(Long derId, Long bspId) {
        UnitEntity fspDer = unitService.getRepository().findById(derId).get();
        FspEntity fsp = fspDer.getFsp();
        //Przycisk 'invite der' powinien byc zablokowany jesli DER jest juz podpiety do Jednostki grafikowej jakiegos BSP.
        if (nonNull(fspDer.getSchedulingUnit())) {
            return false;
        }
        if (fsp.getRole().equals(Role.ROLE_FLEX_SERVICE_PROVIDER)) {
            //Aby BSP mógł zaprosić DERa do jednostki grafikowej, to ten DER musi posiadać "Flex register" na produkt z flagą "Balancing".
            if (!flexPotentialService.isDerOfFspBalancedByRegisteredFlexPotentialProduct(derId)) {
                return false;
            }
            //Przycisk 'invite der' powinien byc zablokowany jesli wlascielem DERa jest FSP i jakis z DERow tego FSP jest juz podpiety do Jednostki grafikowej innego BSP.
            return !isFspJoinedWithOtherBspBySchedulingUnit(fsp.getId(), bspId);
        } else if (fsp.getRole().equals(Role.ROLE_FLEX_SERVICE_PROVIDER_AGGREGATED)) {
            //Przycisk 'invite der' powinien byc dostępny jesli DER nie jest podpiety do zadnego Subportfolio.
            if (isNull(fspDer.getSubportfolio())) {
                return true;
            }
            //Przycisk 'invite der' powinien byc zablokowany jesli wlascielem DERa jest FSPA i jakis z DERow z Subportfolio tego DERa jest juz podpiety do Jednostki grafikowej innego BSP.
            return fspDer.getSubportfolio().getUnits().stream().noneMatch(unit -> nonNull(unit.getSchedulingUnit()) && !unit.getSchedulingUnit().getBsp().getId().equals(bspId));
        }
        return false;
    }

    /**
     * Zwrocenie SchedulingUnits wskazanego BSP, do ktorych mozna dolaczac DERy.
     * 1. Zwracamy SchedulingUnits aktywne i nieoznaczone jako gotowe do testow ani jako certyfikowane.
     * 2. DER może zostać połączony z SchedulingUnit tylko i wyłącznie wtedy, jeżeli posiada "Flex register" (potencjal oznaczony jako zarejestrowany)
     * na wszystkie produkty, które są w typie jednostki grafikowej.
     * 3. Dodatkowa walidacja dla FSPA:
     * W przypadku FSPA jeżeli już jakaś SU danego BSP ma połączenie z DERem FSPA należącym do tego samego subportfolio,
     * to inny DER tego FSPA z tego samego subportfolio może być połączony tylko z tą SU
     * (więc jeżeli zachodzi taki przypadek, to przy spełnieniu powyższych warunków, powinna się dla tego BSP
     * wyświetlać maksymalnie jedna SU, do której został wcześniej podłączony inny DER z tego samego subportfolio).
     * Jesli powyzszy warunek nie jest spelniony, to zwracane sa po prostu aktywne niecertyfikowane jednostki grafikowe danego bsp.
     */
    @Override
    @Transactional(readOnly = true)
    public List<SchedulingUnitMinDTO> getAllCurrentBspSchedulingUnitsToWhichOnesPointedDerCanBeJoined(Long bspId, Long derId) {
        List<SchedulingUnitMinDTO> result = Lists.newArrayList();
        FspCompanyMinDTO derFsp = unitService.getDerFspMin(derId);
        //Ad. pkt 2 - Produkty na który wskazany DER posiada "Flex register"
        List<Long> derPotentialsProductIds = unitService.findDerRegisteredPotentialsProductsIds(derId);
        List<SchedulingUnitEntity> bspSchedulingUnits = Lists.newArrayList();
        if (!derPotentialsProductIds.isEmpty()) {
            if (derFsp.getRole().equals(Role.ROLE_FLEX_SERVICE_PROVIDER)) {
                //Ad. pkt 1
                bspSchedulingUnits = schedulingUnitRepository.findAllByBspIdAndActiveTrueAndReadyForTestsFalseAndCertifiedFalse(bspId);
            } else if (derFsp.getRole().equals(Role.ROLE_FLEX_SERVICE_PROVIDER_AGGREGATED)) {
                //Ad. pkt 3
                List<Long> allDersFromDerSubportfolio = subportfolioService.findAllDerIdsFromDerSubportfolio(derId);
                Optional<SchedulingUnitMinDTO> optSchedulingUnitFromOneOfSubportfolioDers = schedulingUnitRepository.findFirstByUnitIn(allDersFromDerSubportfolio);
                bspSchedulingUnits = optSchedulingUnitFromOneOfSubportfolioDers.map(schedulingUnitMin ->
                    Collections.singletonList(schedulingUnitRepository.getOne(schedulingUnitMin.getId()))).orElseGet(() ->
                    schedulingUnitRepository.findAllByBspIdAndActiveTrueAndReadyForTestsFalseAndCertifiedFalse(bspId));
            }
            //Ad. pkt 2 - porownanie Produktow Jednostki grafikowej z Produktami "Flex register"
            for (SchedulingUnitEntity su : bspSchedulingUnits) {
                if (derPotentialsProductIds.containsAll(su.getSchedulingUnitType().getProducts().stream().map(ProductEntity::getId).collect(Collectors.toSet()))) {
                    result.add(schedulingUnitMapper.toMinDto(su));
                }
            }
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SchedulingUnitMinDTO> findByUnit(Long unitId) {
        return schedulingUnitRepository.findByUnit(unitId);
    }

    @Override
    public boolean existsByUnitIdAndReadyForTestsTrue(Long unitId) {
        return schedulingUnitRepository.existsByUnitIdAndReadyForTestsTrue(unitId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SchedulingUnitDropdownSelectDTO> findAllRegisteredSchedulingUnitsForBspAndProduct(Long bspId, Long productId) {
        return schedulingUnitMapper.toDropdownSelectDto(schedulingUnitRepository.findAllRegisteredSchedulingUnitsForBspAndProduct(bspId, productId));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsBySchedulingUnitIdAndBspId(Long schedulingUnitId, Long bspId) {
        return schedulingUnitRepository.existsByIdAndBspId(schedulingUnitId, bspId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsActiveCertifiedByUserAndProductId(UserEntity user, Long productId) {
        FspEntity fspEntity = fspService.findFspOfUser(user.getId(), user.getLogin())
            .orElseThrow(() -> new IllegalStateException("Cannot find BSP by user id: " + user.getId()));
        return schedulingUnitRepository.existsActiveCertifiedByBspIdAndProductId(fspEntity.getId(), productId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsActiveCertifiedByProductId(Long productId) {
        return schedulingUnitRepository.existsActiveCertifiedByProductId(productId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isFspJoinedWithBspBySchedulingUnit(Long fspId, Long bspId) {
        return unitService.existsByFspIdAndSchedulingUnitBspId(fspId, bspId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsBySchedulingUnitIdAndProductId(Long schedulingUnitId, Long productId) {
        return schedulingUnitRepository.existsBySchedulingUnitIdAndProductId(schedulingUnitId, productId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SchedulingUnitProposalDTO> findSchedulingUnitProposalById(Long proposalId) {
        return schedulingUnitProposalRepository.findById(proposalId).map(schedulingUnitProposalMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FspCompanyMinDTO> findAllBspsUsedInFspProposals(Long fspId, SchedulingUnitProposalType proposalType) {
        return schedulingUnitProposalRepository.findAllBspsUsedInFspProposals(fspId, proposalType);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FspCompanyMinDTO> findAllFspsUsedInBspProposals(Long bspId, SchedulingUnitProposalType proposalType) {
        return schedulingUnitProposalRepository.findAllFspsUsedInBspProposals(bspId, proposalType);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FspCompanyMinDTO> findAllBspsUsedInAllFspsProposals(SchedulingUnitProposalType proposalType) {
        return schedulingUnitProposalRepository.findAllBspsUsedInAllFspsProposals(proposalType);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FspCompanyMinDTO> findAllFspsUsedInAllBspsProposals(SchedulingUnitProposalType proposalType) {
        return schedulingUnitProposalRepository.findAllFspsUsedInAllBspsProposals(proposalType);
    }

    @Override
    public List<Long> findAllWithJoinedDersOfFsp(Long fspId) {
        return schedulingUnitProposalRepository.findAllWithJoinedDersOfFsp(fspId);
    }

    //********************************************************************************** NOTIFICATION ************************************************************************************
    @Override
    @Transactional
    public void registerNewNotificationForSchedulingUnitCreation(SchedulingUnitDTO schedulingUnitDTO) {
        SchedulingUnitTypeEntity type = schedulingUnitTypeService.getRepository().findById(schedulingUnitDTO.getSchedulingUnitType().getId()).get();
        SchedulingUnitDTO dbSchedulingUnit = findById(schedulingUnitDTO.getId())
            .orElseThrow(() -> new RuntimeException("Cannot find SU with id: " + schedulingUnitDTO.getId()));
        Map<NotificationParam, NotificationParamValue> notificationParams = NotificationUtils.ParamsMapBuilder.create()
            .addParam(NotificationParam.ID, schedulingUnitDTO.getId())
            .addParam(NotificationParam.SCHEDULING_UNIT_NAME, schedulingUnitDTO.getName())
            .addParam(NotificationParam.SCHEDULING_UNIT_BSP, dbSchedulingUnit.getBsp().getCompanyName())
            .addParam(NotificationParam.SCHEDULING_UNIT_TYPE, DictionaryUtils.getNlsCode(type))
            .addParam(NotificationParam.SCHEDULING_UNIT_ACTIVE, schedulingUnitDTO.getActive())
            .addParam(NotificationParam.SCHEDULING_UNIT_READY_FOR_TESTS, schedulingUnitDTO.isReadyForTests())
            .addParam(NotificationParam.SCHEDULING_UNIT_CERTIFIED, schedulingUnitDTO.isCertified())
            .addParam(NotificationParam.CREATED_BY, schedulingUnitDTO.getCreatedBy()).build();
        Set<MinimalDTO<Long, String>> usersToBeNotified = getUsersToBeNotifiedWithBSP(schedulingUnitDTO);
        NotificationUtils.registerNewNotificationForSpecifiedUsers(notifierFactory, NotificationEvent.SCHEDULING_UNIT_CREATED, notificationParams, new ArrayList<>(usersToBeNotified));
    }

    @Override
    @Transactional
    public void registerNewNotificationForSchedulingUnitEdition(SchedulingUnitDTO modifiedScheduling, SchedulingUnitDTO oldScheduling) {
        Map<NotificationParam, NotificationParamValue> notificationParams = NotificationUtils.ParamsMapBuilder.create()
            .addParam(NotificationParam.ID, modifiedScheduling.getId())
            .addParam(NotificationParam.SCHEDULING_UNIT_NAME, modifiedScheduling.getName())
            .addModificationParam(NotificationParam.SCHEDULING_UNIT_BSP, oldScheduling.getBsp().getCompanyName(), modifiedScheduling.getBsp().getCompanyName())
            .addModificationParam(NotificationParam.SCHEDULING_UNIT_TYPE, oldScheduling.getSchedulingUnitType().getNlsCode(), modifiedScheduling.getSchedulingUnitType().getNlsCode())
            .addModificationParam(NotificationParam.SCHEDULING_UNIT_ACTIVE, oldScheduling.getActive(), modifiedScheduling.getActive())
            .addModificationParam(NotificationParam.SCHEDULING_UNIT_READY_FOR_TESTS, oldScheduling.isReadyForTests(), modifiedScheduling.isReadyForTests())
            .addModificationParam(NotificationParam.SCHEDULING_UNIT_CERTIFIED, oldScheduling.isCertified(), modifiedScheduling.isCertified())
            .build();
        Set<MinimalDTO<Long, String>> usersToBeNotified = getUsersToBeNotifiedWithBSP(oldScheduling);
        NotificationUtils.registerNewNotificationForSpecifiedUsers(notifierFactory, NotificationEvent.SCHEDULING_UNIT_UPDATED, notificationParams, new ArrayList<>(usersToBeNotified));
    }

    @Override
    @Transactional
    public void sendMailInformingAboutSchedulingUnitCreation(SchedulingUnitDTO schedulingUnitDTO) {
        FspDTO bsp = fspService.findById(schedulingUnitDTO.getBsp().getId())
            .orElseThrow(() -> new RuntimeException("BSP not found with id: " + schedulingUnitDTO.getBsp().getId()));
        SchedulingUnitDTO schedulingUnitDB = getMapper().toDto(schedulingUnitRepository.findById(schedulingUnitDTO.getId())
            .orElseThrow(() -> new RuntimeException("SchedulingUnit not found with id: " + schedulingUnitDTO.getId())));
        Set<MinimalDTO<Long, String>> usersToBeNotified = getUsersToBeNotifiedWithBSP(schedulingUnitDTO);
        usersToBeNotified.forEach(user -> schedulingUnitMailService.informUserAboutNewSchedulingUnitCreation(findUserMinimalById(user.getId()), schedulingUnitDB, bsp));
    }

    @Override
    @Transactional
    public void sendMailInformingAboutSchedulingUnitModification(SchedulingUnitDTO oldSchedulingUnit, SchedulingUnitDTO modifySchedulingUnit) {
        Set<MinimalDTO<Long, String>> usersToBeNotified = getUsersToBeNotifiedWithBSP(oldSchedulingUnit);
        usersToBeNotified.forEach(user -> schedulingUnitMailService.
            informUserAboutSchedulingUnitEdition(findUserMinimalById(user.getId()), oldSchedulingUnit, modifySchedulingUnit));
    }

    @Override
    @Transactional
    public void sendNotificationInformingAboutRegistered(SchedulingUnitDTO schedulingUnitDTO) {
        SchedulingUnitTypeEntity type = schedulingUnitTypeService.getRepository().findById(schedulingUnitDTO.getSchedulingUnitType().getId())
            .orElseThrow(() -> new RuntimeException("Cannot find SU Type with id: " + schedulingUnitDTO.getSchedulingUnitType().getId()));
        SchedulingUnitDTO dbSchedulingUnit = findById(schedulingUnitDTO.getId())
            .orElseThrow(() -> new RuntimeException("Cannot find SU with id: " + schedulingUnitDTO.getId()));
        Map<NotificationParam, NotificationParamValue> notificationParams = NotificationUtils.ParamsMapBuilder.create()
            .addParam(NotificationParam.ID, schedulingUnitDTO.getId())
            .addParam(NotificationParam.SCHEDULING_UNIT_NAME, schedulingUnitDTO.getName())
            .addParam(NotificationParam.SCHEDULING_UNIT_BSP, schedulingUnitDTO.getBsp().getCompanyName())
            .addParam(NotificationParam.SCHEDULING_UNIT_TYPE, DictionaryUtils.getNlsCode(type))
            .addParam(NotificationParam.SCHEDULING_UNIT_ACTIVE, schedulingUnitDTO.getActive())
            .addParam(NotificationParam.SCHEDULING_UNIT_NUMBER_OF_DERS, dbSchedulingUnit.getNumberOfDers())
            .build();

        addCouplingPointsParam(dbSchedulingUnit, notificationParams);
        addPrimaryCouplingPointParams(dbSchedulingUnit, notificationParams);

        Set<MinimalDTO<Long, String>> usersToBeNotified = getUsersToBeNotifiedWithBSP(schedulingUnitDTO);
        NotificationUtils.registerNewNotificationForSpecifiedUsers(notifierFactory, NotificationEvent.SCHEDULING_UNIT_MOVED_TO_FLEX_REGISTER, notificationParams, new ArrayList<>(usersToBeNotified));
    }

    private void addCouplingPointsParam(SchedulingUnitDTO schedulingUnitDTO, Map<NotificationParam, NotificationParamValue> notificationParams) {
        List<LocalizationTypeDTO> couplingPoints = schedulingUnitDTO.getCouplingPoints();
        if (!CollectionUtils.isEmpty(couplingPoints)) {
            notificationParams.put(NotificationParam.SCHEDULING_UNIT_COUPLING_POINT_ID, NotificationParamValue.ParamValueBuilder
                .create()
                .addParam(couplingPoints.stream().map(LocalizationTypeDTO::getName).collect(Collectors.joining(", ")))
                .build());
        }
    }

    private void addPrimaryCouplingPointParams(SchedulingUnitDTO schedulingUnitDTO, Map<NotificationParam, NotificationParamValue> notificationParams) {
        LocalizationTypeDTO primaryCouplingPoint = schedulingUnitDTO.getPrimaryCouplingPoint();
        if (Objects.nonNull(primaryCouplingPoint)) {
            notificationParams.put(NotificationParam.SCHEDULING_UNIT_PRIMARY_CPI, NotificationParamValue.ParamValueBuilder
                .create()
                .addParam(primaryCouplingPoint.getName())
                .build());
        }
    }

    @Override
    @Transactional
    public void sendMailInformingAboutRegistered(SchedulingUnitDTO schedulingUnitDTO) {
        FspDTO bsp = fspService.findById(schedulingUnitDTO.getBsp().getId())
            .orElseThrow(() -> new RuntimeException("BSP not found with id: " + schedulingUnitDTO.getBsp().getId()));
        SchedulingUnitDTO schedulingUnit = getMapper().toDto(schedulingUnitRepository.findById(schedulingUnitDTO.getId())
            .orElseThrow(() -> new RuntimeException("SchedulingUnit not found with id: " + schedulingUnitDTO.getId())));
        Set<MinimalDTO<Long, String>> usersToBeNotified = getUsersToBeNotifiedWithBSP(schedulingUnitDTO);
        usersToBeNotified.forEach(user -> schedulingUnitMailService.informUserAboutTransferOfSuToFlexRegister(findUserMinimalById(user.getId()), schedulingUnit, bsp));
    }

    @Override
    public Long findOwnerBspId(Long schedulingUnitId) {
        return schedulingUnitRepository.getBspIdOfScheduling(schedulingUnitId);
    }

    // Komunikat wyswietlany jest dla:
    // - 1: uzytkownika ktory stworzyl, ostatnio zmodyfikowal i aktualnie modyfikuje danego SU,
    // - 2: uzytkownikow SU
    private Set<MinimalDTO<Long, String>> getUsersToBeNotifiedWithBSP(SchedulingUnitDTO schedulingUnitDTO) {
        //Ad. 1
        UserDTO creatorUser = userService.getCurrentUserDTO().orElseThrow(() -> new RuntimeException("Current logged user not found"));
        Set<MinimalDTO<Long, String>> usersToBeNotified =
            new HashSet<>(userService.getUsersByLogin(NotificationUtils.getLoginsOfUsersToBeNotified(creatorUser.getLogin(), schedulingUnitDTO)));
        //Ad. 2
        usersToBeNotified.addAll(fspService.findFspUsersMin(schedulingUnitDTO.getBsp().getId()));
        return usersToBeNotified;
    }

    private UserEntity findUserMinimalById(Long id) {
        return userService.findOne(id).orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    //********************************************************************************** NOTIFICATION ************************************************************************************

    @Override
    public AbstractJpaRepository<SchedulingUnitEntity, Long> getRepository() {
        return this.schedulingUnitRepository;
    }

    @Override
    public EntityMapper<SchedulingUnitDTO, SchedulingUnitEntity> getMapper() {
        return this.schedulingUnitMapper;
    }
}
