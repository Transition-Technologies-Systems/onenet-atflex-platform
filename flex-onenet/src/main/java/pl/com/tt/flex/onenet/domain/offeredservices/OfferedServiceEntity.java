package pl.com.tt.flex.onenet.domain.offeredservices;

import java.io.Serializable;
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
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.id.enhanced.SequenceStyleGenerator;

import lombok.Getter;
import lombok.Setter;
import pl.com.tt.flex.onenet.domain.audit.AbstractAuditingEntity;
import pl.com.tt.flex.onenet.domain.onenetuser.OnenetUserEntity;

@Getter
@Setter
@Entity
@Table(name = "offered_services")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@GenericGenerator(
		name = "offered_service_id_generator",
		strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
		parameters = {
				@org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "offered_service_seq"),
				@org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.INITIAL_PARAM, value = "10"),
				@org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.INCREMENT_PARAM, value = "1")
		}
)
public class OfferedServiceEntity extends AbstractAuditingEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "offered_service_id_generator")
	private Long id;

	@NotNull
	@Size(max = 50)
	@Column(name = "onenet_id", length = 50, unique = true, nullable = false)
	private String onenetId;

	@Size(min = 1, max = 50)
	@Column(length = 50)
	private String title;

	@Size(min = 1, max = 50)
	@Column(name = "business_object_id", length = 50)
	private String businessObjectId;

	@Size(max = 50)
	@Column(name = "business_object", length = 50)
	private String businessObject;

	@Size(max = 50)
	@Column(name = "service_code", length = 50)
	private String serviceCode;

	@Lob
	@Basic(fetch = FetchType.LAZY)
	@Column(name = "file_schema_zip")
	private byte[] fileSchemaZip;

	@Lob
	@Basic(fetch = FetchType.LAZY)
	@Column(name = "file_schema_sample_zip")
	private byte[] fileSchemaSampleZip;

	@Size(max = 50)
	@Column(length = 50)
	private String description;

	@ManyToMany(fetch = FetchType.LAZY)
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
	@JoinTable(name = "onenet_user_offered_services",
			joinColumns = @JoinColumn(name = "offered_service_id", referencedColumnName = "id"),
			inverseJoinColumns = @JoinColumn(name = "onenet_user_id", referencedColumnName = "id"))
	private Set<OnenetUserEntity> authorizedUsers = new HashSet<>();

}
