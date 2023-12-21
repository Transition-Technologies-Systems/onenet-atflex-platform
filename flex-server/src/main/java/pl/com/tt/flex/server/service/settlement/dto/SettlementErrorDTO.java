package pl.com.tt.flex.server.service.settlement.dto;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Getter;

@Getter
public class SettlementErrorDTO {

    /**
     * Mapa zawiera liste bledow ktore wystapily dla danego pliku podczas walidacji
     * Key: kod błedu
     * Value: lista plikow w ktorym wystapił problem
     */
    private final Map<String, List<String>> invalidFiles = new HashMap<>();
    /**
     * Mapa zawiera liste bledow ktore wystapily dla danej aktywacji/rozliczenia
     * Key: kod błedu
     * Value: lista obiektów w ktorych wystąpił problem
     */
    private final Map<String, List<SettlementMinDTO>> invalidActivationSettlements = new HashMap<>();


    /**
     * Dodaje do mapy kod błędu wraz nazwą pliku w którym on wystąpił
     */
    public void addInvalidFile(String msgKey, String filename) {
        if (invalidFiles.containsKey(msgKey)) {
            invalidFiles.get(msgKey).add(filename);
        } else {
            invalidFiles.put(msgKey, new ArrayList<>(Arrays.asList(filename)));
        }
    }

    /**
     * jeżeli wystąpi bład np. DUPLICATE_SELF_SCHEDULE dla konkretnego planu pracy
     * to do mapy dodawana jest informacja ktory plan pracy wywołał błąd
     */
    public void addInvalidSettlement(String msgKey, SettlementMinDTO settlementMinDTO) {
        if (invalidActivationSettlements.containsKey(msgKey)) {
            invalidActivationSettlements.get(msgKey).add(settlementMinDTO);
        } else {
            invalidActivationSettlements.put(msgKey, Collections.singletonList(settlementMinDTO));
        }
    }
}
