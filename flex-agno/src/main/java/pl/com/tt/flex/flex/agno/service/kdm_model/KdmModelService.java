package pl.com.tt.flex.flex.agno.service.kdm_model;

import pl.com.tt.flex.flex.agno.common.errors.ObjectValidationException;
import pl.com.tt.flex.flex.agno.domain.kdm_model.KdmModelEntity;
import pl.com.tt.flex.flex.agno.service.AbstractService;
import pl.com.tt.flex.model.service.dto.MinimalDTO;
import pl.com.tt.flex.model.service.dto.file.FileDTO;
import pl.com.tt.flex.model.service.dto.kdm_model.KdmModelDTO;
import pl.com.tt.flex.model.service.dto.kdm_model.KdmAreaDTO;
import pl.com.tt.flex.model.service.dto.kdm_model.KdmModelMinimalDTO;
import pl.com.tt.flex.model.service.dto.kdm_model.KdmModelTimestampFileDTO;

import java.util.List;
import java.util.Optional;

public interface KdmModelService extends AbstractService<KdmModelEntity, KdmModelDTO, Long> {

    List<KdmAreaDTO> getAllKdmModels();

    List<MinimalDTO<Long,String>> getAllKdmModelsMin();

    Optional<KdmModelMinimalDTO> getKdmModelMinimal(Long kdmModelId);

    KdmModelDTO updateNameAndLvFlag(KdmModelDTO modelDTO);

}
