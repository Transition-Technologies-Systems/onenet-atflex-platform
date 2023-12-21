package pl.com.tt.flex.onenet.service.connector.mapper;

import org.mapstruct.Mapper;

import pl.com.tt.flex.onenet.model.LoginDTO;
import pl.com.tt.flex.onenet.service.onenetuser.dto.OnenetAuthDTO;

@Mapper(componentModel = "spring")
public interface LoginDTOMapper extends ApiModelMapper<OnenetAuthDTO, LoginDTO> {

	default LoginDTO toLoginDTO(String username, String password) {
		LoginDTO onenetLoginDTO = new LoginDTO();
		onenetLoginDTO.setUsername(username);
		onenetLoginDTO.setPassword(password);
		return onenetLoginDTO;
	}

}
