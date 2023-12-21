package pl.com.tt.flex.server.service.algorithm.agnoAlgorithm.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.com.tt.flex.model.service.dto.localization.LocalizationTypeDTO;

import java.math.BigDecimal;


/**
 * Klasa zawierajaca informacje o danym Derzem ktory zostal zlozony w ofercie.
 * Wykorzystywana do generowania plikow AGNO
 */
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
public class AgnoDerDetailDTO {

    private Long id;
    private String name;
    private LocalizationTypeDTO powerStationType;
    private BigDecimal pMin;
    private BigDecimal pMax; //SourcePower z Dera
    private BigDecimal qMin;
    private BigDecimal qMax;
    private BigDecimal selfScheduleVolume;
}
