package pl.com.tt.flex.server.util;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class AuctionDayAheadDataUtilTest {

    private final String PRODUCT_NAME = "PRODUCT_TEST";
    private final Instant AUCTION_DAY = Instant.parse("2022-03-12T10:00:00Z");


    @Test
    void shouldGenerateAuctionDayAheadName() {
        String auctionNameFormat = "DA_%s_%s";
        String deliveryDate = "20220313";
        String expectedAuctionName = String.format(auctionNameFormat, PRODUCT_NAME, deliveryDate);
        String auctionName = AuctionDayAheadDataUtil.generateAuctionDayAheadName(PRODUCT_NAME, AUCTION_DAY);
        assertThat(auctionName).isEqualTo(expectedAuctionName);
    }

    @Test
    void shouldGenerateSeriesName() {
        String auctionNameFormat = "DA_%s_%s";
        String seriesCreationDate = "20220312";
        String expectedAuctionName = String.format(auctionNameFormat, PRODUCT_NAME, seriesCreationDate);
        String auctionName = AuctionDayAheadDataUtil.generateAuctionSeriesName(PRODUCT_NAME, AUCTION_DAY);
        assertThat(auctionName).isEqualTo(expectedAuctionName);
    }
}
