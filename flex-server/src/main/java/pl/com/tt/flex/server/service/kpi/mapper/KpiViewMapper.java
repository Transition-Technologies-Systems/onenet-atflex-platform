package pl.com.tt.flex.server.service.kpi.mapper;

import org.mapstruct.Mapper;
import pl.com.tt.flex.model.service.dto.kpi.KpiDTO;
import pl.com.tt.flex.server.domain.kpi.KpiView;
import pl.com.tt.flex.server.service.mapper.EntityMapper;

@Mapper(componentModel = "spring")
public interface KpiViewMapper extends EntityMapper<KpiDTO, KpiView> {
}
