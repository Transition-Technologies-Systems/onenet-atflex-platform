//package pl.com.tt.flex.server.service.auction.dto;
//
//import org.junit.jupiter.api.Test;
//import static org.assertj.core.api.Assertions.assertThat;
//
//import pl.com.tt.flex.server.service.auction.offer.dto.AuctionOfferViewDTO;
//
//public class AuctionOfferViewDTOTest {
//
//    @Test
//    public void dtoEqualsVerifier() throws Exception {
//        //TestUtil.equalsVerifier(OfferDTO.class);
//        AuctionOfferViewDTO offerDTO1 = new AuctionOfferViewDTO();
//        offerDTO1.setId(1L);
//        AuctionOfferViewDTO offerDTO2 = new AuctionOfferViewDTO();
//        assertThat(offerDTO1).isNotEqualTo(offerDTO2);
//        offerDTO2.setId(offerDTO1.getId());
//        assertThat(offerDTO1).isEqualTo(offerDTO2);
//        offerDTO2.setId(2L);
//        assertThat(offerDTO1).isNotEqualTo(offerDTO2);
//        offerDTO1.setId(null);
//        assertThat(offerDTO1).isNotEqualTo(offerDTO2);
//    }
//}
