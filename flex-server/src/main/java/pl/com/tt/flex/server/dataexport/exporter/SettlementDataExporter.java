package pl.com.tt.flex.server.dataexport.exporter;

import static pl.com.tt.flex.server.util.DateUtil.getSameDayDateRangeString;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import pl.com.tt.flex.server.dataexport.factory.DataExporterFormat;
import pl.com.tt.flex.server.dataexport.util.CellValueHelper;
import pl.com.tt.flex.server.domain.screen.enumeration.Screen;
import pl.com.tt.flex.server.service.settlement.dto.SettlementViewDTO;
import pl.com.tt.flex.server.service.user.config.screen.UserScreenConfigService;
import pl.com.tt.flex.server.service.user.config.screen.dto.ScreenColumnDTO;

@Component
public class SettlementDataExporter extends AbstractDataExporter<SettlementViewDTO> implements DataExporter<SettlementViewDTO> {

    private int orderNr = 1;
    public final List<ScreenColumnDTO> defaultColumnList = List.of(
        new ScreenColumnDTO("id", true, orderNr++, true),
        new ScreenColumnDTO("derName", true, orderNr++, true),
        new ScreenColumnDTO("offerId", true, orderNr++, true),
        new ScreenColumnDTO("auctionName", true, orderNr++, true),
        new ScreenColumnDTO("companyName", true, orderNr++, true),
        new ScreenColumnDTO("acceptedDeliveryPeriod", true, orderNr++, true),
        new ScreenColumnDTO("acceptedVolume", true, orderNr++, true),
        new ScreenColumnDTO("activatedVolume", true, orderNr++, true),
        new ScreenColumnDTO("settlementAmount", true, orderNr++, true)
    );

    public static final String PREFIX = "exporter.settlement.";

    protected SettlementDataExporter(UserScreenConfigService userScreenConfigService,
                                  MessageSource messageSource, CellValueHelper cellValueHelper) {
        super(userScreenConfigService, messageSource, cellValueHelper);
    }

    @Override
    protected Optional<String> notStandardColumnFill(String columnName, SettlementViewDTO data) {
        if (columnName.equals("acceptedDeliveryPeriod")) {
            return Optional.of(getSameDayDateRangeString(data.getAcceptedDeliveryPeriodFrom(), data.getAcceptedDeliveryPeriodTo()));
        }
        if (columnName.equals("acceptedVolume")) {
            String acceptedVolumeWithUnit = data.getAcceptedVolume().concat(" ").concat(data.getUnit());
            return Optional.of(acceptedVolumeWithUnit);
        }
        if (columnName.equals("activatedVolume") && Objects.nonNull(data.getActivatedVolume())) {
            String activatedVolumeWithUnit = data.getActivatedVolume().toString().concat(" ").concat(data.getUnit());
            return Optional.of(activatedVolumeWithUnit);
        }
        return Optional.empty();
    }

    @Override
    protected List<ScreenColumnDTO> getColumnList() {
        return defaultColumnList;
    }

    @Override
    public boolean supportFormat(DataExporterFormat format) {
        return DataExporterFormat.XLSX.equals(format);
    }

    @Override
    public boolean supportClass(Class clazz) {
        return clazz.equals(SettlementViewDTO.class);
    }

    @Override
    public boolean supportScreen(Screen screen) {
        return screen.equals(Screen.ADMIN_SETTLEMENT) || screen.equals(Screen.USER_SETTLEMENT);
    }

    @Override
    public String getPrefix() {
        return PREFIX;
    }

}
