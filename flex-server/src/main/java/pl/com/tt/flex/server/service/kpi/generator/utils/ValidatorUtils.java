package pl.com.tt.flex.server.service.kpi.generator.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.util.CollectionUtils;
import pl.com.tt.flex.server.common.errors.ObjectValidationException;

import java.util.Collection;

import static pl.com.tt.flex.server.web.rest.errors.ErrorConstants.KPI_EMPTY_DATA_FOR_GIVEN_PARAMETERS;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ValidatorUtils {

    /**
     * Walidacja sprawdzajaca czy znaleziono dane
     */
    public static void checkValid(Collection<?> collection) {
        if (CollectionUtils.isEmpty(collection)) {
            throw new ObjectValidationException("Empty data for given parameters", KPI_EMPTY_DATA_FOR_GIVEN_PARAMETERS);
        }
    }
}
