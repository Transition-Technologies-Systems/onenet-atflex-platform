package pl.com.tt.flex.model.service.dto.auction.offer.da;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.com.tt.flex.model.service.dto.der.DerMinDTO;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class AuctionOfferDersDTO implements Serializable {
    private Long id;
    //id, name, sourcePower, pMin
    private DerMinDTO der;
    @NotEmpty
    private List<AuctionOfferBandDataDTO> bandData = new ArrayList<>();
}
