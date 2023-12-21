package pl.com.tt.flex.server.domain.user.config.screen;

import lombok.*;
import org.dom4j.tree.AbstractEntity;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.id.enhanced.SequenceStyleGenerator;
import pl.com.tt.flex.server.domain.EntityInterface;
import pl.com.tt.flex.server.domain.screen.enumeration.Screen;
import pl.com.tt.flex.server.domain.user.UserEntity;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_screen_config")
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@GenericGenerator(
    name = "user_screen_config_id_generator",
    strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
    parameters = {
        @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "user_screen_config_seq"),
        @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.INITIAL_PARAM, value = "1"),
        @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.INCREMENT_PARAM, value = "1")
    }
)
public class UserScreenConfigEntity extends AbstractEntity implements Serializable, EntityInterface<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_screen_config_id_generator")
    private Long id;

    @ManyToOne(optional = false)
    private UserEntity user;

    @Enumerated(EnumType.STRING)
    @Column(name = "screen_name", nullable = false)
    private Screen screen;

    @OneToMany(mappedBy = "userScreenConfig", orphanRemoval = true, cascade = CascadeType.ALL)
    private List<ScreenColumnEntity> screenColumns;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof UserScreenConfigEntity)) {
            return false;
        }
        return Objects.nonNull(id) && Objects.equals(id, ((UserScreenConfigEntity) o).id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
