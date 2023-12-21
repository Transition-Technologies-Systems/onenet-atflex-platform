package pl.com.tt.flex.onenet.domain.consumedata;

import java.util.HashSet;
import java.util.Set;

import lombok.Data;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Immutable;
import pl.com.tt.flex.onenet.domain.audit.AbstractAuditingEntity;
import pl.com.tt.flex.onenet.domain.onenetuser.OnenetUserEntity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Widok w którym są przechowane wszystkie dane z tabeli consume_data oraz nazwa obiektu biznesowego z
 * tabeli offered_services.
 */
@Data
@Entity
@Immutable
@Table(name = "consume_data_view")
@Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
public class ConsumeDataViewEntity extends AbstractAuditingEntity {
	@Id
	@Column(name = "id")
	private Long id;

	@NotNull
	@Column(name = "title", nullable = false)
	private String title;

	@Column(name = "onenet_id")
	private String onenetId;

	@Column(name = "business_object")
	private String businessObject;

	@Column(name = "data_supplier")
	private String dataSupplier;

	@Column(name = "data_supplier_full")
	private String dataSupplierFull;

	@Size(max = 50)
	@Column(name = "description")
	private String description;

	@Column(name = "file_available")
	private boolean fileAvailable;

	@ManyToMany(fetch = FetchType.LAZY)
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
	@JoinTable(name = "onenet_user_consume_data",
			joinColumns = @JoinColumn(name = "consume_data_id", referencedColumnName = "id"),
			inverseJoinColumns = @JoinColumn(name = "onenet_user_id", referencedColumnName = "id"))
	private Set<OnenetUserEntity> authorizedUsers = new HashSet<>();
}
