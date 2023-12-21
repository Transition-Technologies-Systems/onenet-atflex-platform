package pl.com.tt.flex.server.service.user.config.screen.dto;

import com.google.common.base.Objects;
import lombok.*;
import pl.com.tt.flex.server.domain.screen.enumeration.Screen;

import javax.validation.constraints.NotNull;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserScreenConfigDTO {

    private Long id;

    @NotNull
    private Screen screen;

    private Long userId;

    private List<ScreenColumnDTO> screenColumns;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        UserScreenConfigDTO that = (UserScreenConfigDTO) o;
        return screen == that.screen && Objects.equal(screenColumns, that.screenColumns);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(super.hashCode(), screen, screenColumns);
    }
}
