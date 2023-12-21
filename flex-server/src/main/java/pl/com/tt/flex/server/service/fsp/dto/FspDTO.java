package pl.com.tt.flex.server.service.fsp.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.com.tt.flex.server.domain.fsp.FspEntity;
import pl.com.tt.flex.model.security.permission.Role;
import pl.com.tt.flex.model.service.dto.audit.AbstractAuditingDTO;
import pl.com.tt.flex.server.service.user.dto.UserMinDTO;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.Instant;

/**
 * A DTO for the {@link FspEntity} entity.
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class FspDTO extends AbstractAuditingDTO implements Serializable {

    @NotNull
    private Long id;

    private String companyName;

    @NotNull
    private Instant validFrom;

    private Instant validTo;

    private boolean active;

    private boolean agreementWithTso;

    private Long representativeId;

    private UserMinDTO representative;

    private Role role;

    // flaga dla aktualnie zalogowanego FSP/A informujaca czy jakis z jego DERow jest dolaczony do przynajmniej jednego SchedulingUnit tego BSP
    private Boolean fspJoinedWithBspBySchedulingUnit;
    // flaga dla aktualnie zalogowanego FSP/A informujaca czy ma mozliwosc dodania przynjamniej jednego z swoich DERow do SchedulingUnit tego BSP
    private Boolean canFspJoinToBspSchedulingUnits;

    public FspDTO(@NotNull Long id) {
        this.id = id;
    }
}
