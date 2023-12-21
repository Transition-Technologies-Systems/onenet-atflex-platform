package pl.com.tt.flex.server.domain.fsp;

import lombok.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.id.enhanced.SequenceStyleGenerator;
import pl.com.tt.flex.server.domain.audit.AbstractAuditingEntity;
import pl.com.tt.flex.server.domain.potential.FlexPotentialEntity;
import pl.com.tt.flex.server.domain.schedulingUnit.SchedulingUnitEntity;
import pl.com.tt.flex.server.domain.subportfolio.SubportfolioEntity;
import pl.com.tt.flex.server.domain.user.UserEntity;
import pl.com.tt.flex.server.domain.user.registration.FspUserRegistrationEntity;
import pl.com.tt.flex.model.security.permission.Role;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.Instant;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * A FspEntity - Organisation which is created together with its user owner (UserEntity) as a result of accepted registration process (FspUserRegistrationEntity).
 *
 * @see UserEntity
 * @see FspUserRegistrationEntity
 * Each organistaion has specific Role.
 * @see Role
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "fsp")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@GenericGenerator(
    name = "fsp_id_generator",
    strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
    parameters = {
        @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "fsp_seq"),
        @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.INITIAL_PARAM, value = "1"),
        @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.INCREMENT_PARAM, value = "1")
    }
)
public class FspEntity extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "fsp_id_generator")
    private Long id;

    @Size(max = 254)
    @Column(name = "company_name", length = 254)
    private String companyName;

    @NotNull
    @Column(name = "valid_from", nullable = false)
    private Instant validFrom;

    @Column(name = "valid_to")
    private Instant validTo;

    @Column(name = "active")
    private boolean active;

    @Column(name = "deleted")
    private boolean deleted = false;

    @Column(name = "agreement_with_tso")
    private boolean agreementWithTso = false;

    /**
     * Role for organisation and its users
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role;

    @NotNull
    @OneToOne(optional = false)
    @JoinColumn(name = "owner_id", unique = true)
    private UserEntity owner;

    @OneToMany(mappedBy = "fsp")
    private Set<UserEntity> users;

    @OneToMany(mappedBy = "fsp")
    private Set<FlexPotentialEntity> flexPotentials;

    @OneToMany(mappedBy = "fspa")
    private Set<SubportfolioEntity> subportfolios = new HashSet<>();

    @OneToMany(mappedBy = "bsp")
    private Set<SchedulingUnitEntity> schedulingUnits = new HashSet<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof FspEntity)) {
            return false;
        }
        return Objects.nonNull(id) && Objects.equals(id, ((FspEntity) o).id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
