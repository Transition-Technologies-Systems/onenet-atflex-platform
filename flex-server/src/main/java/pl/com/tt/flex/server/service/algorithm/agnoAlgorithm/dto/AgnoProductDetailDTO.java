package pl.com.tt.flex.server.service.algorithm.agnoAlgorithm.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.com.tt.flex.model.service.dto.product.type.Direction;

import java.math.BigDecimal;

/**
 * Klasa zawierajaca informacje o produkcie na ktory zostala utworzona aukcja
 * Wykorzystywana do generowania plikow AGNO
 */
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
public class AgnoProductDetailDTO {

    private Long id;
    private String productName;
    private Direction productDirection;
    private BigDecimal forecastedPrice;
}
