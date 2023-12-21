package pl.com.tt.flex.model.service.dto.algorithm;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class AlgorithmEvaluationConfigDTO extends AlgorithmStartDTO {

    public AlgorithmEvaluationConfigDTO(AlgorithmStartDTO algStartDTO, AlgorithmType type){
        super(algStartDTO.getKdmModelId(), algStartDTO.getDeliveryDate(), algStartDTO.getOffers());
        this.algorithmType = type;
    }

    @NotNull
    private AlgorithmType algorithmType;

}
