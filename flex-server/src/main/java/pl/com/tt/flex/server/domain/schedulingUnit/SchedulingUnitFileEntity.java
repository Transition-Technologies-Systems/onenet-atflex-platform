package pl.com.tt.flex.server.domain.schedulingUnit;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.id.enhanced.SequenceStyleGenerator;
import pl.com.tt.flex.server.domain.AbstractFileEntity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Objects;

/**
 * A SchedulingUnitFileEntity - attached file to SchedulingUnitEntity.
 * @see SchedulingUnitEntity
 */
@Getter
@Setter
@Entity
@Table(name = "scheduling_unit_file")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@GenericGenerator(
    name = "scheduling_unit_file_id_generator",
    strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
    parameters = {
        @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "scheduling_unit_file_seq"),
        @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.INITIAL_PARAM, value = "1"),
        @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.INCREMENT_PARAM, value = "1")
    }
)
public class SchedulingUnitFileEntity extends AbstractFileEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "scheduling_unit_file_id_generator")
    private Long id;

    /**
     * SchedulingUnit may have multiple files attached
     */
    @NotNull
    @ManyToOne
    @JsonIgnoreProperties(value = "files", allowSetters = true)
    private SchedulingUnitEntity schedulingUnit;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SchedulingUnitFileEntity)) {
            return false;
        }
        return Objects.nonNull(id) && Objects.equals(id, ((SchedulingUnitFileEntity) o).id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
