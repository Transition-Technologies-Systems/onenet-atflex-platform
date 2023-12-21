package pl.com.tt.flex.server.service.dictionary.schedulingUnitType.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.com.tt.flex.model.service.dto.product.ProductMinDTO;

import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class SchedulingUnitTypeMinDTO implements Serializable {

    private Long id;

    private List<ProductMinDTO> products;

    @Size(max = 150)
    private String key;

    @Size(max = 150)
    private String nlsCode;
}
