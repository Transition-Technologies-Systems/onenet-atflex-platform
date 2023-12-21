package pl.com.tt.flex.server.service.unit.selfSchedule.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.com.tt.flex.model.service.dto.MinimalDTO;
import pl.com.tt.flex.model.service.dto.audit.AbstractAuditingDTO;
import pl.com.tt.flex.server.domain.unit.self_schedule.UnitSelfScheduleEntity;
import pl.com.tt.flex.server.service.fsp.dto.FspDTO;
import pl.com.tt.flex.server.service.unit.dto.UnitMinDTO;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * A DTO for the {@link UnitSelfScheduleEntity} entity.
 */

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class UnitSelfScheduleDTO extends AbstractAuditingDTO implements Serializable {

    private Long id;

    private Instant selfScheduleDate;

    private FspDTO fsp;

    private UnitMinDTO unit;

    List<MinimalDTO<String, BigDecimal>> volumes = new ArrayList<>();
}
