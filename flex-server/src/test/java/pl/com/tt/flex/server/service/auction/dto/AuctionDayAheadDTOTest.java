package pl.com.tt.flex.server.service.auction.dto;

import org.junit.jupiter.api.Test;
import pl.com.tt.flex.model.service.dto.auction.da.AuctionDayAheadDTO;

import static org.assertj.core.api.Assertions.assertThat;

public class AuctionDayAheadDTOTest {

    @Test
    public void dtoEqualsVerifier() throws Exception {
        //TestUtil.equalsVerifier(AuctionDTO.class);
        AuctionDayAheadDTO auctionDayAheadDTO1 = new AuctionDayAheadDTO();
        auctionDayAheadDTO1.setId(1L);
        AuctionDayAheadDTO auctionDayAheadDTO2 = new AuctionDayAheadDTO();
        assertThat(auctionDayAheadDTO1).isNotEqualTo(auctionDayAheadDTO2);
        auctionDayAheadDTO2.setId(auctionDayAheadDTO1.getId());
        assertThat(auctionDayAheadDTO1).isEqualTo(auctionDayAheadDTO2);
        auctionDayAheadDTO2.setId(2L);
        assertThat(auctionDayAheadDTO1).isNotEqualTo(auctionDayAheadDTO2);
        auctionDayAheadDTO1.setId(null);
        assertThat(auctionDayAheadDTO1).isNotEqualTo(auctionDayAheadDTO2);
    }
}
