package pl.com.tt.flex.onenet.service.offeredservices.dto;

import java.io.Serializable;

import io.github.jhipster.service.Criteria;
import io.github.jhipster.service.filter.LongFilter;
import io.github.jhipster.service.filter.StringFilter;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
public class OfferedServicesCriteria implements Serializable, Criteria {

	private static final long serialVersionUID = 1L;

	private LongFilter id;
	private StringFilter title;
	private StringFilter onenetId;
	private StringFilter businessObject;
	private StringFilter serviceCode;
	private StringFilter description;

	public OfferedServicesCriteria(OfferedServicesCriteria other) {
		this.id = other.id == null ? null : other.id.copy();
		this.title = other.title == null ? null : other.title.copy();
		this.onenetId = other.onenetId == null ? null : other.onenetId.copy();
		this.businessObject = other.businessObject == null ? null : other.businessObject.copy();
		this.serviceCode = other.serviceCode == null ? null : other.serviceCode.copy();
		this.description = other.description == null ? null : other.description.copy();
	}

	@Override
	public Criteria copy() {
		return new OfferedServicesCriteria(this);
	}
}