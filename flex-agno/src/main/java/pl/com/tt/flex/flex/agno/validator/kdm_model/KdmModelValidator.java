package pl.com.tt.flex.flex.agno.validator.kdm_model;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pl.com.tt.flex.flex.agno.common.errors.ObjectValidationException;
import pl.com.tt.flex.flex.agno.domain.kdm_model.KdmModelEntity;
import pl.com.tt.flex.flex.agno.repository.kdm_model.KdmModelRepository;
import pl.com.tt.flex.flex.agno.validator.ObjectValidator;
import pl.com.tt.flex.model.service.dto.kdm_model.KdmModelDTO;

import java.util.Objects;
import java.util.Optional;

import static pl.com.tt.flex.flex.agno.web.resource.error.ErrorConstants.*;
import static pl.com.tt.flex.flex.agno.web.resource.kdm_model.KdmModelResource.ENTITY_NAME;

@Component
@RequiredArgsConstructor
@Slf4j
public class KdmModelValidator implements ObjectValidator<KdmModelDTO, Long> {

	private final KdmModelRepository kdmModelRepository;

	@Override
	public void checkValid(KdmModelDTO kdmModelDTO) throws ObjectValidationException {
		checkCreated(kdmModelDTO);
	}

	private void checkCreated(KdmModelDTO kdmModelDTO) throws ObjectValidationException {
		if (Objects.isNull(kdmModelDTO.getId())) {
			if (kdmModelRepository.existsByAreaName(kdmModelDTO.getAreaName())) {
				throw new ObjectValidationException("Cannot create because kdm model is exist with this area name",
						CANNOT_CREATE_BECAUSE_KDM_MODEL_WITH_THIS_AREA_NAME_ALREADY_EXIST, ENTITY_NAME,
						kdmModelDTO.getId());
			}
		}
	}

	@Override
	public void checkModifiable(KdmModelDTO kdmModelDTO) throws ObjectValidationException {
		checkValid(kdmModelDTO);
		Optional<KdmModelEntity> entityOptional = kdmModelRepository.findById(kdmModelDTO.getId());
		if (entityOptional.isPresent()
				&& areaNameHasChange(kdmModelDTO, entityOptional.get())
				&& kdmModelRepository.existsByAreaName(kdmModelDTO.getAreaName())) {
			throw new ObjectValidationException("Cannot update because kdm model is exist with this area name",
					CANNOT_UPDATE_BECAUSE_KDM_MODEL_WITH_THIS_AREA_NAME_ALREADY_EXIST, ENTITY_NAME,
					kdmModelDTO.getId());
		}
	}

	@Override
	public void checkDeletable(Long id) throws ObjectValidationException {
		if (kdmModelRepository.isUsedInAlgorithmEvaluation(id)) {
			throw new ObjectValidationException("Cannot remove KDM MODEL, because of attachment to algorithm calculations",
					CANNOT_DELETE_BECAUSE_KDM_MODEL_IS_USED_IN_ALGORITHM_EVALUATION, ENTITY_NAME, id);
		}
	}


	private boolean areaNameHasChange(KdmModelDTO kdmModelDTO, KdmModelEntity dbEntity) {
		return !dbEntity.getAreaName().equals(kdmModelDTO.getAreaName());
	}
}