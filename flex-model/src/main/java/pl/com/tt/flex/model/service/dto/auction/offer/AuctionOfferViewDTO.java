package pl.com.tt.flex.model.service.dto.auction.offer;

import java.util.List;

import lombok.*;
import pl.com.tt.flex.model.security.permission.Authority;
import pl.com.tt.flex.model.security.permission.ViewWithAuthority;
import pl.com.tt.flex.model.service.dto.der.DerMinDTO;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class AuctionOfferViewDTO extends AbstractAuctionOfferDTO {
	@ViewWithAuthority(values = {Authority.FLEX_ADMIN_VIEW_PRICES, Authority.FLEX_USER_AUCTIONS_VIEW_PRICES})
	private String price;
	private String volume;
	private Boolean volumeTooltipVisible;
	private String acceptedVolume;
	private Boolean acceptedVolumeTooltipVisible;
	private Long auctionId;
	private Integer verifiedVolumesPercent;
	private String ders;
	private String couplingPoint;
	private String powerStation;
	private String pointOfConnectionWithLV;
	private String schedulingUnitOrPotential;
	private List<DerMinDTO> derMinDTOs;
	private String flexibilityPotentialVolume;
}
