package pl.com.tt.flex.server.validator.kpi;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.com.tt.flex.model.service.dto.kpi.KpiDTO;
import pl.com.tt.flex.server.common.errors.ObjectValidationException;
import pl.com.tt.flex.server.validator.ObjectValidator;
import pl.com.tt.flex.server.web.rest.errors.BadRequestAlertException;

import java.util.Objects;

import static pl.com.tt.flex.server.web.rest.errors.ErrorConstants.KPI_REQUIRED_TO_SET_DATE_FILTER;
import static pl.com.tt.flex.server.web.rest.kpi.KpiResource.ENTITY_NAME;

@Component
@RequiredArgsConstructor
public class KpiValidator implements ObjectValidator<KpiDTO, Long> {

    public void checkCreated(KpiDTO kpiDTO) {
        if (kpiDTO.getId() != null) {
            throw new BadRequestAlertException("A new kpi cannot already have an ID", ENTITY_NAME, "idexists");
        }
        validDateFilters(kpiDTO);
    }

    private void validDateFilters(KpiDTO kpiDTO) {
        if (kpiDTO.getType().isDateFilter() && (Objects.isNull(kpiDTO.getDateFrom()) || Objects.isNull(kpiDTO.getDateTo()))) {
            throw new ObjectValidationException(
                "Not set required date filters: dateFrom and dateTo", KPI_REQUIRED_TO_SET_DATE_FILTER, ENTITY_NAME);
        }
    }
}
