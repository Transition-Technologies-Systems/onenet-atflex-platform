package pl.com.tt.flex.server.validator.constraints;

import lombok.AllArgsConstructor;
import pl.com.tt.flex.server.service.product.ProductService;
import pl.com.tt.flex.server.service.product.dto.ProductDTO;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Objects;

@AllArgsConstructor
public class UniqueProductFullNameValidator implements ConstraintValidator<UniqueProductFullName, ProductDTO> {

    private final ProductService productService;

    @Override
    public boolean isValid(ProductDTO productDTO, ConstraintValidatorContext context) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate("Product full name is not unique!").addPropertyNode("fullName").addConstraintViolation();

        if (Objects.isNull(productDTO.getId())) {
            return !productService.existsByFullName(productDTO.getFullName());
        }
        return !productService.existsByFullNameAndIdNot(productDTO.getFullName(), productDTO.getId());
    }
}
