package pl.com.tt.flex.server.service.auction.dto;

import org.junit.jupiter.api.Test;
import pl.com.tt.flex.model.service.dto.auction.da.AuctionsSeriesDTO;

import static org.assertj.core.api.Assertions.assertThat;

public class AuctionsSeriesDTOTest {

    @Test
    public void dtoEqualsVerifier() throws Exception {
        //TestUtil.equalsVerifier(AuctionsSeriesDTO.class);
        AuctionsSeriesDTO auctionsSeriesDTO1 = new AuctionsSeriesDTO();
        auctionsSeriesDTO1.setId(1L);
        AuctionsSeriesDTO auctionsSeriesDTO2 = new AuctionsSeriesDTO();
        assertThat(auctionsSeriesDTO1).isNotEqualTo(auctionsSeriesDTO2);
        auctionsSeriesDTO2.setId(auctionsSeriesDTO1.getId());
        assertThat(auctionsSeriesDTO1).isEqualTo(auctionsSeriesDTO2);
        auctionsSeriesDTO2.setId(2L);
        assertThat(auctionsSeriesDTO1).isNotEqualTo(auctionsSeriesDTO2);
        auctionsSeriesDTO1.setId(null);
        assertThat(auctionsSeriesDTO1).isNotEqualTo(auctionsSeriesDTO2);
    }
}
