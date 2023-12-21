package pl.com.tt.flex.model.service.dto.dictionary;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class DictionaryUpdateDTO {
    private DictionaryType type;
}