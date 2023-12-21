package pl.com.tt.flex.server.dataexport.exporter.offer.util;

import org.springframework.util.CollectionUtils;
import pl.com.tt.flex.model.service.dto.auction.offer.da.AuctionDayAheadOfferDTO;
import pl.com.tt.flex.model.service.dto.auction.offer.da.AuctionOfferBandDataDTO;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static pl.com.tt.flex.server.util.AuctionDayAheadDataUtil.*;

public class OfferExportUtils {

    private OfferExportUtils() {
    }

    public static int OFFER_ID_CELL_NR = 0;
    public static int SCHEDULING_UNIT_BAND_NR_CELL = 2;
    public static int TIMESTAMP_START_CELL = 3;
    public static final int MAX_CELL_INDEX = 52;
    public static final String DETAILS_SHEET_NAME_MESSAGE = "exporter.bids.evaluation.details.sheetName";
    public static final String SELF_SCHEDULE_SHORTCUT = "exporter.bids.evaluation.details.shortcut.selfSchedule";
    public static final String SCHEDULING_UNIT_SHORTCUT = "exporter.bids.evaluation.details.shortcut.schedulingUnit";
    public static final String VOLUME_SHORTCUT = "exporter.bids.evaluation.details.shortcut.volume";
    public static final String PRICE_SHORTCUT = "exporter.bids.evaluation.details.shortcut.price";

    /**
     * Zwraca sume zaakceptowanych wolumenow dla danego timestampa i pasma.
     * Gdy żadne wolumen nie jest uzupelniony zwraca pusty optional.
     */
    public static Optional<BigDecimal> getAcceptedVolumeForBandAndTimestamp(AuctionDayAheadOfferDTO offerDTO, Integer bandNumber, String timestamp) {
        List<BigDecimal> volumes = getBandDataStreamForBandNumberAndTimestamp(offerDTO, bandNumber, timestamp)
            .map(AuctionOfferBandDataDTO::getAcceptedVolume)
            .filter(Objects::nonNull).collect(Collectors.toList());
        boolean isPresentAnyVolumes = !CollectionUtils.isEmpty(volumes);
        if (isPresentAnyVolumes) {
            return Optional.ofNullable(sumBandAcceptedVolumeInTimestamp(offerDTO, bandNumber, timestamp));
        }
        return Optional.empty();
    }

    /**
     * Zwraca sume wolumenow dla danego timestampa i pasma.
     * Gdy żadne wolumen nie jest uzupelniony zwraca pusty optional.
     */
    public static Optional<BigDecimal> getVolumeForBandAndTimestamp(AuctionDayAheadOfferDTO offerDTO, Integer bandNumber, String timestamp) {
        List<BigDecimal> volumes = getBandDataStreamForBandNumberAndTimestamp(offerDTO, bandNumber, timestamp)
            .map(AuctionOfferBandDataDTO::getVolume)
            .filter(Objects::nonNull).collect(Collectors.toList());
        boolean isPresentAnyVolumes = !CollectionUtils.isEmpty(volumes);
        if (isPresentAnyVolumes) {
            return Optional.ofNullable(sumBandVolumeInTimestamp(offerDTO, bandNumber, timestamp));
        }
        return Optional.empty();
    }

    /**
     * Dla oferty nie edytowanej,
     * Jezeli wszystkie ceny dla danego timestamp'a i pasam w wszystkich uzupelnionych DERach są takie same,
     * to wtedy zwraca wartość ustawionej ceny, w przeciwnym wypadku w sekcji SU nie powinna byc uzupelnioa cena
     */
    public static Optional<BigDecimal> getPriceForSuSection(AuctionDayAheadOfferDTO offerDTO, Integer bandNumber, String timestamp) {
        boolean areAllPricesSame = areAllPricesTheSameInBandAndTimestamp(offerDTO, bandNumber, timestamp);
        if (areAllPricesSame) {
            BigDecimal price = getBandDataStreamForBandNumberAndTimestamp(offerDTO, bandNumber, timestamp)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Not found price"))
                .getAcceptedPrice();
            return Optional.ofNullable(price);
        }
        return Optional.empty();
    }

    /**
     * Dla oferty edytowanej,
     * Jezeli wszystkie ceny dla danego timestamp'a i pasam w wszystkich uzupelnionych DERach są takie same,
     * to wtedy zwraca wartość ustawioneej ceny, w przeciwnym wypadku w sekcji SU nie powinna byc uzupelniona cena
     */
    public static Optional<BigDecimal> getAcceptedPriceForSuSection(AuctionDayAheadOfferDTO offerDTO, Integer bandNumber, String timestamp) {
        boolean areAllAcceptedPricesSame = areAllAcceptedPricesTheSameInBandAndTimestamp(offerDTO, bandNumber, timestamp);
        if (areAllAcceptedPricesSame) {
            BigDecimal acceptedPrice = getBandDataStreamForBandNumberAndTimestamp(offerDTO, bandNumber, timestamp)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Not found accepted price"))
                .getAcceptedPrice();
            return Optional.ofNullable(acceptedPrice);
        }
        return Optional.empty();
    }
}
