package pl.com.tt.flex.server.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import pl.com.tt.flex.model.service.dto.dictionary.DictionaryType;
import pl.com.tt.flex.server.domain.audit.AbstractAuditingEntity;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;

@Getter
@Setter
@SuperBuilder
@MappedSuperclass
public abstract class AbstractDictionaryEntity extends AbstractAuditingEntity implements Serializable {

    @NotNull
    @Size(max = 100)
    @Column(name = "description_en", length = 100, nullable = false)
    private String descriptionEn;

    @Size(max = 100)
    @Column(name = "description_pl", length = 100, nullable = false)
    private String descriptionPl;

    public AbstractDictionaryEntity() {
    }

    public abstract DictionaryType getDictionaryType();
}
