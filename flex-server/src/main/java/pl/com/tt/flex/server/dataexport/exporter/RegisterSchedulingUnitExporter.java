package pl.com.tt.flex.server.dataexport.exporter;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import pl.com.tt.flex.server.dataexport.factory.DataExporterFormat;
import pl.com.tt.flex.server.dataexport.util.CellValueHelper;
import pl.com.tt.flex.server.domain.screen.enumeration.Screen;
import pl.com.tt.flex.server.service.schedulingUnit.dto.SchedulingUnitDTO;
import pl.com.tt.flex.server.service.user.config.screen.UserScreenConfigService;
import pl.com.tt.flex.server.service.user.config.screen.dto.ScreenColumnDTO;

import java.util.List;
import java.util.Optional;

@Component
public class RegisterSchedulingUnitExporter extends AbstractDataExporter<SchedulingUnitDTO> implements DataExporter<SchedulingUnitDTO> {


    private int orderNr = 1;
    private final List<ScreenColumnDTO> defaultColumnList = List.of(
        new ScreenColumnDTO("id", true, orderNr++, true),
        new ScreenColumnDTO("bsp.representative.companyName", true, orderNr++, true),
        new ScreenColumnDTO("product.shortName", true, orderNr++, true),
        new ScreenColumnDTO("name", true, orderNr++, true),
        new ScreenColumnDTO("createdDate", true, orderNr++, true),
        new ScreenColumnDTO("lastModifiedDate", true, orderNr++, true),
        new ScreenColumnDTO("createdBy", true, orderNr++, true),
        new ScreenColumnDTO("lastModifiedBy", true, orderNr++, true)
    );

    protected RegisterSchedulingUnitExporter(UserScreenConfigService userScreenConfigService,
                                             MessageSource messageSource, CellValueHelper cellValueHelper) {
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
        return screen.equals(Screen.ADMIN_REGISTER_SCHEDULING_UNITS) || screen.equals(Screen.USER_REGISTER_SCHEDULING_UNITS);
    }

    @Override
    public String getPrefix() {
        return "exporter.register.scheduling.unit.";
    }

    /**
     * Na ten moment nie ma specjalnych wymagań do eksportu zarejestrowanych jednostek grafikowych
     * w zależności od roli użytkownika, lecz gdyby się takie pojawiły to można dodać własną listę kolumn.
     */
    @Override
    protected List<ScreenColumnDTO> getColumnList() {
        return defaultColumnList;
    }

    @Override
    protected Optional<String> notStandardColumnFill(String columnName, SchedulingUnitDTO data) {
        return Optional.empty();
    }
}
