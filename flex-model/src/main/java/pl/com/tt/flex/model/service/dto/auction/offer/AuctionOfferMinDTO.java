package pl.com.tt.flex.model.service.dto.auction.offer;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.com.tt.flex.model.service.dto.auction.type.AuctionOfferStatus;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AuctionOfferMinDTO {

	private Long id;

	private Long potentialId;
	//W przypadku Aukcji CMVC id potencjału, dla aukcji DA id SU

	private String potentialName;
	//Dla aukcji DA nazwa SU, dla aukcji CMVC zawsze null

	private String companyName;

	private String volume;

	private String price;

	private AuctionOfferStatus status;
}
