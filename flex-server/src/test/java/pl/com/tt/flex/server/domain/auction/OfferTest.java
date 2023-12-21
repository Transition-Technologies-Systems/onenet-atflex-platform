package pl.com.tt.flex.server.domain.auction;

import org.junit.jupiter.api.Test;
import pl.com.tt.flex.server.domain.auction.offer.cmvc.AuctionCmvcOfferEntity;

import static org.assertj.core.api.Assertions.assertThat;

public class OfferTest {

    @Test
    public void equalsVerifier() throws Exception {
        //TestUtil.equalsVerifier(OfferEntity.class);
        AuctionCmvcOfferEntity offerEntity1 = new AuctionCmvcOfferEntity();
        offerEntity1.setId(1L);
        AuctionCmvcOfferEntity offerEntity2 = new AuctionCmvcOfferEntity();
        offerEntity2.setId(offerEntity1.getId());
        assertThat(offerEntity1).isEqualTo(offerEntity2);
        offerEntity2.setId(2L);
        assertThat(offerEntity1).isNotEqualTo(offerEntity2);
        offerEntity1.setId(null);
        assertThat(offerEntity1).isNotEqualTo(offerEntity2);
    }
}
