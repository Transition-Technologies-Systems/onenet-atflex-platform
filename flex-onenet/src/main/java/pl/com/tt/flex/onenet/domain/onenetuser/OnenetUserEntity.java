package pl.com.tt.flex.onenet.domain.onenetuser;

import java.io.Serializable;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.id.enhanced.SequenceStyleGenerator;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.Setter;
import pl.com.tt.flex.onenet.domain.audit.AbstractAuditingEntity;
import pl.com.tt.flex.onenet.domain.consumedata.ConsumeDataEntity;
import pl.com.tt.flex.onenet.domain.offeredservices.OfferedServiceEntity;

@Getter
@Setter
@Entity
@Table(name = "onenet_user")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@GenericGenerator(
		name = "onenet_user_id_generator",
		strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
		parameters = {
				@org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "onenet_user_seq"),
				@org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.INITIAL_PARAM, value = "10"),
				@org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.INCREMENT_PARAM, value = "1")
		}
)
public class OnenetUserEntity extends AbstractAuditingEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "onenet_user_id_generator")
	private Long id;

	@NotNull
	@Size(max = 50)
	@Column(name = "onenet_id", length = 50, nullable = false)
	private String onenetId;

	@NotNull
	@Size(min = 1, max = 50)
	@Column(length = 50, unique = true, nullable = false)
	private String username;

	@JsonIgnore
	@NotNull
	@Size(max = 60)
	@Column(name = "password_hash", length = 60, nullable = false)
	private String passwordHash;

	@NotNull
	@Size(max = 255)
	@Column(nullable = false)
	private String email;

	@Basic(fetch = FetchType.LAZY)
	@Column(name = "token_hash", nullable = false, columnDefinition = "clob")
	private String tokenHash;

	@NotNull
	@Column(name = "token_expiration_date", nullable = false)
	private Instant tokenValidTo;

	@ManyToMany(fetch = FetchType.LAZY)
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
	@JoinTable(name = "onenet_user_offered_services",
			joinColumns = @JoinColumn(name = "onenet_user_id", referencedColumnName = "id"),
			inverseJoinColumns = @JoinColumn(name = "offered_service_id", referencedColumnName = "id"))
	private Set<OfferedServiceEntity> offeredServices = new HashSet<>();

	@ManyToMany(fetch = FetchType.LAZY)
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
	@JoinTable(name = "onenet_user_consume_data",
			joinColumns = @JoinColumn(name = "onenet_user_id", referencedColumnName = "id"),
			inverseJoinColumns = @JoinColumn(name = "consume_data_id", referencedColumnName = "id"))
	private Set<ConsumeDataEntity> consumeData = new HashSet<>();

}
