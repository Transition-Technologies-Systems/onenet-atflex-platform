package pl.com.tt.flex.server.service.subportfolio;

import pl.com.tt.flex.server.domain.screen.enumeration.Screen;
import pl.com.tt.flex.server.domain.subportfolio.SubportfolioEntity;
import pl.com.tt.flex.server.domain.subportfolio.SubportfolioFileEntity;
import pl.com.tt.flex.server.service.AbstractService;
import pl.com.tt.flex.model.service.dto.file.FileDTO;
import pl.com.tt.flex.server.service.subportfolio.dto.SubportfolioDTO;
import pl.com.tt.flex.server.service.subportfolio.dto.SubportfolioMinDTO;
import pl.com.tt.flex.server.service.unit.dto.UnitDTO;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Service Interface for managing {@link SubportfolioEntity}.
 */
public interface SubportfolioService extends AbstractService<SubportfolioEntity, SubportfolioDTO, Long> {
    SubportfolioDTO save(SubportfolioDTO subportfolioDTO, List<Long> filesToRemove);

    Optional<SubportfolioFileEntity> getSubportfolioFileByFileId(Long fileId);

    List<FileDTO> getZipWithAllFilesOfSubportfolio(Long subportfolioId);

    Optional<SubportfolioDTO> findByIdAndFspaId(Long id, Long fspId);

    Optional<SubportfolioFileEntity> getSubportfolioFileByFileIdAndFspaId(Long fileId, Long fspaId);

    FileDTO exportSubportfoliosToFile(List<SubportfolioDTO> subportfolioToExport, String langKey, boolean isOnlyVisibleColumn, Screen screen) throws IOException;

    List<Long> findByUnit(UnitDTO unitDTO);

    List<SubportfolioEntity> findByFspaId(Long fspaId);

    List<SubportfolioEntity> findAllCertifiedByFspaId(Long fspaId);

    boolean existsBySubportfolioIdAndFspaId(Long subportfolioId, Long fspaId);

    List<String> findAllSubportfolioNames();

    List<SubportfolioMinDTO> findAllFspaCertifiedSubportfoliosMin(Long fspaId);

    List<Long> findAllDerIdsFromDerSubportfolio(Long derId);

    //********************************************************************************** NOTIFICATION ************************************************************************************

    void registerNewNotificationForSubportfolioCreation(SubportfolioDTO subportfolioDTO);

    void sendMailInformingAboutCreation(SubportfolioDTO subportfolioDTO);

    void registerNewNotificationForSubportfolioEdition(SubportfolioDTO subportfolioDTO, SubportfolioDTO oldSubportfolio);

    void sendMailInformingAboutModification(SubportfolioDTO subportfolioDTO, SubportfolioDTO oldSubportfolio);

    //********************************************************************************** NOTIFICATION ************************************************************************************

}
