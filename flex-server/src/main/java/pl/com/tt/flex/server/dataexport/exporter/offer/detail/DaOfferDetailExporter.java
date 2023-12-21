package pl.com.tt.flex.server.dataexport.exporter.offer.detail;

import lombok.extern.slf4j.Slf4j;

import org.apache.poi.xssf.usermodel.*;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import pl.com.tt.flex.model.service.dto.auction.offer.da.AuctionDayAheadOfferDTO;
import pl.com.tt.flex.model.service.dto.auction.offer.da.AuctionOfferBandDataDTO;
import pl.com.tt.flex.model.service.dto.auction.offer.da.AuctionOfferDersDTO;
import pl.com.tt.flex.server.dataexport.exporter.offer.detail.factory.OfferDetailExporter;
import pl.com.tt.flex.server.service.auction.da.AuctionDayAheadService;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static pl.com.tt.flex.server.dataexport.util.CellUtils.*;
import static pl.com.tt.flex.server.util.AuctionDayAheadDataUtil.existTimestampForBand;
import static pl.com.tt.flex.server.util.DateUtil.EXTRA_HOUR_CONSTANT;

@Component
@Slf4j
public class DaOfferDetailExporter extends AbstractDaOfferDetailExporter implements OfferDetailExporter {

    public Locale locale;

    public DaOfferDetailExporter(@Lazy AuctionDayAheadService auctionDayAheadService, MessageSource messageSource) {
        super(auctionDayAheadService, messageSource);
    }

    protected void fillSchedulingUnitsVolumeAndPrice(Integer bandNumber, XSSFRow row, AuctionDayAheadOfferDTO offerDTO, boolean isEdited) {
        AtomicInteger startCell = new AtomicInteger(3);
        for (int i = 1; i <= 25; i++) {
            String timestamp = i == 25 ? EXTRA_HOUR_CONSTANT : String.valueOf(i);
            if (existTimestampForBand(offerDTO, bandNumber, timestamp)) {
                setSumOfVolumesForCell(offerDTO, bandNumber, timestamp, row.createCell(startCell.getAndIncrement()), isEdited);
                setPriceForCell(offerDTO, bandNumber, timestamp, row.createCell(startCell.getAndIncrement()), isEdited);
            } else {
                startCell.getAndIncrement();
                startCell.getAndIncrement();
            }
        }
    }

    /**
     * Uzupelnienie dla danego pasma timestampo'w z cena i wolumenem dla danego DERa
     */
    protected void fillBandDetail(XSSFSheet sheet, AtomicInteger currentRow, int bandNumberCell, AuctionOfferDersDTO der,
                                Map.Entry<Integer, List<AuctionOfferBandDataDTO>> band, Integer bandNumber,
                                boolean isEdited) {
        log.info("fillBandDetail() Start - Fill band der detail for: bandNumber={}, derId={}, derName={}", bandNumber, der.getDer().getId(), der.getDer().getName());
        int currentRowIndex = currentRow.get();
        addStringCellValueIfExist(sheet, currentRowIndex, bandNumberCell, getBandStr(bandNumber));
        List<AuctionOfferBandDataDTO> bandTimestamps = band.getValue();
        AtomicInteger currentCell = new AtomicInteger(3);
        for (int i = 1; i <= 25; i++) {
            String timestamp = i == 25 ? EXTRA_HOUR_CONSTANT : String.valueOf(i);
            Optional<AuctionOfferBandDataDTO> optTimestamp = bandTimestamps.stream().filter(t -> t.getHourNumber().equals(timestamp)).findFirst();
            int volumeCell = currentCell.getAndIncrement();
            int priceCell = currentCell.getAndIncrement();
            if (optTimestamp.isPresent()) {
                AuctionOfferBandDataDTO timestampValue = optTimestamp.get();
                log.debug("fillBandDetail() Fill timestamp: {}", timestampValue.getHourNumber());
                String volume = getStringFromBigDecimal(isEdited ? timestampValue.getAcceptedVolume() : timestampValue.getVolume());
                String price = getStringFromBigDecimal(isEdited ? timestampValue.getAcceptedPrice() : timestampValue.getPrice());
                addStringCellValueIfExist(sheet, currentRowIndex, volumeCell, volume);
                addStringCellValueIfExist(sheet, currentRowIndex, priceCell, price);
            } else {
                log.debug("fillBandDetail() Not found timestamp -> {}", timestamp);
            }
        }
        log.info("fillBandDetail() End - Fill band der detail for: bandNumber={}, derId={}, derName={}", bandNumber, der.getDer().getId(), der.getDer().getName());
    }

    /**
     * Metoda odpowiedzialan za uzupelnienie znakow Wolumen ('W') i Cena ('P') w timestamp'ach
     */
    protected void fillVolumeAndPriceSign(XSSFSheet sheet, AtomicInteger currentRow, int startCell) {
        AtomicInteger currentCell = new AtomicInteger(startCell);
        log.debug("fillVolumeAndPriceSign() Start -  Fill volume and price sign: row={}, startCell={}", currentRow.get(), startCell);
        String volumeShortcut = getVolumeShortcut();
        String priceShortcut = getPriceShortcut();

        for (int i = 1; i <= 25; i++) {
            addStringCellValueIfExist(sheet, currentRow.get(), currentCell.getAndIncrement(), volumeShortcut);
            addStringCellValueIfExist(sheet, currentRow.get(), currentCell.getAndIncrement(), priceShortcut);
        }
        log.debug("fillVolumeAndPriceSign() End - Fill volume and price sign: row={}, startCell={}, endCell={}", currentRow.get(), startCell, currentCell.get());
        currentRow.getAndIncrement();
    }
}
