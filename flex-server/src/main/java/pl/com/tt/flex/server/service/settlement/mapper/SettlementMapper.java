package pl.com.tt.flex.server.service.settlement.mapper;

import org.mapstruct.Mapper;

import pl.com.tt.flex.server.domain.settlement.SettlementEntity;
import pl.com.tt.flex.server.service.mapper.EntityMapper;
import pl.com.tt.flex.server.service.settlement.dto.SettlementDTO;

@Mapper(componentModel = "spring")
public interface SettlementMapper extends EntityMapper<SettlementDTO, SettlementEntity> {
}
