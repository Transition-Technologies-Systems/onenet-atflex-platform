package pl.com.tt.flex.onenet.service.onenetuser.dto;

import java.io.Serializable;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class OnenetUserDTO implements Serializable {
	private Long id;
	private String username;
	private String onenetId;
	private String email;
	private boolean active;
}
