package pl.com.tt.flex.server.domain.unit;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.id.enhanced.SequenceStyleGenerator;
import pl.com.tt.flex.model.service.dto.dictionary.DictionaryType;
import pl.com.tt.flex.server.domain.AbstractDictionaryEntity;
import pl.com.tt.flex.server.domain.unit.enumeration.DerType;
import pl.com.tt.flex.server.service.dictionary.DictionaryListener;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * A DerTypeEntity.
 */
@Getter
@Setter
@SuperBuilder
@Entity
@Table(name = "der_type")
@AllArgsConstructor
@NoArgsConstructor
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@EntityListeners(DictionaryListener.class)
@GenericGenerator(
    name = "der_type_id_generator",
    strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
    parameters = {
        @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "der_type_seq"),
        @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.INITIAL_PARAM, value = "100"),
        @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.INCREMENT_PARAM, value = "1")
    }
)
public class DerTypeEntity extends AbstractDictionaryEntity {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "der_type_id_generator")
    private Long id;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private DerType type;

    @NotNull
    @Column(name = "sder_point", precision = 11, scale = 2)
    private BigDecimal sderPoint;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DerTypeEntity)) {
            return false;
        }
        return Objects.nonNull(getId()) && Objects.equals(getId(), ((DerTypeEntity) o).getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    public DictionaryType getDictionaryType() {
        return DictionaryType.DER_TYPE;
    }
}
