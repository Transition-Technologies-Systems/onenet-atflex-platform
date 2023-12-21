package pl.com.tt.flex.server.service.unit.selfSchedule.dto;

import lombok.*;

import java.io.Serializable;
import java.time.Instant;

@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@NoArgsConstructor
public class UnitSelfScheduleMinDTO implements Serializable {
    private String unitName;
    private String fspName;
    private Instant selfScheduleDate;
}
