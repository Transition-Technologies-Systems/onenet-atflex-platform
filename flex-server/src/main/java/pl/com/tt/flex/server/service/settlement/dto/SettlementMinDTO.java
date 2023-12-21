package pl.com.tt.flex.server.service.settlement.dto;

import java.io.Serializable;
import java.time.Instant;

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
public class SettlementMinDTO implements Serializable {

    private Long id;
    private String companyName;
    private Instant acceptedDeliveryPeriodFrom;
    private Instant acceptedDeliveryPeriodTo;

}
