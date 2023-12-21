package pl.com.tt.flex.model.service.dto.localization;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.com.tt.flex.model.service.dto.audit.AbstractAuditingDTO;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class LocalizationTypeDTO extends AbstractAuditingDTO implements Serializable {

	private Long id;

	@NotNull
	@Size(max = 100)
	private String name;

	private LocalizationType type;
}
