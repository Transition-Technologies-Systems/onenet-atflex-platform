package pl.com.tt.flex.server.service.fsp;

import pl.com.tt.flex.model.security.permission.Role;
import pl.com.tt.flex.model.service.dto.fsp.FspCompanyMinDTO;
import pl.com.tt.flex.server.domain.fsp.FspEntity;
import pl.com.tt.flex.server.domain.screen.enumeration.Screen;
import pl.com.tt.flex.server.domain.user.registration.FspUserRegistrationEntity;
import pl.com.tt.flex.server.service.AbstractService;
import pl.com.tt.flex.model.service.dto.file.FileDTO;
import pl.com.tt.flex.model.service.dto.MinimalDTO;
import pl.com.tt.flex.server.service.fsp.dto.FspDTO;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Service Interface for managing {@link FspEntity}.
 */
public interface FspService extends AbstractService<FspEntity, FspDTO, Long> {

    void createFspForNewlyRegisteredFspUser(FspUserRegistrationEntity fspUserRegistrationEntity);

    void deactivateFspsByValidFromToDates();

    void activateFspsByValidFromToDates();

    boolean isUserOwnerOfAnyFSP(String login);

    Optional<FspEntity> findFspOfUser(Long userId, String userLogin);

    Optional<FspDTO> findFspDtoOfUser(Long userId, String userLogin);

    List<FspCompanyMinDTO> getAllFspCompanyNamesByActiveAndRoles(List<Role> roles);

    List<FspCompanyMinDTO> getAllFspCompanyNamesByRoles(List<Role> roles);

    Set<FspCompanyMinDTO> getAllFspCompanyNamesByRolesWhereAttachedUnitIsActiveAndCertified(List<Role> roles);

    FileDTO exportFspsToFile(List<FspDTO> fsp, String langKey, boolean isOnlyDisplayedData, Screen screen) throws IOException;

    Optional<FspDTO> findByCompanyName(String companyName);

    List<FspCompanyMinDTO> getBspsWithNotEmptySchedulingUnitsMinimal();

    boolean existsByCompanyName(String companyName);

    List<FspCompanyMinDTO> findFspsWithRegisteredPotentialsForProduct(Long productId);

    Role findFspRole(Long fspId);

    List<MinimalDTO<Long, String>> findFspUsersMin(Long fspId);

    List<MinimalDTO<Long, String>> findFspUsersMin(Set<Long> fspIds);

    void registerUpdateNotification(FspDTO oldFsp, FspDTO modifyFsp);

    void sendMailInformingAboutModification(FspDTO oldFsp, FspDTO modifyFsp);
}
