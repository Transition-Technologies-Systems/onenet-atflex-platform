package pl.com.tt.flex.model.service.dto.auction.offer.da;

import java.util.List;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.com.tt.flex.model.service.dto.auction.da.AuctionDayAheadMinDTO;
import pl.com.tt.flex.model.service.dto.auction.offer.AbstractAuctionOfferDTO;
import pl.com.tt.flex.model.service.dto.schedulingUnit.SchedulingUnitMinDTO;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AuctionDayAheadOfferDTO extends AbstractAuctionOfferDTO {

	@NotNull
	private AuctionDayAheadMinDTO auctionDayAhead;
	@NotNull
	private SchedulingUnitMinDTO schedulingUnit;
	@NotEmpty
	private List<AuctionOfferDersDTO> ders;
	private int verifiedVolumesPercent;
}
