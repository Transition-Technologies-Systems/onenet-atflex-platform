package pl.com.tt.flex.model.service.dto.schedulingUnit;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.com.tt.flex.model.service.dto.der.DerMinDTO;
import pl.com.tt.flex.model.service.dto.dictionary.DictionaryDTO;
import pl.com.tt.flex.model.service.dto.fsp.FspCompanyMinDTO;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class SchedulingUnitMinDTO implements Serializable {

    private Long id;
    private String name;
    private DictionaryDTO schedulingUnitType;
    private FspCompanyMinDTO bsp;
    private List<DerMinDTO> ders;

    public SchedulingUnitMinDTO(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public SchedulingUnitMinDTO(Long id, String name, DictionaryDTO schedulingUnitType) {
        this.id = id;
        this.name = name;
        this.schedulingUnitType = schedulingUnitType;
    }
}
