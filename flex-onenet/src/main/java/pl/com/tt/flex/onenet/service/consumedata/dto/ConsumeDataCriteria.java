package pl.com.tt.flex.onenet.service.consumedata.dto;

import io.github.jhipster.service.Criteria;
import io.github.jhipster.service.filter.LongFilter;
import io.github.jhipster.service.filter.StringFilter;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
public class ConsumeDataCriteria implements Serializable, Criteria {
	private LongFilter id;
	private StringFilter title;
	private StringFilter onenetId;
	private StringFilter businessObject;
	private StringFilter dataSupplier;

	public ConsumeDataCriteria(ConsumeDataCriteria other) {
		this.id = other.id == null ? null : other.id.copy();
		this.title = other.title == null ? null : other.title.copy();
		this.onenetId = other.onenetId == null ? null : other.onenetId.copy();
		this.businessObject = other.businessObject == null ? null : other.businessObject.copy();
		this.dataSupplier = other.dataSupplier == null ? null : other.dataSupplier.copy();
	}

	@Override
	public Criteria copy() {
		return new ConsumeDataCriteria(this);
	}
}
