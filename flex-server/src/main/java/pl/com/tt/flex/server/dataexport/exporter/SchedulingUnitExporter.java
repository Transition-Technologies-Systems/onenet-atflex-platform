package pl.com.tt.flex.server.dataexport.exporter;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import pl.com.tt.flex.model.service.dto.localization.LocalizationTypeDTO;
import pl.com.tt.flex.server.dataexport.factory.DataExporterFormat;
import pl.com.tt.flex.server.dataexport.util.CellValueHelper;
import pl.com.tt.flex.server.domain.screen.enumeration.Screen;
import pl.com.tt.flex.server.service.schedulingUnit.dto.SchedulingUnitDTO;
import pl.com.tt.flex.server.service.user.config.screen.UserScreenConfigService;
import pl.com.tt.flex.server.service.user.config.screen.dto.ScreenColumnDTO;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class SchedulingUnitExporter extends AbstractDataExporter<SchedulingUnitDTO> implements DataExporter<SchedulingUnitDTO> {

    private int orderNr = 1;
    private final List<ScreenColumnDTO> defaultColumnList = List.of(
        new ScreenColumnDTO("id", true, orderNr++, true),
        new ScreenColumnDTO("name", true, orderNr++, true),
        new ScreenColumnDTO("bsp.representative.companyName", true, orderNr++, true),
        new ScreenColumnDTO("schedulingUnitType.nlsCode", true, orderNr++, true),
        new ScreenColumnDTO("couplingPoints.name", true, orderNr++, true),
        new ScreenColumnDTO("primaryCouplingPoint.name", true, orderNr++, true),
        new ScreenColumnDTO("numberOfDers", true, orderNr++, true),
        new ScreenColumnDTO("createdDate", true, orderNr++, true),
        new ScreenColumnDTO("lastModifiedDate", true, orderNr++, true),
        new ScreenColumnDTO("active", true, orderNr++, true),
        new ScreenColumnDTO("readyForTests", true, orderNr++, true),
        new ScreenColumnDTO("certified", true, orderNr++, true)
    );

    protected SchedulingUnitExporter(UserScreenConfigService userScreenConfigService, MessageSource messageSource,
                                     CellValueHelper cellValueHelper) {
        super(userScreenConfigService, messageSource, cellValueHelper);
    }

    @Override
    public boolean supportFormat(DataExporterFormat format) {
        return DataExporterFormat.XLSX.equals(format);
    }

    @Override
    public boolean supportClass(Class clazz) {
        return clazz.equals(SchedulingUnitDTO.class);
    }

    @Override
    public boolean supportScreen(Screen screen) {
        return screen.equals(Screen.ADMIN_SCHEDULING_UNITS) || screen.equals(Screen.USER_SCHEDULING_UNITS);
    }

    @Override
    public String getPrefix() {
        return "exporter.scheduling.unit.";
    }

    /**
     * Na ten moment nie ma specjalnych wymagań do eksportu jednostek grafikowych
     * w zależności od roli użytkownika, lecz gdyby się takie pojawiły to można dodać własną listę kolumn.
     */
    @Override
    protected List<ScreenColumnDTO> getColumnList() {
        return defaultColumnList;
    }

    @Override
    protected Optional<String> notStandardColumnFill(String columnName, SchedulingUnitDTO data) {
        if (columnName.equals("couplingPoints.name")) {
            return Optional.of(data).map(SchedulingUnitDTO::getCouplingPoints).filter(list -> !list.isEmpty()).map(points -> points.stream().map(LocalizationTypeDTO::getName).collect(Collectors.joining(", ")));
        }
        if (columnName.equals("schedulingUnitType.nlsCode")) {
            return Optional.of(data).map(SchedulingUnitDTO::getSchedulingUnitType).map(type -> locale.toLanguageTag().equals("pl") ? type.getDescriptionPl() : type.getDescriptionEn());
        }
        return Optional.empty();
    }
}
