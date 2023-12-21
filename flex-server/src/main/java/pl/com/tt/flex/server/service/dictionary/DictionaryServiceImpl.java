package pl.com.tt.flex.server.service.dictionary;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import pl.com.tt.flex.model.service.dto.dictionary.DictionaryDTO;
import pl.com.tt.flex.model.service.dto.dictionary.DictionaryTranslateDTO;
import pl.com.tt.flex.model.service.dto.dictionary.DictionaryType;
import pl.com.tt.flex.server.domain.AbstractDictionaryEntity;
import pl.com.tt.flex.server.domain.schedulingUnit.SchedulingUnitTypeEntity;
import pl.com.tt.flex.server.domain.unit.DerTypeEntity;
import pl.com.tt.flex.server.repository.derType.DerTypeRepository;
import pl.com.tt.flex.server.repository.schedulingUnitType.SchedulingUnitTypeRepository;
import pl.com.tt.flex.server.service.dictionary.derType.dto.DerTypeMinDTO;
import pl.com.tt.flex.server.util.DictionaryUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
public class DictionaryServiceImpl implements DictionaryService {

    private final DerTypeRepository derTypeRepository;
    private final SchedulingUnitTypeRepository schedulingUnitTypeRepository;

    public DictionaryServiceImpl(DerTypeRepository derTypeRepository, SchedulingUnitTypeRepository schedulingUnitTypeRepository) {
        this.derTypeRepository = derTypeRepository;
        this.schedulingUnitTypeRepository = schedulingUnitTypeRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<DictionaryDTO> getDictionary(DictionaryType type) {
        if (DictionaryType.DER_TYPE.equals(type)) {
            return derTypeRepository.findAll().stream().map(DictionaryUtils::getDictionaryDTO).collect(Collectors.toList());
        }
        if (DictionaryType.SCHEDULING_UNIT_TYPE.equals(type)) {
            return schedulingUnitTypeRepository.findAll().stream().map(DictionaryUtils::getDictionaryDTO).collect(Collectors.toList());
        }
        throw new IllegalStateException("Not found dictionary for type: " + type);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DerTypeMinDTO> getDerTypes() {
        List<DerTypeMinDTO> derTypes = derTypeRepository.findAllMin();
        derTypes.forEach(derType -> {
            derType.setKey(DictionaryUtils.getKey(derType.getDescriptionEn()));
            derType.setNlsCode(DictionaryUtils.getNlsCode(DictionaryType.DER_TYPE, derType.getDescriptionEn()));
        });
        return derTypes;
    }

    @Override
    @Transactional(readOnly = true)
    public DictionaryTranslateDTO getDictionaryTranslate(String langKey) {
        DictionaryTranslateDTO dictionaryTranslateDTO = new DictionaryTranslateDTO();
        dictionaryTranslateDTO.setDerType(getDerTypeTranslate(langKey));
        dictionaryTranslateDTO.setSchedulingUnitType(getSchedulingUnitTypeTranslate(langKey));
        return dictionaryTranslateDTO;
    }

    @Override
    @Transactional(readOnly = true)
    public String getDescriptionByLocaleAndIdAndNlsCode(Locale locale, long id, String nlsCode) {
        AbstractDictionaryEntity dictionaryEntity = getDictionaryByIdAndNlsCode(id, nlsCode);
        return getDescriptionByLang(dictionaryEntity, locale.getLanguage());
    }

    private Map<String, String> getDerTypeTranslate(String langKey) {
        List<DerTypeEntity> derTypeEntities = derTypeRepository.findAll();
        Map<String, String> derTranslate = new HashMap<>();
        for (DerTypeEntity derType : derTypeEntities) {
            derTranslate.put(DictionaryUtils.getKey(derType), getDescriptionByLang(derType, langKey));
        }
        return derTranslate;
    }

    private Map<String, String> getSchedulingUnitTypeTranslate(String langKey) {
        List<SchedulingUnitTypeEntity> schedulingUnitTypeEntities = schedulingUnitTypeRepository.findAll();
        Map<String, String> derTranslate = new HashMap<>();
        for (SchedulingUnitTypeEntity suType : schedulingUnitTypeEntities) {
            derTranslate.put(DictionaryUtils.getKey(suType), getDescriptionByLang(suType, langKey));
        }
        return derTranslate;
    }

    private String getDescriptionByLang(AbstractDictionaryEntity dictionaryEntity, String langKey) {
        if (langKey.equals("pl") && !StringUtils.isEmpty(dictionaryEntity.getDescriptionPl())) {
            return dictionaryEntity.getDescriptionPl();
        }
        return dictionaryEntity.getDescriptionEn();
    }

    private AbstractDictionaryEntity getDictionaryByIdAndNlsCode(long id, String nlsCode) {
        DictionaryType dictionaryType = DictionaryUtils.getDictionaryTypeByNlsCode(nlsCode);
        if (dictionaryType.equals(DictionaryType.DER_TYPE)) {
            return derTypeRepository.findById(id).orElseThrow(() -> new IllegalStateException("Not found DerType value for id " + id));
        }
        if (dictionaryType.equals(DictionaryType.SCHEDULING_UNIT_TYPE)) {
            return schedulingUnitTypeRepository.findById(id).orElseThrow(() -> new IllegalStateException("Not found SchedulingUnitType value for id " + id));
        }
        throw new IllegalStateException("Not found dictionary type by nlsCode " + nlsCode);
    }
}
