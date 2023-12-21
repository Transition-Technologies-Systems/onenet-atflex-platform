package pl.com.tt.flex.server.service.algorithm.agnoAlgorithm.eneregy;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import pl.com.tt.flex.server.domain.algorithm.AlgorithmEvaluationEntity;
import pl.com.tt.flex.server.domain.auction.offer.da.AuctionOfferBandDataEntity;

public interface AgnoBmResultsFileReader {

    Map<List<AuctionOfferBandDataEntity>, Long> getBandsByOfferId(AlgorithmEvaluationEntity algEvaluation) throws IOException;

}
