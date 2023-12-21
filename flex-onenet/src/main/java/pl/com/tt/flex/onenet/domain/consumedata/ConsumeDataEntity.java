package pl.com.tt.flex.onenet.domain.consumedata;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.id.enhanced.SequenceStyleGenerator;
import pl.com.tt.flex.onenet.domain.audit.AbstractAuditingEntity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * Encja, w której przechowywane są dane udostępnione przez innych użytkowników Onenet System.
 */
@Getter
@Setter
@Entity
@Table(name = "consume_data")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@GenericGenerator(
		name = "onenet_consume_data_id_generator",
		strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
		parameters = {
				@org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "consume_data_seq"),
				@org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.INITIAL_PARAM, value = "1"),
				@org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.INCREMENT_PARAM, value = "1")
		}
)
public class ConsumeDataEntity extends AbstractAuditingEntity implements Serializable {
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "onenet_consume_data_id_generator")
	private Long id;

	@Column(name = "title")
	private String title;

	@NotNull
	@Size(max = 50)
	@Column(name = "onenet_id", length = 50, unique = true, nullable = false)
	private String onenetId;

	@Size(max = 50)
	@Column(name = "business_object_id", length = 50)
	private String businessObjectId;

	@Size(max = 50)
	@Column(name = "data_supplier", length = 50)
	private String dataSupplier;

	@Size(max = 50)
	@Column(name = "data_supplier_company_name")
	private String dataSupplierCompanyName;

	@Size(max = 50)
	@Column(name = "description")
	private String description;

	@Lob
	@Basic(fetch = FetchType.LAZY)
	@Column(name = "file_zip")
	private byte[] fileZip;
}
