package pl.com.tt.flex.server.service.common;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

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

    /**
     * Metoda podmieniajaca sortowanie dla danego parametru na odpowiednie sortowanie dla danego jezyka.
     * <p>
     * Przyklaodow mamy tabele z kolumna w ktorej znajduje sie enum Type (tlumaczenie na froncie).
     * Aby sorotowac dane dla wybranego jezyka (np. pl) po tej kolumnie nalezy dodac dodatkowa kolumne type_pl,
     * gdzie bedzie zapisywany numer porządkowy dla danej wartości z enuma (najprosciej zrobić to w widoku).
     *
     * @param sortProperty    parametr sortowania ktory nalezy podmienic
     * @param lang            kod kraju po jakim ma byc ustawione sortowanie
     * @param propertyForLang Mapa zawierajaca parametr po jakim nalezy sortowac dla dangeo jezyka
     * @return zwracany jest obiekt Pageable z podmienionym sortowanie dla wybranego jezyka
     * lub nie zmioniony obiekt Pageable gdy sortowanie nie zawieralo parametru do podmienienia
     */
    public static Pageable replaceSortForLang(String sortProperty, Map<String, String> propertyForLang, Pageable pageable, String lang) {
        Sort sort = pageable.getSort();
        if (sort.stream().anyMatch(s -> s.getProperty().equals(sortProperty))) {
            Sort.Direction direction = getDirectionForProperty(sort, sortProperty);
            List<Sort.Order> orders = removeSort(sortProperty, sort);
            return sortByLang(propertyForLang, lang, orders, pageable, direction);
        }
        return pageable;
    }

    private static Pageable sortByLang(Map<String, String> propertyForLang, String lang,
                                       List<Sort.Order> orders,
                                       Pageable pageable, Sort.Direction direction) {
        if (propertyForLang.containsKey(lang)) {
            String sortForLang = propertyForLang.get(lang);
            orders.add(Sort.Order.by(sortForLang).with(direction));
            return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(orders));
        }
        return pageable;
    }

    private static Sort.Direction getDirectionForProperty(Sort sort, String property) {
        return sort.stream().filter(s -> s.getProperty().equals(property))
                .map(Sort.Order::getDirection)
                .findFirst()
                .orElse(Sort.Direction.ASC);
    }

    private static List<Sort.Order> removeSort(String orderToReplace, Sort sort) {
        return sort.stream()
                .filter(s -> !s.getProperty().equals(orderToReplace))
                .collect(Collectors.toList());
    }
}
