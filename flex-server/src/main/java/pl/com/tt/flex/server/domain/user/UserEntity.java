package pl.com.tt.flex.server.domain.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.id.enhanced.SequenceStyleGenerator;
import pl.com.tt.flex.server.config.Constants;
import pl.com.tt.flex.server.domain.EntityInterface;
import pl.com.tt.flex.server.domain.audit.AbstractAuditingEntity;
import pl.com.tt.flex.server.domain.fsp.FspEntity;
import pl.com.tt.flex.server.domain.notification.NotificationUserEntity;
import pl.com.tt.flex.server.domain.user.enumeration.CreationSource;
import pl.com.tt.flex.server.domain.user.registration.FspUserRegistrationEntity;
import pl.com.tt.flex.model.security.permission.Role;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.time.Instant;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * A user.
 */
@Getter
@Setter
@Entity
@Table(name = "users")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@GenericGenerator(
    name = "user_id_generator",
    strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
    parameters = {
        @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "users_seq"),
        @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.INITIAL_PARAM, value = "10"),
        @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.INCREMENT_PARAM, value = "1")
    }
)
public class UserEntity extends AbstractAuditingEntity implements EntityInterface<Long> {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_id_generator")
    private Long id;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CreationSource creationSource;

    @NotNull
    @Pattern(regexp = Constants.LOGIN_REGEX)
    @Size(min = 1, max = 50)
    @Column(length = 50, unique = true, nullable = false)
    private String login;

    @JsonIgnore
    @NotNull
    @Size(min = 60, max = 60)
    @Column(name = "password_hash", length = 60, nullable = false)
    private String password;

    @Size(max = 50)
    @Column(name = "first_name", length = 50)
    private String firstName;

    @Size(max = 50)
    @Column(name = "last_name", length = 50)
    private String lastName;

    @Email
    @Size(min = 5, max = 254)
    @Column(length = 254, unique = true, nullable = false)
    private String email;

    @Size(max = 20)
    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @NotNull
    @Column(nullable = false)
    private boolean activated = false;

    @NotNull
    private boolean deleted = false;

    @Size(min = 2, max = 10)
    @Column(name = "lang_key", length = 10)
    private String langKey;

    @Size(max = 20)
    @Column(name = "activation_key", length = 20)
    @JsonIgnore
    private String activationKey;

    @Size(max = 20)
    @Column(name = "reset_key", length = 20)
    @JsonIgnore
    private String resetKey;

    @Column(name = "reset_date")
    private Instant resetDate = null;

    @NotNull
    @Column(nullable = false)
    private boolean passwordChangeOnFirstLogin;

    @NotNull
    @Column(nullable = false)
    private Integer unsuccessfulLoginCount = 0;

    @Column(nullable = true)
    private Instant lastSuccessfulLoginDate;

    @Enumerated(EnumType.STRING)
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_role", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role")
    private Set<Role> roles = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fsp_id")
    private FspEntity fsp;

    @OneToOne(mappedBy = "fspUser")
    private FspUserRegistrationEntity fspUserRegistration;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private Set<NotificationUserEntity> notifications = new HashSet<>();

    // Nazwa firmy przeniesiona do FSP
    public String getCompanyName() {
        return Objects.nonNull(getFsp()) ? getFsp().getCompanyName() : StringUtils.EMPTY;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof UserEntity)) {
            return false;
        }
        return Objects.nonNull(id) && Objects.equals(id, ((UserEntity) o).id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "UserEntity{" + "id=" + id + ", login='" + login + '\'' + '}';
    }

	public String getUserName() {
        return firstName + " " + lastName;
	}

    public boolean hasRole(Role role) {
        return this.roles.contains(role);
    }
}
