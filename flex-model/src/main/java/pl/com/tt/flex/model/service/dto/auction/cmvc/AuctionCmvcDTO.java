package pl.com.tt.flex.model.service.dto.auction.cmvc;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import pl.com.tt.flex.model.service.dto.auction.offer.AuctionOfferMinDTO;
import pl.com.tt.flex.model.service.dto.auction.type.AuctionStatus;
import pl.com.tt.flex.model.service.dto.auction.type.AuctionType;
import pl.com.tt.flex.model.service.dto.localization.LocalizationTypeDTO;
import pl.com.tt.flex.model.service.dto.product.ProductMinDTO;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class AuctionCmvcDTO implements Serializable {

	private Long id;

	private String name;

	private AuctionStatus status;

	@JsonInclude(JsonInclude.Include.NON_NULL)
	private String productId;

	@JsonInclude(JsonInclude.Include.NON_NULL)
	private String productName;

	private AuctionType auctionType = AuctionType.CAPACITY;

	private String localizationMerged;

	private List<LocalizationTypeDTO> localization;

	@NotNull
	private Instant deliveryDateFrom;

	@NotNull
	private Instant deliveryDateTo;

	@NotNull
	private Instant gateOpeningTime;

	@NotNull
	private Instant gateClosureTime;

	private BigDecimal minDesiredPower;

	private BigDecimal maxDesiredPower;

	@JsonInclude(JsonInclude.Include.NON_NULL)
	private ProductMinDTO product;

	private String createdBy;

	private Instant createdDate;

	private String lastModifiedBy;

	private Instant lastModifiedDate;

	private String statusCode;

	private boolean canAddBid;

	private List<AuctionOfferMinDTO> offers;
}
