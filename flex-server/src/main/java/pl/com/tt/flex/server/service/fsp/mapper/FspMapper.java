package pl.com.tt.flex.server.service.fsp.mapper;


import java.util.Optional;
import pl.com.tt.flex.server.domain.fsp.FspEntity;
import pl.com.tt.flex.server.service.fsp.dto.FspDTO;

import org.mapstruct.*;
import pl.com.tt.flex.server.service.mapper.EntityMapper;
import pl.com.tt.flex.server.service.unit.dto.UnitDTO;
import pl.com.tt.flex.server.service.user.mapper.UserMapper;

/**
 * Mapper for the entity {@link FspEntity} and its DTO {@link FspDTO}.
 */
@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface FspMapper extends EntityMapper<FspDTO, FspEntity> {

    @Mapping(source = "owner", target = "representative")
    @Mapping(source = "owner.firstName", target = "representative.firstName")
    @Mapping(source = "owner.lastName", target = "representative.lastName")
    @Mapping(source = "owner.email", target = "representative.email")
    @Mapping(source = "owner.phoneNumber", target = "representative.phoneNumber")
    @Mapping(source = "owner.companyName", target = "representative.companyName")
    FspDTO toDto(FspEntity fspEntity);

    @Mapping(source = "representative.id", target = "owner")
    FspEntity toEntity(FspDTO fspDTO);

    default FspEntity fromId(Long id) {
        if (id == null) {
            return null;
        }
        FspEntity fspEntity = new FspEntity();
        fspEntity.setId(id);
        return fspEntity;
    }

    default FspEntity fromUnitDto(UnitDTO unitDto) {
        return FspEntity.builder()
            .id(unitDto.getFspId())
            .companyName(Optional.ofNullable(unitDto.getFsp())
                .map(FspDTO::getCompanyName)
                .orElse(null))
            .build();
    }
}
