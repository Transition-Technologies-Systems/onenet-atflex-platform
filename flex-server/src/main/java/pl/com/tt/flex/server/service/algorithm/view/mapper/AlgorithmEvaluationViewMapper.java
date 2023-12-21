package pl.com.tt.flex.server.service.algorithm.view.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pl.com.tt.flex.model.service.dto.algorithm.AlgorithmEvaluationViewDTO;
import pl.com.tt.flex.server.domain.algorithm.AlgorithmEvaluationViewEntity;
import pl.com.tt.flex.server.service.mapper.EntityMapper;

@Mapper(componentModel = "spring")
public interface AlgorithmEvaluationViewMapper extends EntityMapper<AlgorithmEvaluationViewDTO, AlgorithmEvaluationViewEntity> {

    @Mapping(source = "id", target = "evaluationId")
    @Mapping(source = "createdDate", target = "creationDate")
    @Mapping(source = "algorithmStatus", target = "status")
    AlgorithmEvaluationViewDTO toDto(AlgorithmEvaluationViewEntity evaluationViewEntity);

}
