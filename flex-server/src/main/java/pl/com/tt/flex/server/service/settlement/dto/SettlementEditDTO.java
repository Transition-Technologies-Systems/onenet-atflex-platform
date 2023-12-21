package pl.com.tt.flex.server.service.settlement.dto;

import java.io.Serializable;
import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class SettlementEditDTO implements Serializable {

    private BigDecimal activatedVolume;
    private BigDecimal settlementAmount;
    private String unit;

}
