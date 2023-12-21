package pl.com.tt.flex.server.dataexport.exporter;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import pl.com.tt.flex.model.service.dto.localization.LocalizationTypeDTO;
import pl.com.tt.flex.server.dataexport.factory.DataExporterFormat;
import pl.com.tt.flex.server.dataexport.util.CellValueHelper;
import pl.com.tt.flex.server.domain.screen.enumeration.Screen;
import pl.com.tt.flex.server.service.user.config.screen.UserScreenConfigService;
import pl.com.tt.flex.server.service.user.config.screen.dto.ScreenColumnDTO;
import pl.com.tt.flex.server.service.subportfolio.dto.SubportfolioDTO;

import java.util.List;
import java.util.Optional;

@Component
public class SubportfolioDataExporter extends AbstractDataExporter<SubportfolioDTO> implements DataExporter<SubportfolioDTO> {

    private int orderNr = 1;
    private final List<ScreenColumnDTO> defaultColumnList = List.of(
        new ScreenColumnDTO("id", true, orderNr++, true),
        new ScreenColumnDTO("name", true, orderNr++, true),
        new ScreenColumnDTO("numberOfDers", true, orderNr++, true),
        new ScreenColumnDTO("combinedPowerOfDers", true, orderNr++, true),
        new ScreenColumnDTO("couplingPointIdTypes", true, orderNr++, true),
        new ScreenColumnDTO("mrid", true, orderNr++, true),
        new ScreenColumnDTO("fspa.representative.companyName", true, orderNr++, true),
        new ScreenColumnDTO("createdDate", true, orderNr++, true),
        new ScreenColumnDTO("lastModifiedDate", true, orderNr++, true),
        new ScreenColumnDTO("validFrom", true, orderNr++, true),
        new ScreenColumnDTO("validTo", true, orderNr++, true),
        new ScreenColumnDTO("active", true, orderNr++, true),
        new ScreenColumnDTO("certified", true, orderNr++, true)
    );

    protected SubportfolioDataExporter(UserScreenConfigService userScreenConfigService,
                                       MessageSource messageSource, CellValueHelper cellValueHelper) {
        super(userScreenConfigService, messageSource, cellValueHelper);
    }

    @Override
    public boolean supportFormat(DataExporterFormat format) {
        return DataExporterFormat.XLSX.equals(format);
    }

    @Override
    public boolean supportClass(Class clazz) {
        return clazz.equals(SubportfolioDTO.class);
    }

    @Override
    public boolean supportScreen(Screen screen) {
        return screen.equals(Screen.ADMIN_SUBPORTFOLIO) || screen.equals(Screen.USER_SUBPORTFOLIO);
    }

    @Override
    public String getPrefix() {
        return "exporter.subportfolio.";
    }

    /**
     * Na ten moment nie ma specjalnych wymagań do eksportu subportfolio w zależności od roli użytkownika,
     * lecz gdyby się takie pojawiły to można dodać własną listę kolumn.
     */
    @Override
    protected List<ScreenColumnDTO> getColumnList() {
        return defaultColumnList;
    }

    @Override
    protected Optional<String> notStandardColumnFill(String columnName, SubportfolioDTO data) {
        if (columnName.equals("couplingPointIdTypes")) {
            return Optional.of(data).map(SubportfolioDTO::getCouplingPointIdTypes).filter(list -> !list.isEmpty()).map(types -> types.get(0)).map(LocalizationTypeDTO::getName);
        }
        return Optional.empty();
    }
}
