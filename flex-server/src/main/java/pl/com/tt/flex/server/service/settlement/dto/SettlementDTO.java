package pl.com.tt.flex.server.service.settlement.dto;

import java.io.Serializable;
import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.com.tt.flex.model.service.dto.audit.AbstractAuditingDTO;
import pl.com.tt.flex.server.service.unit.dto.UnitDTO;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class SettlementDTO extends AbstractAuditingDTO implements Serializable {

    private Long id;
    private UnitDTO der;
    private Long offerId;
    private String acceptedVolume;
    private BigDecimal activatedVolume;
    private BigDecimal settlementAmount;

}
