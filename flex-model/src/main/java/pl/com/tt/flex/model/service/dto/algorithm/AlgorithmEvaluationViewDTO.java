package pl.com.tt.flex.model.service.dto.algorithm;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.com.tt.flex.model.service.dto.auction.offer.AuctionOfferMinDTO;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class AlgorithmEvaluationViewDTO implements Serializable {
	private Long evaluationId;
	private String kdmModelName;
	private AlgorithmType typeOfAlgorithm;
	private Instant deliveryDate;
	private Instant creationDate;
	private Instant endDate;
	private AlgorithmStatus status;
	private List<AuctionOfferMinDTO> offers;
}
