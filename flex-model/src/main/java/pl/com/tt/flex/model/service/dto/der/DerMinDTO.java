package pl.com.tt.flex.model.service.dto.der;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.com.tt.flex.model.service.dto.dictionary.DictionaryDTO;
import pl.com.tt.flex.model.service.dto.fsp.FspCompanyMinDTO;

import java.io.Serializable;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class DerMinDTO implements Serializable {

    private Long id;
    private String name;
    private BigDecimal sourcePower;
    private BigDecimal pMin;

}
