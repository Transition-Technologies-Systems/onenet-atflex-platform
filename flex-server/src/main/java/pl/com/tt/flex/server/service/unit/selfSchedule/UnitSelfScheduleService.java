package pl.com.tt.flex.server.service.unit.selfSchedule;

import org.springframework.web.multipart.MultipartFile;
import pl.com.tt.flex.model.service.dto.MinimalDTO;
import pl.com.tt.flex.model.service.dto.file.FileDTO;
import pl.com.tt.flex.server.common.errors.ObjectValidationException;
import pl.com.tt.flex.server.domain.unit.self_schedule.UnitSelfScheduleEntity;
import pl.com.tt.flex.server.service.AbstractService;
import pl.com.tt.flex.server.service.unit.dto.UnitMinDTO;
import pl.com.tt.flex.server.service.unit.selfSchedule.dto.UnitSelfScheduleDTO;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service Interface for managing {@link UnitSelfScheduleEntity}.
 */
public interface UnitSelfScheduleService extends AbstractService<UnitSelfScheduleEntity, UnitSelfScheduleDTO, Long> {

    void save(MultipartFile[] multipartFile, boolean isAdminTemplate) throws ObjectValidationException, IOException;

    FileDTO getTemplate(boolean isAdminTemplate) throws IOException;

    UnitSelfScheduleDTO getDetail(Long id) throws IOException;

    void throwExceptionIfExistSelfSchedule(MultipartFile[] multipartFile, boolean isAdminTemplate) throws IOException, ObjectValidationException;

    Optional<UnitSelfScheduleDTO> findByDateAndUnitId(Instant date, Long unitId);

    Map<UnitMinDTO, Map<String, BigDecimal>> findVolumesForDersAndDateMap(List<Long> derId, Instant selfScheduleDate);

    Map<Long, List<MinimalDTO<String, BigDecimal>>> findVolumesForDersAndSelfScheduleDate(List<Long> derId, Instant selfScheduleDate);

    List<MinimalDTO<String, BigDecimal>> findVolumesForDerAndSelfScheduleDate(Long derId, Instant selfScheduleDate);

    Map<Long, List<MinimalDTO<String, BigDecimal>>> findVolumesForOffer(Long offerId);
}
