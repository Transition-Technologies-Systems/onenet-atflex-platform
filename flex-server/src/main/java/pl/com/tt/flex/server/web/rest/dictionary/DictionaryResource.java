package pl.com.tt.flex.server.web.rest.dictionary;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.com.tt.flex.model.service.dto.dictionary.DictionaryDTO;
import pl.com.tt.flex.model.service.dto.dictionary.DictionaryTranslateDTO;
import pl.com.tt.flex.model.service.dto.dictionary.DictionaryType;
import pl.com.tt.flex.server.domain.common.enumeration.FileExtension;
import pl.com.tt.flex.server.domain.user.registration.enumeration.FspUserRegistrationStatus;
import pl.com.tt.flex.server.service.dictionary.DictionaryService;
import pl.com.tt.flex.server.service.dictionary.derType.dto.DerTypeMinDTO;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static pl.com.tt.flex.model.security.permission.Authority.FLEX_SYS_DICTIONARY_RESOURCE_VIEW;


/**
 * REST controller for Dictionary values.
 */

@Slf4j
@RestController
@RequestMapping("/api/dictionary")
public class DictionaryResource {

    private final DictionaryService dictionaryService;

    public DictionaryResource(DictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
    }

    /**
     * {@code GET} : get dictionary with FspUserRegistrationStatuses.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of String FspUserRegistrationStatuses values in body.
     */
    @GetMapping("/getFspUserRegistrationStatuses")
    @PreAuthorize("hasAuthority(\"" + FLEX_SYS_DICTIONARY_RESOURCE_VIEW + "\")")
    public List<String> getFspUserRegistrationStatuses(){
        log.debug("REST request to get dictionary with FspUserRegistrationStatuses");
        return Arrays.stream(FspUserRegistrationStatus.values()).map(FspUserRegistrationStatus::name).collect(Collectors.toList());
    }

    /**
     * {@code GET} : get dictionary with supported FileExtensions on the server.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of String FileExtensions values in body.
     */
    @GetMapping("/getFileExtensions")
    @PreAuthorize("hasAuthority(\"" + FLEX_SYS_DICTIONARY_RESOURCE_VIEW + "\")")
    public List<String> getFileExtensions(){
        log.debug("REST request to get dictionary with FileExtensions");
        return Arrays.stream(FileExtension.values()).map(FileExtension::name).collect(Collectors.toList());
    }

    /**
     * {@code GET} : get dictionary by the type.
     *
     * @param type the type of dictionary.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of DictionaryDTO.
     */
    @PreAuthorize("hasAuthority(\"" + FLEX_SYS_DICTIONARY_RESOURCE_VIEW + "\")")
    @GetMapping("/get-by-type/{type}")
    public ResponseEntity<List<DictionaryDTO>> getDictionaries(@PathVariable DictionaryType type) {
        log.debug("REST request to get dictionaries by type : {}", type);
        List<DictionaryDTO> dictionaries = dictionaryService.getDictionary(type);
        return ResponseEntity.ok().body(dictionaries);
    }


    /**
     * {@code GET} : get all DerTypes.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of DictionaryDTO.
     */
    @PreAuthorize("hasAuthority(\"" + FLEX_SYS_DICTIONARY_RESOURCE_VIEW + "\")")
    @GetMapping("/get-der-types")
    public ResponseEntity<List<DerTypeMinDTO>> getDerTypesDictionary() {
        log.debug("REST request to get DerTypes dictionary");
        List<DerTypeMinDTO> dictionaries = dictionaryService.getDerTypes();
        return ResponseEntity.ok().body(dictionaries);
    }

    /**
     * {@code GET} : get dictionary translate by the lang key.
     *
     * @param langKey the language of the dictionary
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of DictionaryTranslateDTO.
     */
    @PreAuthorize("hasAuthority(\"" + FLEX_SYS_DICTIONARY_RESOURCE_VIEW + "\")")
    @GetMapping("/translate/{langKey}")
    public ResponseEntity<DictionaryTranslateDTO> getDictionaries(@PathVariable String langKey) {
        log.debug("REST request to get dictionaries translate by lang key : {}", langKey);
        DictionaryTranslateDTO dictionaryTranslate = dictionaryService.getDictionaryTranslate(langKey);
        return ResponseEntity.ok().body(dictionaryTranslate);
    }
}
