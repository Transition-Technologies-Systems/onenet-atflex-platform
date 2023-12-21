package pl.com.tt.flex.model.service.dto.auction.offer;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import pl.com.tt.flex.model.security.permission.Role;
import pl.com.tt.flex.model.service.dto.auction.type.AuctionOfferStatus;
import pl.com.tt.flex.model.service.dto.auction.type.AuctionOfferType;
import pl.com.tt.flex.model.service.dto.auction.type.AuctionStatus;
import pl.com.tt.flex.model.service.dto.audit.AbstractAuditingDTO;

import javax.validation.constraints.NotNull;
import java.time.Instant;

@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
public abstract class AbstractAuctionOfferDTO extends AbstractAuditingDTO {

    private Long id;

    private AuctionOfferStatus status;
    private AuctionStatus auctionStatus;
    @NotNull
    private AuctionOfferType type;
    @NotNull
    private Boolean volumeDivisibility;
    @NotNull
    private Instant deliveryPeriodFrom;
    @NotNull
    private Instant deliveryPeriodTo;
    @NotNull
    private Boolean deliveryPeriodDivisibility;
    private Instant acceptedDeliveryPeriodFrom;
    private Instant acceptedDeliveryPeriodTo;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String auctionName;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String productId;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String productName;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String companyName;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Role role;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String fspId;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String offerCategory;
}
