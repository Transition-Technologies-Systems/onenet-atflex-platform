package pl.com.tt.flex.flex.agno.service.kdm_model;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.com.tt.flex.flex.agno.domain.kdm_model.KdmModelEntity;
import pl.com.tt.flex.flex.agno.repository.AbstractJpaRepository;
import pl.com.tt.flex.flex.agno.repository.kdm_model.KdmModelRepository;
import pl.com.tt.flex.flex.agno.service.common.AbstractServiceImpl;
import pl.com.tt.flex.flex.agno.service.kdm_model.mapper.KdmModelMapper;
import pl.com.tt.flex.flex.agno.service.mapper.EntityMapper;
import pl.com.tt.flex.flex.agno.web.resource.error.BadRequestAlertException;
import pl.com.tt.flex.model.service.dto.MinimalDTO;
import pl.com.tt.flex.model.service.dto.kdm_model.KdmAreaDTO;
import pl.com.tt.flex.model.service.dto.kdm_model.KdmModelDTO;
import pl.com.tt.flex.model.service.dto.kdm_model.KdmModelMinimalDTO;
import pl.com.tt.flex.model.service.dto.kdm_model.KdmModelTimestampFileDTO;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Comparator.comparing;
import static pl.com.tt.flex.flex.agno.web.resource.kdm_model.KdmModelResource.ENTITY_NAME;

@Service
@Slf4j
@Transactional
public class KdmModelServiceImpl extends AbstractServiceImpl<KdmModelEntity, KdmModelDTO, Long> implements KdmModelService {

	private final KdmModelRepository kdmModelRepository;
	private final KdmModelMapper kdmModelMapper;
	private final KdmModelTimestampFileService kdmModelTimestampFileService;

	public KdmModelServiceImpl(KdmModelRepository kdmModelRepository, KdmModelMapper kdmModelMapper, KdmModelTimestampFileService kdmModelTimestampFileService) {
		this.kdmModelRepository = kdmModelRepository;
		this.kdmModelMapper = kdmModelMapper;
		this.kdmModelTimestampFileService = kdmModelTimestampFileService;
	}

	@Override
	@Transactional(readOnly = true)
	public List<KdmAreaDTO> getAllKdmModels() {
		return kdmModelRepository.findAll()
				.stream()
				.map(kdmModelMapper::toAreaDTO)
				.distinct()
				.sorted(comparing(kdm -> kdm.getAreaName().toLowerCase()))
				.collect(Collectors.toList());
	}

	@Override
	@Transactional(readOnly = true)
	public List<MinimalDTO<Long, String>> getAllKdmModelsMin() {
		return kdmModelRepository.findAll()
				.stream()
				.map(kdm -> new MinimalDTO<Long, String>(kdm.getId(), kdm.getAreaName()))
				.distinct()
				.sorted(comparing(kdm -> kdm.getValue().toLowerCase()))
				.collect(Collectors.toList());
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<KdmModelMinimalDTO> getKdmModelMinimal(Long kdmModelId) {
		Optional<KdmModelEntity> entityOpt = kdmModelRepository.findById(kdmModelId);
		if (entityOpt.isEmpty()) {
			return Optional.empty();
		}
		return Optional.ofNullable(kdmModelMapper.toMinimalDTO(entityOpt.get()));
	}

	@Override
	public AbstractJpaRepository<KdmModelEntity, Long> getRepository() {
		return this.kdmModelRepository;
	}

	@Override
	public EntityMapper<KdmModelDTO, KdmModelEntity> getMapper() {
		return this.kdmModelMapper;
	}

	@Override
	public KdmModelDTO updateNameAndLvFlag(KdmModelDTO modelDTO) {
		loadTimestampFilesToKdmModel(modelDTO);
		return save(modelDTO);
	}

	private void loadTimestampFilesToKdmModel(KdmModelDTO kdmModelDTO) {
		KdmModelDTO oldDto = findById(kdmModelDTO.getId())
				.orElseThrow(() -> new BadRequestAlertException("Invalid id", ENTITY_NAME, kdmModelDTO.getId().toString()));
		List<KdmModelTimestampFileDTO> timestamps = oldDto.getTimestampFiles();
		timestamps.forEach(t -> t.setFileDTO(kdmModelTimestampFileService.findKdmTimestampFileByTimestampAndKdmModelId(t.getTimestamp(), kdmModelDTO.getId())));
		kdmModelDTO.setTimestampFiles(timestamps);
	}
}
