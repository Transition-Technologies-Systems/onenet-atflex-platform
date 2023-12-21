package pl.com.tt.flex.server.service.user.config.screen.dto;

import lombok.*;

import javax.validation.constraints.NotNull;
import java.util.Objects;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ScreenColumnDTO {

    @NotNull
    private String columnName;

    @NotNull
    private boolean visible;

    private Integer orderNr;

    private boolean export;


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ScreenColumnDTO)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        ScreenColumnDTO that = (ScreenColumnDTO) o;
        return visible == that.visible &&
            Objects.equals(columnName, that.columnName) &&
            Objects.equals(orderNr, that.orderNr) &&
            Objects.equals(export, that.export);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), columnName, visible, orderNr, export);
    }
}
