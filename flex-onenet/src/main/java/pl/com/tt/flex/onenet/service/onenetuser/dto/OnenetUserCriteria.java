package pl.com.tt.flex.onenet.service.onenetuser.dto;

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
public class OnenetUserCriteria implements Serializable, Criteria {

	private static final long serialVersionUID = 1L;

	private LongFilter id;

	private StringFilter username;

	private StringFilter onenetId;

	private StringFilter email;

	public OnenetUserCriteria(OnenetUserCriteria other) {
		this.id = other.id == null ? null : other.id.copy();
		this.username = other.username == null ? null : other.username.copy();
		this.onenetId = other.onenetId == null ? null : other.onenetId.copy();
		this.email = other.email == null ? null : other.email.copy();
	}

	@Override
	public Criteria copy() {
		return new OnenetUserCriteria(this);
	}
}
