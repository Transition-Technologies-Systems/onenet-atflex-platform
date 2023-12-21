package pl.com.tt.flex.server.service.kpi.generator.requested.flexibility.power.fillfactor;

import lombok.Getter;
import org.apache.commons.lang3.Validate;

import java.math.BigDecimal;

@Getter
class RequestedAndOfferedVolume {

    private final BigDecimal offeredVolume;
    private final BigDecimal requestedVolume;

    RequestedAndOfferedVolume(BigDecimal requestedVolume, BigDecimal offeredVolume) {
        Validate.notNull(requestedVolume, "requestedVolume cannot be null!");
        Validate.notNull(offeredVolume, "offeredVolume cannot be null!");

        this.offeredVolume = offeredVolume;
        this.requestedVolume = requestedVolume;
    }
}
