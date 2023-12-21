package pl.com.tt.flex.server.service.product.dto;

import lombok.*;
import pl.com.tt.flex.server.validator.constraints.UniqueProductFullName;
import pl.com.tt.flex.server.validator.constraints.UniqueProductShortName;

/**
 * A DTO used for storing edited product data and sending them via e-mail
 */
@Getter
@Setter
@Builder
@UniqueProductShortName
@UniqueProductFullName
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ProductMailDTO {
    private ProductDTO productDTO;
    private String psoUser;
    private String ssoUsers;
}
