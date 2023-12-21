package pl.com.tt.flex.onenet.service.onenetuser.dto;

import java.io.Serializable;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class OnenetAuthDTO implements Serializable {

	private String username;
	private String password;

}
