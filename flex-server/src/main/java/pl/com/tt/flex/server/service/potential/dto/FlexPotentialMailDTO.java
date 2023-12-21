package pl.com.tt.flex.server.service.potential.dto;

import lombok.*;
import pl.com.tt.flex.server.service.product.dto.ProductDTO;
import pl.com.tt.flex.server.validator.constraints.UniqueProductFullName;
import pl.com.tt.flex.server.validator.constraints.UniqueProductShortName;

/**
 * A DTO used for storing edited flex potential data and sending them via e-mail
 */
@Getter
@Setter
@Builder
@UniqueProductShortName
@UniqueProductFullName
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class FlexPotentialMailDTO {
    private FlexPotentialDTO flexPotentialDTO;
    private ProductDTO productDTO;
    private String fspCompanyName;
    private String fspDers;
}
