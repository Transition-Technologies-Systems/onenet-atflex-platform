//package pl.com.tt.flex.server.service.mapper;
//
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import pl.com.tt.flex.server.service.auction.cmvc.mapper.AuctionCmvcMapper;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//public class AuctionCmvcMapperTest {
//
//    private AuctionCmvcMapper auctionCmvcMapper;
//
//    @BeforeEach
//    public void setUp() {
//        auctionCmvcMapper = new AuctionCmvcMapperImpl();
//    }
//
//    @Test
//    public void testEntityFromId() {
//        Long id = 1L;
//        assertThat(auctionCmvcMapper.fromId(id).getId()).isEqualTo(id);
//        assertThat(auctionCmvcMapper.fromId(null)).isNull();
//    }
//}
