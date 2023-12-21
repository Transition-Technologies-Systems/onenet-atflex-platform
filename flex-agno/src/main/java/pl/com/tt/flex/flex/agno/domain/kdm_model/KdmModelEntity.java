package pl.com.tt.flex.flex.agno.domain.kdm_model;

import lombok.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.id.enhanced.SequenceStyleGenerator;
import pl.com.tt.flex.flex.agno.domain.audit.AbstractAuditingEntity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * A KdmModelEntity.
 */
@Getter
@Setter
@Builder
@Entity
@Table(name = "kdm_model")
@AllArgsConstructor
@NoArgsConstructor
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@GenericGenerator(
        name = "kdm_model_id_generator",
        strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
        parameters = {
                @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "kdm_model_seq"),
                @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.INITIAL_PARAM, value = "1"),
                @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.INCREMENT_PARAM, value = "1")
        }
)
public class KdmModelEntity extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "kdm_model_id_generator")
    private Long id;

    @NotNull
    @Column(name = "area_name")
    private String areaName;

    @Column(name = "lv_model")
    private boolean lvModel = false;

    @OneToMany(mappedBy = "kdmModel", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<KdmModelTimestampFileEntity> timestampFiles = new HashSet<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof KdmModelEntity)) {
            return false;
        }
        return Objects.nonNull(id) && Objects.equals(id, ((KdmModelEntity) o).id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}

