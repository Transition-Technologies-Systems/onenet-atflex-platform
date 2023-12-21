package pl.com.tt.flex.model.service.dto.potential;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import pl.com.tt.flex.model.service.dto.audit.AbstractAuditingDTO;
import pl.com.tt.flex.model.service.dto.fsp.FspCompanyMinDTO;
import pl.com.tt.flex.model.service.dto.product.type.ProductBidSizeUnit;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class FlexPotentialMinDTO extends AbstractAuditingDTO {

	private Long id;
	private FspCompanyMinDTO fsp;
	private BigDecimal volume;
	private ProductBidSizeUnit volumeUnit;
	private String productName;
	private Integer fullActivationTime;
	private Integer minDeliveryDuration;

	public FlexPotentialMinDTO(Long id, BigDecimal volume) {
		this.id = id;
		this.volume = volume;
	}

	public FlexPotentialMinDTO(Long id, BigDecimal volume, ProductBidSizeUnit volumeUnit, String productName, Integer fullActivationTime, Integer minDeliveryDuration) {
		this.id = id;
		this.volume = volume;
		this.volumeUnit = volumeUnit;
		this.productName = productName;
		this.fullActivationTime = fullActivationTime;
		this.minDeliveryDuration = minDeliveryDuration;
	}
}
