package pl.com.tt.flex.server.service.product.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.com.tt.flex.server.domain.product.ProductEntity;

import java.io.Serializable;

/**
 * A DTO for the {@link ProductEntity} entity.
 */
@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
public class ProductNameMinDTO implements Serializable {

    private Long id;
    private String name;

    public ProductNameMinDTO(Long id, String name) {
        this.id = id;
        this.name = name;
    }
}
