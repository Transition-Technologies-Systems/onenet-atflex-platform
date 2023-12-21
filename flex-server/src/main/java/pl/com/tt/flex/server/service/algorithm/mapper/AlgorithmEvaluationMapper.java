package pl.com.tt.flex.server.service.algorithm.mapper;

import org.mapstruct.Mapper;

import org.mapstruct.Mapping;
import pl.com.tt.flex.model.service.dto.algorithm.AlgorithmEvaluationDTO;
import pl.com.tt.flex.server.domain.algorithm.AlgorithmEvaluationEntity;
import pl.com.tt.flex.server.service.mapper.EntityMapper;

@Mapper(componentModel = "spring")
public interface AlgorithmEvaluationMapper extends EntityMapper<AlgorithmEvaluationDTO, AlgorithmEvaluationEntity> {

    @Mapping(source = "id", target = "evaluationId")
    @Mapping(source = "createdDate", target = "creationDate")
    @Mapping(source = "algorithmStatus", target = "status")
    AlgorithmEvaluationDTO toDto(AlgorithmEvaluationEntity algorithmEvaluationEntity);

}
