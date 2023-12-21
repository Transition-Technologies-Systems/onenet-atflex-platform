package pl.com.tt.flex.server.dataexport.exporter.offer.detail.factory.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import pl.com.tt.flex.server.dataexport.exporter.offer.detail.DaOfferDetailExporter;
import pl.com.tt.flex.server.dataexport.exporter.offer.detail.SetoOfferDetailExporter;
import pl.com.tt.flex.server.dataexport.exporter.offer.detail.enumeration.LevelOfDetail;
import pl.com.tt.flex.server.dataexport.exporter.offer.detail.factory.OfferDetailExporter;
import pl.com.tt.flex.server.dataexport.exporter.offer.detail.factory.OfferDetailExporterFactory;
import pl.com.tt.flex.server.service.auction.da.AuctionDayAheadService;

@Slf4j
@Component
public class OfferDetailExporterFactoryImpl implements OfferDetailExporterFactory {

    /**
     * Metoda fabrykująca, która w zależności od wybranego poziomu szczegółów zwróci odpowiedni obiekt eksportujący szczegółowy widok ofert.
     */
    @Override
    public OfferDetailExporter createOfferDetailExporter(AuctionDayAheadService auctionDayAheadService, MessageSource source, LevelOfDetail detail) {
        if (detail.equals(LevelOfDetail.SETO_DETAIL_SHEET)) {
            return new SetoOfferDetailExporter(auctionDayAheadService, source);
        } else {
            return new DaOfferDetailExporter(auctionDayAheadService, source);
        }
    }
}
