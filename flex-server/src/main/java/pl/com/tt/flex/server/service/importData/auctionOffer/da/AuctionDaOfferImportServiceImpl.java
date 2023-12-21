package pl.com.tt.flex.server.service.importData.auctionOffer.da;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pl.com.tt.flex.model.service.dto.MinimalDTO;
import pl.com.tt.flex.model.service.dto.auction.da.AuctionDayAheadDTO;
import pl.com.tt.flex.model.service.dto.auction.offer.da.AuctionDayAheadOfferDTO;
import pl.com.tt.flex.model.service.dto.auction.offer.da.AuctionOfferBandDataDTO;
import pl.com.tt.flex.model.service.dto.auction.type.AuctionOfferStatus;
import pl.com.tt.flex.server.common.errors.ObjectValidationException;
import pl.com.tt.flex.server.service.auction.da.AuctionDayAheadService;
import pl.com.tt.flex.server.service.auction.offer.AuctionOfferService;
import pl.com.tt.flex.server.service.importData.auctionOffer.dto.AuctionOfferImportDataResult;
import pl.com.tt.flex.server.service.importData.auctionOffer.dto.AuctionOfferSchedulingUnitDTO;
import pl.com.tt.flex.server.validator.auction.da.AuctionDayAheadDetailOfferImportValidator;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.*;
import java.util.stream.Collectors;

import static pl.com.tt.flex.model.service.dto.auction.type.AuctionOfferStatus.ACCEPTED;
import static pl.com.tt.flex.model.service.dto.auction.type.AuctionOfferStatus.REJECTED;
import static pl.com.tt.flex.server.util.AuctionDayAheadDataUtil.sumBandAcceptedVolumeInTimestamp;
import static pl.com.tt.flex.server.web.rest.errors.ErrorConstants.IMPORT_OTHER;

@Component
@AllArgsConstructor
@Slf4j
public class AuctionDaOfferImportServiceImpl implements AuctionDaOfferImportService {

    private final AuctionDayAheadService dayAheadService;
    private final AuctionOfferService auctionOfferService;
    private final AuctionDayAheadDetailOfferImportValidator validator;

    @Override
    public AuctionOfferImportDataResult importBids(List<AuctionOfferSchedulingUnitDTO> daBids) {
        log.debug("importData() End - import DayAhead offers");
        Map<String, List<AuctionOfferSchedulingUnitDTO>> groupingByOfferId = daBids.stream().collect(Collectors.groupingBy(AuctionOfferSchedulingUnitDTO::getOfferId));
        AuctionOfferImportDataResult auctionOfferImportDataResultDTOs = new AuctionOfferImportDataResult();
        groupingByOfferId.forEach((strBidId, suData) -> {
            try {
                log.debug("importData() Start - import bid with ID={}", strBidId);
                validator.checkValid(strBidId, suData);
                long bidId = getBidId(strBidId);
                updateDaOffer(bidId, suData);
                auctionOfferImportDataResultDTOs.addImportedBids(bidId);
                log.debug("importData() End - Successfully import bid with ID={}", strBidId);
            } catch (ObjectValidationException e) {
                auctionOfferImportDataResultDTOs.addNotImportedBids(new MinimalDTO<>(String.valueOf(getBidId(strBidId)), e.getMsgKey()));
                log.debug("importData() End - Not import bid={} with error msg: {}", strBidId, e.getMessage());
            } catch (Exception e) {
                auctionOfferImportDataResultDTOs.addNotImportedBids(new MinimalDTO<>(strBidId, IMPORT_OTHER));
                log.debug("importData() End - Not import bid={} with error msg: {}", strBidId, e.getMessage());
                e.printStackTrace();
            }
        });
        log.debug("importData() End - import DayAhead offers");
        return auctionOfferImportDataResultDTOs;
    }

    /**
     * Metoda aktualizacja oferty. Aktualizowane sa wolumeny DERow na podstawie obliczonego wspolczynika GDF.
     */
    @Transactional
    private void updateDaOffer(Long bidId, List<AuctionOfferSchedulingUnitDTO> volumes) throws ObjectValidationException {
        auctionOfferService.saveVolumeTransferredToBM(List.of(bidId));
        AuctionDayAheadOfferDTO daOfferToUpdate = dayAheadService.findOfferById(bidId)
            .orElseThrow(() -> new RuntimeException("AuctionDaOffer not found with id: " + bidId));
        AuctionDayAheadDTO auctionDayAhead = dayAheadService.findById(daOfferToUpdate.getAuctionDayAhead().getId())
            .orElseThrow(() -> new RuntimeException("AuctionDayAhead not found with id: " + daOfferToUpdate.getAuctionDayAhead().getId()));
        List<Triple<Integer, String, BigDecimal>> currentSchedulingUnitVolumes = groupingSuVolumesByBandNumberAndTimestamp(daOfferToUpdate);
        List<AuctionOfferBandDataDTO> bands = daOfferToUpdate.getDers().stream().flatMap(d -> d.getBandData().stream())
            .filter(d -> Objects.nonNull(d.getAcceptedVolume())).collect(Collectors.toList());
        for (int timestamp = 1; timestamp <= 25; timestamp++) {
            for (int bandNo = -10; bandNo <= 10; bandNo++) {
                String ts = timestamp == 25 ? "2a" : String.valueOf(timestamp);
                updateVolumesForTimestampAndBand(volumes, currentSchedulingUnitVolumes, bands, bandNo, ts);
            }
        }
        AuctionDayAheadOfferDTO auctionDayAheadOfferDTO = dayAheadService.saveOffer(daOfferToUpdate, auctionDayAhead.getStatus(), false);
        changeOfferStatus(auctionDayAheadOfferDTO);
    }

    /**
     * Metoda aktualizuje wolumeny dla konkretnego pasma i timestampa. Suma wolumenów poszczególnych derów po zaktualizowaniu musi być równa wolumenowi w sekcji JG,
     * dlatego najniższy z wolumenów jest obliczany jako różnica zmienionego wolumenu sekcji JG i sumy zaktualizowanych wolumenów pozostałych derów.
     */
    private void updateVolumesForTimestampAndBand(List<AuctionOfferSchedulingUnitDTO> volumes,
        List<Triple<Integer, String, BigDecimal>> currentSchedulingUnitVolumes, List<AuctionOfferBandDataDTO> bands, int bandNo, String timestamp) {

        List<AuctionOfferBandDataDTO> editedBands = getBandDataByTimestampAndBandNumber(bands, timestamp, bandNo);
        if (editedBands.size() != 0) {
            editedBands.sort(Comparator.comparing(AuctionOfferBandDataDTO::getVolume).reversed());
            BigDecimal volumeSum = BigDecimal.ZERO;
            for (int i = 0; i < editedBands.size() - 1; i++) {
                AuctionOfferBandDataDTO band = editedBands.get(i);
                BigDecimal schedulingUnitVolumeToUpdate = getSchedulingUnitVolumeToUpdate(volumes, band.getBandNumber(), band.getHourNumber());
                BigDecimal currentSchedulingUnitVolume = getCurrentSchedulingUnitVolume(currentSchedulingUnitVolumes, band.getBandNumber(), band.getHourNumber());
                BigDecimal derAcceptedVolume = band.getAcceptedVolume();
                BigDecimal newVolume = recalculateAcceptedVolume(currentSchedulingUnitVolume, schedulingUnitVolumeToUpdate, derAcceptedVolume);
                volumeSum = volumeSum.add(newVolume);
                band.setAcceptedVolume(newVolume);
            }
            AuctionOfferBandDataDTO band = editedBands.get(editedBands.size()-1);
            BigDecimal schedulingUnitVolumeToUpdate = getSchedulingUnitVolumeToUpdate(volumes, band.getBandNumber(), band.getHourNumber());
            band.setAcceptedVolume(schedulingUnitVolumeToUpdate.subtract(volumeSum));
        }
    }

    /**
     *  Metoda pobiera listę pasm i zwraca listę obiektów AuctionOfferBandDataDTO dla konkretnego pasma i timestampa
     */
    private List<AuctionOfferBandDataDTO> getBandDataByTimestampAndBandNumber(List<AuctionOfferBandDataDTO> bands, String timestamp, int bandNumber) {
        return bands.stream().filter(b -> Objects.equals(b.getBandNumber(), bandNumber) && Objects.equals(b.getHourNumber(), timestamp)).collect(Collectors.toList());
    }

    /**
     * Korekcja zaakceptowanego wolumenu o współczynnik GDF.
     * Współczynnik GDF jest wynikiem dzielenia wartości wolumenu zaakceptowanego dla konkretnego dera przez początkowy wolumen jednostki grafikowej.
     * Aby otrzymać wolumen zaakceptowany dla konkretnego dera, należy pomnożyć zmieniony wolumen jednostki grafikowej przez współczynnik GDF.
     */
    private BigDecimal recalculateAcceptedVolume(BigDecimal currentSchedulingUnitVolume, BigDecimal schedulingUnitVolumeToUpdate, BigDecimal derAcceptedVolume) {
        MathContext mc = new MathContext(3);
        if (Objects.isNull(derAcceptedVolume) || derAcceptedVolume.compareTo(BigDecimal.ZERO) == 0 ||
            Objects.isNull(currentSchedulingUnitVolume) || currentSchedulingUnitVolume.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return schedulingUnitVolumeToUpdate.divide(currentSchedulingUnitVolume, mc).multiply(derAcceptedVolume);
    }

    /**
     * Status aukcji po imporcie zmienia sie następująco:
     * - gdy wszystkie wolumeny jednostki grafikowej są rowne 0 w pasmach nie zerowych -> REJECTED,
     * - gdy nie wszystie wolumeny jednostki grafikowej są rowne 0 w pasmach nie zerowych -> ACCEPTED
     */
    private void changeOfferStatus(AuctionDayAheadOfferDTO auctionDayAheadOfferDTO) {
        // Pogrupowane Wolumeny Jednostki Grafikowej po pasmie i timestampi'e
        List<Triple<Integer, String, BigDecimal>> suVolumes = groupingSuVolumesByBandNumberAndTimestamp(auctionDayAheadOfferDTO);
        long nonZeroVolumes = suVolumes.stream()
            .filter(volume -> volume.getLeft() != 0)
            .filter(volume -> Objects.nonNull(volume.getRight()))
            .filter(volume -> volume.getRight().compareTo(BigDecimal.ZERO) != 0)
            .count();
        if (nonZeroVolumes == 0) {
            auctionOfferService.updateStatus(REJECTED, Collections.singletonList(auctionDayAheadOfferDTO.getId()));
            log.info("changeOfferStatus() All volumes in schedulingUnit equals 0. Set status={} for offerID={}", REJECTED, auctionDayAheadOfferDTO.getId());
        } else {
            auctionOfferService.updateStatus(AuctionOfferStatus.ACCEPTED, Collections.singletonList(auctionDayAheadOfferDTO.getId()));
            log.info("changeOfferStatus() Set status={} for offerID={}", ACCEPTED, auctionDayAheadOfferDTO.getId());
        }
    }

    /**
     * Grupowanie aktualnie zapisanych wartosci schedulingUnitVolume po pasmie i timestamp'ie
     */
    private List<Triple<Integer, String, BigDecimal>> groupingSuVolumesByBandNumberAndTimestamp(AuctionDayAheadOfferDTO daOfferToUpdate) {
        return daOfferToUpdate.getDers().stream().flatMap(d -> d.getBandData().stream())
            .map(b -> Triple.of(b.getBandNumber(), b.getHourNumber(), sumBandAcceptedVolumeInTimestamp(daOfferToUpdate, b.getBandNumber(), b.getHourNumber())))
            .collect(Collectors.toList());
    }

    private BigDecimal getCurrentSchedulingUnitVolume(List<Triple<Integer, String, BigDecimal>> currentSchedulingUnitVolumes, int bandNumber, String timestamp) {
        return currentSchedulingUnitVolumes.stream()
            .filter(s -> s.getLeft().equals(bandNumber))
            .filter(s -> s.getMiddle().equals(timestamp))
            .map(Triple::getRight)
            .findFirst().orElse(null);
    }

    /**
     * Metdoa szukająca zaaktualizowanego wolumenu pobranego z skoroszytu po numerze pasma i timestamp'ie.
     * Gdy danego wolumenu nie ma zwracane jest 0.
     */
    private BigDecimal getSchedulingUnitVolumeToUpdate(List<AuctionOfferSchedulingUnitDTO> volumes, int bandNumber, String timestamp) {
        return volumes.stream()
            .filter(d -> d.getBandNumber() == bandNumber)
            .filter(d -> d.getTimestamp().equals(timestamp))
            .map(AuctionOfferSchedulingUnitDTO::getVolume)
            .filter(Objects::nonNull)
            .findFirst()
            .map(v -> new BigDecimal(v.replaceAll(",", ".")))
            .orElse(BigDecimal.ZERO);
    }

    /**
     * Id ofert do aktualizacji zapisane sa w formacie: 1934up
     */
    public long getBidId(String stringBidId) {
        return Long.parseLong(stringBidId.replaceAll("[^\\d]", ""));
    }
}
