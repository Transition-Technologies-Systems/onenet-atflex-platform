package pl.com.tt.flex.server.dataexport.exporter.offer.detail.factory;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import pl.com.tt.flex.model.service.dto.auction.offer.AuctionOfferViewDTO;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public interface OfferDetailExporter {
    byte[] fillOfferDetailSheet(XSSFWorkbook workbook, List<AuctionOfferViewDTO> daOffers, Locale locale) throws IOException;
}
