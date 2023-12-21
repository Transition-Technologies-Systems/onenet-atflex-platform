package pl.com.tt.flex.server.service.unit.mapper;


import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pl.com.tt.flex.server.domain.unit.UnitGeoLocationEntity;
import pl.com.tt.flex.server.service.mapper.EntityMapper;
import pl.com.tt.flex.server.service.unit.dto.UnitGeoLocationDTO;

/**
 * Mapper for the entity {@link UnitGeoLocationEntity} and its DTO {@link UnitGeoLocationDTO}.
 */
@Mapper(componentModel = "spring", uses = {UnitMapper.class})
public interface UnitGeoLocationMapper extends EntityMapper<UnitGeoLocationDTO, UnitGeoLocationEntity> {

    @Mapping(source = "unit.id", target = "unitId")
    UnitGeoLocationDTO toDto(UnitGeoLocationEntity unitGeoLocationEntity);

    @Mapping(source = "unitId", target = "unit")
    UnitGeoLocationEntity toEntity(UnitGeoLocationDTO unitDTO);

//    default UnitGeoLocationEntity fromId(Long id) {
//        if (id == null) {
//            return null;
//        }
//        UnitGeoLocationEntity unitGeoLocationEntity = new UnitGeoLocationEntity();
//        unitGeoLocationEntity.setId(id);
//        return unitGeoLocationEntity;
//    }
}
