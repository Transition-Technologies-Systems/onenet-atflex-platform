package pl.com.tt.flex.server.service.unit.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import pl.com.tt.flex.server.domain.unit.UnitGeoLocationEntity;
import pl.com.tt.flex.model.service.dto.audit.AbstractAuditingDTO;

import java.io.Serializable;

/**
 * A DTO for the {@link UnitGeoLocationEntity} entity.
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
public class UnitGeoLocationDTO extends AbstractAuditingDTO implements Serializable {
    private Long id;

    @ApiModelProperty(example = "52.24423404202796")
    private String latitude;

    @ApiModelProperty(example = "20.982350812202593")
    private String longitude;

    private boolean mainLocation;

    private Long unitId;
}
