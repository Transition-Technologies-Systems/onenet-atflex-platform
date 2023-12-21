package pl.com.tt.flex.server.web.rest.auction.cmvc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import pl.com.tt.flex.server.FlexserverApp;
import pl.com.tt.flex.server.repository.auction.cmvc.AuctionCmvcRepository;
import pl.com.tt.flex.server.service.auction.cmvc.mapper.AuctionCmvcMapper;

import javax.persistence.EntityManager;

import static pl.com.tt.flex.model.security.permission.Authority.*;

/**
 * Integration tests for the {@link AuctionCmvcResourceAdmin} REST controller.
 */
@SpringBootTest(classes = FlexserverApp.class)
@AutoConfigureMockMvc
@WithMockUser(authorities = {FLEX_USER_AUCTIONS_CMVC_MANAGE, FLEX_USER_AUCTIONS_CMVC_VIEW, FLEX_USER_AUCTIONS_CMVC_DELETE})
public class AuctionCmvcResourceUserIT extends AuctionCmvcResourceIT {

    private final static String requestUri = "/api/user/auctions-cmvc";

    @Autowired
    public AuctionCmvcResourceUserIT(AuctionCmvcRepository auctionCmvcRepository, AuctionCmvcMapper auctionCmvcMapper,
                                     EntityManager em, MockMvc restAuctionCmvcMockMvc) {
        super(auctionCmvcRepository, auctionCmvcMapper, em, restAuctionCmvcMockMvc, requestUri);
    }
}
