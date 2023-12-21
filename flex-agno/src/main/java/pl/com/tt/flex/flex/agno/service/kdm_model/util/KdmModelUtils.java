package pl.com.tt.flex.flex.agno.service.kdm_model.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pl.com.tt.flex.flex.agno.util.TimestampFileUtil;
import pl.com.tt.flex.model.service.dto.file.FileDTO;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class KdmModelUtils {

    /**
     * Pobranie listy stacji transformatorowych/punktów przyłączenia do sieci nn na podstawie wybranego obszaru
     *
     * @param fileDTO - plik kdm
     * @return lista stacji transformatorowych/punktów przyłączenia do sieci nn
     */
    public static List<String> getPowerStationsFromKdm(FileDTO fileDTO) {
        log.debug("getPowerStationsFromKdm() Start - get list of stations from kdm {}", fileDTO.getFileName());
        String startLine = "WEZLY";
        String endLine = "GALEZIE";
        List<String> stations = new ArrayList<>();
        try (BufferedReader br = TimestampFileUtil.getFileBufferedReader(fileDTO)) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.contains(startLine)) {
                    while ((line = br.readLine()) != null && !line.contains(endLine)) {
                        String[] lineWithCouplingPoints = line.split("\\s+");
                        String couplingPoint = lineWithCouplingPoints[0];
                        stations.add(couplingPoint);
                        log.debug("getPowerStationsFromKdm() Add stations: {}", couplingPoint);
                    }
                    break;
                }
            }
        } catch (IOException e) {
            log.debug("getPowerStationsFromKdm() Problem with get list of stations from kdm {}. Exception msg: {}", fileDTO.getFileName(), e.getMessage());
            e.printStackTrace();
        }
        log.debug("getPowerStationsFromKdm() End - get list of stations from kdm {}. Found {} stations.", fileDTO.getFileName(), stations.size());
        return stations;
    }

}
