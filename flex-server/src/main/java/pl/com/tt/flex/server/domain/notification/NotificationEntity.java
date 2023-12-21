package pl.com.tt.flex.server.domain.notification;

import lombok.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import java.io.Serializable;
import java.time.Instant;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.id.enhanced.SequenceStyleGenerator;
import pl.com.tt.flex.server.domain.enumeration.NotificationEvent;
import pl.com.tt.flex.server.util.InstantUtil;

/**
 * A NotificationEntity.
 */
@Getter
@Setter
@Builder
@Entity
@Table(name = "notification")
@NoArgsConstructor
@AllArgsConstructor
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@GenericGenerator(
    name = "notification_id_generator",
    strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
    parameters = {
        @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "notification_seq"),
        @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.INITIAL_PARAM, value = "1"),
        @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.INCREMENT_PARAM, value = "1")
    }
)
public class NotificationEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "notification_id_generator")
    private Long id;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false)
    private NotificationEvent eventType;

    @NotNull
    @Column(name = "created_date", nullable = false)
    private Instant createdDate = InstantUtil.now();

    @NotEmpty
    @OneToMany(mappedBy = "notification", cascade = {CascadeType.PERSIST})
    private Set<NotificationUserEntity> notificationUsers = new HashSet<>();

    @OneToMany(mappedBy = "notification", cascade = {CascadeType.PERSIST})
    private Set<NotificationParamEntity> notificationParams = new HashSet<>();


    public NotificationEntity addNotificationUser(NotificationUserEntity notificationUser) {
        this.notificationUsers.add(notificationUser);
        notificationUser.setNotification(this);
        return this;
    }

    public NotificationEntity removeNotificationUser(NotificationUserEntity notificationUser) {
        this.notificationUsers.remove(notificationUser);
        notificationUser.setNotification(null);
        return this;
    }

    public NotificationEntity addNotificationParam(NotificationParamEntity notificationParam) {
        this.notificationParams.add(notificationParam);
        notificationParam.setNotification(this);
        return this;
    }

    public NotificationEntity removeNotificationParam(NotificationParamEntity notificationParam) {
        this.notificationParams.remove(notificationParam);
        notificationParam.setNotification(null);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof NotificationEntity)) {
            return false;
        }
        return Objects.nonNull(id) && Objects.equals(id, ((NotificationEntity) o).id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "NotificationEntity{" +
            "id=" + getId() +
            "}";
    }
}
