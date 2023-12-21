package pl.com.tt.flex.onenet.service.common;

import java.util.Objects;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

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
