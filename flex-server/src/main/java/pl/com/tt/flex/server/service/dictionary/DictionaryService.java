package pl.com.tt.flex.server.service.dictionary;

import org.springframework.transaction.annotation.Transactional;
import pl.com.tt.flex.model.service.dto.dictionary.DictionaryDTO;
import pl.com.tt.flex.model.service.dto.dictionary.DictionaryTranslateDTO;
import pl.com.tt.flex.model.service.dto.dictionary.DictionaryType;
import pl.com.tt.flex.server.service.dictionary.derType.dto.DerTypeMinDTO;

import java.util.List;
import java.util.Locale;

public interface DictionaryService {

    @Transactional(readOnly = true)
    List<DictionaryDTO> getDictionary(DictionaryType type);

    List<DerTypeMinDTO> getDerTypes();

    @Transactional(readOnly = true)
    DictionaryTranslateDTO getDictionaryTranslate(String langKey);

    String getDescriptionByLocaleAndIdAndNlsCode(Locale locale, long id, String nlsCode);
}
