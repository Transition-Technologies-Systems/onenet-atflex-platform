package pl.com.tt.flex.server.domain.auction;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

import pl.com.tt.flex.server.domain.auction.da.AuctionDayAheadEntity;
import pl.com.tt.flex.server.web.rest.TestUtil;

public class AuctionDayAheadTest {

    @Test
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(AuctionDayAheadEntity.class);
        AuctionDayAheadEntity auctionDayAheadEntity1 = new AuctionDayAheadEntity();
        auctionDayAheadEntity1.setId(1L);
        AuctionDayAheadEntity auctionDayAheadEntity2 = new AuctionDayAheadEntity();
        auctionDayAheadEntity2.setId(auctionDayAheadEntity1.getId());
        assertThat(auctionDayAheadEntity1).isEqualTo(auctionDayAheadEntity2);
        auctionDayAheadEntity2.setId(2L);
        assertThat(auctionDayAheadEntity1).isNotEqualTo(auctionDayAheadEntity2);
        auctionDayAheadEntity1.setId(null);
        assertThat(auctionDayAheadEntity1).isNotEqualTo(auctionDayAheadEntity2);
    }
}
