package pl.com.tt.flex.server.dataexport.util.header;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Locale;

@Getter
@AllArgsConstructor
public class LocaleTranslation {
    private Locale locale;
    private String translation;
}
