package pl.com.tt.flex.model.service.dto.auction.offer.da;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.com.tt.flex.model.security.permission.Authority;
import pl.com.tt.flex.model.security.permission.ViewWithAuthority;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class AuctionOfferBandDataDTO implements Serializable {
	private Long id;
	@NotNull
	private String hourNumber;
	@NotNull
	private int bandNumber;
	@NotNull
	private BigDecimal volume;
	private BigDecimal acceptedVolume;
	private BigDecimal volumeTransferredToBM;
	@ViewWithAuthority(values = {Authority.FLEX_ADMIN_VIEW_PRICES, Authority.FLEX_USER_AUCTIONS_VIEW_PRICES})
	private BigDecimal price;
	@ViewWithAuthority(values = {Authority.FLEX_ADMIN_VIEW_PRICES, Authority.FLEX_USER_AUCTIONS_VIEW_PRICES})
	private BigDecimal acceptedPrice;

	@JsonProperty("isEdited")    // istnieje bug w serializacji booleanów, bez tej adnotacji spring wycina "is"
	private boolean isEdited;
}
