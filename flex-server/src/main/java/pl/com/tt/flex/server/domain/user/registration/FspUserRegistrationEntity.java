package pl.com.tt.flex.server.domain.user.registration;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import javax.validation.constraints.*;

import java.io.Serializable;
import java.util.*;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.id.enhanced.SequenceStyleGenerator;
import pl.com.tt.flex.server.domain.audit.AbstractAuditingEntity;
import pl.com.tt.flex.server.domain.user.UserEntity;
import pl.com.tt.flex.server.domain.user.registration.enumeration.FspUserRegistrationStatus;
import pl.com.tt.flex.model.security.permission.Role;

/**
 * A FspUserRegistrationEntity - registration process of organisation (FspEntity) and its user owner (UserEntity) with specified Role
 * task FLEXPLATF-9
 */
@Getter
@Setter
@Entity
@Table(name = "fsp_user_registration")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@GenericGenerator(
    name = "fsp_user_reg_id_generator",
    strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
    parameters = {
        @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "fsp_user_reg_seq"),
        @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.INITIAL_PARAM, value = "1"),
        @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.INCREMENT_PARAM, value = "1")
    }
)
public class FspUserRegistrationEntity extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "fsp_user_reg_id_generator")
    private Long id;

    @NotNull
    @Size(max = 50)
    @Column(name = "first_name", length = 50, nullable = false)
    private String firstName;

    @NotNull
    @Size(max = 50)
    @Column(name = "last_name", length = 50, nullable = false)
    private String lastName;

    @NotNull
    @Size(max = 254)
    @Column(name = "company_name", length = 254, nullable = false)
    private String companyName;

    @NotNull
    @Size(min = 5, max = 254)
    @Column(name = "email", length = 254, nullable = false, unique = true)
    private String email;

    @NotNull
    @Size(max = 20)
    @Column(name = "phone_number", length = 20, nullable = false)
    private String phoneNumber;

    /**
     * Status of the new FSP user registration process
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private FspUserRegistrationStatus status = FspUserRegistrationStatus.NEW;

    /**
     * Security key used to authenticate candidate outside the system e.g via email
     */
    @Size(max = 20)
    @Column(name = "security_key", length = 20)
    @JsonIgnore
    private String securityKey;

    @OneToOne
    @JoinColumn(unique = true)
    private UserEntity fspUser;

    /**
     * Role for organisation and its users
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "user_target_role", nullable = false)
    private Role userTargetRole;

    /**
     * Fsp user registration process may have multiple comments added
     */
    @OneToMany(mappedBy = "fspUserRegistration", cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE})
    private Set<FspUserRegistrationCommentEntity> comments = new HashSet<>();

    @NotNull
    @Column(nullable = false)
    private boolean readByAdmin;

    @NotNull
    @Size(max = 2)
    @Column(name = "lang_key", nullable = false)
    private String langKey;

    @NotNull
    @Column(name = "rules_confirmation", nullable = false)
    private boolean rulesConfirmation;

    @NotNull
    @Column(name = "rodo_confirmation", nullable = false)
    private boolean rodoConfirmation;

    public String getUserName() {
        return this.firstName + " " + this.getLastName();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof FspUserRegistrationEntity)) {
            return false;
        }
        return Objects.nonNull(id) && Objects.equals(id, ((FspUserRegistrationEntity) o).id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "FspUserRegistrationEntity{" +
            "id=" + id +
            ", fspUser=" + fspUser +
            '}';
    }
}
