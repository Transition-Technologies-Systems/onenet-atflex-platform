package pl.com.tt.flex.server.service.kpi.generator.transaction.volume;

import lombok.Getter;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.Pair;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

@Getter
public class TransactionVolumeData {
    private final Map<Pair<String, LocalDate>, BigDecimal> numberOfVolumesGroupingByProductNameAndDeliveryDate;
    private final Map<String, BigDecimal> numberOfVolumesnGroupingByProductName;

    public TransactionVolumeData(Map<Pair<String, LocalDate>, BigDecimal> numberOfVolumesGroupingByProductNameAndDeliveryDate,
                                 Map<String, BigDecimal> numberOfVolumesnGroupingByProductName) {
        Validate.notNull(numberOfVolumesGroupingByProductNameAndDeliveryDate, "NumberOfVolumesGroupingByProductNameAndDeliveryDate cannot be null");
        Validate.notNull(numberOfVolumesnGroupingByProductName, "NumberOfVolumesGroupingByProductNameAndDeliveryDate cannot be null");

        this.numberOfVolumesGroupingByProductNameAndDeliveryDate = numberOfVolumesGroupingByProductNameAndDeliveryDate;
        this.numberOfVolumesnGroupingByProductName = numberOfVolumesnGroupingByProductName;
    }
}
