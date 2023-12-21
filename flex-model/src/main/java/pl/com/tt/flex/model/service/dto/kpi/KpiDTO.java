package pl.com.tt.flex.model.service.dto.kpi;

import lombok.*;
import pl.com.tt.flex.model.service.dto.audit.AbstractAuditingDTO;

import javax.validation.constraints.NotNull;
import java.time.Instant;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class KpiDTO extends AbstractAuditingDTO {

	private Long id;

	@NotNull
	private KpiType type;

	private Instant dateFrom;

	private Instant dateTo;

	public static KpiDTOBuilder builder() {
		return new KpiDTOBuilder();
	}

	@Override
	public String toString() {
		return "KpiDTO{" +
				"id=" + id +
				", type=" + type +
				", dateFrom=" + dateFrom +
				", dateTo=" + dateTo +
				'}';
	}


}
