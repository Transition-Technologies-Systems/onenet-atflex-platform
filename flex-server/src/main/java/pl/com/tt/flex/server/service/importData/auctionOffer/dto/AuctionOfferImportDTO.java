package pl.com.tt.flex.server.service.importData.auctionOffer.dto;

import lombok.*;
import pl.com.tt.flex.model.service.dto.MinimalDTO;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@ToString
public class AuctionOfferImportDTO implements Serializable {

    private final List<Long> importedBids = new ArrayList<>();
    // Id trzymamy jako String, w celu przeslania blędnie podanych ID w importowanym pliku np. 12aa
    List<MinimalDTO<String, String>> notImportedBids = new ArrayList<>();
}
