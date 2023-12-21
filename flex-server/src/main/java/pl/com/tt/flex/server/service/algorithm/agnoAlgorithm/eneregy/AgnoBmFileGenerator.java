package pl.com.tt.flex.server.service.algorithm.agnoAlgorithm.eneregy;

import pl.com.tt.flex.server.service.algorithm.agnoAlgorithm.dto.AgnoCouplingPointDTO;
import pl.com.tt.flex.server.service.algorithm.agnoAlgorithm.dto.AgnoHourNumberDTO;
import pl.com.tt.flex.model.service.dto.file.FileDTO;

import java.io.IOException;
import java.time.LocalDate;

public interface AgnoBmFileGenerator {

    FileDTO getBmFile(AgnoCouplingPointDTO couplingPoint, AgnoHourNumberDTO hourNumber, LocalDate deliveryDate) throws IOException;
}
