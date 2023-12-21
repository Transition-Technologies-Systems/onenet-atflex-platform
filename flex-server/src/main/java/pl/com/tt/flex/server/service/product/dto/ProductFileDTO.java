package pl.com.tt.flex.server.service.product.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.com.tt.flex.server.domain.product.ProductFileEntity;
import pl.com.tt.flex.model.service.dto.audit.AbstractAuditingDTO;
import pl.com.tt.flex.model.service.dto.file.FileDTO;

import java.io.Serializable;

/**
 * A DTO for the {@link ProductFileEntity} entity.
 */

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ProductFileDTO extends AbstractAuditingDTO implements Serializable {

    private Long id;

    private FileDTO fileDTO;

    private Long productId;

    public ProductFileDTO(FileDTO fileDTO) {
        this.fileDTO = fileDTO;
    }
}
