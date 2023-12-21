package pl.com.tt.flex.model.service.dto.auction.type;

import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

/**
 * The OfferStatus enumeration.
 * <p>
 * Na dzień dzisiejszy nie mamy dostępu do algorytmów określających, które oferty od BSP są najbardziej opłacalne
 * i przez to kolejne statusy po "Pending" nie mogą być automatycznie wskazywane. Jednakowoż będzie to wykonywane
 * z poziomu okna "Bids evaluation" FLEXPLATF-735.
 */
@Getter
public enum AuctionOfferStatus {
    PENDING("Pending", "W trakcie"), // gdy oferta została złożona, aukcja trwa lub zakończyła się
    ACCEPTED("Accepted", "Zaakceptowana"),
    REJECTED("Rejected", "Odrzucona"),
    VOLUMES_VERIFIED("Volumes verified", "Zweryfikowano wolumeny");

    private final String descriptionEn;
    private final String descriptionPl;

    AuctionOfferStatus(String descriptionEn, String descriptionPl) {
        this.descriptionEn = descriptionEn;
        this.descriptionPl = descriptionPl;
    }

    public static Optional<AuctionOfferStatus> findStatusByDescription(String description) {
        return Arrays.stream(AuctionOfferStatus.values())
                .filter(s -> s.getDescriptionEn().equalsIgnoreCase(description) || s.getDescriptionPl()
                        .equalsIgnoreCase(description)).findFirst();
    }
}
