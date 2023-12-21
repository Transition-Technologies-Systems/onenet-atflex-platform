//package pl.com.tt.flex.server.domain.auction;
//
//import org.junit.jupiter.api.Test;
//import static org.assertj.core.api.Assertions.assertThat;
//import pl.com.tt.flex.server.web.rest.TestUtil;
//
//public class AuctionCmvcTest {
//
//    @Test
//    public void equalsVerifier() throws Exception {
//        TestUtil.equalsVerifier(AuctionCmvcEntity.class);
//        AuctionCmvcEntity auctionCmvcEntity1 = new AuctionCmvcEntity();
//        auctionCmvcEntity1.setId(1L);
//        AuctionCmvcEntity auctionCmvcEntity2 = new AuctionCmvcEntity();
//        auctionCmvcEntity2.setId(auctionCmvcEntity1.getId());
//        assertThat(auctionCmvcEntity1).isEqualTo(auctionCmvcEntity2);
//        auctionCmvcEntity2.setId(2L);
//        assertThat(auctionCmvcEntity1).isNotEqualTo(auctionCmvcEntity2);
//        auctionCmvcEntity1.setId(null);
//        assertThat(auctionCmvcEntity1).isNotEqualTo(auctionCmvcEntity2);
//    }
//}
