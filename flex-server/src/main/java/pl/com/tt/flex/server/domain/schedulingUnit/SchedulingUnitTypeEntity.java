package pl.com.tt.flex.server.domain.schedulingUnit;

import lombok.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.id.enhanced.SequenceStyleGenerator;
import pl.com.tt.flex.model.service.dto.dictionary.DictionaryType;
import pl.com.tt.flex.server.domain.AbstractDictionaryEntity;
import pl.com.tt.flex.server.domain.product.ProductEntity;
import pl.com.tt.flex.server.service.dictionary.DictionaryListener;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * A SchedulingUnitTypeEntity.
 */
@Getter
@Setter
@Builder
@Entity
@Table(name = "scheduling_unit_type")
@AllArgsConstructor
@NoArgsConstructor
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@EntityListeners(DictionaryListener.class)
@GenericGenerator(
    name = "scheduling_unit_type_id_generator",
    strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
    parameters = {
        @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "scheduling_unit_type_seq"),
        @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.INITIAL_PARAM, value = "100"),
        @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.INCREMENT_PARAM, value = "1")
    }
)
public class SchedulingUnitTypeEntity extends AbstractDictionaryEntity {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "scheduling_unit_type_id_generator")
    private Long id;

    @NotEmpty
    @ManyToMany
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @JoinTable(name = "scheduling_unit_type_products",
        joinColumns = @JoinColumn(name = "scheduling_unit_type_id", referencedColumnName = "id"),
        inverseJoinColumns = @JoinColumn(name = "product_id", referencedColumnName = "id"))
    private Set<ProductEntity> products = new HashSet<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SchedulingUnitTypeEntity)) {
            return false;
        }
        return Objects.nonNull(getId()) && Objects.equals(getId(), ((SchedulingUnitTypeEntity) o).getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    public DictionaryType getDictionaryType() {
        return DictionaryType.SCHEDULING_UNIT_TYPE;
    }
}
