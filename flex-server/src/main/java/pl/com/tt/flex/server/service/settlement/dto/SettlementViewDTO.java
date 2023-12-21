package pl.com.tt.flex.server.service.settlement.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.com.tt.flex.model.service.dto.audit.AbstractAuditingDTO;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class SettlementViewDTO extends AbstractAuditingDTO implements Serializable {

    private Long id;
    private String derName;
    private Long offerId;
    private String auctionName;
    private String companyName;
    private Instant acceptedDeliveryPeriodFrom;
    private Instant acceptedDeliveryPeriodTo;
    private String acceptedVolume;
    private BigDecimal activatedVolume;
    private BigDecimal settlementAmount;
    private String unit;
    private Boolean acceptedVolumeTooltipVisible;
    private Boolean acceptedVolumeCmvcTooltipVisible;

}
