package pl.com.tt.flex.server.service.user.registration.dto;

import javax.validation.constraints.*;
import java.io.Serializable;
import java.util.List;

import io.swagger.annotations.ApiModelProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import pl.com.tt.flex.server.domain.user.registration.enumeration.FspUserRegistrationStatus;
import pl.com.tt.flex.server.domain.user.registration.FspUserRegistrationEntity;
import pl.com.tt.flex.model.security.permission.Role;
import pl.com.tt.flex.model.service.dto.audit.AbstractAuditingDTO;
import pl.com.tt.flex.model.service.dto.MinimalDTO;
import pl.com.tt.flex.server.validator.constraints.UniqueCompanyName;
import pl.com.tt.flex.server.validator.constraints.UniqueEmail;

/**
 * A DTO for the {@link FspUserRegistrationEntity} entity.
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
public class FspUserRegistrationDTO extends AbstractAuditingDTO implements Serializable {

    private Long id;

    @NotNull
    @Size(max = 50)
    @ApiModelProperty(required = true)
    private String firstName;

    @NotNull
    @Size(max = 50)
    @ApiModelProperty(required = true)
    private String lastName;

    @NotNull
    @UniqueCompanyName
    @Size(max = 254)
    @ApiModelProperty(required = true)
    private String companyName;

    @NotNull
    @UniqueEmail
    @ApiModelProperty(required = true)
    @Size(min = 5, max = 254)
    private String email;

    @NotNull
    @Size(max = 20)
    @ApiModelProperty(required = true)
    private String phoneNumber;

    @NotNull
    @ApiModelProperty(value = "Status of the new FSP user registration process (default: NEW)")
    private FspUserRegistrationStatus status = FspUserRegistrationStatus.NEW;

    private Long fspUserId;

    private String langKey;

    @NotNull
    private Role userTargetRole = Role.ROLE_FLEX_SERVICE_PROVIDER;

    private List<MinimalDTO<Long, String>> files;

    private boolean readByAdmin;

    private boolean rulesConfirmation;

    private boolean rodoConfirmation;

    @Override
    public String toString() {
        return "FspUserRegistrationDTO{" +
            "id=" + id + '}';
    }
}
