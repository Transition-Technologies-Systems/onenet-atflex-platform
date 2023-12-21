package pl.com.tt.flex.model.service.dto.kpi;

import javax.validation.constraints.NotNull;
import java.time.Instant;

public class KpiDTOBuilder {
	private String createdBy;
	private Instant createdDate;
	private String lastModifiedBy;
	private Instant lastModifiedDate;
	private Long id;
	private @NotNull KpiType type;
	private Instant dateFrom;
	private Instant dateTo;

	public KpiDTOBuilder() {
	}

	public KpiDTOBuilder createdBy(String createdBy) {
		this.createdBy = createdBy;
		return this;
	}

	public KpiDTOBuilder createdDate(Instant createdDate) {
		this.createdDate = createdDate;
		return this;
	}

	public KpiDTOBuilder lastModifiedBy(String lastModifiedBy) {
		this.lastModifiedBy = lastModifiedBy;
		return this;
	}

	public KpiDTOBuilder lastModifiedDate(Instant lastModifiedDate) {
		this.lastModifiedDate = lastModifiedDate;
		return this;
	}

	public KpiDTOBuilder id(Long id) {
		this.id = id;
		return this;
	}

	public KpiDTOBuilder type(KpiType type) {
		this.type = type;
		return this;
	}

	public KpiDTOBuilder dateFrom(Instant dateFrom) {
		this.dateFrom = dateFrom;
		return this;
	}

	public KpiDTOBuilder dateTo(Instant dateTo) {
		this.dateTo = dateTo;
		return this;
	}

	public KpiDTO build() {
		KpiDTO kpiDTO = new KpiDTO();
		kpiDTO.setCreatedBy(createdBy);
		kpiDTO.setCreatedDate(createdDate);
		kpiDTO.setLastModifiedBy(lastModifiedBy);
		kpiDTO.setLastModifiedDate(lastModifiedDate);
		kpiDTO.setId(id);
		kpiDTO.setType(type);
		kpiDTO.setDateFrom(dateFrom);
		kpiDTO.setDateTo(dateTo);
		return kpiDTO;
	}
}
