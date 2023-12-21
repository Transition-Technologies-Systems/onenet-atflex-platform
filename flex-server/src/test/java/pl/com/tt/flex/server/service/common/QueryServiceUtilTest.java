package pl.com.tt.flex.server.service.common;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static pl.com.tt.flex.server.service.common.QueryServiceUtil.replaceSortForLang;

class QueryServiceUtilTest {

    final String PL_LANG = "pl";
    final String EN_LANG = "en";
    final String TEST_TYPE = "test_type";
    final String TEST_TYPE_PL = "test_type_pl";
    final String TEST_TYPE_EN = "test_type_en";
    final String DEFAULT_SORT = "id";

    @Test
    void replaceSortForLang_shouldReplaceSort() {
        //given
        PageRequest pageable = PageRequest.of(1, 1, Sort.Direction.ASC, TEST_TYPE, DEFAULT_SORT);
        Map<String, String> sortForLang = Map.of(PL_LANG, TEST_TYPE_PL, EN_LANG, TEST_TYPE_EN);

        //when
        Pageable result = replaceSortForLang(TEST_TYPE, sortForLang, pageable, PL_LANG);

        //then
        assertEquals(2, result.getSort().stream().count());
        assertTrue(
                result.getSort().stream().anyMatch(s -> s.getProperty().equals(TEST_TYPE_PL)),
                "Sort fot lang not set!"
        );
        assertFalse(
                result.getSort().stream().anyMatch(s -> s.getProperty().equals(TEST_TYPE)),
                "Not delete sort parameters!"
        );
    }

    @Test
    void replaceSortForLang_shouldNotReplaceSortWhenMapNotContainsLang() {
        //given
        PageRequest pageable = PageRequest.of(1, 1, Sort.Direction.ASC, TEST_TYPE);
        Map<String, String> sortForLang = Map.of(PL_LANG, TEST_TYPE_PL);

        //when
        Pageable result = replaceSortForLang(TEST_TYPE, sortForLang, pageable, EN_LANG);

        //then
        assertEquals(1, result.getSort().stream().count());
        assertTrue(
                result.getSort().stream().anyMatch(s -> s.getProperty().equals(TEST_TYPE)),
                "Delete sort parameter!"
        );
    }

    @Test
    void replaceSortForLang_shouldNotReplaceWhenParameterToReplaceNotExistInPageable() {
        //given
        PageRequest pageable = PageRequest.of(1, 1, Sort.Direction.ASC, DEFAULT_SORT);
        Map<String, String> sortForLang = Map.of(PL_LANG, TEST_TYPE_PL);

        //when
        Pageable result = replaceSortForLang(TEST_TYPE, sortForLang, pageable, EN_LANG);

        //then
        assertEquals(1, result.getSort().stream().count());
        assertTrue(
                result.getSort().stream().anyMatch(s -> s.getProperty().equals(DEFAULT_SORT)),
                "Delete sort parameter!"
        );
        assertFalse(
                result.getSort().stream().anyMatch(s -> s.getProperty().equals(TEST_TYPE)),
                "Sorting incorrectly added!"
        );
    }
}