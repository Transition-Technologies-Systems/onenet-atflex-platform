package pl.com.tt.flex.server.service.kpi.generator.transaction.number;

import lombok.Getter;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.Pair;

import java.time.LocalDate;
import java.util.Map;

@Getter
public class TransactionNumberData {

    private final Map<Pair<String, LocalDate>, Long> numberOfTransactionGroupingByProductNameAndDeliveryDate;
    private final Map<String, Long> numberOfTransactionGroupingByProductName;

    public TransactionNumberData(Map<Pair<String, LocalDate>, Long> numberOfTransactionGroupingByProductNameAndDeliveryDate,
                                 Map<String, Long> numberOfTransactionGroupingByProductName) {
        Validate.notNull(numberOfTransactionGroupingByProductName, "NumberOfTransactionGroupingByProductNameAndDeliveryDate cannot be null");
        Validate.notNull(numberOfTransactionGroupingByProductName, "NumberOfTransactionGroupingByProductName cannot be null");

        this.numberOfTransactionGroupingByProductNameAndDeliveryDate = numberOfTransactionGroupingByProductNameAndDeliveryDate;
        this.numberOfTransactionGroupingByProductName = numberOfTransactionGroupingByProductName;
    }
}
