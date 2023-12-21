package pl.com.tt.flex.server.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.context.MessageSource;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MessageUtils {


    public static final Locale LOCALE_EN = Locale.forLanguageTag("en");
    public static final Locale LOCALE_PL = Locale.forLanguageTag("pl");

    /**
    * Znajduje tlumaczenia dla podanych jezykow
    */
    public static List<String> getMessagesForLanguages(MessageSource messageSource, String code, Locale... locales) {
        return Arrays.stream(locales).map(l -> messageSource.getMessage(code, null, l))
            .collect(Collectors.toUnmodifiableList());
    }


}
