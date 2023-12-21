package pl.com.tt.flex.server.dataexport.exporter;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import pl.com.tt.flex.server.dataexport.factory.DataExporterFormat;
import pl.com.tt.flex.server.dataexport.util.CellValueHelper;
import pl.com.tt.flex.server.domain.screen.enumeration.Screen;
import pl.com.tt.flex.server.service.potential.dto.FlexPotentialDTO;
import pl.com.tt.flex.server.service.unit.dto.UnitMinDTO;
import pl.com.tt.flex.server.service.user.config.screen.UserScreenConfigService;
import pl.com.tt.flex.server.service.user.config.screen.dto.ScreenColumnDTO;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class FlexPotentialDataExporter extends AbstractDataExporter<FlexPotentialDTO> implements DataExporter<FlexPotentialDTO> {

    private int orderNr = 1;
    private final List<ScreenColumnDTO> defaultColumnList = List.of(
        new ScreenColumnDTO("id", true, orderNr++, true),
        new ScreenColumnDTO("product.shortName", true, orderNr++, true),
        new ScreenColumnDTO("fsp.representative.companyName", true, orderNr++, true),
        new ScreenColumnDTO("units", true, orderNr++, true),
        new ScreenColumnDTO("volume", true, orderNr++, true),
        new ScreenColumnDTO("volumeUnit", true, orderNr++, true),
        new ScreenColumnDTO("divisibility", true, orderNr++, true),
        new ScreenColumnDTO("fullActivationTime", true, orderNr++, true),
        new ScreenColumnDTO("minDeliveryDuration", true, orderNr++, true),
        new ScreenColumnDTO("createdDate", true, orderNr++, true),
        new ScreenColumnDTO("lastModifiedDate", true, orderNr++, true),
        new ScreenColumnDTO("createdBy", true, orderNr++, true),
        new ScreenColumnDTO("lastModifiedBy", true, orderNr++, true),
        new ScreenColumnDTO("validFrom", true, orderNr++, true),
        new ScreenColumnDTO("validTo", true, orderNr++, true),
        new ScreenColumnDTO("active", true, orderNr++, true),
        new ScreenColumnDTO("productPrequalification", true, orderNr++, true),
        new ScreenColumnDTO("staticGridPrequalification", true, orderNr++, true)
    );

    protected FlexPotentialDataExporter(UserScreenConfigService userScreenConfigService,
                                        MessageSource messageSource, CellValueHelper cellValueHelper) {
        super(userScreenConfigService, messageSource, cellValueHelper);
    }

    @Override
    public boolean supportFormat(DataExporterFormat format) {
        return format.equals(DataExporterFormat.XLSX);
    }

    @Override
    public boolean supportClass(Class clazz) {
        return clazz.equals(FlexPotentialDTO.class);
    }

    @Override
    public boolean supportScreen(Screen screen) {
        return screen.equals(Screen.ADMIN_FLEXIBILITY_POTENTIALS)
            || screen.equals(Screen.ADMIN_FLEX_REGISTER)
            || screen.equals(Screen.USER_FLEXIBILITY_POTENTIALS)
            || screen.equals(Screen.USER_REGISTER_FLEXIBILITY_POTENTIALS);
    }

    @Override
    public String getPrefix() {
        return "exporter.flex.potential.";
    }

    /**
     * Na ten moment nie ma specjalnych wymagań do eksportu danych flex potential w zależności
     * od roli użytkownika, lecz gdyby się takie pojawiły to można dodać własną listę kolumn.
     */
    @Override
    protected List<ScreenColumnDTO> getColumnList() {
        return defaultColumnList;
    }

    @Override
    protected Optional<String> notStandardColumnFill(String columnName, FlexPotentialDTO data) {
        if (columnName.equals("units")) {
            return Optional.ofNullable(data.getUnits()).map(units -> units.stream().map(UnitMinDTO::getName).collect(Collectors.joining(", ")));
        }
        return Optional.empty();
    }
}
