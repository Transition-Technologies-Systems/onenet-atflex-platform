package pl.com.tt.flex.model.service.dto.dictionary;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class DictionaryDTO implements Serializable {

    private Long id;
    private String value;
    private String nlsCode;

	 private String descriptionEn;
	 private String descriptionPl;
}
