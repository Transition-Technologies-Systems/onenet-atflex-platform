package pl.com.tt.flex.server.service.settlement;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

import org.springframework.data.util.Pair;
import org.springframework.web.multipart.MultipartFile;

import pl.com.tt.flex.model.service.dto.file.FileDTO;
import pl.com.tt.flex.server.domain.auction.offer.cmvc.AuctionCmvcOfferEntity;
import pl.com.tt.flex.server.domain.auction.offer.da.AuctionDayAheadOfferEntity;
import pl.com.tt.flex.server.domain.screen.enumeration.Screen;
import pl.com.tt.flex.server.domain.settlement.SettlementEntity;
import pl.com.tt.flex.server.service.AbstractService;
import pl.com.tt.flex.server.service.settlement.dto.SettlementDTO;
import pl.com.tt.flex.server.service.settlement.dto.SettlementEditDTO;
import pl.com.tt.flex.server.service.settlement.dto.SettlementViewDTO;

public interface SettlementService extends AbstractService<SettlementEntity, SettlementDTO, Long> {

    SettlementEditDTO getSettlementMin(Long id);

    SettlementViewDTO getSettlementView(Long id);

    SettlementViewDTO getSettlementView(Long id, Long fspId);

    void updateSettlement(Long id, SettlementEditDTO settlementMin);

    void generateSettlementsForOffer(AuctionCmvcOfferEntity cmvcOffer);

    void generateSettlementsForOffer(AuctionDayAheadOfferEntity daOffer);

    FileDTO exportSettlementsToFile(List<SettlementViewDTO> settlementsToExport, String langKey, boolean isOnlyDisplayedData, Screen screen, Pair<Instant, Instant> acceptedDeliveryPeriod) throws IOException;

	void importSettlementUpdates(MultipartFile[] multipartFiles) throws IOException;
}
