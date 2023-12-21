package pl.com.tt.flex.server.dataexport.exporter;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import pl.com.tt.flex.server.dataexport.factory.DataExporterFormat;
import pl.com.tt.flex.server.dataexport.util.CellValueHelper;
import pl.com.tt.flex.server.domain.screen.enumeration.Screen;
import pl.com.tt.flex.server.service.product.dto.ProductDTO;
import pl.com.tt.flex.server.service.user.config.screen.UserScreenConfigService;
import pl.com.tt.flex.server.service.user.config.screen.dto.ScreenColumnDTO;

import java.util.List;
import java.util.Optional;

@Component
public class ProductDataExporter extends AbstractDataExporter<ProductDTO> implements DataExporter<ProductDTO> {

    private int orderNr = 1;
    private final List<ScreenColumnDTO> defaultColumnList = List.of(
        new ScreenColumnDTO("id", true, orderNr++, true),
        new ScreenColumnDTO("fullName", true, orderNr++, true),
        new ScreenColumnDTO("shortName", true, orderNr++, true),
        new ScreenColumnDTO("locational", true, orderNr++, true),
        new ScreenColumnDTO("minBidSize", true, orderNr++, true),
        new ScreenColumnDTO("maxBidSize", true, orderNr++, true),
        new ScreenColumnDTO("bidSizeUnit", true, orderNr++, true),
        new ScreenColumnDTO("direction", true, orderNr++, true),
        new ScreenColumnDTO("maxFullActivationTime", true, orderNr++, true),
        new ScreenColumnDTO("minRequiredDeliveryDuration", true, orderNr++, true),
        new ScreenColumnDTO("createdDate", true, orderNr++, true),
        new ScreenColumnDTO("lastModifiedDate", true, orderNr++, true),
        new ScreenColumnDTO("active", true, orderNr++, true),
        new ScreenColumnDTO("balancing", true, orderNr++, true),
        new ScreenColumnDTO("cmvc", true, orderNr++, true),
        new ScreenColumnDTO("validFrom", true, orderNr++, true),
        new ScreenColumnDTO("validTo", true, orderNr++, true)
    );

    protected ProductDataExporter(UserScreenConfigService userScreenConfigService,
                                  MessageSource messageSource, CellValueHelper cellValueHelper) {
        super(userScreenConfigService, messageSource, cellValueHelper);
    }

    @Override
    public boolean supportFormat(DataExporterFormat format) {
        return DataExporterFormat.XLSX.equals(format);
    }

    @Override
    public boolean supportClass(Class clazz) {
        return clazz.equals(ProductDTO.class);
    }

    @Override
    public boolean supportScreen(Screen screen) {
        return screen.equals(Screen.ADMIN_PRODUCTS) || screen.equals(Screen.USER_PRODUCTS);
    }

    @Override
    public String getPrefix() {
        return "exporter.product.";
    }

    /**
     * Na ten moment nie ma specjalnych wymagań do eksportu danych produktów w zależności
     * od roli użytkownika, lecz gdyby się takie pojawiły to można dodać własną listę kolumn.
     */
    @Override
    protected List<ScreenColumnDTO> getColumnList() {
        return defaultColumnList;
    }

    @Override
    protected Optional<String> notStandardColumnFill(String columnName, ProductDTO data) {
        return Optional.empty();
    }
}
