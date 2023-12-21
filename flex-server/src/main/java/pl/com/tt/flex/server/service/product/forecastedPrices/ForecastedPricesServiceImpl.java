package pl.com.tt.flex.server.service.product.forecastedPrices;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;
import pl.com.tt.flex.model.service.dto.file.FileDTO;
import pl.com.tt.flex.server.common.errors.ObjectValidationException;
import pl.com.tt.flex.server.domain.product.ForecastedPricesEntity;
import pl.com.tt.flex.server.repository.product.forecastedPrices.ForecastedPricesRepository;
import pl.com.tt.flex.server.service.common.AbstractServiceImpl;
import pl.com.tt.flex.server.service.mapper.EntityMapper;
import pl.com.tt.flex.server.service.product.forecastedPrices.dto.ForecastedPricesDTO;
import pl.com.tt.flex.server.service.product.forecastedPrices.dto.ForecastedPricesErrorDTO;
import pl.com.tt.flex.server.service.product.forecastedPrices.dto.ForecastedPricesMinDTO;
import pl.com.tt.flex.server.service.product.forecastedPrices.mapper.ForecastedPricesMapper;
import pl.com.tt.flex.server.service.product.forecastedPrices.utils.ForecastedPricesUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


/**
 * Service Implementation for managing {@link ForecastedPricesEntity}.
 */
@Service
@Transactional
@Slf4j
public class ForecastedPricesServiceImpl extends AbstractServiceImpl<ForecastedPricesEntity, ForecastedPricesDTO, Long> implements ForecastedPricesService {

    private static final String DUPLICATE_FORECASTED_PRICES = "DUPLICATE_FORECASTED_PRICES";

    private final ForecastedPricesRepository repository;
    private final ForecastedPricesMapper mapper;
    private final ForecastedPricesUtils forecastedPricesUtils;


    public ForecastedPricesServiceImpl(ForecastedPricesRepository repository, ForecastedPricesMapper mapper,
        ForecastedPricesUtils forecastedPricesUtils) {
        this.repository = repository;
        this.mapper = mapper;
        this.forecastedPricesUtils = forecastedPricesUtils;
    }

    @Override
    @Transactional
    public void save(MultipartFile[] multipartFiles) throws ObjectValidationException, IOException {
        List<ForecastedPricesDTO> forecastedPricesFromFiles = forecastedPricesUtils.getForecastedPricesFromFiles(multipartFiles);
        forecastedPricesFromFiles.forEach(this::saveForecastedPrices);
    }

    private void saveForecastedPrices(ForecastedPricesDTO forecastedPricesDTO) {
        Optional<ForecastedPricesEntity> dbForecastedPricesOpt = repository.findByForecastedPricesDateAndProductId(forecastedPricesDTO.getForecastedPricesDate(), forecastedPricesDTO.getProduct().getId());
        if (dbForecastedPricesOpt.isPresent()) {
            ForecastedPricesDTO dbForecastedPrices = mapper.toDto(dbForecastedPricesOpt.get());
            dbForecastedPrices.setPrices(forecastedPricesDTO.getPrices());
            save(dbForecastedPrices);
            log.debug("save() Update forecasted prices entity [id:{}] with date {} for product_id {}", forecastedPricesDTO.getId(),
                forecastedPricesDTO.getForecastedPricesDate(), forecastedPricesDTO.getProduct().getId());
        } else {
            save(forecastedPricesDTO);
            log.debug("save() Save new forecasted prices entity with date {} for product_id {}",
                forecastedPricesDTO.getForecastedPricesDate(), forecastedPricesDTO.getProduct().getId());
        }
    }

    @Override
    public FileDTO getTemplate() throws IOException {
        String templatePath = forecastedPricesUtils.getTemplatePath();
        log.debug("getTemplate() Get Import ForecastedPrices Template from path: {}", templatePath);
        ByteArrayOutputStream outputStream = forecastedPricesUtils.fillSelfScheduleDataInTemplate(templatePath);
        String extension = ".xlsx";
        return new FileDTO(forecastedPricesUtils.getTemplateFilename() + extension, outputStream.toByteArray());
    }

    @Override
    public void throwExceptionIfExistForecastedPrices(MultipartFile[] multipartFiles) throws IOException, ObjectValidationException {
        List<ForecastedPricesDTO> forecastedPricesFromFiles = forecastedPricesUtils.getForecastedPricesFromFiles(multipartFiles);
        List<ForecastedPricesMinDTO> existingForecastedPrices = findExistingForecastedPrices(forecastedPricesFromFiles);
        if (!CollectionUtils.isEmpty(existingForecastedPrices)) {
            log.debug("throwExceptionIfExistForecastedPrices() Found {} existing forecasted prices in db", existingForecastedPrices.size());
            ForecastedPricesErrorDTO forecastedPricesErrorDTO = new ForecastedPricesErrorDTO();
            forecastedPricesErrorDTO.getInvalidForecastedPrices().put(DUPLICATE_FORECASTED_PRICES, existingForecastedPrices.stream().distinct().collect(Collectors.toList()));
            throw new ObjectValidationException("Forecasted prices already exist for date", DUPLICATE_FORECASTED_PRICES, forecastedPricesUtils.forecastedPricesErrorToJson(forecastedPricesErrorDTO));
        }
    }

    private List<ForecastedPricesMinDTO> findExistingForecastedPrices(List<ForecastedPricesDTO> forecastedPrices) {
        List<ForecastedPricesMinDTO> existingForecastedPrices = new ArrayList<>();
        forecastedPrices.forEach(forecastedPricesDTO -> {
            if (repository.findByForecastedPricesDateAndProductId(forecastedPricesDTO.getForecastedPricesDate(), forecastedPricesDTO.getProduct().getId()).isPresent()) {
                existingForecastedPrices.add(forecastedPricesUtils.toMinDTO(forecastedPricesDTO));
            }
        });
        return existingForecastedPrices;
    }

    @Override
    @Transactional(readOnly = true)
    public ForecastedPricesDTO getDetail(Long id) {
        ForecastedPricesEntity entity = repository.findById(id)
            .orElseThrow(() -> new RuntimeException("Cannot find Forecasted Prices with id: " + id));
        return mapper.toDto(entity);
    }

    @Override
    @SneakyThrows
    @Transactional(readOnly = true)
    public Optional<ForecastedPricesDTO> findByProductAndForecastedPriceDate(Long productId, Instant forecastedPriceDate) {
        Optional<ForecastedPricesEntity> forecastedPriceOpt = repository.findByForecastedPricesDateAndProductId(forecastedPriceDate, productId);
        if (forecastedPriceOpt.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(mapper.toDto(forecastedPriceOpt.get()));
    }

    @Transactional(readOnly = true)
    public boolean existForecastedPricesForProductAndDeliveryDate(Long productId, Instant forecastedPriceDate) {
        Optional<ForecastedPricesEntity> forecastedPriceOpt = repository.findByForecastedPricesDateAndProductId(forecastedPriceDate, productId);
        return forecastedPriceOpt.isPresent();
    }

    @Override
    public ForecastedPricesRepository getRepository() {
        return repository;
    }

    @Override
    public EntityMapper<ForecastedPricesDTO, ForecastedPricesEntity> getMapper() {
        return mapper;
    }
}
