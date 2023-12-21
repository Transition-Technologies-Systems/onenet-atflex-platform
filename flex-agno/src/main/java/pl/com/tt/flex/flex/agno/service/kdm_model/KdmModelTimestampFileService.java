package pl.com.tt.flex.flex.agno.service.kdm_model;

import org.springframework.transaction.annotation.Transactional;
import pl.com.tt.flex.flex.agno.common.errors.ObjectValidationException;
import pl.com.tt.flex.flex.agno.domain.kdm_model.KdmModelTimestampFileEntity;
import pl.com.tt.flex.flex.agno.service.AbstractService;
import pl.com.tt.flex.model.service.dto.file.FileDTO;
import pl.com.tt.flex.model.service.dto.kdm_model.KdmModelTimestampFileDTO;

import java.util.List;

public interface KdmModelTimestampFileService  extends AbstractService<KdmModelTimestampFileEntity, KdmModelTimestampFileDTO, Long> {

	List<KdmModelTimestampFileDTO> findAllByKdmModelId(Long kdmModelId);

	FileDTO findKdmTimestampFileByTimestampAndKdmModelId(String timestamp, Long kdmModelId);

	void updateAllTimestampsForKdmModel(String kdmModelId, List<KdmModelTimestampFileDTO> timestampFileDTOS)
			throws ObjectValidationException;
}
