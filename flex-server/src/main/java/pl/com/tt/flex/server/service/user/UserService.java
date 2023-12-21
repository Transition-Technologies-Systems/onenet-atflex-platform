package pl.com.tt.flex.server.service.user;

import com.google.common.collect.Sets;
import io.github.jhipster.security.RandomUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import pl.com.tt.flex.model.security.permission.Role;
import pl.com.tt.flex.server.common.errors.mail.EmailAlreadyUsedException;
import pl.com.tt.flex.server.common.errors.user.InvalidPasswordException;
import pl.com.tt.flex.server.common.errors.user.LoginContainsInvalidCharactersException;
import pl.com.tt.flex.server.config.ApplicationProperties;
import pl.com.tt.flex.server.config.Constants;
import pl.com.tt.flex.server.domain.enumeration.NotificationEvent;
import pl.com.tt.flex.server.domain.enumeration.NotificationParam;
import pl.com.tt.flex.server.domain.fsp.FspEntity;
import pl.com.tt.flex.server.domain.user.UserEntity;
import pl.com.tt.flex.server.domain.user.enumeration.CreationSource;
import pl.com.tt.flex.server.domain.user.registration.FspUserRegistrationEntity;
import pl.com.tt.flex.server.domain.user.registration.enumeration.FspUserRegistrationStatus;
import pl.com.tt.flex.server.repository.fsp.FspRepository;
import pl.com.tt.flex.server.repository.user.UserRepository;
import pl.com.tt.flex.server.security.SecurityUtils;
import pl.com.tt.flex.model.service.dto.file.FileDTO;
import pl.com.tt.flex.model.service.dto.MinimalDTO;
import pl.com.tt.flex.server.service.mail.MailService;
import pl.com.tt.flex.server.service.notification.dto.NotificationParamValue;
import pl.com.tt.flex.server.service.notification.factory.NotifierFactory;
import pl.com.tt.flex.server.service.notification.util.NotificationUtils;
import pl.com.tt.flex.server.service.user.dto.UserDTO;
import pl.com.tt.flex.server.service.user.dto.UserMinDTO;
import pl.com.tt.flex.server.service.user.mapper.UserMapper;
import pl.com.tt.flex.server.util.InstantUtil;
import pl.com.tt.flex.server.web.rest.errors.user.CurrentUserNotFoundException;
import pl.com.tt.flex.server.web.rest.user.RulesFileType;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;
import static pl.com.tt.flex.model.security.permission.Role.*;

/**
 * Service class for managing users.
 */
@Slf4j
@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final CacheManager cacheManager;

    private final FspRepository fspRepository;

    private final UserMapper userMapper;

    private final MailService mailService;

    private final ApplicationProperties applicationProperties;

    private final UserOnlineService userOnlineService;

    private final NotifierFactory notifierFactory;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, CacheManager cacheManager, FspRepository fspRepository, UserMapper userMapper,
                       MailService mailService, ApplicationProperties applicationProperties, UserOnlineService userOnlineService, @Lazy NotifierFactory notifierFactory) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.cacheManager = cacheManager;
        this.fspRepository = fspRepository;
        this.userMapper = userMapper;
        this.mailService = mailService;
        this.applicationProperties = applicationProperties;
        this.userOnlineService = userOnlineService;
        this.notifierFactory = notifierFactory;
    }

    // new not activated User with role ROLE_FSP_USER_REGISTRATION
    public UserEntity createUserForFspRegistration(FspUserRegistrationEntity fspUserReg) {
        if (existsByEmailIgnoreCase(fspUserReg.getEmail())) {
            throw new EmailAlreadyUsedException();
        }
        UserEntity user = new UserEntity();
        user.setLogin("fsp" + fspUserReg.getId()); //login zostanie nadpisany przez uzytkownika przy jego aktywacji
        user.setActivationKey(RandomUtil.generateActivationKey());
        user.setActivated(false);
        user.setRoles(Sets.newHashSet(Role.ROLE_FSP_USER_REGISTRATION));
        user.setFirstName(fspUserReg.getFirstName());
        user.setLastName(fspUserReg.getLastName());
        user.setEmail(fspUserReg.getEmail().toLowerCase());
        user.setPhoneNumber(fspUserReg.getPhoneNumber());
        user.setLangKey(fspUserReg.getLangKey());
        String encryptedPassword = passwordEncoder.encode(RandomUtil.generatePassword());
        user.setPassword(encryptedPassword);
        user.setCreationSource(CreationSource.REGISTRATION);
        userRepository.save(user);
        this.clearUserCaches(user);
        log.debug("Created new not activated FSP User '{}' with role '{}' during fspUserRegistration process [id: {}]",
            user, Role.ROLE_FSP_USER_REGISTRATION, fspUserReg.getId());
        return user;
    }

    public Optional<UserEntity> activateAndSetPassword(String key, String password, String login) {
        return userRepository.findOneByActivationKey(key)
//            .filter(user -> user.getCreatedDate().isAfter(InstantUtil.now().minusSeconds(86400)))
            .map(user -> {
                user.setActivated(true);
                user.setActivationKey(null);
                user.setPassword(passwordEncoder.encode(password));
                if (loginContainsCorrectCharacters(login)) {
                    user.setLogin(login);
                } else {
                    throw new LoginContainsInvalidCharactersException("Login cannot contain Polish characters");
                }
                user.setResetDate(InstantUtil.now());
                if (nonNull(user.getFspUserRegistration())) {
                    user.getFspUserRegistration().setStatus(FspUserRegistrationStatus.USER_ACCOUNT_ACTIVATED_BY_FSP);
                }
                this.clearUserCaches(user);
                log.debug("Activated user: {}", user);
                return user;
            });
    }

    public Optional<UserEntity> completePasswordReset(String newPassword, String key) {
        log.debug("Reset user password for reset key {}", key);
        return userRepository.findOneByResetKey(key)
            .filter(user -> user.getResetDate().isAfter(InstantUtil.now().minusSeconds(86400)))
            .map(user -> {
                user.setPassword(passwordEncoder.encode(newPassword));
                user.setResetKey(null);
                user.setResetDate(null);
                user.setPasswordChangeOnFirstLogin(false);
                user.setActivated(true);
                this.clearUserCaches(user);
                return user;
            });
    }

    public Optional<UserEntity> requestPasswordReset(String mail) {
        return userRepository.findOneByEmailAndActivatedTrueIgnoreCase(mail)
            .filter(UserEntity::isActivated)
            .map(user -> {
                user.setResetKey(RandomUtil.generateResetKey());
                user.setResetDate(InstantUtil.now());
                this.clearUserCaches(user);
                return user;
            });
    }

    public UserDTO createUser(UserDTO userDTO) {
        UserEntity user = new UserEntity();
        user.setLogin(userDTO.getLogin());
        user.setFirstName(userDTO.getFirstName());
        user.setLastName(userDTO.getLastName());
        user.setPhoneNumber(userDTO.getPhoneNumber());
        user.setEmail(userDTO.getEmail().toLowerCase());
        user.setCreationSource(CreationSource.ADMIN);
        user.setRoles(userDTO.getRoles());
        user.setActivated(userDTO.isActivated());
        user.setPasswordChangeOnFirstLogin(userDTO.isPasswordChangeOnFirstLogin());

        if (userDTO.getLangKey() == null) {
            user.setLangKey(Constants.DEFAULT_LANGUAGE);
        } else {
            user.setLangKey(userDTO.getLangKey());
        }

        if (userDTO.getPassword() == null) {
            user.setPassword(passwordEncoder.encode(RandomUtil.generatePassword()));
            user.setResetKey(RandomUtil.generateResetKey());
            user.setResetDate(InstantUtil.now());
        } else {
            String encryptedPassword = passwordEncoder.encode(userDTO.getPassword());
            user.setPassword(encryptedPassword);
        }

        userRepository.save(user);

        if (userDTO.getFspId() != null) {
            addUserToFsp(userDTO.getFspId(), user);
        }
        registerNewNotification(user, NotificationEvent.USER_CREATED);
        mailService.sendCreationEmail(user);
        this.clearUserCaches(user);
        log.debug("Created Information for User: {}", user);
        return userMapper.userToUserDTO(user);
    }

    /**
     * Links User with its FSP company if User is destinated for FSP platform
     */
    private void addUserToFsp(Long fspId, UserEntity user) {
        if (FSP_ORGANISATIONS_ROLES.stream().anyMatch(role -> user.getRoles().contains(role))) {
            FspEntity fsp = fspRepository.getOne(fspId);
            user.setFsp(fsp);
        }
    }

    /**
     * Update all information for a specific user, and return the modified user.
     *
     * @param userDTO user to update.
     * @return updated user.
     */
    @Transactional
    public UserDTO updateUser(UserDTO userDTO) {
        return userRepository.findById(userDTO.getId()).map(user -> {
            user.setLogin(userDTO.getLogin());
            user.setFirstName(userDTO.getFirstName());
            user.setLastName(userDTO.getLastName());
            user.setEmail(userDTO.getEmail().toLowerCase());
            user.setPhoneNumber(userDTO.getPhoneNumber());
            user.setRoles(userDTO.getRoles());
            if (user.isActivated() && !userDTO.isActivated()) {
                userOnlineService.logout(user.getLogin());
            }
            user.setActivated(userDTO.isActivated());
            if (nonNull(userDTO.getFspId())) {
                FspEntity fsp = fspRepository.getOne(userDTO.getFspId());
                user.setFsp(fsp);
            }
            this.clearUserCaches(user);
            log.debug("Changed Information for User: {}", user);
            return userMapper.userToUserDTO(user);
        }).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    /**
     * Performs soft delete on user with given id.
     * Deactivates user and marks it as deleted, also clears user's cache.
     */
    public void deleteUser(Long id) {
        log.info("deleteUser() Deleting user with id: {}", id);
        userRepository.findById(id)
            .ifPresent(user -> {
                user.setDeleted(true);
                user.setActivated(false);
                userRepository.save(user);
                this.clearUserCaches(user);
                userOnlineService.logout(user.getLogin());
                log.debug("Deleted User: {}", user);
            });
    }

    @Transactional
    public void changePassword(String currentClearTextPassword, String newPassword) {
        SecurityUtils.getCurrentUserLogin()
            .flatMap(userRepository::findOneByLoginAndDeletedIsFalse)
            .ifPresent(user -> {
                String currentEncryptedPassword = user.getPassword();
                if (!passwordEncoder.matches(currentClearTextPassword, currentEncryptedPassword)) {
                    throw new InvalidPasswordException();
                }
                String encryptedPassword = passwordEncoder.encode(newPassword);
                user.setPassword(encryptedPassword);
                user.setPasswordChangeOnFirstLogin(false);
                this.clearUserCaches(user);
                log.debug("Changed password for User: {}", user);
            });
    }

    private void registerNewNotification(UserEntity userEntity, NotificationEvent event) {
        NotificationUtils.ParamsMapBuilder builder = NotificationUtils.ParamsMapBuilder.create();
        if (userEntity.getFsp() != null && isUserFsp(userEntity)) {
            builder.addParam(NotificationParam.COMPANY, userEntity.getFsp().getCompanyName());
        }
        Map<NotificationParam, NotificationParamValue> notificationParams = builder.addParam(NotificationParam.ID, userEntity.getId())
            .addParam(NotificationParam.LOGIN, userEntity.getLogin())
            .addParam(NotificationParam.NAME, userEntity.getFirstName())
            .addParam(NotificationParam.LAST_NAME, userEntity.getLastName())
            .addParam(NotificationParam.PHONE_NUMBER, userEntity.getPhoneNumber())
            .addParam(NotificationParam.EMAIL, userEntity.getEmail())
            .addParam(NotificationParam.ROLE, getRolesAsString(userEntity.getRoles()))
            .addParam(NotificationParam.ACTIVE, userEntity.isActivated())
            .build();

        // Przy tworzeniu użytkownika przez panel administracyjny komunikat pojawia się tylko u użytkownika który go stworzył
        List<MinimalDTO<Long, String>> usersToBeNotified = getUsersByLogin(Set.of(userEntity.getCreatedBy()));
        NotificationUtils.registerNewNotificationForSpecifiedUsers(notifierFactory, event, notificationParams, usersToBeNotified);
    }

    private String getRolesAsString(Set<Role> userRoles) {
        return userRoles.stream().map(Role::getShortName).collect(Collectors.joining(", "));
    }

    private boolean isUserFsp(UserEntity userEntity) {
        return userEntity.hasRole(ROLE_FLEX_SERVICE_PROVIDER) || userEntity.hasRole(ROLE_FLEX_SERVICE_PROVIDER_AGGREGATED);
    }

    public void registerNewNotificationForUserEdition(UserEntity oldUserEntity, UserDTO userDTO) {
        NotificationUtils.ParamsMapBuilder builder = NotificationUtils.ParamsMapBuilder.create();
        Map<NotificationParam, NotificationParamValue> params = builder.addParam(NotificationParam.ID, userDTO.getId())
            .addParam(NotificationParam.LOGIN, userDTO.getLogin())
            .addModificationParam(NotificationParam.NAME, oldUserEntity.getFirstName(), userDTO.getFirstName())
            .addModificationParam(NotificationParam.LAST_NAME, oldUserEntity.getLastName(), userDTO.getLastName())
            .addModificationParam(NotificationParam.PHONE_NUMBER, oldUserEntity.getPhoneNumber(), userDTO.getPhoneNumber())
            .addModificationParam(NotificationParam.EMAIL, oldUserEntity.getEmail(), userDTO.getEmail())
            .addModificationParam(NotificationParam.ROLE, oldUserEntity.getRoles(), userDTO.getRoles())
            .addModificationParam(NotificationParam.ACTIVE, oldUserEntity.isActivated(), userDTO.isActivated())
            .build();
        addFspParamForUserEditionNotification(oldUserEntity, userDTO, params);
        List<MinimalDTO<Long, String>> usersToBeNotified = getUsersToBeNotified(oldUserEntity);
        NotificationUtils.registerNewNotificationForSpecifiedUsers(notifierFactory, NotificationEvent.USER_UPDATED, params, usersToBeNotified);
    }

    private void addFspParamForUserEditionNotification(UserEntity oldUserEntity, UserDTO userDTO, Map<NotificationParam, NotificationParamValue> params) {
        if (oldUserEntity.getFsp() != null) {
            params.put(NotificationParam.COMPANY, NotificationParamValue.ParamValueBuilder.create().addParam(userDTO.getCompanyName()).build());
        }
    }

    private List<MinimalDTO<Long, String>> getUsersToBeNotified(UserEntity userEntity) {
        List<MinimalDTO<Long, String>> usersToBeNotified;
        String currentUserLogin = getCurrentUser().getLogin();

        if (!userEntity.getCreatedBy().equals(userEntity.getLastModifiedBy()) && !userEntity.getLastModifiedBy().equals(currentUserLogin) &&
            !userEntity.getCreatedBy().equals(currentUserLogin)) {
            usersToBeNotified = getUsersByLogin(Set.of(userEntity.getCreatedBy(), userEntity.getLastModifiedBy(), currentUserLogin));
        } else if (!userEntity.getCreatedBy().equals(userEntity.getLastModifiedBy()) && userEntity.getLastModifiedBy().equals(currentUserLogin)) {
            usersToBeNotified = getUsersByLogin(Set.of(userEntity.getCreatedBy(), userEntity.getLastModifiedBy()));
        } else if (!userEntity.getCreatedBy().equals(userEntity.getLastModifiedBy()) && userEntity.getCreatedBy().equals(currentUserLogin)) {
            usersToBeNotified = getUsersByLogin(Set.of(userEntity.getCreatedBy(), userEntity.getLastModifiedBy()));
        } else if (userEntity.getCreatedBy().equals(userEntity.getLastModifiedBy()) && !userEntity.getCreatedBy().equals(currentUserLogin)) {
            usersToBeNotified = getUsersByLogin(Set.of(userEntity.getCreatedBy(), currentUserLogin));
        } else {
            usersToBeNotified = getUsersByLogin(Set.of(userEntity.getCreatedBy()));
        }

        return usersToBeNotified;
    }

    private boolean loginContainsCorrectCharacters(String login) {
        // znaki alfanumeryczne i podłoga są dozwolone
        String regex = "^[a-zA-Z0-9_]+$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(login);

        return matcher.find();
    }

    @Transactional(readOnly = true)
    public List<UserMinDTO> getUsersForPsoAndSso() {
        return userRepository.findNotAdminUsersByRole(Set.of(ROLE_TRANSMISSION_SYSTEM_OPERATOR, ROLE_DISTRIBUTION_SYSTEM_OPERATOR));
    }

    @Transactional(readOnly = true)
    public List<UserMinDTO> getUsersByIds(Set<Long> ids) {
        return userRepository.getUsersByIds(ids);
    }

    @Transactional(readOnly = true)
    public List<MinimalDTO<Long, String>> getUsersByLogin(Set<String> users) {
        return userRepository.getUsersByLogin(users);
    }

    @Transactional(readOnly = true)
    public Page<UserDTO> getAllManagedUsers(Pageable pageable) {
        return userRepository.findAllByLoginNotAndDeletedIsFalse(pageable, Constants.ANONYMOUS_USER).map(UserDTO::new);
    }

    @Transactional(readOnly = true)
    public Optional<UserDTO> getUserByLogin(String login) {
        return userRepository.findOneByLoginAndDeletedFalse(login).flatMap(userEntity -> Optional.of(userMapper.userToUserDTO(userEntity)));
    }

    @Transactional(readOnly = true)
    public UserEntity getCurrentUser() {
        return SecurityUtils.getCurrentUserLogin().flatMap(userRepository::findOneByLoginAndDeletedFalse).orElseThrow(() -> new CurrentUserNotFoundException("Cannot find current User"));
    }

    @Transactional(readOnly = true)
    public UserEntity getCurrentUserFetchFsp() {
        return SecurityUtils.getCurrentUserLogin().flatMap(userRepository::findOneByLoginAndDeletedFalseFetchFsp).orElseThrow(() -> new CurrentUserNotFoundException("Cannot find current User"));
    }

    @Transactional(readOnly = true)
    public Optional<UserDTO> getCurrentUserDTO() {
        return SecurityUtils.getCurrentUserLogin().flatMap(userRepository::findOneByLoginAndDeletedFalse).flatMap(userEntity -> Optional.of(userMapper.userToUserDTO(userEntity)));
    }

    private void clearUserCaches(UserEntity user) {
        Objects.requireNonNull(cacheManager.getCache(UserRepository.USERS_BY_LOGIN_CACHE)).evict(user.getLogin());
        if (user.getEmail() != null) {
            Objects.requireNonNull(cacheManager.getCache(UserRepository.USERS_BY_EMAIL_CACHE)).evict(user.getEmail());
        }
    }

    @Transactional(readOnly = true)
    public List<UserEntity> findUsersByRole(Role role) {
        return userRepository.findDistinctByActivatedTrueAndDeletedFalseAndRolesIn(Collections.singleton(role));
    }

    @Transactional(readOnly = true)
    public List<UserEntity> findUsersByRole(Set<Role> roles) {
        return userRepository.findDistinctByActivatedTrueAndDeletedFalseAndRolesIn(roles);
    }

    @Transactional(readOnly = true)
    public List<MinimalDTO<Long, String>> getUsersByRolesMinimal(Set<Role> roles) {
        return userRepository.getUsersByRolesMinimal(roles);
    }

    @Transactional(readOnly = true)
    public boolean doesUserHaveOneOfGivenRoles(Long userId, List<Role> roles) {
        return userRepository.existsByIdAndRolesIn(userId, roles);
    }

    @Transactional(readOnly = true)
    public List<MinimalDTO<Long, String>> getUsersByCompanyNameMinimal(String companyName) {
        return userRepository.getUsersByCompanyName(companyName);
    }

    @Transactional(readOnly = true)
    public Optional<UserEntity> findOne(Long userId) {
        return userRepository.findById(userId);
    }

    @Transactional(readOnly = true)
    public Optional<UserEntity> findOneByLogin(String login) {
        return userRepository.findOneByLoginAndDeletedFalse(login);
    }


    /**
     * Check whether email is already in use by not deleted user.
     */
    @Transactional(readOnly = true)
    public boolean existsByEmailIgnoreCase(String email) {
        return userRepository.existsByEmailIgnoreCaseAndDeleted(email, false);
    }

    @Transactional(readOnly = true)
    public boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id) {
        return userRepository.existsByEmailIgnoreCaseAndDeletedAndIdNot(email, false, id);
    }


    public Optional<FileDTO> getManualFile(String filename) throws IOException {
        FileSystemResource fileSystemResource = new FileSystemResource(applicationProperties.getUsersManual().getFilesPath() + "/" + filename);
        if (fileSystemResource.exists()) {
            byte[] fileData = FileUtils.readFileToByteArray(fileSystemResource.getFile());
            String fileName = fileSystemResource.getFilename();
            return Optional.of(new FileDTO(fileName, fileData));
        }
        log.debug("Not found file {} in directory {}", filename, applicationProperties.getUsersManual().getFilesPath());
        return Optional.empty();
    }

    public List<MinimalDTO<String, String>> getListOfManualFiles() {
        String dir = applicationProperties.getUsersManual().getFilesPath();

        Collection<File> files = FileUtils.listFiles(new File(dir), null, false);
        List<MinimalDTO<String, String>> listOfFiles = new ArrayList<>();
        files.forEach(f -> {
            MinimalDTO<String, String> minimalDTO = new MinimalDTO<>();
            minimalDTO.setId(f.getName());
            minimalDTO.setValue(FilenameUtils.getBaseName(f.getName()));
            listOfFiles.add(minimalDTO);
        });
        return listOfFiles;
    }

    public Optional<FileDTO> getRulesFile(RulesFileType type) throws IOException {
        if (type.equals(RulesFileType.RODO)) {
            FileSystemResource fileSystemResource = new FileSystemResource(applicationProperties.getUsersManual().getRodoFilePath());
            return getFileFromSystemResource(type, fileSystemResource, applicationProperties.getUsersManual().getRodoFilePath());
        }

        if (type.equals(RulesFileType.RULES)) {
            FileSystemResource fileSystemResource = new FileSystemResource(applicationProperties.getUsersManual().getRulesFilePath());
            return getFileFromSystemResource(type, fileSystemResource, applicationProperties.getUsersManual().getRulesFilePath());
        }
        log.debug("Not found file with type {}", type);
        return Optional.empty();
    }

    private Optional<FileDTO> getFileFromSystemResource(RulesFileType type, FileSystemResource fileSystemResource, String filePath) throws IOException {
        if (!fileSystemResource.exists()) {
            log.debug("Not found file {} with type {}", filePath, type);
            return Optional.empty();
        }
        byte[] fileData = FileUtils.readFileToByteArray(fileSystemResource.getFile());
        String fileName = fileSystemResource.getFilename();
        return Optional.of(new FileDTO(fileName, fileData));
    }

    @Transactional
    public void changeUserLanguage(String langKey) {
        getCurrentUser().setLangKey(langKey);
    }

    @Transactional
    public void clearFailedLoginCounterAndSetLoginDate(String login) {
        userRepository.findOneByLoginAndDeletedIsFalse(login).ifPresent(u -> {
            u.setLastSuccessfulLoginDate(InstantUtil.now());
            u.setUnsuccessfulLoginCount(0);
        });
    }

    @Transactional
    public void incrementFailedLoginCounter(String login) {
        userRepository.findOneByLoginAndDeletedIsFalse(login).ifPresent(u -> u.setUnsuccessfulLoginCount(u.getUnsuccessfulLoginCount() + 1));
    }

    @Transactional(readOnly = true)
    public String getLangKeyForCurrentLoggedUser() {
        UserEntity userEntity = getCurrentUser();
        String langKey = userEntity.getLangKey();
        clearUserCaches(userEntity);
        return langKey;
    }

    public boolean isUserLoggedIn() {
        return SecurityUtils.getCurrentUserLogin().flatMap(userRepository::findOneByLoginAndDeletedFalse).isPresent();
    }
}
