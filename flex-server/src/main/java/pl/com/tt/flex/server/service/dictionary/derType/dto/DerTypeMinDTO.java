package pl.com.tt.flex.server.service.dictionary.derType.dto;

import lombok.*;
import pl.com.tt.flex.server.domain.unit.DerTypeEntity;
import pl.com.tt.flex.server.domain.unit.enumeration.DerType;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * A Minimal DTO for the {@link DerTypeEntity} entity.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class DerTypeMinDTO implements Serializable {

    private Long id;

    @NotNull
    private DerType type;

    private String descriptionEn;

    private String descriptionPl;

    private String key;

    private String nlsCode;

    public DerTypeMinDTO(Long id, DerType type, String descriptionEn) {
        this.id = id;
        this.type = type;
        this.descriptionEn = descriptionEn;
    }
}
