package pl.com.tt.flex.server.domain.activityMonitor;

import lombok.*;
import org.dom4j.tree.AbstractEntity;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.id.enhanced.SequenceStyleGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import pl.com.tt.flex.server.config.AppModuleName;
import pl.com.tt.flex.server.domain.EntityInterface;

import javax.persistence.*;
import java.io.Serializable;
import java.time.Instant;

/**
 * Entity with activity events performed by user (only for user who performed operation).
 */

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "activity_monitor")
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@GenericGenerator(
    name = "activity_monitor_id_generator",
    strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
    parameters = {
        @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "activity_monitor_seq"),
        @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.INITIAL_PARAM, value = "1"),
        @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.INCREMENT_PARAM, value = "1")
    }
)
@EntityListeners(AuditingEntityListener.class)
public class ActivityMonitorEntity extends AbstractEntity implements Serializable, EntityInterface<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "activity_monitor_id_generator")
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @CreatedDate
    @Column(name = "created_date", updatable = false)
    private Instant createdDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "event", nullable = false)
    private ActivityEvent event;

    /**
     * Login of the user who performed activity event.
     */
    @Column(name = "login", nullable = false)
    private String login;

    /**
     * A field that identifies the object on which the activity event is performed.
     */
    @Column(name = "object_id")
    private String objectId;

    @Column(name = "error_code")
    private String errorCode;

    @Column(name = "error_message")
    private String errorMessage;

    @Column(name = "http_request_uri_path")
    private String httpRequestUriPath;

    @Column(name = "http_response_status")
    private String httpResponseStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "app_module_name")
    private AppModuleName appModuleName;
}
