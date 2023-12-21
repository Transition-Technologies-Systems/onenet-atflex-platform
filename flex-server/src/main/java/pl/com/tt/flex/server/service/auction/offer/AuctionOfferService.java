package pl.com.tt.flex.server.service.auction.offer;

import java.io.IOException;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pl.com.tt.flex.model.service.dto.auction.offer.AuctionOfferViewDTO;
import pl.com.tt.flex.model.service.dto.auction.type.AuctionOfferStatus;
import pl.com.tt.flex.model.service.dto.file.FileDTO;
import pl.com.tt.flex.server.dataexport.exporter.offer.detail.enumeration.LevelOfDetail;
import pl.com.tt.flex.server.domain.auction.offer.cmvc.AuctionCmvcOfferEntity;
import pl.com.tt.flex.server.domain.email.enumeration.EmailType;
import pl.com.tt.flex.server.domain.screen.enumeration.Screen;
import pl.com.tt.flex.server.service.mail.dto.NotificationResultDTO;
import pl.com.tt.flex.server.service.product.dto.ProductNameMinDTO;

/**
 * Service Interface for managing {@link AuctionCmvcOfferEntity}.
 */
@Service
@Transactional
public interface AuctionOfferService {

    void updateStatus(AuctionOfferStatus status, List<Long> auctionOfferIds);

    FileDTO exportOffersToFile(List<AuctionOfferViewDTO> offers, String langKey, Screen screen, LevelOfDetail detai) throws IOException;

    List<ProductNameMinDTO> findAllProductsUsedInOffer();

    void sendInformationAboutAcceptedCapacityOffer(List<Long> ids);

    void sendReminderAboutActivation(List<Long> ids);

    boolean areAllOffersPendingOrVerified(List<Long> ids);

    boolean areAllDayAheadVolumesVerified(List<Long> ids);

    boolean areAllAuctionsClosed(List<Long> ids);

    boolean isCmvcOffer(Long id);

    NotificationResultDTO exportOffersToFileAndSendEmail(List<AuctionOfferViewDTO> content, String langKeyForCurrentLoggedUser, Screen adminBidsEvaluation, LevelOfDetail standardDetailSheet, EmailType type) throws IOException;

    void saveVolumeTransferredToBM(List<Long> offerId);
}
