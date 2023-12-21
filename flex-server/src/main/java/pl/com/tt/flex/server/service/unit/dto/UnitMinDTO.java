package pl.com.tt.flex.server.service.unit.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.com.tt.flex.server.domain.unit.UnitDirectionOfDeviation;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
public class UnitMinDTO {

    private Long id;
    private String name;
    private Long fspId;
    private String fspCompanyName;
    private BigDecimal connectionPower;
    private String subportfolioName;
    private Long schedulingUnitId;
    private BigDecimal sourcePower;
    private BigDecimal pMin;
    private UnitDirectionOfDeviation unitDirectionOfDeviation;

    private Long version;
    private Instant createdDate;

    private Boolean sder;

    public UnitMinDTO(Long id, Long version, Instant createdDate) {
        this.id = id;
        this.version = version;
        this.createdDate = createdDate;
    }

    public UnitMinDTO(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public UnitMinDTO(Long id, String name, String subportfolioName) {
        this.id = id;
        this.name = name;
        this.subportfolioName = subportfolioName;
    }

    public UnitMinDTO(Long id, String name, Long fspId, BigDecimal sourcePower, BigDecimal pMin, UnitDirectionOfDeviation unitDirectionOfDeviation) {
        this.id = id;
        this.name = name;
        this.fspId = fspId;
        this.sourcePower = sourcePower;
        this.pMin = pMin;
        this.unitDirectionOfDeviation = unitDirectionOfDeviation;
    }

    public UnitMinDTO(Long id, String name, Long fspId) {
        this.id = id;
        this.name = name;
        this.fspId = fspId;
    }
}
