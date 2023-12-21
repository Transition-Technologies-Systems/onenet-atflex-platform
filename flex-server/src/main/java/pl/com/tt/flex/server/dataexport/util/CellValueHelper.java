package pl.com.tt.flex.server.dataexport.util;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import pl.com.tt.flex.model.service.dto.auction.type.AuctionOfferStatus;
import pl.com.tt.flex.model.service.dto.dictionary.DictionaryDTO;
import pl.com.tt.flex.model.service.dto.product.type.Direction;
import pl.com.tt.flex.model.service.dto.product.type.ProductBidSizeUnit;
import pl.com.tt.flex.server.domain.unit.UnitDirectionOfDeviation;
import pl.com.tt.flex.server.service.dictionary.DictionaryService;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Objects;

@Component
public class CellValueHelper {

    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss").withZone(ZoneId.of("Europe/Paris"));

    private final DictionaryService dictionaryService;

    public CellValueHelper(DictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
    }

    public String getCellValue(Object o, MessageSource messageSource, Locale locale) {
        if (Objects.isNull(o)) {
            return "";
        }
        if (o instanceof Instant) {
            return dateTimeFormatter.format((Instant) o);
        }
        if (o instanceof String) {
            return o.toString();
        }
        if (o instanceof Long) {
            return o.toString();
        }
        if (o instanceof Integer) {
            return o.toString();
        }
        if (o instanceof Boolean) {
            return messageSource.getMessage("exporter.boolean." + o, null, locale);
        }
        if (o instanceof BigDecimal) {
            return o.toString();
        }
        if (o instanceof ProductBidSizeUnit) {
            return messageSource.getMessage("exporter.product.bid.size.unit." + o, null, locale);
        }
        if (o instanceof UnitDirectionOfDeviation) {
            return messageSource.getMessage("exporter.unit.directionOfDeviation." + o, null, locale);
        }
        if (o instanceof AuctionOfferStatus) {
            if (locale.getLanguage().equals("pl")) {
                return ((AuctionOfferStatus) o).getDescriptionPl();
            }
            return ((AuctionOfferStatus) o).getDescriptionEn();
        }
        if (o instanceof DictionaryDTO) {
            return dictionaryService.getDescriptionByLocaleAndIdAndNlsCode(locale, ((DictionaryDTO) o).getId(), ((DictionaryDTO) o).getNlsCode());
        }
        if (o instanceof Direction) {
            return messageSource.getMessage("exporter.product.direction." + o, null, locale);
        }
        return "";
    }
}
