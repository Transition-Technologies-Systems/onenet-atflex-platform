package pl.com.tt.flex.server.service.algorithm.agnoAlgorithm.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Klasa zawierajaca CouplingPoints'y wykorzystane w ofertach na dany dzień dostawy.
 * Wykorzystywana do generowania plikow AGNO
 */
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
public class AgnoInputDataDTO {

    private List<AgnoCouplingPointDTO> couplingPoints = new ArrayList<>();
    private Instant deliveryDate;

    public List<AgnoCouplingPointDTO> addAgnoCouplingPoint(AgnoCouplingPointDTO agnoCouplingPointDTO) {
        if (couplingPoints.stream().noneMatch(cp -> cp.getCouplingPointId().getId().equals(agnoCouplingPointDTO.getCouplingPointId().getId()))) {
            couplingPoints.add(agnoCouplingPointDTO);
        }
        return couplingPoints;
    }
}
