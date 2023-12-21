package pl.com.tt.flex.server.validator.selfSchedule;

import lombok.Getter;

import java.util.*;

@Getter
public class DerExceedTechnicalLimits {

    /**
     * Mapa zawiera liste DERow ktore podczas importu przekroczyly limit techniczny.
     * <p>
     * Key: der,
     * Value: lista plikow w ktorym wystapil problem dla danego dera
     */
    Map<String, List<String>> ders = new HashMap<>();

    /**
     * Jeżeli podczas importu wystąpi błąd z przekroczeniem limitu technicznego danego DERa
     * to do mapy dodawana jest nazwa DERa oraz nazwa pliku z nim związanego
     */
    public void addDerExceedTechnicalLimits(String derName, String filename) {
        Optional<Map.Entry<String, List<String>>> optionalDer = ders.entrySet().stream()
            .filter(der -> der.getKey().equals(derName)).findFirst();
        if (optionalDer.isPresent()) {
            ders.get(derName).add(filename);
        } else {
            ders.put(derName, new ArrayList<>(Arrays.asList(filename)));
        }
    }
}
