package pl.com.tt.flex.server.service.user.registration;

import io.github.jhipster.security.RandomUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.com.tt.flex.server.domain.user.UserEntity;
import pl.com.tt.flex.server.domain.user.registration.FspUserRegistrationCommentEntity;
import pl.com.tt.flex.server.domain.user.registration.FspUserRegistrationEntity;
import pl.com.tt.flex.server.domain.user.registration.FspUserRegistrationFileEntity;
import pl.com.tt.flex.server.domain.user.registration.enumeration.FspUserRegCommentCreationSource;
import pl.com.tt.flex.server.domain.user.registration.enumeration.FspUserRegGeneratedCommentText;
import pl.com.tt.flex.server.repository.user.registration.FspUserRegistrationCommentRepository;
import pl.com.tt.flex.server.repository.user.registration.FspUserRegistrationFileRepository;
import pl.com.tt.flex.server.repository.user.registration.FspUserRegistrationRepository;
import pl.com.tt.flex.model.security.permission.Role;
import pl.com.tt.flex.model.service.dto.file.FileDTO;
import pl.com.tt.flex.server.service.fsp.FspService;
import pl.com.tt.flex.server.service.user.UserOnlineService;
import pl.com.tt.flex.server.service.user.UserService;
import pl.com.tt.flex.server.service.user.registration.dto.FspUserRegistrationCommentDTO;
import pl.com.tt.flex.server.service.user.registration.dto.FspUserRegistrationDTO;
import pl.com.tt.flex.server.service.user.registration.dto.FspUserRegistrationFileDTO;
import pl.com.tt.flex.server.service.user.registration.mapper.FspUserRegistrationCommentMapper;
import pl.com.tt.flex.server.service.user.registration.mapper.FspUserRegistrationFileMapper;
import pl.com.tt.flex.server.service.user.registration.mapper.FspUserRegistrationMapper;
import pl.com.tt.flex.server.util.ZipUtil;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;
import static pl.com.tt.flex.server.domain.user.registration.enumeration.FspUserRegistrationStatus.*;

/**
 * Service Implementation for managing {@link FspUserRegistrationEntity}.
 */
@Slf4j
@Service
@Transactional
public class FspUserRegistrationServiceImpl implements FspUserRegistrationService {

    private final FspUserRegistrationRepository fspUserRegistrationRepository;
    private final FspUserRegistrationFileRepository fspUserRegistrationFileRepository;
    private final FspUserRegistrationCommentRepository fspUserRegistrationCommentRepository;

    private final FspUserRegistrationMapper fspUserRegistrationMapper;
    private final FspUserRegistrationFileMapper fspUserRegistrationFileMapper;
    private final FspUserRegistrationCommentMapper fspUserRegistrationCommentMapper;

    private final UserService userService;
    private final FspService fspService;
    private final UserOnlineService userOnlineService;

    public FspUserRegistrationServiceImpl(FspUserRegistrationRepository fspUserRegistrationRepository, FspUserRegistrationFileRepository fspUserRegistrationFileRepository,
        FspUserRegistrationCommentRepository fspUserRegistrationCommentRepository, FspUserRegistrationMapper fspUserRegistrationMapper,
        FspUserRegistrationFileMapper fspUserRegistrationFileMapper, FspUserRegistrationCommentMapper fspUserRegistrationCommentMapper,
        UserService userService, FspService fspService, UserOnlineService userOnlineService) {

        this.fspUserRegistrationRepository = fspUserRegistrationRepository;
        this.fspUserRegistrationFileRepository = fspUserRegistrationFileRepository;
        this.fspUserRegistrationCommentRepository = fspUserRegistrationCommentRepository;
        this.fspUserRegistrationMapper = fspUserRegistrationMapper;
        this.fspUserRegistrationFileMapper = fspUserRegistrationFileMapper;
        this.fspUserRegistrationCommentMapper = fspUserRegistrationCommentMapper;
        this.userService = userService;
        this.fspService = fspService;
        this.userOnlineService = userOnlineService;
    }

    @Transactional(readOnly = true)
    public Optional<FspUserRegistrationDTO> findOne(Long id) {
        return fspUserRegistrationRepository.findById(id).map(fspUserRegistrationMapper::toDto);
    }

    @Override
    @Transactional
    public FspUserRegistrationEntity createRegistrationRequest(FspUserRegistrationDTO fspUserRegistrationDTO, List<FspUserRegistrationFileDTO> fspFileDTOS) {
        FspUserRegistrationEntity fspUserRegistrationEntity = fspUserRegistrationMapper.toEntity(fspUserRegistrationDTO);
        fspUserRegistrationEntity.setReadByAdmin(false);
        fspUserRegistrationEntity.setLangKey(fspUserRegistrationDTO.getLangKey());
        fspUserRegistrationEntity.setSecurityKey(RandomUtil.generateRandomAlphanumericString());
        addInitialCommentWithFiles(fspUserRegistrationEntity, fspFileDTOS);
        fspUserRegistrationEntity = fspUserRegistrationRepository.save(fspUserRegistrationEntity);
        log.debug("FspUserRegistration saved with id: {}", fspUserRegistrationEntity.getId());
        return fspUserRegistrationEntity;
    }

    private void addInitialCommentWithFiles(FspUserRegistrationEntity fspUserRegistrationEntity, List<FspUserRegistrationFileDTO> fspFileDTOS) {
        FspUserRegistrationCommentEntity initialCommentForFiles = new FspUserRegistrationCommentEntity();
        initialCommentForFiles.setCreationSource(FspUserRegCommentCreationSource.INITIAL);
        initialCommentForFiles.getFiles().addAll(fspUserRegistrationFileMapper.toEntity(fspFileDTOS));
        for (FspUserRegistrationFileEntity fspUserRegistrationFileEntity : initialCommentForFiles.getFiles()) {
            fspUserRegistrationFileEntity.setComment(initialCommentForFiles);
        }
        initialCommentForFiles.setFspUserRegistration(fspUserRegistrationEntity);
        fspUserRegistrationEntity.getComments().add(initialCommentForFiles);
    }

    @Override
    @Transactional
    public FspUserRegistrationDTO confirmNewRegistrationRequestByFsp(Long fspUserRegId) {
        FspUserRegistrationEntity fspUserRegistrationEntity = fspUserRegistrationRepository.getOne(fspUserRegId);
        fspUserRegistrationEntity.setStatus(CONFIRMED_BY_FSP);
        fspUserRegistrationEntity.setSecurityKey(null);
        log.debug("Registration request confirmed of fspUserRegistration with id: {}", fspUserRegistrationEntity.getId());
        return fspUserRegistrationMapper.toDto(fspUserRegistrationEntity);
    }

    @Override
    @Transactional
    public void withdrawNewRegistrationRequestByFsp(Long fspUserRegId, boolean removeDbEntry) {
        FspUserRegistrationEntity fspUserRegistrationEntity = fspUserRegistrationRepository.getOne(fspUserRegId);
        if (removeDbEntry) {
            fspUserRegistrationRepository.delete(fspUserRegistrationEntity);
            log.debug("FspUserRegistration with id {} has been removed from db", fspUserRegistrationEntity.getId());
        } else {
            fspUserRegistrationEntity.setStatus(WITHDRAWN_BY_FSP);
            fspUserRegistrationEntity.setSecurityKey(null);
            log.debug("FspUserRegistration with id {} has been updated with status {}", fspUserRegistrationEntity.getId(), fspUserRegistrationEntity.getStatus());
        }
    }

    @Override
    @Transactional
    public FspUserRegistrationEntity preConfirmRegistrationRequestByMo(Long fspUserRegId) {
        FspUserRegistrationEntity fspUserRegistrationEntity = fspUserRegistrationRepository.getOne(fspUserRegId);
        fspUserRegistrationEntity.setStatus(PRE_CONFIRMED_BY_MO);
        UserEntity preFspUser = userService.createUserForFspRegistration(fspUserRegistrationEntity);
        fspUserRegistrationEntity.setFspUser(preFspUser);
        return fspUserRegistrationEntity;
    }

    @Override
    @Transactional
    public FspUserRegistrationEntity acceptRegistrationRequestByMo(Long fspUserRegId) {
        FspUserRegistrationEntity fspUserRegistrationEntity = fspUserRegistrationRepository.getOne(fspUserRegId);
        fspUserRegistrationEntity.setStatus(ACCEPTED_BY_MO);
        fspUserRegistrationEntity.setReadByAdmin(true);
        fspUserRegistrationEntity.getFspUser().getRoles().add(fspUserRegistrationEntity.getUserTargetRole());
        fspUserRegistrationEntity.getFspUser().getRoles().remove(Role.ROLE_FSP_USER_REGISTRATION);
        userOnlineService.logout(fspUserRegistrationEntity.getFspUser().getLogin());
        fspUserRegistrationEntity.getComments().add(generateCommentForCurrentUser(FspUserRegGeneratedCommentText.ACCEPTED_BY_ADMIN, fspUserRegistrationEntity));
        log.debug("FspUserRegistration with id {} has been updated with status {} and role {} has been added to user with id {}", fspUserRegistrationEntity.getId(),
            fspUserRegistrationEntity.getStatus(), fspUserRegistrationEntity.getUserTargetRole(), fspUserRegistrationEntity.getFspUser().getId());
        fspService.createFspForNewlyRegisteredFspUser(fspUserRegistrationEntity);
        return fspUserRegistrationEntity;
    }

    private FspUserRegistrationCommentEntity generateCommentForCurrentUser(FspUserRegGeneratedCommentText text, FspUserRegistrationEntity fspUserRegistrationEntity) {
        FspUserRegistrationCommentEntity commentEntity = new FspUserRegistrationCommentEntity();
        commentEntity.setText(text.name());
        commentEntity.setCreationSource(FspUserRegCommentCreationSource.GENERATED);
        commentEntity.setFspUserRegistration(fspUserRegistrationEntity);
        commentEntity.setUser(userService.getCurrentUser());
        return commentEntity;
    }

    @Override
    @Transactional
    public FspUserRegistrationEntity rejectRegistrationRequestByMo(Long fspUserRegId) {
        FspUserRegistrationEntity fspUserRegistrationEntity = fspUserRegistrationRepository.getOne(fspUserRegId);
        fspUserRegistrationEntity.setStatus(REJECTED_BY_MO);
        if (nonNull(fspUserRegistrationEntity.getFspUser())) {
            UserEntity fspUser = fspUserRegistrationEntity.getFspUser();
            fspUser.getRoles().clear();
            userService.deleteUser(fspUserRegistrationEntity.getFspUser().getId());
            log.debug("Fsp user with id {} is deactivated", fspUser.getId());
        }
        fspUserRegistrationEntity.getComments().add(generateCommentForCurrentUser(FspUserRegGeneratedCommentText.REJECTED_BY_ADMIN, fspUserRegistrationEntity));
        log.debug("FspUserRegistration with id {} has been updated with status {}", fspUserRegistrationEntity.getId(), fspUserRegistrationEntity.getStatus());
        return fspUserRegistrationEntity;
    }

    @Override
    @Transactional
    public FspUserRegistrationEntity withdrawPreConfirmedRegistrationRequestByFsp(Long fspUserRegId) {
        FspUserRegistrationEntity fspUserRegistrationEntity = fspUserRegistrationRepository.getOne(fspUserRegId);
        fspUserRegistrationEntity.setStatus(WITHDRAWN_BY_FSP);
        fspUserRegistrationEntity.getComments().add(generateCommentForCurrentUser(FspUserRegGeneratedCommentText.WITHDRAWN_BY_USER, fspUserRegistrationEntity));
        fspUserRegistrationEntity.getFspUser().getRoles().clear();

        userService.deleteUser(fspUserRegistrationEntity.getFspUser().getId());
        log.debug("FspUserRegistration with id {} has been updated with status {} and user with id {} is deactivated", fspUserRegistrationEntity.getId(),
            fspUserRegistrationEntity.getStatus(), fspUserRegistrationEntity.getFspUser().getId());
        return fspUserRegistrationEntity;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<FspUserRegistrationDTO> findOneBySecurityKey(String key) {
        return fspUserRegistrationRepository.findOneBySecurityKey(key).map(fspUserRegistrationMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<FspUserRegistrationFileEntity> getFspUserRegFileByFileId(Long fspUserRegFileId) {
        return fspUserRegistrationFileRepository.findById(fspUserRegFileId);
    }

    @Override
    @Transactional
    public void addFileToFspUserRegistration(FspUserRegistrationFileDTO fspUserRegfileDTO) {
        fspUserRegistrationFileRepository.save(fspUserRegistrationFileMapper.toEntity(fspUserRegfileDTO));
    }

    @Override
    @Transactional(readOnly = true)
    public List<FileDTO> getZipWithAllFilesOfFspUserRegistration(Long fspUserRegId) {
        FspUserRegistrationEntity registrationEntity = fspUserRegistrationRepository.getOne(fspUserRegId);
        List<FileDTO> fileDTOS = Lists.newArrayList();
        registrationEntity.getComments().forEach(comment -> fileDTOS.addAll(comment.getFiles().stream().map(file -> new FileDTO(file.getFileName(),
            ZipUtil.zipToFiles(file.getFileZipData()).get(0).getBytesData())).collect(Collectors.toList())));
        return fileDTOS;
    }

    @Override
    @Transactional
    public FspUserRegistrationCommentDTO addCommentToFspUserRegistration(FspUserRegistrationCommentDTO commentDTO, UserEntity currentUser) {
        commentDTO.setUserId(currentUser.getId());
        commentDTO.setCreationSource(FspUserRegCommentCreationSource.NORMAL);
        FspUserRegistrationCommentEntity commentEntity = fspUserRegistrationCommentRepository.save(fspUserRegistrationCommentMapper.toEntity(commentDTO));
        FspUserRegistrationEntity registrationEntity = fspUserRegistrationRepository.getOne(commentDTO.getFspUserRegistrationId());
        registrationEntity.setReadByAdmin(!isCandidate(currentUser));
//        registrationEntity.getComments().add(fspUserRegistrationCommentMapper.toEntity(commentDTO));
        return fspUserRegistrationCommentMapper.toDto(commentEntity);
    }

    private boolean isCandidate(UserEntity currentUser) {
        return currentUser.getRoles().contains(Role.ROLE_FSP_USER_REGISTRATION);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FspUserRegistrationCommentDTO> findAllCommentsOfFspUserRegistration(Long fspUserRegId) {
        return fspUserRegistrationCommentMapper.toDto(fspUserRegistrationCommentRepository.findAllByFspUserRegistrationIdOrderByIdAsc(fspUserRegId));
    }

    @Override
    public void markFspUserRegistrationAsReadByAdmin(Long fspUserRegId, UserEntity currentUser) {
        FspUserRegistrationEntity registrationEntity = fspUserRegistrationRepository.getOne(fspUserRegId);
        registrationEntity.setReadByAdmin(true);
        FspUserRegistrationCommentEntity comment = new FspUserRegistrationCommentEntity();
        comment.setCreationSource(FspUserRegCommentCreationSource.GENERATED);
        comment.setText(FspUserRegGeneratedCommentText.READ_BY_ADMIN.name());
        comment.setUser(currentUser);
        comment.setFspUserRegistration(registrationEntity);
        registrationEntity.getComments().add(comment);
        fspUserRegistrationRepository.save(registrationEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<FspUserRegistrationDTO> findOneByUserActivationKey(String key) {
        return fspUserRegistrationRepository.findOneByFspUserActivationKey(key).map(fspUserRegistrationMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<FspUserRegistrationDTO> findOneByFspUserId(Long fspUserId) {
        return fspUserRegistrationRepository.findOneByFspUserId(fspUserId).map(fspUserRegistrationMapper::toDto);
    }
}
