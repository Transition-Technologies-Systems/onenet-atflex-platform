package pl.com.tt.flex.server.service.unit.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.com.tt.flex.model.service.dto.audit.AbstractAuditingDTO;
import pl.com.tt.flex.model.service.dto.localization.LocalizationTypeDTO;
import pl.com.tt.flex.model.service.dto.schedulingUnit.SchedulingUnitMinDTO;
import pl.com.tt.flex.server.domain.unit.UnitDirectionOfDeviation;
import pl.com.tt.flex.server.domain.unit.UnitEntity;
import pl.com.tt.flex.server.service.dictionary.derType.dto.DerTypeMinDTO;
import pl.com.tt.flex.server.service.fsp.dto.FspDTO;
import pl.com.tt.flex.server.service.subportfolio.dto.SubportfolioMinDTO;
import pl.com.tt.flex.server.validator.constraints.UniqueUnitName;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * A DTO for the UnitEntity (DER - Distributed Energy Resource).
 * {@link UnitEntity}
 */
@Getter
@Setter
@UniqueUnitName
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class UnitDTO extends AbstractAuditingDTO implements Serializable {

    private Long id;

    private Long version = 0L;

    @Size(max = 50)
    private String name;

    @Size(max = 50)
    private String code;

    private boolean aggregated;

    private Long fspId;

    private Instant validFrom;

    private Instant validTo;

    private boolean active;

    private boolean certified;

    private List<UnitGeoLocationDTO> geoLocations;

    private FspDTO fsp;

    @NotNull
    @Size(max = 100)
    @ApiModelProperty(required = true)
    private String ppe;

    private List<LocalizationTypeDTO> couplingPointIdTypes;

    @Size(max = 200)
    private String mridTso;

    @Size(max = 200)
    private String mridDso;

    private List<LocalizationTypeDTO> powerStationTypes;

    private List<LocalizationTypeDTO> pointOfConnectionWithLvTypes;

    @NotNull
    private BigDecimal sourcePower;

    @NotNull
    private BigDecimal connectionPower;

    @NotNull
    private UnitDirectionOfDeviation directionOfDeviation;

    private Boolean sder;

    private SchedulingUnitMinDTO schedulingUnit;

    private SubportfolioMinDTO subportfolio;

    //flaga informujaca aktualnie zalogowanego BSP czy ma mozliwosc zaproszenia tego DERa do przynajmniej jednego z swoich SchedulingUnit
    private Boolean canBspInviteFspDerToSchedulingUnit;

    //FLEX-ADMIN w oknie DERs ikony "plusa" i przyciski "Inivte DER" dostępne tylko przy DERach, które mają "Flex register" na produkt "Balancing" + poprzednie wymagania
    private Boolean balancedByFlexPotentialProduct;

    private DerTypeMinDTO derTypeReception;

    private DerTypeMinDTO derTypeEnergyStorage;

    private DerTypeMinDTO derTypeGeneration;

    private BigDecimal pMin;

    private BigDecimal qMin;

    private BigDecimal qMax;

}
