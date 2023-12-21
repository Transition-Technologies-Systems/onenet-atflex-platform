package pl.com.tt.flex.server.service.kpi.mapper;

import org.mapstruct.Mapper;
import pl.com.tt.flex.model.service.dto.kpi.KpiDTO;
import pl.com.tt.flex.server.domain.kpi.KpiEntity;
import pl.com.tt.flex.server.service.mapper.EntityMapper;

/**
 * Mapper for the entity {@link KpiEntity} and its DTO {@link KpiDTO}.
 */
@Mapper(componentModel = "spring")
public interface KpiMapper extends EntityMapper<KpiDTO, KpiEntity> {
}
