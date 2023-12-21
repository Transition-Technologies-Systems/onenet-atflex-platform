package pl.com.tt.flex.server.dataexport.exporter;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import pl.com.tt.flex.model.service.dto.localization.LocalizationTypeDTO;
import pl.com.tt.flex.server.dataexport.factory.DataExporterFormat;
import pl.com.tt.flex.server.dataexport.util.CellValueHelper;
import pl.com.tt.flex.server.domain.screen.enumeration.Screen;
import pl.com.tt.flex.server.service.user.config.screen.UserScreenConfigService;
import pl.com.tt.flex.server.service.user.config.screen.dto.ScreenColumnDTO;
import pl.com.tt.flex.server.service.unit.dto.UnitDTO;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Component
public class UnitDataExporter extends AbstractDataExporter<UnitDTO> implements DataExporter<UnitDTO> {

    private int orderNr = 1;
    private final List<ScreenColumnDTO> defaultColumnList = List.of(
        new ScreenColumnDTO("id", true, orderNr++, true),
        new ScreenColumnDTO("name", true, orderNr++, true),
        new ScreenColumnDTO("code", true, orderNr++, true),
        new ScreenColumnDTO("directionOfDeviation", true, orderNr++, true),
        new ScreenColumnDTO("sourcePower", true, orderNr++, true),
        new ScreenColumnDTO("connectionPower", true, orderNr++, true),
        new ScreenColumnDTO("pMin", true, orderNr++, true),
        new ScreenColumnDTO("qMin", true, orderNr++, true),
        new ScreenColumnDTO("qMax", true, orderNr++, true),
        new ScreenColumnDTO("derType", true, orderNr++, true),
        new ScreenColumnDTO("sder", true, orderNr++, true),
        new ScreenColumnDTO("aggregated", true, orderNr++, true),
        new ScreenColumnDTO("fsp.representative.companyName", true, orderNr++, true),
        new ScreenColumnDTO("createdDate", true, orderNr++, true),
        new ScreenColumnDTO("lastModifiedDate", true, orderNr++, true),
        new ScreenColumnDTO("validFrom", true, orderNr++, true),
        new ScreenColumnDTO("validTo", true, orderNr++, true),
        new ScreenColumnDTO("subportfolio.name", true, orderNr++, true),
        new ScreenColumnDTO("schedulingUnit.name", true, orderNr++, true),
        new ScreenColumnDTO("ppe", true, orderNr++, true),
        new ScreenColumnDTO("powerStationTypes", true, orderNr++, true),
        new ScreenColumnDTO("couplingPointIdTypes", true, orderNr++, true),
        new ScreenColumnDTO("pointOfConnectionWithLvTypes", true, orderNr++, true),
        new ScreenColumnDTO("active", true, orderNr++, true),
        new ScreenColumnDTO("certified", true, orderNr++, true)
    );

    protected UnitDataExporter(UserScreenConfigService userScreenConfigService,
                               MessageSource messageSource, CellValueHelper cellValueHelper) {
        super(userScreenConfigService, messageSource, cellValueHelper);
    }

    @Override
    public boolean supportFormat(DataExporterFormat format) {
        return format.equals(DataExporterFormat.XLSX);
    }

    @Override
    public boolean supportClass(Class clazz) {
        return clazz.equals(UnitDTO.class);
    }

    @Override
    public boolean supportScreen(Screen screen) {
        return screen.equals(Screen.ADMIN_UNITS) || screen.equals(Screen.USER_UNITS);
    }

    @Override
    public String getPrefix() {
        return "exporter.unit.";
    }


    /**
     * Metoda zwracająca listę kolumn dla danego użytkownika. Na ten moment nie ma specjalnych wymagań do eksportu
     * zasobów w zależności od roli użytkownika, lecz gdyby się takie pojawiły to można dodać własną listę kolumn.
     */
    @Override
    protected List<ScreenColumnDTO> getColumnList() {
        return defaultColumnList;
    }

    @Override
    protected Optional<String> notStandardColumnFill(String columnName, UnitDTO data) {
        if (columnName.equals("pointOfConnectionWithLvTypes")) {
            return Optional.of(data).map(UnitDTO::getPointOfConnectionWithLvTypes).filter(list -> !list.isEmpty()).map(types -> types.get(0)).map(LocalizationTypeDTO::getName);
        }
        if (columnName.equals("couplingPointIdTypes")) {
            return Optional.of(data).map(UnitDTO::getCouplingPointIdTypes).filter(list -> !list.isEmpty()).map(types -> types.get(0)).map(LocalizationTypeDTO::getName);
        }
        if (columnName.equals("powerStationTypes")) {
            return Optional.of(data).map(UnitDTO::getPowerStationTypes).filter(list -> !list.isEmpty()).map(types -> types.get(0)).map(LocalizationTypeDTO::getName);
        }
        if (columnName.equals("derType")) {
            return getDerTypeString(data);
        }
        return Optional.empty();
    }

    private Optional<String> getDerTypeString(UnitDTO data) {
        Optional<String> storageType = Optional.of(data).map(UnitDTO::getDerTypeEnergyStorage).filter(Objects::nonNull).map(type -> locale.toLanguageTag().equals("pl") ?
            "Magazyn energii: " + type.getDescriptionPl() : "Energy storage: " + type.getDescriptionEn());
        Optional<String> generationType = Optional.of(data).map(UnitDTO::getDerTypeGeneration).filter(Objects::nonNull).map(type -> locale.toLanguageTag().equals("pl") ?
            "Generacja: " + type.getDescriptionPl() : "Generation: " + type.getDescriptionEn());
        Optional<String> receptionType = Optional.of(data).map(UnitDTO::getDerTypeReception).filter(Objects::nonNull).map(type -> locale.toLanguageTag().equals("pl") ?
            "Odbiór: " + type.getDescriptionPl() : "Reception: " + type.getDescriptionEn());
        StringBuilder derTypeStringBuilder = new StringBuilder();
        storageType.ifPresent(value -> derTypeStringBuilder.append(value).append("/ "));
        generationType.ifPresent(value -> derTypeStringBuilder.append(value).append("/ "));
        receptionType.ifPresent(value -> derTypeStringBuilder.append(value).append("/ "));
        int length = derTypeStringBuilder.length();
        if (length > 0) {
            derTypeStringBuilder.setLength(length - 2); // usuń ostatnie "/ "
        }
        return Optional.of(derTypeStringBuilder.toString());
    }
}
