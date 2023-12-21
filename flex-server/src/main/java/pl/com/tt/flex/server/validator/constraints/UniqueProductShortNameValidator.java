package pl.com.tt.flex.server.validator.constraints;

import lombok.AllArgsConstructor;
import pl.com.tt.flex.server.service.product.ProductService;
import pl.com.tt.flex.server.service.product.dto.ProductDTO;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Objects;

@AllArgsConstructor
public class UniqueProductShortNameValidator implements ConstraintValidator<UniqueProductShortName, ProductDTO> {

    private final ProductService productService;

    @Override
    public boolean isValid(ProductDTO productDTO, ConstraintValidatorContext context) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate("Product short name is not unique!").addPropertyNode("shortName").addConstraintViolation();

        if (Objects.isNull(productDTO.getId())) {
            return !productService.existsByShortName(productDTO.getShortName());
        }
        return !productService.existsByShortNameAndIdNot(productDTO.getShortName(), productDTO.getId());
    }
}
