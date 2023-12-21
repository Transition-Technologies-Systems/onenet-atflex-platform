package pl.com.tt.flex.onenet.service.onenetuser.mapper;

import org.mapstruct.Mapper;

import pl.com.tt.flex.onenet.domain.onenetuser.OnenetUserEntity;
import pl.com.tt.flex.onenet.service.mapper.EntityMapper;
import pl.com.tt.flex.onenet.service.onenetuser.dto.OnenetUserDTO;

@Mapper(componentModel = "spring")
public interface OnenetUserMapper extends EntityMapper<OnenetUserDTO, OnenetUserEntity> {

	OnenetUserDTO toDto(OnenetUserEntity onenetUserEntity);

}
