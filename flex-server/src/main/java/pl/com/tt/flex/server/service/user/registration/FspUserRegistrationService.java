package pl.com.tt.flex.server.service.user.registration;

import pl.com.tt.flex.server.domain.user.UserEntity;
import pl.com.tt.flex.server.domain.user.registration.FspUserRegistrationEntity;
import pl.com.tt.flex.server.domain.user.registration.FspUserRegistrationFileEntity;
import pl.com.tt.flex.model.service.dto.file.FileDTO;
import pl.com.tt.flex.server.service.user.registration.dto.FspUserRegistrationCommentDTO;
import pl.com.tt.flex.server.service.user.registration.dto.FspUserRegistrationDTO;
import pl.com.tt.flex.server.service.user.registration.dto.FspUserRegistrationFileDTO;

import java.util.List;
import java.util.Optional;

/**
 * Service Interface for managing {@link pl.com.tt.flex.server.domain.user.registration.FspUserRegistrationEntity}.
 */
public interface FspUserRegistrationService {

    Optional<FspUserRegistrationDTO> findOne(Long id);

    FspUserRegistrationEntity createRegistrationRequest(FspUserRegistrationDTO fspUserRegistrationDTO, List<FspUserRegistrationFileDTO> fspFileDTOS);

	FspUserRegistrationDTO confirmNewRegistrationRequestByFsp(Long fspUserRegId);

    void withdrawNewRegistrationRequestByFsp(Long fspUserRegId, boolean removeDbEntry);

    FspUserRegistrationEntity preConfirmRegistrationRequestByMo(Long id);

    FspUserRegistrationEntity acceptRegistrationRequestByMo(Long fspUserRegId);

    FspUserRegistrationEntity rejectRegistrationRequestByMo(Long fspUserRegId);

    FspUserRegistrationEntity withdrawPreConfirmedRegistrationRequestByFsp(Long fspUserRegId);

    Optional<FspUserRegistrationDTO> findOneBySecurityKey(String key);

    Optional<FspUserRegistrationFileEntity> getFspUserRegFileByFileId(Long fspUserRegFileId);

    void addFileToFspUserRegistration(FspUserRegistrationFileDTO fspFileDTO);

    List<FileDTO> getZipWithAllFilesOfFspUserRegistration(Long fspUserRegId);

    FspUserRegistrationCommentDTO addCommentToFspUserRegistration(FspUserRegistrationCommentDTO fspUserRegistrationCommentDTO, UserEntity currentUser);

    List<FspUserRegistrationCommentDTO> findAllCommentsOfFspUserRegistration(Long fspUserRegId);

    void markFspUserRegistrationAsReadByAdmin(Long fspUserRegId, UserEntity currentUser);

    Optional<FspUserRegistrationDTO> findOneByUserActivationKey(String key);

    Optional<FspUserRegistrationDTO> findOneByFspUserId(Long fspUserId);
}
