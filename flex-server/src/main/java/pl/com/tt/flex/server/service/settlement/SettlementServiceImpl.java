package pl.com.tt.flex.server.service.settlement;

import static pl.com.tt.flex.server.dataexport.exporter.offer.detail.enumeration.LevelOfDetail.STANDARD_DETAIL_SHEET;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.context.MessageSource;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pl.com.tt.flex.model.service.dto.file.FileDTO;
import pl.com.tt.flex.server.dataexport.exporter.DataExporter;
import pl.com.tt.flex.server.dataexport.exporter.SettlementDataExporter;
import pl.com.tt.flex.server.dataexport.factory.DataExporterFactory;
import pl.com.tt.flex.server.dataexport.factory.DataExporterFormat;
import pl.com.tt.flex.server.dataimport.impl.SettlementImport;
import pl.com.tt.flex.server.domain.auction.offer.cmvc.AuctionCmvcOfferEntity;
import pl.com.tt.flex.server.domain.auction.offer.da.AuctionDayAheadOfferEntity;
import pl.com.tt.flex.server.domain.auction.offer.da.AuctionOfferBandDataEntity;
import pl.com.tt.flex.server.domain.auction.offer.da.AuctionOfferDersEntity;
import pl.com.tt.flex.server.domain.screen.enumeration.Screen;
import pl.com.tt.flex.server.domain.settlement.SettlementEntity;
import pl.com.tt.flex.server.domain.settlement.SettlementViewEntity;
import pl.com.tt.flex.server.domain.unit.UnitEntity;
import pl.com.tt.flex.server.repository.AbstractJpaRepository;
import pl.com.tt.flex.server.repository.settlement.SettlementRepository;
import pl.com.tt.flex.server.repository.settlement.SettlementViewRepository;
import pl.com.tt.flex.server.service.common.AbstractServiceImpl;
import pl.com.tt.flex.server.service.mapper.EntityMapper;
import pl.com.tt.flex.server.service.settlement.dto.SettlementDTO;
import pl.com.tt.flex.server.service.settlement.dto.SettlementEditDTO;
import pl.com.tt.flex.server.service.settlement.dto.SettlementViewDTO;
import pl.com.tt.flex.server.service.settlement.mapper.SettlementMapper;
import pl.com.tt.flex.server.service.settlement.mapper.SettlementViewMapper;

@Slf4j
@Service
@RequiredArgsConstructor
public class SettlementServiceImpl extends AbstractServiceImpl<SettlementEntity, SettlementDTO, Long> implements SettlementService {

    private final SettlementRepository settlementRepository;
    private final SettlementViewRepository settlementViewRepository;
    private final SettlementMapper settlementMapper;
    private final SettlementViewMapper settlementViewMapper;
    private final DataExporterFactory dataExporterFactory;
    private final SettlementImport settlementImport;
    private final MessageSource messageSource;

    @Override
    public SettlementEditDTO getSettlementMin(Long id) {
        return settlementViewMapper.toSettlementEditDTO(getViewById(id));
    }

    @Override
    public SettlementViewDTO getSettlementView(Long id) {
        return settlementViewMapper.toDto(getViewById(id));
    }

    @Override
    public SettlementViewDTO getSettlementView(Long id, Long fspId) {
        return settlementViewMapper.toDto(getViewById(id, fspId));
    }

    @Override
    @Transactional
    public void updateSettlement(Long id, SettlementEditDTO settlementMin) {
        SettlementEntity settlement = getEntityById(id);
        settlement.setActivatedVolume(settlementMin.getActivatedVolume());
        settlement.setSettlementAmount(settlementMin.getSettlementAmount());
    }

    @Override
    public void generateSettlementsForOffer(AuctionCmvcOfferEntity cmvcOffer) {
        Set<SettlementEntity> entitiesToSave = new HashSet<>();
        settlementRepository.deleteByOfferId(cmvcOffer.getId());
        for (UnitEntity der : cmvcOffer.getFlexPotential().getUnits()) {
            entitiesToSave.add(createSettlementEntity(cmvcOffer, der));
        }
        settlementRepository.saveAll(entitiesToSave);
    }

    @Override
    public void generateSettlementsForOffer(AuctionDayAheadOfferEntity daOffer) {
        Set<SettlementEntity> entitiesToSave = new HashSet<>();
        settlementRepository.deleteByOfferId(daOffer.getId());
        for (AuctionOfferDersEntity offerDer : daOffer.getUnits()) {
            boolean derContainsVolume = offerDer.getBandData().stream().filter(band -> !band.getBandNumber().equals("0"))
                .map(AuctionOfferBandDataEntity::getAcceptedVolume).filter(Objects::nonNull).anyMatch(acceptedVolume -> BigDecimal.ZERO.compareTo(acceptedVolume) != 0);
            if (derContainsVolume) {
                entitiesToSave.add(createSettlementEntity(offerDer));
            }
        }
        settlementRepository.saveAll(entitiesToSave);
    }

    @Override
    public FileDTO exportSettlementsToFile(List<SettlementViewDTO> settlementsToExport, String langKey, boolean isOnlyDisplayedData, Screen screen, Pair<Instant, Instant> acceptedDeliveryPeriod) throws IOException {
        DataExporter<SettlementViewDTO> dataExporter = dataExporterFactory.getDataExporter(DataExporterFormat.XLSX, SettlementViewDTO.class, screen);
        Locale locale = Locale.forLanguageTag(langKey);
        FileDTO file = dataExporter.export(settlementsToExport, locale, screen, isOnlyDisplayedData, STANDARD_DETAIL_SHEET);
        return new FileDTO(getFilename(acceptedDeliveryPeriod, locale), file.getBase64StringData());
    }

    @Override
    @Transactional
    public void importSettlementUpdates(MultipartFile[] multipartFiles) throws IOException {
        settlementImport.doImport(multipartFiles).forEach(this::updateEditableFields);
    }

    private void updateEditableFields(SettlementViewDTO settlementViewDTO) {
        settlementRepository.findById(settlementViewDTO.getId()).ifPresent(dbSettlement -> {
            dbSettlement.setActivatedVolume(settlementViewDTO.getActivatedVolume());
            dbSettlement.setSettlementAmount(settlementViewDTO.getSettlementAmount());
        });
    }

    private SettlementEntity getEntityById(Long settlementId) {
        return settlementRepository.findById(settlementId).orElseThrow(() -> new IllegalStateException("Cannot find settlement with id: " + settlementId));
    }

    private SettlementViewEntity getViewById(Long settlementId) {
        return settlementViewRepository.findById(settlementId).orElseThrow(() -> new IllegalStateException("Cannot find settlement view with id: " + settlementId));
    }

    private SettlementViewEntity getViewById(Long settlementId, Long fspId) {
        return Optional.of(getViewById(settlementId)).filter(view -> view.getFspId().equals(fspId))
            .orElseThrow(() -> new IllegalStateException("Cannot find settlement with id: " + settlementId + " available for fsp with id: " + fspId));
    }

    private SettlementEntity createSettlementEntity(AuctionCmvcOfferEntity cmvcOffer, UnitEntity der) {
        return SettlementEntity.builder()
            .unit(der)
            .offerId(cmvcOffer.getId())
            .acceptedVolume(cmvcOffer.getVolume().toString())
            .build();
    }

    private SettlementEntity createSettlementEntity(AuctionOfferDersEntity offerDersEntity) {
        return SettlementEntity.builder()
            .unit(offerDersEntity.getUnit())
            .offerId(offerDersEntity.getOffer().getId())
            .acceptedVolume(calculateAcceptedVolumeForDer(offerDersEntity))
            .build();
    }

    private String calculateAcceptedVolumeForDer(AuctionOfferDersEntity offerDersEntity) {
        List<AuctionOfferBandDataEntity> bands = offerDersEntity.getBandData();
        List<BigDecimal> positiveVolumes = bands.stream()
            .filter(band -> !band.getBandNumber().equals("0") && !band.getBandNumber().startsWith("-"))
            .map(AuctionOfferBandDataEntity::getAcceptedVolume).collect(Collectors.toList());
        List<BigDecimal> negativeVolumes = bands.stream()
            .filter(band -> band.getBandNumber().charAt(0) == '-')
            .map(AuctionOfferBandDataEntity::getAcceptedVolume).collect(Collectors.toList());
        if (!positiveVolumes.isEmpty() && negativeVolumes.isEmpty()) {
            return getSumString(positiveVolumes);
        } else if (positiveVolumes.isEmpty() && !negativeVolumes.isEmpty()) {
            return "-" + getSumString(negativeVolumes);
        } else if (!positiveVolumes.isEmpty() && !negativeVolumes.isEmpty()) {
            return getSumString(positiveVolumes) + "/-" + getSumString(negativeVolumes);
        }
        return "";
    }

    private String getSumString(List<BigDecimal> volumes) {
        return volumes.stream().filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add).toString();
    }

    /** 2023-05-11T22:00:00Z - 2023-05-12T22:00:00Z --> activation_objects_20230512-20230512.xlsx
     * pierwsza data jest interpretowana jako początek dnia, druga jako koniec
     */
    protected String getFilename(Pair<Instant, Instant> acceptedDeliveryPeriod, Locale locale) {
        String filenameFormat = "%s_%s-%s.xlsx"; // filename_dateFrom-dateTo.xlsx
        String datePattern = "yyyyMMdd";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(datePattern).withZone(ZoneId.systemDefault());
        String filename = messageSource.getMessage(SettlementDataExporter.PREFIX + "fileName", null, locale);
        String rangeFromFormatted = formatter.format(acceptedDeliveryPeriod.getFirst());
        String rangeToFormatted = formatter.format(acceptedDeliveryPeriod.getSecond().minus(Duration.ofDays(1)));
        return String.format(filenameFormat, filename, rangeFromFormatted, rangeToFormatted);
    }

    @Override
    public AbstractJpaRepository<SettlementEntity, Long> getRepository() {
        return this.settlementRepository;
    }

    @Override
    public EntityMapper<SettlementDTO, SettlementEntity> getMapper() {
        return this.settlementMapper;
    }

}
