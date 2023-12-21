package pl.com.tt.flex.server.dataexport.exporter;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import pl.com.tt.flex.model.security.permission.Role;
import pl.com.tt.flex.server.dataexport.factory.DataExporterFormat;
import pl.com.tt.flex.server.dataexport.util.CellValueHelper;
import pl.com.tt.flex.server.domain.screen.enumeration.Screen;
import pl.com.tt.flex.server.service.fsp.dto.FspDTO;
import pl.com.tt.flex.server.service.user.config.screen.UserScreenConfigService;
import pl.com.tt.flex.server.service.user.config.screen.dto.ScreenColumnDTO;

import java.util.List;
import java.util.Optional;

@Component
public class FspDataExporter extends AbstractDataExporter<FspDTO> implements DataExporter<FspDTO> {

    private int orderNr = 1;
    private final List<ScreenColumnDTO> defaultColumnList = List.of(
        new ScreenColumnDTO("id", true, orderNr++, true),
        new ScreenColumnDTO("representative.companyName", true, orderNr++, true),
        new ScreenColumnDTO("role", true, orderNr++, true),
        new ScreenColumnDTO("createdDate", true, orderNr++, true),
        new ScreenColumnDTO("lastModifiedDate", true, orderNr++, true),
        new ScreenColumnDTO("active", true, orderNr++, true),
        new ScreenColumnDTO("validFrom", true, orderNr++, true),
        new ScreenColumnDTO("validTo", true, orderNr++, true),
        new ScreenColumnDTO("representative.firstName", true, orderNr++, true),
        new ScreenColumnDTO("representative.lastName", true, orderNr++, true),
        new ScreenColumnDTO("representative.email", true, orderNr++, true),
        new ScreenColumnDTO("representative.phoneNumber", true, orderNr++, true)
    );

    public FspDataExporter(UserScreenConfigService userScreenConfigService,
                           MessageSource messageSource, CellValueHelper cellValueHelper) {
        super(userScreenConfigService, messageSource, cellValueHelper);
    }

    @Override
    public boolean supportFormat(DataExporterFormat format) {
        return format.equals(DataExporterFormat.XLSX);
    }

    @Override
    public boolean supportClass(Class clazz) {
        return clazz.equals(FspDTO.class);
    }

    @Override
    public boolean supportScreen(Screen screen) {
        return screen.equals(Screen.ADMIN_FSP);
    }

    @Override
    public String getPrefix() {
        return "exporter.fsp.";
    }

    /**
     * Na ten moment nie ma specjalnych wymagań do eksportu danych FSP w zależności
     * od roli użytkownika, lecz gdyby się takie pojawiły to można dodać własną listę kolumn.
     */
    @Override
    protected List<ScreenColumnDTO> getColumnList() {
        return defaultColumnList;
    }

    @Override
    protected Optional<String> notStandardColumnFill(String columnName, FspDTO data) {
        if (columnName.equals("role")) {
            return Optional.of(data).map(FspDTO::getRole).map(Role::toString).map(role -> messageSource.getMessage("exporter.role." + role, null, locale));
        }
        return Optional.empty();
    }
}
