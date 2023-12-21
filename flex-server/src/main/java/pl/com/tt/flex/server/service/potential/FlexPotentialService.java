package pl.com.tt.flex.server.service.potential;

import org.springframework.transaction.annotation.Transactional;
import pl.com.tt.flex.model.service.dto.potential.FlexPotentialMinDTO;
import pl.com.tt.flex.server.domain.potential.FlexPotentialEntity;
import pl.com.tt.flex.server.domain.potential.FlexPotentialFileEntity;
import pl.com.tt.flex.server.domain.screen.enumeration.Screen;
import pl.com.tt.flex.server.service.AbstractService;
import pl.com.tt.flex.model.service.dto.file.FileDTO;
import pl.com.tt.flex.model.service.dto.MinimalDTO;
import pl.com.tt.flex.server.service.potential.dto.FlexPotentialDTO;
import pl.com.tt.flex.server.service.unit.dto.UnitDTO;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Service Interface for managing {@link FlexPotentialEntity}.
 */
public interface FlexPotentialService extends AbstractService<FlexPotentialEntity, FlexPotentialDTO, Long> {

    FlexPotentialDTO save(FlexPotentialDTO flexPotentialDTO, List<Long> filesToRemove);

    @Transactional(readOnly = true)
    Optional<FlexPotentialFileEntity> getFlexPotentialFileByFileId(Long fileId);

    @Transactional(readOnly = true)
    List<FileDTO> getZipWithAllFilesOfFlexPotential(Long flexPotentialId);

    FileDTO exportFlexPotentialToFile(List<FlexPotentialDTO> flexPotentialToExport, String langKey, boolean isOnlyDisplayedData, Screen screen) throws IOException;

    @Transactional(readOnly = true)
    boolean isUserHasPermissionToFlexPotential(Long id, String fspCompanyName);

    @Transactional(readOnly = true)
    MinimalDTO<Long, String> findFlexPotentialMinWithFspCompanyName(Long id);

    void deactivateFlexPotentialsByValidFromToDates();

    void activateFlexPotentialsByValidFromToDates();

    @Transactional(readOnly = true)
    List<String> getAllDerNameJoinedToFP();

    @Transactional(readOnly = true)
    List<String> getAllDerNameJoinedToFPByFspId(Long fspId);

    @Transactional(readOnly = true)
    List<String> getAllDerNameJoinedToFlexRegister();

    @Transactional(readOnly = true)
    List<String> getAllDerNameJoinedToFlexRegisterByFspId(Long fspId);

    @Transactional(readOnly = true)
    List<Long> findActiveByUnit(UnitDTO unitDTO);

    @Transactional(readOnly = true)
    List<Long> findByUnit(UnitDTO unitDTO);

    List<FlexPotentialMinDTO> findAllRegisteredFlexPotentialsForFspAndProduct(Long fspId, Long productId);

    boolean isDerOfFspBalancedByRegisteredFlexPotentialProduct(Long derId);

    boolean existsByFlexPotentialIdAndFspId(Long id, Long id1);

    boolean isAtLeastOneDerOfFspBalancedByRegisteredFlexPotentialProduct(Long fspId);

    //********************************************************************************** NOTIFICATION ************************************************************************************
    void registerCreatedNotification(FlexPotentialDTO flexPotentialDTO);

    void sendMailInformingAboutCreation(FlexPotentialDTO flexPotentialDTO);

    void registerUpdatedNotification(FlexPotentialDTO oldFlexPotentialDTO, FlexPotentialDTO modifyFlexPotentialDTO);

    void sendMailInformingAboutModification(FlexPotentialDTO oldFlexPotentialDTO, FlexPotentialDTO modifyFlexPotentialDTO);

    void sendNotificationInformingAboutRegistered(FlexPotentialDTO modifyFlexPotentialDTO);

    void sendMailInformingAboutRegistered(FlexPotentialDTO modifyFlexPotentialDTO);

    void sendNotificationAboutDeleted(FlexPotentialDTO modifyFlexPotentialDTO);
    //********************************************************************************** NOTIFICATION ************************************************************************************

}
