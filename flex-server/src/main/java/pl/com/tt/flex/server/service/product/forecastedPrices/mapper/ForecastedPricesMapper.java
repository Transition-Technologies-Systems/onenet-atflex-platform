package pl.com.tt.flex.server.service.product.forecastedPrices.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import pl.com.tt.flex.model.service.dto.MinimalDTO;
import pl.com.tt.flex.server.domain.product.ForecastedPricesEntity;
import pl.com.tt.flex.server.service.mapper.FileEntityMapper;
import pl.com.tt.flex.server.service.product.forecastedPrices.dto.ForecastedPricesDTO;
import pl.com.tt.flex.server.service.product.mapper.ProductMapper;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static pl.com.tt.flex.server.util.DateUtil.sortedHourNumbers;

@Mapper(componentModel = "spring", uses = {ProductMapper.class})
public interface ForecastedPricesMapper extends FileEntityMapper<ForecastedPricesDTO, ForecastedPricesEntity> {

    @Mapping(source = "prices", target = "prices", qualifiedByName = "toPriceEntity")
    ForecastedPricesDTO toDto(ForecastedPricesEntity forecastedPricesEntity);

    @Mapping(source = "product.id", target = "product")
    @Mapping(source = "prices", target = "prices", qualifiedByName = "toPriceDto")
    ForecastedPricesEntity toEntity(ForecastedPricesDTO forecastedPricesDTO);

    default ForecastedPricesEntity fromId(Long id) {
        if (id == null) {
            return null;
        }
        ForecastedPricesEntity forecastedPricesEntity = new ForecastedPricesEntity();
        forecastedPricesEntity.setId(id);
        return forecastedPricesEntity;
    }

    @Named("toPriceEntity")
    default Map<String, BigDecimal> toPriceEntity(List<MinimalDTO<String, BigDecimal>> prices) {
        return prices.stream().collect(Collectors.toMap(MinimalDTO::getId, MinimalDTO::getValue));
    }

    @Named("toPriceDto")
    default List<MinimalDTO<String, BigDecimal>> toPriceDto(Map<String, BigDecimal> prices) {
        return prices.entrySet().stream().map(price -> new MinimalDTO<>(price.getKey(), price.getValue()))
            .sorted(Comparator.comparingInt(c -> sortedHourNumbers.indexOf(c.getId())))
            .collect(Collectors.toList());
    }
}
