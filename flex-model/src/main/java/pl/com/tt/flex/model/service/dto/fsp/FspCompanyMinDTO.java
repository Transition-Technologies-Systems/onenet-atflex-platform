package pl.com.tt.flex.model.service.dto.fsp;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.com.tt.flex.model.security.permission.Role;

import java.io.Serializable;

@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
public class FspCompanyMinDTO implements Serializable {

	private Long id;
	private String companyName;
	private Role role;

	public FspCompanyMinDTO(Long id, String companyName, Role role) {
		this.id = id;
		this.companyName = companyName;
		this.role = role;
	}
}
