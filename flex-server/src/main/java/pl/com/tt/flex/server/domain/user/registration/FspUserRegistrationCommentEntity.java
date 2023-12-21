package pl.com.tt.flex.server.domain.user.registration;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.id.enhanced.SequenceStyleGenerator;
import pl.com.tt.flex.server.domain.audit.AbstractAuditingEntity;
import pl.com.tt.flex.server.domain.user.UserEntity;
import pl.com.tt.flex.server.domain.user.registration.enumeration.FspUserRegCommentCreationSource;

import javax.persistence.*;
import javax.validation.constraints.*;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * A FspUserRegCommentEntity - comments added to FspUserRegistration.
 * @see FspUserRegistrationEntity
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "fsp_user_registration_comment")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@GenericGenerator(
    name = "fsp_u_reg_comm_id_generator",
    strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
    parameters = {
        @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "fsp_u_reg_comm_seq"),
        @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.INITIAL_PARAM, value = "1"),
        @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.INCREMENT_PARAM, value = "1")
    }
)
public class FspUserRegistrationCommentEntity extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "fsp_u_reg_comm_id_generator")
    private Long id;

    @Size(min = 1, max = 1000)
    @Column(name = "text", length = 1000)
    private String text;

    @ManyToOne
    @JsonIgnoreProperties(value = "comments", allowSetters = true)
    private FspUserRegistrationEntity fspUserRegistration;

    @OneToMany(mappedBy = "comment", cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE})
    private Set<FspUserRegistrationFileEntity> files = new HashSet<>();

    @ManyToOne
    private UserEntity user;

    @NotNull
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private FspUserRegCommentCreationSource creationSource;

    public FspUserRegistrationCommentEntity(@NotNull @Size(min = 1, max = 1000) String text, FspUserRegistrationEntity fspUserRegistration) {
        this.text = text;
        this.fspUserRegistration = fspUserRegistration;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof FspUserRegistrationCommentEntity)) {
            return false;
        }
        return Objects.nonNull(id) && Objects.equals(id, ((FspUserRegistrationCommentEntity) o).id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
