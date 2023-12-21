package pl.com.tt.flex.onenet.domain.onenetuser;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.id.enhanced.SequenceStyleGenerator;

import lombok.Getter;
import lombok.Setter;
import pl.com.tt.flex.onenet.domain.EntityInterface;

@Getter
@Setter
@Entity
@Table(name = "active_onenet_user")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@GenericGenerator(
		name = "active_onenet_user_id_generator",
		strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
		parameters = {
				@org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "active_onenet_user_seq"),
				@org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.INITIAL_PARAM, value = "10"),
				@org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.INCREMENT_PARAM, value = "1")
		}
)
public class ActiveOnenetUserEntiy implements EntityInterface<Long> {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "active_onenet_user_id_generator")
	private Long id;

	@NotNull
	@Size(min = 1, max = 50)
	@Column(name = "flex_platform_username", length = 50, unique = true, nullable = false)
	private String flexUsername;

	@NotNull
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "onenet_user_id", referencedColumnName = "id", nullable = false)
	private OnenetUserEntity activeOnenetUser;

}
