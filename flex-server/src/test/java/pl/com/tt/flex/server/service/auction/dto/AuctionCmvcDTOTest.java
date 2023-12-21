//package pl.com.tt.flex.server.service.auction.dto;
//
//import org.junit.jupiter.api.Test;
//import static org.assertj.core.api.Assertions.assertThat;
//
//import pl.com.tt.flex.server.web.rest.TestUtil;
//
//public class AuctionCmvcDTOTest {
//
//    @Test
//    public void dtoEqualsVerifier() throws Exception {
//        TestUtil.equalsVerifier(AuctionCmvcDTO.class);
//        AuctionCmvcDTO auctionCmvcDTO1 = new AuctionCmvcDTO();
//        auctionCmvcDTO1.setId(1L);
//        AuctionCmvcDTO auctionCmvcDTO2 = new AuctionCmvcDTO();
//        assertThat(auctionCmvcDTO1).isNotEqualTo(auctionCmvcDTO2);
//        auctionCmvcDTO2.setId(auctionCmvcDTO1.getId());
//        assertThat(auctionCmvcDTO1).isEqualTo(auctionCmvcDTO2);
//        auctionCmvcDTO2.setId(2L);
//        assertThat(auctionCmvcDTO1).isNotEqualTo(auctionCmvcDTO2);
//        auctionCmvcDTO1.setId(null);
//        assertThat(auctionCmvcDTO1).isNotEqualTo(auctionCmvcDTO2);
//    }
//}
