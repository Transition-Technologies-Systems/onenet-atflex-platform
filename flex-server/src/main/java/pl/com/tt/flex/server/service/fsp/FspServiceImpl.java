package pl.com.tt.flex.server.service.fsp;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.com.tt.flex.model.security.permission.Role;
import pl.com.tt.flex.model.service.dto.fsp.FspCompanyMinDTO;
import pl.com.tt.flex.server.dataexport.exporter.DataExporter;
import pl.com.tt.flex.server.dataexport.factory.DataExporterFactory;
import pl.com.tt.flex.server.dataexport.factory.DataExporterFormat;
import pl.com.tt.flex.server.domain.enumeration.NotificationEvent;
import pl.com.tt.flex.server.domain.enumeration.NotificationParam;
import pl.com.tt.flex.server.domain.fsp.FspEntity;
import pl.com.tt.flex.server.domain.potential.FlexPotentialEntity;
import pl.com.tt.flex.server.domain.screen.enumeration.Screen;
import pl.com.tt.flex.server.domain.user.UserEntity;
import pl.com.tt.flex.server.domain.user.registration.FspUserRegistrationEntity;
import pl.com.tt.flex.server.repository.AbstractJpaRepository;
import pl.com.tt.flex.server.repository.fsp.FspRepository;
import pl.com.tt.flex.server.repository.potential.FlexPotentialRepository;
import pl.com.tt.flex.server.service.common.AbstractServiceImpl;
import pl.com.tt.flex.model.service.dto.file.FileDTO;
import pl.com.tt.flex.model.service.dto.MinimalDTO;
import pl.com.tt.flex.server.service.fsp.dto.FspDTO;
import pl.com.tt.flex.server.service.fsp.mapper.FspMapper;
import pl.com.tt.flex.server.service.mail.fsp.FspMailService;
import pl.com.tt.flex.server.service.mapper.EntityMapper;
import pl.com.tt.flex.server.service.notification.dto.NotificationParamValue;
import pl.com.tt.flex.server.service.notification.factory.NotifierFactory;
import pl.com.tt.flex.server.service.notification.util.NotificationUtils;
import pl.com.tt.flex.server.service.user.UserOnlineService;
import pl.com.tt.flex.server.service.user.UserService;
import pl.com.tt.flex.server.service.user.dto.UserDTO;
import pl.com.tt.flex.server.util.InstantUtil;
import pl.com.tt.flex.server.validator.fsp.FspValidator;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static pl.com.tt.flex.server.dataexport.exporter.offer.detail.enumeration.LevelOfDetail.STANDARD_DETAIL_SHEET;
import static pl.com.tt.flex.server.domain.enumeration.NotificationEvent.BSP_UPDATED;
import static pl.com.tt.flex.server.domain.enumeration.NotificationEvent.FSP_UPDATED;

/**
 * Service Implementation for managing {@link FspEntity}.
 */
@Slf4j
@Service
@Transactional
@AllArgsConstructor
public class FspServiceImpl extends AbstractServiceImpl<FspEntity, FspDTO, Long> implements FspService {

    private final FspRepository fspRepository;
    private final FspMapper fspMapper;
    private final FlexPotentialRepository flexPotentialRepository;
    private final DataExporterFactory dataExporterFactory;
    private final UserService userService;
    private final FspValidator fspValidator;
    private final UserOnlineService userOnlineService;
    private final NotifierFactory notifierFactory;
    private final FspMailService fspMailService;

    @Override
    @Transactional
    public void createFspForNewlyRegisteredFspUser(FspUserRegistrationEntity fspUserRegistrationEntity) {
        UserEntity fspUser = fspUserRegistrationEntity.getFspUser();
        FspEntity fsp = new FspEntity();
        fspUser.setFsp(fsp);
        fsp.setOwner(fspUser);
        fsp.setRole(fspUserRegistrationEntity.getUserTargetRole());
        fsp.setCompanyName(fspUserRegistrationEntity.getCompanyName());
        Instant now = InstantUtil.now();
        fsp.setValidFrom(now.truncatedTo(ChronoUnit.HOURS)); //w validFrom ustawiamy pelne godziny (11:00, 12:00 itd.)
        fsp.setCreatedDate(now);
        fsp.setActive(true);
        fsp = fspRepository.save(fsp);
        log.debug("Fsp with id {} has been created for User with id {}", fsp.getId(), fspUser.getId());
    }

    @Override
    @Transactional
    public FspDTO save(FspDTO fspDTO) {
        FspEntity fspEntity = getMapper().toEntity(fspDTO);
        if (fspValidator.isDeactivateOperation(fspDTO)) {
            log.debug("save() Deactivating FSP [id: {}]", fspEntity.getId());
            deactivateFsp(fspRepository.getOne(fspDTO.getId()));
        }
        if (fspValidator.isActivateOperation(fspDTO)) {
            log.debug("save() Activating FSP [id: {}]", fspEntity.getId());
            activateFsp(fspRepository.getOne(fspDTO.getId()));
        }
        updateFspOwner(fspRepository.getOne(fspDTO.getId()).getOwner(), fspDTO);
        fspRepository.save(fspEntity);
        fspDTO = getMapper().toDto(fspEntity);
        return fspDTO;
    }

    private void updateFspOwner(UserEntity userEntity, FspDTO fspDTO) {
        userEntity.setFirstName(fspDTO.getRepresentative().getFirstName());
        userEntity.setLastName(fspDTO.getRepresentative().getLastName());
        userEntity.setPhoneNumber(fspDTO.getRepresentative().getPhoneNumber());
        userEntity.setEmail(fspDTO.getRepresentative().getEmail());
    }

    @Override
    @Transactional
    public void deactivateFspsByValidFromToDates() {
        List<FspEntity> expiredFsps = fspRepository.findFspsToDeactivateByValidFromToDates();
        expiredFsps.forEach(fsp -> {
            if (findActiveFlexPotentialsOfFsp(fsp.getId()).isEmpty()) {
                deactivateFsp(fsp);
            } else {
                log.debug("deactivateFspsByValidFromToDates() Expired FSP [id: {}] has some active FlexPotentials. It will be not deactivated.)", fsp.getId());
            }
        });
    }

    /**
     * Deactivation of FSP and belonging Users to it.
     */
    private void deactivateFsp(FspEntity fsp) {
        log.debug("deactivateFsp() Deactivating FSP [id: {}]", fsp.getId());
        fsp.setActive(false);
        log.debug("deactivateFsp() Deactivating FSP Users");
        fsp.getUsers().forEach(u -> {
            log.debug("deactivateFsp() Deactivating FSP user [userId: {}]", u.getId());
            u.setActivated(false);
            userOnlineService.logout(u.getLogin());
        });
    }

    @Override
    @Transactional
    public void activateFspsByValidFromToDates() {
        List<FspEntity> fspsToActivate = fspRepository.findFspsToActivateByValidFromToDates();
        fspsToActivate.forEach(fsp -> {
            log.debug("activateFspsByValidFromToDates() Activating Fsp [id: {}]", fsp.getId());
            activateFsp(fsp);
        });
    }

    /**
     * Activation of FSP and belonging Users to it.
     */
    private void activateFsp(FspEntity fsp) {
        log.debug("activateFsp() Activating FSP [id: {}]", fsp.getId());
        fsp.setActive(true);
        log.debug("activateFsp() Activating FSP Users");
        fsp.getUsers().forEach(u -> {
            log.debug("activateFsp() Activating FSP user [userId: {}]", u.getId());
            u.setActivated(true);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isUserOwnerOfAnyFSP(String login) {
        return fspRepository.existsByOwner_Login(login);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<FspEntity> findFspOfUser(Long userId, String userLogin) {
        if (isUserOwnerOfAnyFSP(userLogin)) {
            return fspRepository.findByOwnerId(userId);
        }
        return fspRepository.findByUsersId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<FspDTO> findFspDtoOfUser(Long userId, String userLogin) {
        return Optional.ofNullable(fspMapper.toDto(findFspOfUser(userId, userLogin).orElse(null)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<FspCompanyMinDTO> getAllFspCompanyNamesByActiveAndRoles(List<Role> roles) {
        return fspRepository.findAllFspCompanyNameMinimalByActiveAndRoles(roles);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FspCompanyMinDTO> getAllFspCompanyNamesByRoles(List<Role> roles) {
        return fspRepository.findAllFspCompanyNameMinimalByRoles(roles);
    }

    @Override
    @Transactional(readOnly = true)
    public Set<FspCompanyMinDTO> getAllFspCompanyNamesByRolesWhereAttachedUnitIsActiveAndCertified(List<Role> roles) {
        Set<FspCompanyMinDTO> fspCompanyMinDTOS = fspRepository.findAllFspActiveCompanyNameMinimalByRolesWhereAttachedUnitIsActiveAndCertified(roles);
        return fspCompanyMinDTOS;

    }

    @Override
    public FileDTO exportFspsToFile(List<FspDTO> fsps, String langKey, boolean isOnlyDisplayedData, Screen screen) throws IOException {
        DataExporter<FspDTO> dataExporter = dataExporterFactory.getDataExporter(DataExporterFormat.XLSX, FspDTO.class, screen);
        return dataExporter.export(fsps, Locale.forLanguageTag(langKey), screen, isOnlyDisplayedData, STANDARD_DETAIL_SHEET);
    }

    private List<FlexPotentialEntity> findActiveFlexPotentialsOfFsp(Long fspId) {
        return flexPotentialRepository.findActiveFlexPotentialsOfFsp(fspId);
    }

    @Override
    public Optional<FspDTO> findByCompanyName(String companyName) {
        return fspRepository.findByCompanyName(companyName).map(fspMapper::toDto);
    }

    @Override
    @Transactional
    // flaga deleted dla fsp i uzytkownikow tego fsp
    public void delete(Long fspId) {
        log.debug("delete() Deleting FSP [id: {}]", fspId);
        FspEntity fspToDelete = fspRepository.getOne(fspId);
        fspToDelete.setActive(false);
        fspToDelete.setDeleted(true);
        fspToDelete.getUsers().forEach(user -> {
            log.debug("delete() Deleting FSP User [userId: {}]", user.getId());
            userService.deleteUser(user.getId());
        });
    }

    @Override
    @Transactional(readOnly = true)
    public List<FspCompanyMinDTO> getBspsWithNotEmptySchedulingUnitsMinimal() {
        return fspRepository.findAllActiveBspWithNotEmptySchedulingUnitsMinimal();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByCompanyName(String companyName) {
        return fspRepository.existsByCompanyNameIgnoreCase(companyName);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FspCompanyMinDTO> findFspsWithRegisteredPotentialsForProduct(Long productId) {
        return fspRepository.findFspsWithRegisteredPotentialsForProduct(productId);
    }

    @Override
    @Transactional(readOnly = true)
    public Role findFspRole(Long fspId) {
        return fspRepository.findFspRole(fspId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MinimalDTO<Long, String>> findFspUsersMin(Long fspId) {
        return fspRepository.findFspUsersMin(fspId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MinimalDTO<Long, String>> findFspUsersMin(Set<Long> fspIds) {
        return fspRepository.findFspUsersMin(fspIds);
    }

    //********************************************************************************** NOTIFICATION ************************************************************************************

    @Override
    @Transactional
    public void registerUpdateNotification(FspDTO oldFsp, FspDTO modifyFsp) {
        UserEntity modifyRepresentative = userService.findOne(modifyFsp.getRepresentative().getId()).get();
        Map<NotificationParam, NotificationParamValue> notificationParams = NotificationUtils.ParamsMapBuilder.create()
            .addParam(NotificationParam.FSP_NAME, modifyFsp.getCompanyName())
            .addParam(NotificationParam.ID, modifyFsp.getId())
            .addModificationParam(NotificationParam.COMPANY, oldFsp.getCompanyName(), modifyFsp.getCompanyName())
            .addModificationParam(NotificationParam.NAME, oldFsp.getRepresentative().getFirstName(), modifyRepresentative.getFirstName())
            .addModificationParam(NotificationParam.LAST_NAME, oldFsp.getRepresentative().getLastName(), modifyRepresentative.getLastName())
            .addModificationParam(NotificationParam.EMAIL, oldFsp.getRepresentative().getEmail(), modifyRepresentative.getEmail())
            .addModificationParam(NotificationParam.PHONE_NUMBER, oldFsp.getRepresentative().getPhoneNumber(), modifyRepresentative.getPhoneNumber())
            .addModificationParam(NotificationParam.VALID_FROM, oldFsp.getValidFrom(), modifyFsp.getValidFrom())
            .addModificationParam(NotificationParam.VALID_TO, oldFsp.getValidTo(), modifyFsp.getValidTo())
            .addModificationParam(NotificationParam.ACTIVE, oldFsp.isActive(), modifyFsp.isActive())
            .build();

        if (oldFsp.getRole().equals(Role.ROLE_BALANCING_SERVICE_PROVIDER) && oldFsp.isAgreementWithTso() != modifyFsp.isAgreementWithTso()) {
            notificationParams.put(NotificationParam.AGREEMENT_WITH_TSO, NotificationParamValue.ParamValueBuilder.create().addParam(modifyFsp.isAgreementWithTso()).build());
        }

        Set<MinimalDTO<Long, String>> usersToBeNotified = getUsersToBeNotified(oldFsp);
        NotificationEvent notificationEvent = getNotificationEventByRole(oldFsp);
        NotificationUtils.registerNewNotificationForSpecifiedUsers(notifierFactory, notificationEvent, notificationParams, new ArrayList<>(usersToBeNotified));
    }

    private NotificationEvent getNotificationEventByRole(FspDTO oldFsp) {
        if (Role.ROLE_BALANCING_SERVICE_PROVIDER.equals(oldFsp.getRole())) {
            return BSP_UPDATED;
        }
        return FSP_UPDATED;
    }

    @Override
    @Transactional
    public void sendMailInformingAboutModification(FspDTO oldFsp, FspDTO modifyFsp) {
        UserEntity modifyRepresentative = userService.findOne(modifyFsp.getRepresentative().getId()).get();
        Set<MinimalDTO<Long, String>> usersToBeNotified = getUsersToBeNotified(oldFsp);
        usersToBeNotified.forEach(u -> fspMailService.informUserAboutFspEdition(userService.findOne(u.getId()).get(), oldFsp, modifyFsp, modifyRepresentative));
    }

    // Komunikat wyswietlany jest dla:
    // - uzytkownika ktory stworzy FSP,
    // - uzytkownika ktory ostatnio zmodyfikowal danego FSP,
    // - uzytkownika ktory aktualnie modyfikuje danego FSP,
    // - uzytkownikow przyspisanych do FSP
    private Set<MinimalDTO<Long, String>> getUsersToBeNotified(FspDTO oldFspDto) {
        //uzytkownik ktory stworzyl FSP
        Set<MinimalDTO<Long, String>> usersToBeNotified = new HashSet(userService.getUsersByLogin(Set.of(oldFspDto.getCreatedBy())));
        //uzytkownik ktory ostatnio modyfikowal FSP
        usersToBeNotified.addAll(userService.getUsersByLogin(Set.of(oldFspDto.getLastModifiedBy())));
        //uzytkownik ktory aktualnie modyfikuje FSP
        UserDTO modifiedUser = userService.getCurrentUserDTO().get();
        usersToBeNotified.add(new MinimalDTO<>(modifiedUser.getId(), modifiedUser.getLogin()));
        // uzytkownicy nalezacy do FSP
        List<MinimalDTO<Long, String>> fspUsersMin = fspRepository.findFspUsersMin(oldFspDto.getId());
        usersToBeNotified.addAll(fspUsersMin);
        return usersToBeNotified;
    }

    //********************************************************************************** NOTIFICATION ************************************************************************************


    @Override
    public AbstractJpaRepository<FspEntity, Long> getRepository() {
        return this.fspRepository;
    }

    @Override
    public EntityMapper<FspDTO, FspEntity> getMapper() {
        return this.fspMapper;
    }
}
