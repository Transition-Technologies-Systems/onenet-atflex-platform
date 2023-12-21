package pl.com.tt.flex.server.dataexport.util.header;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class HeaderUtils {

    /**
     * Zwraca index dla danego kodu kolumny
     *
     * @param columnCode kod kolumny (nazwa pola z DTO)
     * @param headerList lista headerow z eksportowanego pliku
     */
    public static int getIndexByColumnCode(List<Header> headerList, String columnCode) {
        return headerList.stream()
            .filter(h -> h.getColumnCode().equals(columnCode))
            .map(Header::getIndex)
            .findFirst().orElseThrow(() -> new IllegalStateException("Cannot found index for column code: " + columnCode));
    }
}
