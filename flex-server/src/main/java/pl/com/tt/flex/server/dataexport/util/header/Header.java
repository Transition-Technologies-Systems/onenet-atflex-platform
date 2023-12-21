package pl.com.tt.flex.server.dataexport.util.header;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class Header {

    private int index;
    /**
     * Kod kolumny, na podstawie ktorego wyznaczana jest nazwa kolumny (nazwa pola z DTO)
     */
    private String columnCode;
    /**
     * Lista z dozwolonymi dla danej kolumny tlumaczen
     */
    private List<LocaleTranslation> columnTranslations;

}
