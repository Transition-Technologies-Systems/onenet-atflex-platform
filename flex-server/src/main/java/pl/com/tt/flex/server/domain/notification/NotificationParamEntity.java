package pl.com.tt.flex.server.domain.notification;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.id.enhanced.SequenceStyleGenerator;

import javax.persistence.*;
import javax.validation.constraints.*;

import java.io.Serializable;
import java.util.Objects;

/**
 * A NotificationParamEntity.
 */
@Getter
@Setter
@Entity
@Table(name = "notification_param")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@GenericGenerator(
    name = "notification_param_id_generator",
    strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
    parameters = {
        @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "notification_param_seq"),
        @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.INITIAL_PARAM, value = "1"),
        @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.INCREMENT_PARAM, value = "1")
    }
)
public class NotificationParamEntity implements Serializable {

    public static final int MAX_LENGTH_FOR_PARAM_VALUE = 1000;

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "notification_param_id_generator")
    private Long id;

    @NotNull
    @Size(max = 150)
    @Column(name = "name", length = 150, nullable = false)
    private String name;

    @Size(max = 1000)
    @Column(name = "value", length = MAX_LENGTH_FOR_PARAM_VALUE)
    private String value;

    // Zapis obiektu jako params np. jsona
    @Column(name = "object")
    private byte[] object;

    @ManyToOne
    @JsonIgnoreProperties(value = "notificationParams", allowSetters = true)
    private NotificationEntity notification;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof NotificationParamEntity)) {
            return false;
        }
        return Objects.nonNull(id) && Objects.equals(id, ((NotificationParamEntity) o).id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
