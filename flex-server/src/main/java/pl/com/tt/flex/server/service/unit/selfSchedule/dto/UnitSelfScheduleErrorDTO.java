package pl.com.tt.flex.server.service.unit.selfSchedule.dto;

import lombok.Getter;

import java.util.*;

@Getter
public class UnitSelfScheduleErrorDTO {

    /**
     * Mapa zawiera liste bledow ktore wystapily dla danego pliku podczas walidacji
     * <p>
     * Key: kod błedu,
     * Value: lista plikow w ktorym wystapił problem
     */
    private final Map<String, List<String>> invalidFiles = new HashMap<>();
    /**
     * Mapa zawiera liste bledow ktore wystapily dla danego DER
     * <p>
     * Key: kod błedu,
     * Value: lista planow pracy w ktorym wystąpił problem
     */
    private final Map<String, List<UnitSelfScheduleMinDTO>> invalidSelfSchedule = new HashMap<>();


    /**
     * jeżeli wystąpi bład np. error.selfSchedule.templateIncorrect w pliku test.xlsx, test2.xlsx
     * to do mapy dodawane są nazwy plików informujące o tym w ktorych plikach został wykryty dany błąd
     */
    public void addInvalidFilename(String msgKey, String filename) {
        Optional<Map.Entry<String, List<String>>> msgKeyOpt = invalidFiles.entrySet().stream()
            .filter(invalidFile -> invalidFile.getKey().equals(msgKey))
            .findFirst();
        if (msgKeyOpt.isPresent()) {
            invalidFiles.get(msgKey).add(filename);
        } else {
            invalidFiles.put(msgKey, new ArrayList<>(Arrays.asList(filename)));
        }
    }

    /**
     * jeżeli wystąpi bład np. DUPLICATE_SELF_SCHEDULE dla konkretnego planu pracy
     * to do mapy dodawana jest informacja ktory plan pracy wywołał błąd
     */
    public void addInvalidSelfSchedules(String msgKey, UnitSelfScheduleMinDTO selfScheduleMinDTO) {
        Optional<Map.Entry<String, List<UnitSelfScheduleMinDTO>>> msgKeyOpt = invalidSelfSchedule.entrySet().stream()
            .filter(invalidFile -> invalidFile.getKey().equals(msgKey))
            .findFirst();
        if (msgKeyOpt.isPresent()) {
            invalidSelfSchedule.get(msgKey).add(selfScheduleMinDTO);
        } else {
            invalidSelfSchedule.put(msgKey, Collections.singletonList(selfScheduleMinDTO));
        }
    }
}
