package pl.com.tt.flex.server.service.auction.da.series;

import pl.com.tt.flex.model.service.dto.auction.da.AuctionsSeriesDTO;
import pl.com.tt.flex.server.domain.auction.da.AuctionsSeriesEntity;
import pl.com.tt.flex.server.service.AbstractService;

import java.util.List;

/**
 * Service Interface for managing {@link AuctionsSeriesEntity}.
 */
public interface AuctionsSeriesService extends AbstractService<AuctionsSeriesEntity, AuctionsSeriesDTO, Long> {

    List<AuctionsSeriesEntity> findAuctionsToCreate();

    List<AuctionsSeriesEntity> findAllByProductId(Long productId);

    void updateAuctionName(String auctionName, Long id);

    boolean canDeleteAuctionSeries(Long seriesId);
}
