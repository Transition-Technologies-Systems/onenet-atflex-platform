package pl.com.tt.flex.onenet.service.offeredservices.mapper;

import org.mapstruct.Mapper;

import pl.com.tt.flex.onenet.domain.offeredservices.OfferedServiceEntity;
import pl.com.tt.flex.onenet.service.mapper.EntityMapper;
import pl.com.tt.flex.onenet.service.offeredservices.dto.OfferedServiceDTO;
import pl.com.tt.flex.onenet.service.offeredservices.dto.OfferedServiceFullDTO;
import pl.com.tt.flex.onenet.service.offeredservices.dto.OfferedServiceMinDTO;

@Mapper(componentModel = "spring")
public interface OfferedServicesMapper extends EntityMapper<OfferedServiceDTO, OfferedServiceEntity> {

	OfferedServiceDTO toDto(OfferedServiceEntity offeredService);

	OfferedServiceEntity toEntity(OfferedServiceFullDTO offeredService);

	OfferedServiceMinDTO toMinDto(OfferedServiceEntity offeredService);

}
