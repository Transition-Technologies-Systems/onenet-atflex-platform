package pl.com.tt.flex.server.service.algorithm.agnoAlgorithm.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Klasa zawierajaca informacje dotyczace danej godziny gieldowej.
 * Zawiera informacje o:
 * * liscie ofert zlozonych na dana godzine gieldowa {@link AgnoOfferDetailDTO}
 * * liscie produktow wykorzystanych w aukcjach na ktore zostaly zlozone oferty {@link AgnoProductDetailDTO}
 * * zasobach zlozonych w ofertach {@link AgnoDerDetailDTO}
 * Wykorzystywana do generowania plikow AGNO
 */
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
public class AgnoHourNumberDTO {

    private String hourNumber; //numer godziny gieldowej
    private List<AgnoOfferDetailDTO> offerDetails = new ArrayList<>(); // lista z ofertami (dery, produktu, forecastedPrices ... )
    private List<AgnoProductDetailDTO> productList = new ArrayList<>();
    private List<AgnoDerDetailDTO> derList = new ArrayList<>();

    public List<AgnoOfferDetailDTO> addAgnoOfferDetail(AgnoOfferDetailDTO offerDetailDTO) {
        offerDetails.add(offerDetailDTO);
        return offerDetails;
    }

    public List<AgnoProductDetailDTO> addProductToHour(AgnoProductDetailDTO agnoProductDetailDTO) {
        if (productList.stream().noneMatch(p -> p.getId().equals(agnoProductDetailDTO.getId()))) {
            productList.add(agnoProductDetailDTO);
        }
        return productList;
    }

    public List<AgnoDerDetailDTO> addDerToHour(AgnoDerDetailDTO derDTO) {
        if (derList.stream().noneMatch(u -> u.getId().equals(derDTO.getId()))) {
            derList.add(derDTO);
        }
        return derList;
    }

    public AgnoHourNumberDTO(String hourNumber) {
        this.hourNumber = hourNumber;
    }
}
