package pl.com.tt.flex.flex.agno.domain.kdm_model;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.id.enhanced.SequenceStyleGenerator;
import pl.com.tt.flex.flex.agno.domain.AbstractFileEntity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Objects;

/**
 * A KdmModelEntity.
 */
@Getter
@Setter
@Builder
@Entity
@Table(name = "kdm_model_timestamp_file")
@AllArgsConstructor
@NoArgsConstructor
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@GenericGenerator(
        name = "kdm_model_timestamp_file_id_generator",
        strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
        parameters = {
                @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "kdm_model_timestamp_file_seq"),
                @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.INITIAL_PARAM, value = "1"),
                @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.INCREMENT_PARAM, value = "1")
        }
)
public class KdmModelTimestampFileEntity extends AbstractFileEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "kdm_model_timestamp_file_id_generator")
    private Long id;

    @NotNull
    @ManyToOne
    @JsonIgnoreProperties(value = "timestampFiles", allowSetters = true)
    private KdmModelEntity kdmModel;

    @NotNull
    @Column(name = "timestamp")
    private String timestamp;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof KdmModelTimestampFileEntity)) {
            return false;
        }
        return Objects.nonNull(id) && Objects.equals(id, ((KdmModelTimestampFileEntity) o).id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
