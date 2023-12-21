package pl.com.tt.flex.server.config.jackson.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import pl.com.tt.flex.model.security.permission.ViewWithAuthority;
import pl.com.tt.flex.server.security.SecurityUtils;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;

public class ViewWithAuthoritySerializerUtil {

    ViewWithAuthoritySerializerUtil() {
    }

    /**
     * Jezeli pole zawiera adnotacje ${@link ViewWithAuthority} to sprawdzane jest
     * czy zalogowany użytkownik posiada uprawnienia do podglądu danego pola.
     * - Gdy użytkownik posiada daną role -> true
     * - Gdy użytkownik nie ma uprawnien -> false
     * - Gdy nie ma adnotacji -> true
     */
    static boolean isUserHaveAuthority(Object value, JsonGenerator g) {
        if (Objects.nonNull(value) && Objects.nonNull(g.getOutputContext().getCurrentName())
            && Objects.nonNull(g.getCurrentValue())) {
            String currentName = g.getOutputContext().getCurrentName();
            Field field = findField(currentName, g.getCurrentValue());
            if (Objects.nonNull(field) && field.isAnnotationPresent(ViewWithAuthority.class)) {
                String[] authorities = field.getAnnotation(ViewWithAuthority.class).values();
                return Arrays.stream(authorities).anyMatch(SecurityUtils::isCurrentUserInAuthority);
            }
        }
        return true;
    }

    static Field findField(String fieldName, Object o) {
        Class<?> current = o.getClass();
        do {
            try {
                return current.getDeclaredField(fieldName);
            } catch (Exception e) {
            }
        } while ((current = current.getSuperclass()) != null);
        return null;
    }

    /**
     * Sprawdza czy dany obiekt pochodzi z podanej listy pakietow
     */
    static boolean isObjectComesFromPackage(Set<String> packages, Object object) {
        if (Objects.isNull(object)) {
            return false;
        }

        Package packageOfSerializationObject = object.getClass().getPackage();
        return packages.stream().anyMatch(p -> packageOfSerializationObject.getName().startsWith(p));
    }
}
