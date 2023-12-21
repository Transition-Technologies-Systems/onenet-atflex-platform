package pl.com.tt.flex.server.service.user.registration.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import pl.com.tt.flex.server.domain.user.registration.FspUserRegistrationEntity;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * A DTO for the {@link FspUserRegistrationEntity} entity.
 * Contains only data from form sent by candidate.
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
public class FspUserRegistrationFormDTO implements Serializable {

    @NotNull
    @Size(max = 50)
    @ApiModelProperty(required = true)
    private String firstName;

    @NotNull
    @Size(max = 50)
    @ApiModelProperty(required = true)
    private String lastName;

    @NotNull
    @Size(max = 254)
    @ApiModelProperty(required = true)
    private String companyName;

    @NotNull
    @ApiModelProperty(required = true)
    @Size(min = 5, max = 254)
    private String email;

    @NotNull
    @Size(max = 20)
    @ApiModelProperty(required = true)
    private String phoneNumber;
}
