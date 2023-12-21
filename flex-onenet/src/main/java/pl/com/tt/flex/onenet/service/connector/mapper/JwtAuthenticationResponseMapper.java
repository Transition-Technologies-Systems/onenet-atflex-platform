package pl.com.tt.flex.onenet.service.connector.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import pl.com.tt.flex.onenet.model.JwtAuthenticationResponseDTO;
import pl.com.tt.flex.onenet.service.onenetuser.dto.OnenetAuthResponseDTO;

@Mapper(componentModel = "spring")
public interface JwtAuthenticationResponseMapper extends ApiModelMapper<OnenetAuthResponseDTO, JwtAuthenticationResponseDTO> {

	@Mapping(source = "user.email", target = "email")
	@Mapping(source = "user.id", target = "onenetId")
	OnenetAuthResponseDTO toDto(JwtAuthenticationResponseDTO apiModel);

}
