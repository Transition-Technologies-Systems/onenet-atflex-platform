package pl.com.tt.flex.server.service.algorithm.agnoAlgorithm.capacity;

import static pl.com.tt.flex.model.service.dto.algorithm.AlgorithmType.PBCM;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.com.tt.flex.model.service.dto.file.FileDTO;
import pl.com.tt.flex.server.dataimport.ImportDataException;
import pl.com.tt.flex.server.dataimport.factory.DataImportFactory;
import pl.com.tt.flex.server.dataimport.factory.DataImportFormat;
import pl.com.tt.flex.server.domain.algorithm.AlgorithmEvaluationEntity;
import pl.com.tt.flex.server.service.algorithm.AlgorithmEvaluationService;
import pl.com.tt.flex.server.service.importData.algorithm.AlgorithmPcbmImportData;
import pl.com.tt.flex.server.util.AuctionDayAheadDataUtil;
import pl.com.tt.flex.server.validator.algorithm.agnoAlgorithm.AlgorithmImportValidator;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

@RequiredArgsConstructor
@Slf4j
@Service
@Transactional
public class CapacityAgnoAlgorithmResultsServiceImpl implements CapacityAgnoAlgorithmResultsService {
    private final DataImportFactory dataImportFactory;
    private final AlgorithmImportValidator algorithmImportValidator;
    private final AlgorithmEvaluationService algorithmEvaluationService;

    @Override
    @Transactional
    public void parsePbcmAlgorithmResults(List<FileDTO> files, Long evaluationId, String langKey) {
        log.debug("parsePbcmAlgorithmResults() START - begin import of {} files", files.size());
        for (FileDTO file : files) {
            if (PBCM.getFileNamePattern().matcher(file.getFileName()).matches()) {    // zip z plikami wyjściowymi zawiera również pliki nieistotne na tym etapie przetwarzania wyników, zostaną one pominięte aby uniknąć błędów
                try {
                    parseResults(getTimestampFromFilename(file.getFileName()), evaluationId, file, langKey);
                } catch (ImportDataException | IOException e) {
                    log.error("parsePbcmAlgorithmResults() ERROR - error while parsing PBCM results: {}", e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }

    private Long getTimestampFromFilename(String filename) {
        String timestamp = filename.replaceAll("[.][^.]*$", "");
        timestamp = timestamp.substring(timestamp.length() - 2);
        if (timestamp.startsWith("_")) {
            timestamp = timestamp.substring(1);
        }
        return Long.valueOf(timestamp);
    }

    private void parseResults(Long timestampIndex, Long evaluationId, FileDTO file, String langKey) throws IOException, ImportDataException {
        log.debug("parseResults() START - import data from file {}", file.getFileName());
        List<AlgorithmPcbmImportData> pcbmAlgorithmResults = dataImportFactory.getDataImport(AlgorithmPcbmImportData.class, DataImportFormat.XLSX)
            .doImport(file, Locale.forLanguageTag(langKey));
        for (AlgorithmPcbmImportData algorithmPcbmImportData : pcbmAlgorithmResults) {
            algorithmImportValidator.checkPbcmValid(algorithmPcbmImportData);
            updateBid(timestampIndex, evaluationId, algorithmPcbmImportData);
        }
    }

    private void updateBid(Long timestampIndex, Long evaluationId, AlgorithmPcbmImportData algorithmPcbmImportData) {
        AlgorithmEvaluationEntity algorithmEvaluationEntity = algorithmEvaluationService.findAlgorithmEvaluationEntityById(evaluationId);
        algorithmEvaluationEntity.getDaOffers().stream()
            .filter(auctionDayAheadOfferEntity -> auctionDayAheadOfferEntity.getAuctionDayAhead().getProduct().getShortName().equals(algorithmPcbmImportData.getType())
                && auctionDayAheadOfferEntity.getAuctionDayAhead().getProduct().getDirection().name().equalsIgnoreCase(algorithmPcbmImportData.getProductType()))
            .flatMap(auctionDayAheadOfferEntity -> auctionDayAheadOfferEntity.getUnits().stream())
            .filter(auctionOfferDersEntity -> auctionOfferDersEntity.getUnit().getName().equals(algorithmPcbmImportData.getDerName()))
            .flatMap(auctionOfferDersEntity -> auctionOfferDersEntity.getBandData().stream()
                .filter(auctionOfferBandDataEntity -> !auctionOfferBandDataEntity.getBandNumber().equals("0")
                    && auctionOfferBandDataEntity.getHourNumber().equals(String.valueOf(timestampIndex))))
            .forEach(auctionOfferBandDataEntity -> {
                auctionOfferBandDataEntity.setAcceptedVolume(BigDecimal.valueOf(Double.parseDouble(algorithmPcbmImportData.getPower())));
                AuctionDayAheadDataUtil.markBandAsEditedUpdateOfferStatus(auctionOfferBandDataEntity);
            });
    }

}
