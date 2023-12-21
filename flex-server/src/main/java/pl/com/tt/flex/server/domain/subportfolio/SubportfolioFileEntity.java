package pl.com.tt.flex.server.domain.subportfolio;

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
 * A SubportfolioFileEntity - attached file to SubportfolioEntity.
 *
 * @see SubportfolioEntity
 */
@Getter
@Setter
@Entity
@Table(name = "subportfolio_file")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@GenericGenerator(
    name = "subportfolio_file_id_generator",
    strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
    parameters = {
        @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "subportfolio_file_seq"),
        @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.INITIAL_PARAM, value = "1"),
        @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.INCREMENT_PARAM, value = "1")
    }
)
public class SubportfolioFileEntity extends AbstractFileEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "subportfolio_file_id_generator")
    private Long id;

    /**
     * Subportfolio may have multiple files attached
     */
    @NotNull
    @ManyToOne
    @JsonIgnoreProperties(value = "files", allowSetters = true)
    private SubportfolioEntity subportfolio;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SubportfolioFileEntity)) {
            return false;
        }
        return Objects.nonNull(id) && Objects.equals(id, ((SubportfolioFileEntity) o).id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
