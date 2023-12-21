package pl.com.tt.flex.server.domain.user.config.screen;

import lombok.*;
import org.dom4j.tree.AbstractEntity;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.id.enhanced.SequenceStyleGenerator;
import pl.com.tt.flex.server.domain.EntityInterface;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "screen_columns")
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@GenericGenerator(
    name = "screen_columns_id_generator",
    strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
    parameters = {
        @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "screen_columns_seq"),
        @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.INITIAL_PARAM, value = "1"),
        @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.INCREMENT_PARAM, value = "1")
    }
)
public class ScreenColumnEntity extends AbstractEntity implements Serializable, EntityInterface<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "screen_columns_id_generator")
    private Long id;

    @ManyToOne(optional = false)
    private UserScreenConfigEntity userScreenConfig;

    @Column(nullable = false)
    private String columnName;

    @Column(nullable = false)
    private boolean visible;

    @Column(nullable = false)
    private boolean export;

    private Integer orderNr;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ScreenColumnEntity)) {
            return false;
        }
        return Objects.nonNull(id) && Objects.equals(id, ((ScreenColumnEntity) o).id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
