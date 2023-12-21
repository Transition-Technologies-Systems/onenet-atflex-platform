package pl.com.tt.flex.server.service.product.forecastedPrices.dto;

import lombok.Getter;

import java.util.*;

@Getter
public class ForecastedPricesErrorDTO {
    private final Map<String, List<String>> invalidFiles = new HashMap<>();
    private final Map<String, List<ForecastedPricesMinDTO>> invalidForecastedPrices = new HashMap<>();

    public void addInvalidFilename(String msgKey, String filename) {
        Optional<Map.Entry<String, List<String>>> msgKeyOpt = invalidFiles.entrySet().stream()
            .filter(invalidFile -> invalidFile.getKey().equals(msgKey))
            .findFirst();
        if (msgKeyOpt.isEmpty()) {
            invalidFiles.put(msgKey, Collections.singletonList(filename));
        }
    }

    public void addInvalidForecastedPrices(String msgKey, ForecastedPricesMinDTO forecastedPricesMinDTO) {
        Optional<Map.Entry<String, List<ForecastedPricesMinDTO>>> msgKeyOpt = invalidForecastedPrices.entrySet().stream()
            .filter(invalidFile -> invalidFile.getKey().equals(msgKey))
            .findFirst();
        if (msgKeyOpt.isPresent()) {
            invalidForecastedPrices.get(msgKey).add(forecastedPricesMinDTO);
        } else {
            invalidForecastedPrices.put(msgKey, Collections.singletonList(forecastedPricesMinDTO));
        }
    }
}
