package pl.com.tt.flex.server.service.algorithm.agnoAlgorithm;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pl.com.tt.flex.model.service.dto.MinimalDTO;
import pl.com.tt.flex.model.service.dto.algorithm.AlgEvaluationModuleDTO;
import pl.com.tt.flex.model.service.dto.algorithm.AlgorithmEvaluationConfigDTO;
import pl.com.tt.flex.model.service.dto.algorithm.AlgorithmEvaluationDTO;
import pl.com.tt.flex.model.service.dto.auction.offer.da.AuctionDayAheadOfferDTO;
import pl.com.tt.flex.model.service.dto.auction.offer.da.AuctionOfferBandDataDTO;
import pl.com.tt.flex.model.service.dto.auction.offer.da.AuctionOfferDersDTO;
import pl.com.tt.flex.model.service.dto.auction.type.AuctionOfferType;
import pl.com.tt.flex.model.service.dto.file.FileDTO;
import pl.com.tt.flex.model.service.dto.localization.LocalizationTypeDTO;
import pl.com.tt.flex.model.service.dto.product.type.Direction;
import pl.com.tt.flex.server.service.algorithm.agnoAlgorithm.dto.*;
import pl.com.tt.flex.server.service.product.dto.ProductDTO;
import pl.com.tt.flex.server.service.product.forecastedPrices.dto.ForecastedPricesDTO;
import pl.com.tt.flex.server.service.unit.UnitService;
import pl.com.tt.flex.server.service.unit.dto.UnitDTO;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static pl.com.tt.flex.server.util.DateUtil.sortedHourNumbers;

@Slf4j
@AllArgsConstructor
public abstract class AlgorithmAbstract {

    private final UnitService unitService;

    protected AlgEvaluationModuleDTO getAlgEvaluationModuleDTO(AlgorithmEvaluationConfigDTO evaluationConfigDTO, FileDTO inputFiles, AlgorithmEvaluationDTO evaluationDTO) {
        AlgEvaluationModuleDTO algEvaluationModuleDTO = new AlgEvaluationModuleDTO();
        algEvaluationModuleDTO.setEvaluationId(evaluationDTO.getEvaluationId());
        algEvaluationModuleDTO.setTypeOfAlgorithm(evaluationConfigDTO.getAlgorithmType());
        algEvaluationModuleDTO.setInputFilesZip(inputFiles);
        algEvaluationModuleDTO.setKdmModelId(evaluationConfigDTO.getKdmModelId());
        return algEvaluationModuleDTO;
    }

    /**
     * uzupelnienie wymaganych pol dla pliku wsadowego zwiazanych z Oferta (dla danej godziny gieldowej)
     * kazde pasmo dodawane jest oddzielnie
     */
    protected void setAgnoOfferDetailDTO(AgnoHourNumberDTO agnoHourNumberDTO, AuctionDayAheadOfferDTO offer,
                                         List<AuctionOfferBandDataDTO> bandData, UnitDTO der, ProductDTO productDTO) {
        getNonZeroBandData(bandData).forEach(band -> {
            AgnoOfferDetailDTO agnoOfferDetailDTO = new AgnoOfferDetailDTO();
            agnoOfferDetailDTO.setProductShortName(productDTO.getShortName());
            agnoOfferDetailDTO.setProductDirection(getOfferDirection(offer, productDTO, band));
            agnoOfferDetailDTO.setDerName(der.getName());
            agnoOfferDetailDTO.setBandVolume(band.getVolume());
            agnoOfferDetailDTO.setBandPrice(band.getPrice());
            agnoHourNumberDTO.addAgnoOfferDetail(agnoOfferDetailDTO);
            log.debug("setAgnoOfferDetailDTO() Add offer detail with: OfferId: {}, DerID: {}, HourNumber: {}, BandNumber: {}",
                offer.getId(), der.getId(), agnoHourNumberDTO.getHourNumber(), band.getBandNumber());
        });
    }

    /**
     * Dla oferty zlozonej na Capacity kierunek ustalany jest na podstawie kierunku produktu
     * Dla oferty zlozonej na Energie kierunek ustalany jest na podstawie pasma: pasmo > 0 -> UP , pasmo < 0 -> DOWN
     */
    protected Direction getOfferDirection(AuctionDayAheadOfferDTO offer, ProductDTO productDTO, AuctionOfferBandDataDTO band) {
        if (offer.getType().equals(AuctionOfferType.CAPACITY)) {
            return productDTO.getDirection();
        } else {
            return band.getBandNumber() > 0 ? Direction.UP : Direction.DOWN;
        }
    }

    /**
     * uzupelnienie wymaganych pol dla pliku wsadowego zwiazanych z Produktem (dla danej godziny gieldowej)
     */
    protected void setProductDetail(AgnoHourNumberDTO agnoHourNumberDTO, ProductDTO productDTO, ForecastedPricesDTO forecastedPrices, String hourNumber) {
        if (agnoHourNumberDTO.getProductList().stream().noneMatch(p -> p.getId().equals(productDTO.getId()))) {
            AgnoProductDetailDTO agnoProductDetailDTO = new AgnoProductDetailDTO();
            agnoProductDetailDTO.setId(productDTO.getId());
            agnoProductDetailDTO.setProductName(productDTO.getShortName());
            agnoProductDetailDTO.setProductDirection(productDTO.getDirection());
            BigDecimal forecastedPrice = getForecastedPriceByProductAndHourNumber(forecastedPrices.getPrices(), hourNumber).orElseThrow(() ->
                new IllegalStateException(String.format("Cannot find forecasted price for: productId %s, hourNumber: %s ", productDTO.getId(), hourNumber)));
            agnoProductDetailDTO.setForecastedPrice(forecastedPrice);
            agnoHourNumberDTO.addProductToHour(agnoProductDetailDTO);
            log.debug("setProductDetail() Add product detail. productId: {}, hourNumber: {}", productDTO.getId(), hourNumber);
        }
    }

    /**
     * uzupelnienie wymaganych pol dla pliku wsadowego zwiazanych z DERem (dla danej godziny gieldowej)
     */
    protected void setUnitDetail(AgnoHourNumberDTO agnoHourNumberDTO, UnitDTO der, List<MinimalDTO<String, BigDecimal>> selfScheduleVolumes,
                                 String hourNumber, boolean lvModel, boolean isDerHasCorrectPowerStationButIncorrectPointOfConnectionWithLV) {
        if (agnoHourNumberDTO.getDerList().stream().noneMatch(d -> d.getId().equals(der.getId()))) {
            AgnoDerDetailDTO agnoDerDetailDTO = new AgnoDerDetailDTO();
            agnoDerDetailDTO.setId(der.getId());
            agnoDerDetailDTO.setName(der.getName());
            agnoDerDetailDTO.setPMin(der.getPMin());
            agnoDerDetailDTO.setPMax(der.getSourcePower());
            agnoDerDetailDTO.setQMin(der.getQMin());
            agnoDerDetailDTO.setQMax(der.getQMax());
            LocalizationTypeDTO powerStationType = getPowerStationType(der, lvModel, isDerHasCorrectPowerStationButIncorrectPointOfConnectionWithLV);
            agnoDerDetailDTO.setPowerStationType(powerStationType);
            BigDecimal selfScheduleVolume = getSelfScheduleVolumeByHourNumber(selfScheduleVolumes, hourNumber).orElseThrow(() ->
                new IllegalStateException(String.format("Cannot find self schedule volume for: derId %s, hourNumber: %s ", der.getId(), hourNumber)));
            agnoDerDetailDTO.setSelfScheduleVolume(selfScheduleVolume);
            agnoHourNumberDTO.addDerToHour(agnoDerDetailDTO);
            log.debug("setUnitDetail() Add der detail. derId: {}, hourNumber: {}", der.getId(), hourNumber);
        }
    }

    /**
     * Jeżeli lvModel == true :
     * - zwracany jest PointOfConnectionWithLv jeżeli jest uzupełniony w przeciwnym wypadku zwracany jest PowerStationType
     * Jeżeli lvModel == false lub DER ma poprawne Power station i niepoprawne Point of connection with LV :
     * -zwracany jest PowerStationType
     */
    private LocalizationTypeDTO getPowerStationType(UnitDTO der, boolean lvModel, boolean isDerHasCorrectPowerStationButIncorrectPointOfConnectionWithLV) {
        if (lvModel && !isDerHasCorrectPowerStationButIncorrectPointOfConnectionWithLV) {
            return der.getPointOfConnectionWithLvTypes().stream()
                .findFirst()
                .orElse(getPowerStationType(der));
        } else if (isDerHasCorrectPowerStationButIncorrectPointOfConnectionWithLV) {
            return AlgorithmAbstract.this.getPowerStationType(der);
        }
        return AlgorithmAbstract.this.getPowerStationType(der);
    }

    private LocalizationTypeDTO getPowerStationType(UnitDTO der) {
        return der.getPowerStationTypes().stream()
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Cannot find Power Station MV/lV number for Der with id: " + der.getId()));
    }

    /**
     * pobieranie wolumenu z planu pracy Dera na dana godzine gieldowa
     */
    private Optional<BigDecimal> getSelfScheduleVolumeByHourNumber(List<MinimalDTO<String, BigDecimal>> selfScheduleVolumes, String hourNumber) {
        return selfScheduleVolumes.stream().filter(s -> s.getId().equals(hourNumber)).findFirst().map(MinimalDTO::getValue);
    }

    /**
     * pobieranie ceny prognozowanej ceny Produktu na dana godzine gieldowa
     */
    private Optional<BigDecimal> getForecastedPriceByProductAndHourNumber(List<MinimalDTO<String, BigDecimal>> forecastedPrices, String hourNumber) {
        return forecastedPrices.stream().filter(s -> s.getId().equals(hourNumber)).findFirst().map(MinimalDTO::getValue);

    }

    /**
     * W Capacity moze byc tylko jedno nie zerowe PASMO, w Energy moze byc kilka pasm
     */
    private List<AuctionOfferBandDataDTO> getNonZeroBandData(List<AuctionOfferBandDataDTO> bandData) {
        return bandData.stream().filter(band -> band.getBandNumber() != 0).collect(Collectors.toList());
    }

    protected Map<String, List<AuctionOfferBandDataDTO>> getBandDataGroupingByHourNumber(AuctionOfferDersDTO offerDer) {
        return offerDer.getBandData().stream().collect(groupingBy(AuctionOfferBandDataDTO::getHourNumber))
            .entrySet().stream().sorted(Comparator.comparingInt(c -> sortedHourNumbers.indexOf(c.getKey())))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
    }

    protected AgnoHourNumberDTO getAgnoHourNumberDTO(AgnoCouplingPointDTO agnoCouplingPointDTO, String hourNumber) {
        return agnoCouplingPointDTO.getAgnoHourNumbers()
            .stream()
            .filter(hourDetail -> hourDetail.getHourNumber().equals(hourNumber))
            .findFirst()
            .orElse(new AgnoHourNumberDTO(hourNumber));
    }

    protected AgnoCouplingPointDTO getAgnoCouplingPointDTO(AgnoInputDataDTO agnoInputDataDTO, LocalizationTypeDTO cpId) {
        return agnoInputDataDTO.getCouplingPoints()
            .stream()
            .filter(cp -> cp.getCouplingPointId().equals(cpId))
            .findFirst()
            .orElse(new AgnoCouplingPointDTO(cpId));
    }
}
