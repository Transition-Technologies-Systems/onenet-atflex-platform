package pl.com.tt.flex.server.service.algorithm.agnoAlgorithm.capacity;

import pl.com.tt.flex.server.service.algorithm.agnoAlgorithm.dto.AgnoCouplingPointDTO;
import pl.com.tt.flex.server.service.algorithm.agnoAlgorithm.dto.AgnoHourNumberDTO;
import pl.com.tt.flex.model.service.dto.file.FileDTO;

import java.io.IOException;
import java.time.LocalDate;

public interface AgnoPbcmFileGenerator {

    FileDTO getPbcmFile(AgnoCouplingPointDTO couplingPoint, AgnoHourNumberDTO hourNumber, LocalDate deliveryDate) throws IOException;
}
