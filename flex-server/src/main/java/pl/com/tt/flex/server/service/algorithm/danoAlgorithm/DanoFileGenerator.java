package pl.com.tt.flex.server.service.algorithm.danoAlgorithm;

import pl.com.tt.flex.model.service.dto.file.FileDTO;
import pl.com.tt.flex.server.service.algorithm.agnoAlgorithm.dto.AgnoCouplingPointDTO;
import pl.com.tt.flex.server.service.algorithm.agnoAlgorithm.dto.AgnoHourNumberDTO;

import java.io.IOException;
import java.time.LocalDate;

public interface DanoFileGenerator {

    FileDTO getDanoFile(AgnoCouplingPointDTO couplingPoint, AgnoHourNumberDTO hourNumber, LocalDate deliveryDate) throws IOException;
}
