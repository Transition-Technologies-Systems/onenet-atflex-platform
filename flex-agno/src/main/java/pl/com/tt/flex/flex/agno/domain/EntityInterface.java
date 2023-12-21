package pl.com.tt.flex.flex.agno.domain;

import java.io.Serializable;
import java.util.Objects;

public interface EntityInterface<ID extends Serializable> extends Serializable {

    ID getId();

    void setId(ID id);

    default boolean isNew() {
        return Objects.isNull(getId());
    }
}
