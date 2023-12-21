package pl.com.tt.flex.server.service.algorithm.agnoAlgorithm.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.com.tt.flex.model.service.dto.product.type.Direction;

import java.math.BigDecimal;

/**
 * Klasa zawierajaca informacje o ofercie złożonej na daną godzinę giełdową.
 * Wykorzystywana do generowania plikow AGNO
 */
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
public class AgnoOfferDetailDTO {

    private String derName;
    private String productShortName;
    private Direction productDirection;
    private BigDecimal bandVolume;
    private BigDecimal bandPrice;
}
