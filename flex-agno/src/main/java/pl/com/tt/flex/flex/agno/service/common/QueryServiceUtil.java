package pl.com.tt.flex.flex.agno.service.common;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Objects;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class QueryServiceUtil {

    public static Pageable setDefaultOrder(Pageable page, String defaultOrderProperty) {
        Sort sort = page.getSort();
        if (sort.stream().noneMatch(s -> Objects.equals(s.getProperty(), defaultOrderProperty))) {
            Sort sortWithDefaultId = sort.and(Sort.by(defaultOrderProperty).ascending());
            page = PageRequest.of(page.getPageNumber(), page.getPageSize(), sortWithDefaultId);
        }
        return page;
    }
}
