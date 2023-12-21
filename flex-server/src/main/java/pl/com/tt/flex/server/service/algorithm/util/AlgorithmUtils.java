package pl.com.tt.flex.server.service.algorithm.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AlgorithmUtils {
    public static double convertPowerFromMwhToKwh(String powerInMwh) {
        return Double.parseDouble(powerInMwh) * 1000;
    }

    public static double convertPowerFromKwhToMwh(String powerInKwh) {
        return Double.parseDouble(powerInKwh) / 1000;
    }
}
