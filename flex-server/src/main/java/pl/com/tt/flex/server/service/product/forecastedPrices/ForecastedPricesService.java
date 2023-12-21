package pl.com.tt.flex.server.service.product.forecastedPrices;

import org.springframework.web.multipart.MultipartFile;
import pl.com.tt.flex.server.common.errors.ObjectValidationException;
import pl.com.tt.flex.server.domain.product.ForecastedPricesEntity;
import pl.com.tt.flex.server.service.AbstractService;
import pl.com.tt.flex.model.service.dto.file.FileDTO;
import pl.com.tt.flex.server.service.product.forecastedPrices.dto.ForecastedPricesDTO;

import java.io.IOException;
import java.time.Instant;
import java.util.Optional;

/**
 * Service Interface for managing {@link ForecastedPricesEntity}.
 */
public interface ForecastedPricesService extends AbstractService<ForecastedPricesEntity, ForecastedPricesDTO, Long> {

    void save(MultipartFile[] multipartFiles) throws ObjectValidationException, IOException;

    FileDTO getTemplate() throws IOException;

    void throwExceptionIfExistForecastedPrices(MultipartFile[] multipartFile) throws IOException, ObjectValidationException;

    ForecastedPricesDTO getDetail(Long id) throws IOException;

    Optional<ForecastedPricesDTO> findByProductAndForecastedPriceDate(Long productId, Instant deliveryDateFromNow);

    boolean existForecastedPricesForProductAndDeliveryDate(Long productId, Instant forecastedPriceDate);
}
