package pl.com.tt.flex.server.service.product.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import org.apache.commons.compress.utils.Lists;
import pl.com.tt.flex.model.service.dto.product.type.Direction;
import pl.com.tt.flex.model.service.dto.product.type.ProductBidSizeUnit;
import pl.com.tt.flex.server.domain.product.ProductEntity;
import pl.com.tt.flex.model.service.dto.audit.AbstractAuditingDTO;
import pl.com.tt.flex.model.service.dto.file.FileMinDTO;
import pl.com.tt.flex.server.validator.constraints.UniqueProductFullName;
import pl.com.tt.flex.server.validator.constraints.UniqueProductShortName;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * A DTO for the {@link ProductEntity} entity.
 */
@Getter
@Setter
@Builder
@UniqueProductShortName
@UniqueProductFullName
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ProductDTO extends AbstractAuditingDTO implements Serializable {

    private Long id;

    private Long version = 0L;

    @NotNull
    @Size(max = 255)
    private String fullName;

    @NotNull
    @Size(max = 50)
    private String shortName;

    @NotNull
    private boolean locational;

    /**
     * wartosc dziesietna z 1 miejscem po przecinku
     */
    @NotNull
    @ApiModelProperty(value = "wartosc dziesietna z 1 miejscem po przecinku", required = true)
    private BigDecimal minBidSize;

    /**
     * wartosc dziesietna z 1 miejscem po przecinku
     */
    @NotNull
    @ApiModelProperty(value = "wartosc dziesietna z 1 miejscem po przecinku", required = true)
    private BigDecimal maxBidSize;

    @NotNull
    private ProductBidSizeUnit bidSizeUnit;

    @NotNull
    @ApiModelProperty(value = "Maximum time for full activation of product in seconds")
    private Integer maxFullActivationTime;

    @NotNull
    @ApiModelProperty(value = "Minimum required duration of product delivery in minutes")
    private Integer minRequiredDeliveryDuration;

    @NotNull
    private boolean active;

    @NotNull
    private Instant validFrom;

    @NotNull
    private Instant validTo;

    private Long psoUserId;

    @NotNull
    private boolean balancing;

    @NotNull
    private boolean cmvc;

    @NotNull
    private Direction direction;

    private List<Long> ssoUserIds = Lists.newArrayList();

    private List<FileMinDTO> filesMinimal = Lists.newArrayList();

    private List<ProductFileDTO> files = Lists.newArrayList();

    private List<Long> removeFiles = Lists.newArrayList();

    private Instant firstVersionCreatedDate;

    private String firstVersionCreatedBy;
}
