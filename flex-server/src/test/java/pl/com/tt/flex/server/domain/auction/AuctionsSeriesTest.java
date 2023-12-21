package pl.com.tt.flex.server.domain.auction;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

import pl.com.tt.flex.server.domain.auction.da.AuctionsSeriesEntity;
import pl.com.tt.flex.server.web.rest.TestUtil;

public class AuctionsSeriesTest {

    @Test
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(AuctionsSeriesEntity.class);
        AuctionsSeriesEntity auctionsSeriesEntity1 = new AuctionsSeriesEntity();
        auctionsSeriesEntity1.setId(1L);
        AuctionsSeriesEntity auctionsSeriesEntity2 = new AuctionsSeriesEntity();
        auctionsSeriesEntity2.setId(auctionsSeriesEntity1.getId());
        assertThat(auctionsSeriesEntity1).isEqualTo(auctionsSeriesEntity2);
        auctionsSeriesEntity2.setId(2L);
        assertThat(auctionsSeriesEntity1).isNotEqualTo(auctionsSeriesEntity2);
        auctionsSeriesEntity1.setId(null);
        assertThat(auctionsSeriesEntity1).isNotEqualTo(auctionsSeriesEntity2);
    }
}
