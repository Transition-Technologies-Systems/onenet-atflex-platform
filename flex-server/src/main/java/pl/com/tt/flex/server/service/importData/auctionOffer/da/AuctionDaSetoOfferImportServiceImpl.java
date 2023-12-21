package pl.com.tt.flex.server.service.importData.auctionOffer.da;

import static pl.com.tt.flex.server.util.AuctionDayAheadDataUtil.markBandAsEditedUpdateOfferStatus;
import static pl.com.tt.flex.server.web.rest.errors.ErrorConstants.IMPORT_OTHER;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pl.com.tt.flex.model.service.dto.MinimalDTO;
import pl.com.tt.flex.server.common.errors.ObjectValidationException;
import pl.com.tt.flex.server.domain.auction.offer.da.AuctionDayAheadOfferEntity;
import pl.com.tt.flex.server.domain.auction.offer.da.AuctionOfferDersEntity;
import pl.com.tt.flex.server.repository.auction.offer.AuctionDayAheadOfferRepository;
import pl.com.tt.flex.server.service.importData.auctionOffer.dto.AuctionOfferImportDataResult;
import pl.com.tt.flex.server.service.importData.auctionOffer.dto.AuctionOfferSetoImportData;

@Slf4j
@Component
@Transactional
@AllArgsConstructor
public class AuctionDaSetoOfferImportServiceImpl implements AuctionDaSetoOfferImportService {

    private final AuctionDayAheadOfferRepository auctionDayAheadOfferRepository;

    @Override
    public AuctionOfferImportDataResult importBids(List<AuctionOfferSetoImportData> daBids) {
        log.debug("importData() End - import DayAhead offers");
        Map<Long, List<AuctionOfferSetoImportData>> dataByOfferId = daBids.stream().collect(Collectors.groupingBy(AuctionOfferSetoImportData::getOfferId));
        AuctionOfferImportDataResult auctionOfferImportDataResultDTOs = new AuctionOfferImportDataResult();
        dataByOfferId.forEach((bidId, setoData) -> {
            try {
                log.debug("importData() Start - import bid with ID={}", bidId);
                updateDaOffer(bidId, setoData);
                auctionOfferImportDataResultDTOs.addImportedBids(bidId);
                log.debug("importData() End - Successfully import bid with ID={}", bidId);
            } catch (ObjectValidationException e) {
                auctionOfferImportDataResultDTOs.addNotImportedBids(new MinimalDTO<>(String.valueOf(bidId), e.getMsgKey()));
                log.debug("importData() End - Not import bid={} with error msg: {}", bidId, e.getMessage());
            } catch (Exception e) {
                auctionOfferImportDataResultDTOs.addNotImportedBids(new MinimalDTO<>(bidId.toString(), IMPORT_OTHER));
                log.debug("importData() End - Not import bid={} with error msg: {}", bidId, e.getMessage());
                e.printStackTrace();
            }
        });
        log.debug("importData() End - import DayAhead offers");
        return auctionOfferImportDataResultDTOs;
    }

    private void updateDaOffer(Long bidId, List<AuctionOfferSetoImportData> setoData) throws ObjectValidationException {
        AuctionDayAheadOfferEntity daOfferToUpdate = auctionDayAheadOfferRepository.findByIdFetchUnits(bidId).orElseThrow(() -> new IllegalStateException("Cannot find offer by id " + bidId.toString()));
        setoData.stream().collect(Collectors.groupingBy(AuctionOfferSetoImportData::getDerName)).forEach((derName, data) -> updateOfferDer(derName, data, daOfferToUpdate));
    }

    private void updateOfferDer(String derName, List<AuctionOfferSetoImportData> setoData, AuctionDayAheadOfferEntity daOfferToUpdate) {
        AuctionOfferDersEntity offerDerToUpdate = daOfferToUpdate.getUnits().stream().filter(unit -> unit.getUnit().getName().equals(derName)).findAny().get();
        setoData.forEach(data -> updateBand(data, offerDerToUpdate));
    }

    private void updateBand(AuctionOfferSetoImportData importedData, AuctionOfferDersEntity offerDerToUpdate) {
        // Numer pasma nie jest ustawiany podczas importu ofert capacity i dlatego domyślnie jest zainicjowany jako null.
        // Pasma o numerze 0, zawierające plan pracy, nie są importowane z pliku.
        offerDerToUpdate.getBandData().stream().filter(dbBand -> dbBand.getHourNumber().equals(importedData.getTimestamp()) && !dbBand.getBandNumber().equals("0")
                && (Objects.isNull(importedData.getBand()) || Objects.equals(importedData.getBand(), dbBand.getBandNumber())))
            .filter(band -> Objects.nonNull(band.getAcceptedVolume()))  //Edycja usuniętego pasma jest niedozwolona
            .forEach(bandToUpdate -> {
                bandToUpdate.setAcceptedVolume(BigDecimal.valueOf(importedData.getVolume()));
                markBandAsEditedUpdateOfferStatus(bandToUpdate);
            });
    }
}
