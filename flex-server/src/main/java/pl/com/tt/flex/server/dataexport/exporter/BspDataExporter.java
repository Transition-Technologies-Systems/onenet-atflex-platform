package pl.com.tt.flex.server.dataexport.exporter;

import static pl.com.tt.flex.server.domain.screen.enumeration.Screen.ADMIN_BSP;
import static pl.com.tt.flex.server.domain.screen.enumeration.Screen.USER_BSP;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import pl.com.tt.flex.server.dataexport.factory.DataExporterFormat;
import pl.com.tt.flex.server.dataexport.util.CellValueHelper;
import pl.com.tt.flex.server.domain.screen.enumeration.Screen;
import pl.com.tt.flex.server.service.fsp.dto.FspDTO;
import pl.com.tt.flex.server.service.user.config.screen.UserScreenConfigService;
import pl.com.tt.flex.server.service.user.config.screen.dto.ScreenColumnDTO;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Component
public class BspDataExporter extends AbstractDataExporter<FspDTO> implements DataExporter<FspDTO> {

    private int orderNr = 1;
    private final List<ScreenColumnDTO> defaultColumnList = List.of(
        new ScreenColumnDTO("id", true, orderNr++, true),
        new ScreenColumnDTO("representative.companyName", true, orderNr++, true),
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
    private final Set<Screen> SUPPORTED_SCREENS = Set.of(ADMIN_BSP, USER_BSP);

    public BspDataExporter(UserScreenConfigService userScreenConfigService,
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
        return SUPPORTED_SCREENS.contains(screen);
    }

    @Override
    public String getPrefix() {
        return "exporter.bsp.";
    }

    /**
     * Na ten moment nie ma specjalnych wymagań do eksportu danych BSP w zależności od roli użytkownika,
     * lecz gdyby się takie pojawiły to można dodać własną listę kolumn.
     */
    @Override
    protected List<ScreenColumnDTO> getColumnList() {
        return defaultColumnList;
    }

    @Override
    protected Optional<String> notStandardColumnFill(String columnName, FspDTO data) {
        return Optional.empty();
    }
}
