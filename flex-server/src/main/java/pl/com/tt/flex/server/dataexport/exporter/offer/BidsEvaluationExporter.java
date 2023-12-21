package pl.com.tt.flex.server.dataexport.exporter.offer;

import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import pl.com.tt.flex.model.service.dto.auction.offer.AuctionOfferViewDTO;
import pl.com.tt.flex.server.dataexport.exporter.AbstractDataExporter;
import pl.com.tt.flex.server.dataexport.exporter.DataExporter;
import pl.com.tt.flex.server.dataexport.exporter.offer.detail.enumeration.LevelOfDetail;
import pl.com.tt.flex.server.dataexport.exporter.offer.detail.factory.OfferDetailExporter;
import pl.com.tt.flex.server.dataexport.exporter.offer.detail.factory.OfferDetailExporterFactory;
import pl.com.tt.flex.server.dataexport.exporter.offer.detail.factory.impl.OfferDetailExporterFactoryImpl;
import pl.com.tt.flex.server.dataexport.factory.DataExporterFormat;
import pl.com.tt.flex.server.dataexport.util.CellValueHelper;
import pl.com.tt.flex.server.domain.screen.enumeration.Screen;
import pl.com.tt.flex.server.service.auction.da.AuctionDayAheadService;
import pl.com.tt.flex.server.service.user.config.screen.UserScreenConfigService;
import pl.com.tt.flex.server.service.user.config.screen.dto.ScreenColumnDTO;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.TimeZone;

@Component
@Slf4j
public class BidsEvaluationExporter extends AbstractDataExporter<AuctionOfferViewDTO> implements DataExporter<AuctionOfferViewDTO> {

    private final AuctionDayAheadService auctionDayAheadService;
    private final MessageSource messageSource;
    private final String offersSheetNameMessage = "exporter.bids.evaluation.offers.sheetName";

    private int orderNr = 1;
    final List<ScreenColumnDTO> defaultColumnList = List.of(
        new ScreenColumnDTO("id", true, orderNr++, true),
        new ScreenColumnDTO("auctionId", true, orderNr++, true),
        new ScreenColumnDTO("auctionName", true, orderNr++, true),
        new ScreenColumnDTO("productName", true, orderNr++, true),
        new ScreenColumnDTO("companyName", true, orderNr++, true),
        new ScreenColumnDTO("ders", true, orderNr++, true),
        new ScreenColumnDTO("couplingPoint", true, orderNr++, true),
        new ScreenColumnDTO("powerStation", true, orderNr++, true),
        new ScreenColumnDTO("pointOfConnectionWithLV", true, orderNr++, true),
        new ScreenColumnDTO("status", true, orderNr++, true),
        new ScreenColumnDTO("price", true, orderNr++, true),
        new ScreenColumnDTO("volume", true, orderNr++, true),
        new ScreenColumnDTO("volumeDivisibility", true, orderNr++, true),
        new ScreenColumnDTO("deliveryPeriod", true, orderNr++, true),
        new ScreenColumnDTO("deliveryPeriodDivisibility", true, orderNr++, true),
        new ScreenColumnDTO("acceptedVolume", true, orderNr++, true),
        new ScreenColumnDTO("acceptedDeliveryPeriod", true, orderNr++, true)
    );

    protected BidsEvaluationExporter(UserScreenConfigService userScreenConfigService, MessageSource messageSource,
                                     CellValueHelper cellValueHelper, @Lazy AuctionDayAheadService auctionDayAheadService) {
        super(userScreenConfigService, messageSource, cellValueHelper);
        this.auctionDayAheadService = auctionDayAheadService;
        this.messageSource = messageSource;
    }

    @Override
    protected byte[] getData(List<AuctionOfferViewDTO> valueToSave, Screen screen, LevelOfDetail detail) throws IOException {
        byte[] data = super.getData(valueToSave, screen, null);
        XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(data));
        workbook.setSheetName(0, messageSource.getMessage(offersSheetNameMessage, null, locale));
        OfferDetailExporterFactory offerDetailExporterFactory = new OfferDetailExporterFactoryImpl();
        OfferDetailExporter detailExporter = offerDetailExporterFactory.createOfferDetailExporter(auctionDayAheadService, messageSource, detail);
        return detailExporter.fillOfferDetailSheet(workbook, valueToSave, locale);
    }

    @Override
    public boolean supportFormat(DataExporterFormat format) {
        return format.equals(DataExporterFormat.XLSX);
    }

    @Override
    public boolean supportClass(Class clazz) {
        return clazz.equals(AuctionOfferViewDTO.class);
    }

    @Override
    public boolean supportScreen(Screen screen) {
        return screen.equals(Screen.ADMIN_BIDS_EVALUATION);
    }

    @Override
    public String getPrefix() {
        return "exporter.bids.evaluation.";
    }

    /**
     * Metoda zwracająca listę kolumn. Dla roli DSO nie eksportujemy kolumny Cena, dla reszty użytkowników eksportujemy
     * wszystkie kolumny.
     */
    @Override
    protected List<ScreenColumnDTO> getColumnList() {
        return defaultColumnList;
    }

    @Override
    protected Optional<String> notStandardColumnFill(String columnName, AuctionOfferViewDTO data) {
        if (columnName.equals("deliveryPeriod")) {
            String deliveryPeriod = combineDeliveryPeriod(data.getDeliveryPeriodFrom(), data.getDeliveryPeriodTo());
            return Optional.of(deliveryPeriod);
        }
        if (columnName.equals("acceptedDeliveryPeriod")) {
            String acceptedDeliveryPeriod = combineDeliveryPeriod(data.getAcceptedDeliveryPeriodFrom(), data.getAcceptedDeliveryPeriodTo());
            return Optional.of(acceptedDeliveryPeriod);
        }
        return Optional.empty();
    }

    //20.12.2021 08:00, 20.12.2021 16:00  -->  20.12.2021 08:00 - 16:00
    private String combineDeliveryPeriod(Instant deliveryPeriodFrom, Instant deliveryPeriodTo) {
        if (Objects.isNull(deliveryPeriodFrom) || Objects.isNull(deliveryPeriodTo)) {
            return Strings.EMPTY;
        }

        String format = "%s %s - %s";
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        LocalDate date = LocalDate.ofInstant(deliveryPeriodFrom, TimeZone.getDefault().toZoneId());
        LocalTime timeFrom = LocalTime.ofInstant(deliveryPeriodFrom, TimeZone.getDefault().toZoneId());
        LocalTime timeTo = LocalTime.ofInstant(deliveryPeriodTo, TimeZone.getDefault().toZoneId());

        // W eksportowanym pliku godziny zapisujemy w formacie 24H.
        // Gdy okres dostawy ma być do pólnocy, to ustawiamy godzine 24:00
        //np. okres od 2022.05.10 00:00 do 2022.05.11 00:00 to okres ustawiamy w nastepujacy sposob -> 00:00 - 24:00
        String timeToStr;
        if (timeTo.equals(LocalTime.of(0, 0))) {
            timeToStr = "24:00";
        } else {
            timeToStr = timeTo.format(timeFormatter);
        }

        return String.format(format, date.format(dateFormatter), timeFrom.format(timeFormatter), timeToStr);
    }
}
