package pl.com.tt.flex.server.service.kpi.generator.fsp.number;

import lombok.Getter;
import org.apache.commons.lang3.Validate;

import java.math.BigDecimal;

@Getter
public class NumberOfFspData {

    private final BigDecimal numberOfFsp;

    public NumberOfFspData(BigDecimal numberOfFsp) {
        Validate.notNull(numberOfFsp, "NumberOfFsp cannot be null");
        this.numberOfFsp = numberOfFsp;
    }
}
