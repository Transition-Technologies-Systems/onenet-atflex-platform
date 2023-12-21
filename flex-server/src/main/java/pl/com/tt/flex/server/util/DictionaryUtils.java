package pl.com.tt.flex.server.util;

import pl.com.tt.flex.model.service.dto.dictionary.DictionaryType;
import pl.com.tt.flex.server.domain.AbstractDictionaryEntity;
import pl.com.tt.flex.model.service.dto.dictionary.DictionaryDTO;

public class DictionaryUtils {

    private DictionaryUtils() {
    }

    public static String getKey(AbstractDictionaryEntity dictionaryEntity) {
        if (dictionaryEntity != null && dictionaryEntity.getDescriptionEn() != null) {
            return dictionaryEntity.getDescriptionEn().trim().replace(" ", "_").toUpperCase();
        }
        return null;
    }

    public static String getNlsCode(AbstractDictionaryEntity dictionaryEntity) {
        if (dictionaryEntity != null && dictionaryEntity.getDescriptionEn() != null && dictionaryEntity.getDictionaryType() != null) {
            return dictionaryEntity.getDictionaryType() + "." + getKey(dictionaryEntity);
        }
        return null;
    }

    public static String getKey(String descriptionEn) {
        if (descriptionEn != null) {
            return descriptionEn.trim().replace(" ", "_").toUpperCase();
        }
        return null;
    }

    public static String getNlsCode(DictionaryType dictionaryType, String descriptionEn) {
        if (descriptionEn != null) {
            return dictionaryType + "." + getKey(descriptionEn);
        }
        return null;
    }

    public static DictionaryType getDictionaryTypeByNlsCode(String nlsCode) {
        return DictionaryType.valueOf(nlsCode.split("\\.")[0]);
    }

    public static DictionaryDTO getDictionaryDTO(AbstractDictionaryEntity dictionaryEntity) {
        DictionaryDTO dictionaryDTO = new DictionaryDTO();
        dictionaryDTO.setId(dictionaryEntity.getId());
        dictionaryDTO.setValue(getKey(dictionaryEntity));
        dictionaryDTO.setNlsCode(getNlsCode(dictionaryEntity));
        dictionaryDTO.setDescriptionEn(dictionaryEntity.getDescriptionEn());
        dictionaryDTO.setDescriptionPl(dictionaryEntity.getDescriptionPl());
        return dictionaryDTO;
    }
}
