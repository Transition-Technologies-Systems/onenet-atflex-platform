package pl.com.tt.flex.model.service.dto.dictionary;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class DictionaryTranslateDTO {

    @JsonProperty("DER_TYPE")
    Map<String, String> derType;

    @JsonProperty("SCHEDULING_UNIT_TYPE")
    Map<String, String> schedulingUnitType;
}
