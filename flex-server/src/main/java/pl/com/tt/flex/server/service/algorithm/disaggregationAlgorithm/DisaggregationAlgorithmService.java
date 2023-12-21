package pl.com.tt.flex.server.service.algorithm.disaggregationAlgorithm;

import java.io.IOException;

import org.springframework.web.multipart.MultipartFile;

import pl.com.tt.flex.server.common.errors.ObjectValidationException;
import pl.com.tt.flex.server.service.algorithm.agnoAlgorithm.factory.AgnoAlgorithm;

public interface DisaggregationAlgorithmService extends AgnoAlgorithm {

    void startOfferUpdateImport(MultipartFile multipartFile) throws ObjectValidationException, IOException;

}
