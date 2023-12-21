package pl.com.tt.flex.server.service.unit.selfSchedule;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;
import pl.com.tt.flex.model.service.dto.MinimalDTO;
import pl.com.tt.flex.model.service.dto.file.FileDTO;
import pl.com.tt.flex.server.common.errors.ObjectValidationException;
import pl.com.tt.flex.server.domain.unit.self_schedule.UnitSelfScheduleEntity;
import pl.com.tt.flex.server.repository.unit.selfSchedule.SelfScheduleRepository;
import pl.com.tt.flex.server.service.common.AbstractServiceImpl;
import pl.com.tt.flex.server.service.mapper.EntityMapper;
import pl.com.tt.flex.server.service.unit.dto.UnitMinDTO;
import pl.com.tt.flex.server.service.unit.selfSchedule.dto.UnitSelfScheduleDTO;
import pl.com.tt.flex.server.service.unit.selfSchedule.dto.UnitSelfScheduleErrorDTO;
import pl.com.tt.flex.server.service.unit.selfSchedule.dto.UnitSelfScheduleMinDTO;
import pl.com.tt.flex.server.service.unit.selfSchedule.mapper.UnitSelfScheduleMapper;
import pl.com.tt.flex.server.service.unit.selfSchedule.util.SelfScheduleUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static pl.com.tt.flex.server.web.rest.errors.ErrorConstants.DUPLICATE_SELF_SCHEDULE;
import static pl.com.tt.flex.server.web.rest.errors.ErrorConstants.SELF_SCHEDULE_TEMPLATE_INCORRECT;


/**
 * Service Implementation for managing {@link UnitSelfScheduleEntity}.
 */
@Service
@Transactional
@Slf4j
public class UnitSelfScheduleServiceImpl extends AbstractServiceImpl<UnitSelfScheduleEntity, UnitSelfScheduleDTO, Long> implements UnitSelfScheduleService {

    private final SelfScheduleRepository repository;
    private final UnitSelfScheduleMapper mapper;
    private final SelfScheduleUtils selfScheduleUtils;

    public UnitSelfScheduleServiceImpl(SelfScheduleRepository repository, UnitSelfScheduleMapper mapper, SelfScheduleUtils selfScheduleUtils) {
        this.repository = repository;
        this.mapper = mapper;
        this.selfScheduleUtils = selfScheduleUtils;
    }

    @Override
    @Transactional
    public void save(MultipartFile[] multipartFile, boolean isAdminTemplate) throws ObjectValidationException, IOException {
        List<UnitSelfScheduleDTO> selfScheduleFromFile = selfScheduleUtils.getSelfScheduleFromFiles(isAdminTemplate, multipartFile);
        selfScheduleFromFile.forEach(this::saveSelfScheduleVolumes);
    }

    private void saveSelfScheduleVolumes(UnitSelfScheduleDTO unitSelfScheduleDTO) {
        Optional<UnitSelfScheduleEntity> dbSelfScheduleDerOpt = repository.findBySelfScheduleDateAndUnitId(unitSelfScheduleDTO.getSelfScheduleDate(), unitSelfScheduleDTO.getUnit().getId());
        if (dbSelfScheduleDerOpt.isPresent()) {
            UnitSelfScheduleDTO dbSelfSchedule = mapper.toDto(dbSelfScheduleDerOpt.get());
            dbSelfSchedule.setVolumes(unitSelfScheduleDTO.getVolumes());
            save(dbSelfSchedule);
            log.debug("save() Update Self Schedule Entity [id:{}] with date {} for unit_id {}",
                unitSelfScheduleDTO.getId(), unitSelfScheduleDTO.getSelfScheduleDate(), unitSelfScheduleDTO.getUnit().getId());
        } else {
            save(unitSelfScheduleDTO);
            log.debug("save() Save new  Self Schedule Entity with date {} and unit_id {}",
                unitSelfScheduleDTO.getSelfScheduleDate(), unitSelfScheduleDTO.getUnit().getId());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public FileDTO getTemplate(boolean isAdminTemplate) throws IOException {
        String templatePath = selfScheduleUtils.getTemplatePath(isAdminTemplate);
        log.debug("getTemplate() Get Import SelfSchedule Template from path: {}", templatePath);
        ByteArrayOutputStream outputStream = selfScheduleUtils.fillSelfScheduleDataInTemplate(isAdminTemplate, templatePath);
        String extension = ".xlsx";
        return new FileDTO(selfScheduleUtils.getTemplateFilename() + extension, outputStream.toByteArray());
    }

    @Override
    @Transactional(readOnly = true)
    public UnitSelfScheduleDTO getDetail(Long id) {
        UnitSelfScheduleEntity entity = repository.findById(id).orElseThrow(() -> new RuntimeException("Cannot find Self Schedule with id: " + id));
        return mapper.toDto(entity);
    }

    /**
     * Jeżeli przynajmniej jeden plan pracy jest już obecnie zaimportowany do bazy to rzuca bład: DUPLICATE_SELF_SCHEDULE
     * W odpowiedzi z bledem do params dodawany jest obiekt {@link UnitSelfScheduleErrorDTO} ktory zawiera informacje ktore
     * plany pracy spowodowały błąd.
     */
    public void throwExceptionIfExistSelfSchedule(MultipartFile[] multipartFile, boolean isAdminTemplate) throws IOException, ObjectValidationException {
        List<UnitSelfScheduleDTO> selfScheduleFromFiles = selfScheduleUtils.getSelfScheduleFromFiles(isAdminTemplate, multipartFile);
        List<UnitSelfScheduleMinDTO> existSelfSchedule = findExistSelfSchedule(selfScheduleFromFiles);
        if (!CollectionUtils.isEmpty(existSelfSchedule)) {
            log.debug("throwExceptionIfExistSelfSchedule() Found {} exists self schedule in db", existSelfSchedule.size());
            UnitSelfScheduleErrorDTO unitSelfScheduleErrorDTO = new UnitSelfScheduleErrorDTO();
            unitSelfScheduleErrorDTO.getInvalidSelfSchedule().put(DUPLICATE_SELF_SCHEDULE, existSelfSchedule.stream().distinct().collect(Collectors.toList()));
            throw new ObjectValidationException("Self-schedule is exist for date", SELF_SCHEDULE_TEMPLATE_INCORRECT, selfScheduleUtils.selfScheduleErrorToJson(unitSelfScheduleErrorDTO));
        }
    }

    /**
     * Metoda znajduje plany pracy ktore już zostały zaimportowane do bazy
     */
    private List<UnitSelfScheduleMinDTO> findExistSelfSchedule(List<UnitSelfScheduleDTO> selfScheduleFromFiles) {
        List<UnitSelfScheduleMinDTO> existSelfSchedule = new ArrayList<>();
        selfScheduleFromFiles.forEach(selfScheduleDTO -> {
            if (repository.findBySelfScheduleDateAndUnitId(selfScheduleDTO.getSelfScheduleDate(), selfScheduleDTO.getUnit().getId()).isPresent()) {
                existSelfSchedule.add(selfScheduleUtils.toMinDTO(selfScheduleDTO));
            }
        });
        return existSelfSchedule;
    }

    @Override
    @SneakyThrows
    @Transactional(readOnly = true)
    public Optional<UnitSelfScheduleDTO> findByDateAndUnitId(Instant date, Long unitId) {
        Optional<UnitSelfScheduleEntity> selfScheduleOpt = repository.findBySelfScheduleDateAndUnitId(date, unitId);
        if (selfScheduleOpt.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(mapper.toDto(selfScheduleOpt.get()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<MinimalDTO<String, BigDecimal>> findVolumesForDerAndSelfScheduleDate(Long derId, Instant selfScheduleDate) {
        Optional<UnitSelfScheduleDTO> selfScheduleOpt = findByDateAndUnitId(selfScheduleDate, derId);
        if (selfScheduleOpt.isEmpty()) {
            return new ArrayList<>();
        }
        return selfScheduleOpt.get().getVolumes();
    }

    @Transactional(readOnly = true)
    public Map<UnitMinDTO, Map<String, BigDecimal>> findVolumesForDersAndDateMap(List<Long> derId, Instant selfScheduleDate) {
        return repository.findAllBySelfScheduleDateAndUnitIdIn(selfScheduleDate, derId).stream()
            .map(mapper::toDto)
            .collect(Collectors.toMap(UnitSelfScheduleDTO::getUnit, s -> s.getVolumes().stream().collect(Collectors.toMap(MinimalDTO::getId, MinimalDTO::getValue))));
    }

    @Transactional(readOnly = true)
    public Map<Long, List<MinimalDTO<String, BigDecimal>>> findVolumesForDersAndSelfScheduleDate(List<Long> derId, Instant selfScheduleDate) {
        return repository.findAllBySelfScheduleDateAndUnitIdIn(selfScheduleDate, derId).stream()
            .map(mapper::toDto)
            .collect(Collectors.toMap(s->s.getUnit().getId(), UnitSelfScheduleDTO::getVolumes));
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, List<MinimalDTO<String, BigDecimal>>> findVolumesForOffer(Long offerId) {
        return repository.findByOfferId(offerId).stream()
            .map(mapper::toDto)
            .map(schedule -> Pair.of(schedule.getUnit().getId(), schedule.getVolumes()))
            .collect(Collectors.toMap(Pair::getKey, Pair::getValue));
    }

    @Override
    public SelfScheduleRepository getRepository() {
        return repository;
    }

    @Override
    public EntityMapper<UnitSelfScheduleDTO, UnitSelfScheduleEntity> getMapper() {
        return mapper;
    }
}
