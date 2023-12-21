package pl.com.tt.flex.server.dataexport.exporter.offer.detail.factory;

import org.springframework.context.MessageSource;
import pl.com.tt.flex.server.dataexport.exporter.offer.detail.enumeration.LevelOfDetail;
import pl.com.tt.flex.server.service.auction.da.AuctionDayAheadService;

public interface OfferDetailExporterFactory {
    OfferDetailExporter createOfferDetailExporter(AuctionDayAheadService auctionDayAheadService, MessageSource messageSource, LevelOfDetail detail);
}
