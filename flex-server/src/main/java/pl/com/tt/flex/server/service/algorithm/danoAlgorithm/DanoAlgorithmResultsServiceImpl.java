package pl.com.tt.flex.server.service.algorithm.danoAlgorithm;

import static pl.com.tt.flex.model.service.dto.algorithm.AlgorithmType.DANO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.com.tt.flex.model.service.dto.file.FileDTO;
import pl.com.tt.flex.server.dataimport.ImportDataException;
import pl.com.tt.flex.server.dataimport.factory.DataImportFactory;
import pl.com.tt.flex.server.dataimport.factory.DataImportFormat;
import pl.com.tt.flex.server.domain.algorithm.AlgorithmEvaluationEntity;
import pl.com.tt.flex.server.domain.auction.offer.da.AuctionOfferBandDataEntity;
import pl.com.tt.flex.server.service.algorithm.AlgorithmEvaluationService;
import pl.com.tt.flex.server.service.importData.algorithm.AlgorithmDanoImportData;
import pl.com.tt.flex.server.util.AuctionDayAheadDataUtil;
import pl.com.tt.flex.server.validator.algorithm.agnoAlgorithm.AlgorithmImportValidator;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
@Service
@Transactional
public class DanoAlgorithmResultsServiceImpl implements DanoAlgorithmResultsService {
    private final DataImportFactory dataImportFactory;
    private final AlgorithmImportValidator algorithmImportValidator;
    private final AlgorithmEvaluationService algorithmEvaluationService;
    private final Pattern timestampPattern = Pattern.compile("dgia_input_dano_\\S*_(\\d+).xlsx");

    @Override
    public void parseDanoAlgorithmResults(List<FileDTO> files, Long evaluationId, String langKey) throws IOException, ImportDataException {
        log.debug("parseDanoAlgorithmResults() START - begin import of {} files", files.size());
        for (FileDTO file : files) {
            if (DANO.getFileNamePattern().matcher(file.getFileName()).matches()) {  // zip z plikami wyjściowymi zawiera również pliki nieistotne na tym etapie przetwarzania wyników, zostaną one pominięte aby uniknąć błędów
                long timestampIndex = getTimestampFromFilename(file);
                parseResults(timestampIndex, evaluationId, file, langKey);
            }
        }
    }

    private void parseResults(Long timestampIndex, Long evaluationId, FileDTO file, String langKey) throws IOException, ImportDataException {
        log.debug("parseResults() START - import data from file {}", file.getFileName());
        List<AlgorithmDanoImportData> danoAlgorithmResults = dataImportFactory.getDataImport(AlgorithmDanoImportData.class, DataImportFormat.XLSX).doImport(file, Locale.forLanguageTag(langKey));
        for(AlgorithmDanoImportData result : danoAlgorithmResults) {
            algorithmImportValidator.checkDanoValid(evaluationId, result);
            updateBid(timestampIndex, evaluationId, danoAlgorithmResults);
        }
    }

    /**
     * Jezeli w importowanym pliku nie ma DERa dla danej oferty, to ustawiamy w acceptedVolume 0.
     * Identyfikacja miejsca zaktualizowania wolumenu(power) powinna się odbywać za pomocą
     * trzech parametrów: nazwy DERa, kierunku pasma oraz ceny(te atrybuty nie zostaną zmienione przez algorytm).
     * */
    private void updateBid(Long timestampIndex, Long evaluationId, List<AlgorithmDanoImportData> danoAlgorithmResults) {
        AlgorithmEvaluationEntity algorithmEvaluationEntity = algorithmEvaluationService.findAlgorithmEvaluationEntityById(evaluationId);
        List<AuctionOfferBandDataEntity> bandsToUpdate = algorithmEvaluationEntity.getDaOffers().stream()
            .flatMap(auctionDayAheadOfferEntity -> auctionDayAheadOfferEntity.getUnits().stream())
            .flatMap(auctionOfferDersEntity -> auctionOfferDersEntity.getBandData().stream().filter(auctionOfferBandDataEntity -> !auctionOfferBandDataEntity.getBandNumber().equals("0")
                && auctionOfferBandDataEntity.getHourNumber().equals(String.valueOf(timestampIndex))))
            .collect(Collectors.toList());
        for (AuctionOfferBandDataEntity bandData : bandsToUpdate) {
            String derName = bandData.getOfferDer().getUnit().getName();
            String bandNumber = bandData.getBandNumber();
            if (isExistDerInResult(danoAlgorithmResults, derName)) {
                String powerToUpdate = danoAlgorithmResults.stream()
                    .filter(result -> result.getDerName().equals(derName))
                    .filter(result -> result.getProductType().equals(getBandType(bandNumber)))
                    .filter(result -> BigDecimal.valueOf(Double.parseDouble(result.getPrice())).compareTo(bandData.getPrice()) == 0)
                    .map(AlgorithmDanoImportData::getPower)
                    .findFirst().get();
                bandData.setAcceptedVolume(BigDecimal.valueOf(Double.parseDouble(powerToUpdate)));
                AuctionDayAheadDataUtil.markBandAsEditedUpdateOfferStatus(bandData);
            }
        }
    }

    private boolean isExistDerInResult(List<AlgorithmDanoImportData> danoAlgorithmResults, String derName) {
        return danoAlgorithmResults.stream().anyMatch(result -> result.getDerName().equals(derName));
    }

    private String getBandType(String bandNumber) {
        int nr = Integer.parseInt(bandNumber);
        if (nr > 0) {
            return "up";
        } else if (nr < 0) {
            return "down";
        } else {
            return "ss";
        }
    }

    private long getTimestampFromFilename(FileDTO file) {
        Matcher matcher = timestampPattern.matcher(file.getFileName());
        matcher.matches();
        return Long.parseLong(matcher.group(1));
    }
}
