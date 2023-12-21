package pl.com.tt.flex.server.validator.common;

import static pl.com.tt.flex.server.domain.activityMonitor.ActivityEvent.SELF_SCHEDULED_CREATED_ERROR;
import static pl.com.tt.flex.server.web.rest.errors.ErrorConstants.FILE_EXTENSION_NOT_SUPPORTED;
import static pl.com.tt.flex.server.web.rest.errors.ErrorConstants.FROM_DATE_AFTER_TO_DATE;
import static pl.com.tt.flex.server.web.rest.errors.ErrorConstants.FROM_DATE_BEFORE_CREATED_DATE;
import static pl.com.tt.flex.server.web.rest.product.forecastedPrices.ForecastedPricesResourceAdmin.ENTITY_NAME;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Set;

import org.springframework.web.multipart.MultipartFile;

import lombok.extern.slf4j.Slf4j;
import pl.com.tt.flex.server.common.errors.ObjectValidationException;
import pl.com.tt.flex.server.domain.activityMonitor.ActivityEvent;
import pl.com.tt.flex.server.domain.common.enumeration.FileExtension;
import pl.com.tt.flex.server.service.common.dto.FileDTOUtil;

@Slf4j
public class CommonValidatorUtil {

    /**
     * Daty validFrom/validTo sprawdzamy dla pelnych godzin (11:00, 12:00 itd.)
     */
    public static void checkValidFromToDates(Instant validFrom, Instant validTo, Instant createdDate, String entityName, ActivityEvent activityEvent, Long entityId)
        throws ObjectValidationException {
        Instant validFromHours = validFrom.truncatedTo(ChronoUnit.HOURS);
        Instant validToHours = validTo.truncatedTo(ChronoUnit.HOURS);
        Instant createdDateHours = createdDate.truncatedTo(ChronoUnit.HOURS);
        if (validFromHours.isBefore(createdDateHours)) {
            throw new ObjectValidationException("ValidFrom is before createdDate", FROM_DATE_BEFORE_CREATED_DATE, entityName, activityEvent, entityId);
        }
        if (validFromHours.plus(1, ChronoUnit.HOURS).isAfter(validToHours)) {
            throw new ObjectValidationException("ValidTo has to be higher than validFrom", FROM_DATE_AFTER_TO_DATE, entityName, activityEvent, entityId);
        }
    }

    public static void checkFileExtensionValid(MultipartFile multipartFile, Set<FileExtension> supportedExtensions) throws ObjectValidationException {
        ObjectValidationException exception = new ObjectValidationException("Not supported file extension", FILE_EXTENSION_NOT_SUPPORTED,
            ENTITY_NAME, SELF_SCHEDULED_CREATED_ERROR, null);
        try {
            FileExtension fileExtension = FileDTOUtil.getFileExtension(multipartFile.getOriginalFilename());
            if (!supportedExtensions.contains(fileExtension)) {
                log.debug("checkFileExtensionValid() - Incorrect file extension");
                throw exception;
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw exception;
        }
    }
}
