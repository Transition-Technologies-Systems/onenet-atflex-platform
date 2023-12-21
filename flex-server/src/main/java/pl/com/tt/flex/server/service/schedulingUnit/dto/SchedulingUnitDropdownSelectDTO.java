package pl.com.tt.flex.server.service.schedulingUnit.dto;

import java.io.Serializable;
import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.com.tt.flex.model.service.dto.der.DerMinDTO;
import pl.com.tt.flex.model.service.dto.dictionary.DictionaryDTO;

@Getter
@Setter
@NoArgsConstructor
public class SchedulingUnitDropdownSelectDTO implements Serializable {

    private Long id;
    private String name;
    private DictionaryDTO schedulingUnitType;
    private List<DerMinDTO> ders;

    public SchedulingUnitDropdownSelectDTO(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public SchedulingUnitDropdownSelectDTO(Long id, String name, DictionaryDTO schedulingUnitType) {
        this.id = id;
        this.name = name;
        this.schedulingUnitType = schedulingUnitType;
    }
}
