package pl.com.tt.flex.server.service.algorithm.agnoAlgorithm.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.com.tt.flex.model.service.dto.localization.LocalizationTypeDTO;

import java.util.ArrayList;
import java.util.List;

/**
 * Klasa zawierajaca informacje o CouplingPoincie wykorzystanym w ofertach.
 * Wykorzystywana do generowania plikow AGNO
 */
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
public class AgnoCouplingPointDTO {
    private LocalizationTypeDTO couplingPointId;
    private List<AgnoHourNumberDTO> agnoHourNumbers = new ArrayList<>();

    public AgnoCouplingPointDTO(LocalizationTypeDTO couplingPointId) {
        this.couplingPointId = couplingPointId;
    }

    public List<AgnoHourNumberDTO> addHourNumber(AgnoHourNumberDTO agnoHourNumberDTO) {
        if (agnoHourNumbers.stream().noneMatch(hn -> hn.getHourNumber().equals(agnoHourNumberDTO.getHourNumber()))) {
            agnoHourNumbers.add(agnoHourNumberDTO);
        }
        return agnoHourNumbers;
    }


}
