package pl.com.tt.flex.server.validator.algorithm.danoAlgorithm;

import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import pl.com.tt.flex.model.service.dto.algorithm.AlgorithmEvaluationConfigDTO;
import pl.com.tt.flex.model.service.dto.algorithm.AlgorithmType;
import pl.com.tt.flex.model.service.dto.auction.offer.da.AuctionDayAheadOfferDTO;
import pl.com.tt.flex.model.service.dto.file.FileDTO;
import pl.com.tt.flex.server.common.errors.ObjectValidationException;
import pl.com.tt.flex.server.service.auction.da.AuctionDayAheadService;
import pl.com.tt.flex.server.util.ZipUtil;
import pl.com.tt.flex.server.validator.algorithm.AbstractAlgorithmValidator;
import pl.com.tt.flex.server.web.rest.algorithm.resource.FlexAgnoAlgorithmResource;

import java.util.List;
import java.util.Set;

import static pl.com.tt.flex.model.service.dto.algorithm.AlgorithmType.DANO;
import static pl.com.tt.flex.server.web.rest.errors.ErrorConstants.AGNO_ALGORITHM_CANNOT_BE_RUN_BECAUSE_NOT_CHOOSE_ONLY_ENERGY_RELATED_BIDS;
import static pl.com.tt.flex.server.web.rest.errors.ErrorConstants.AGNO_ALGORITHM_CANNOT_BE_RUN_BECAUSE_NO_BIDS_HAVE_BEEN_SUBMITTED;

@Component
public class DanoAlgorithmValidator extends AbstractAlgorithmValidator {
    private final AuctionDayAheadService auctionDayAheadService;

    public DanoAlgorithmValidator(AuctionDayAheadService auctionDayAheadService,
                                  FlexAgnoAlgorithmResource flexAgnoAlgorithmResource) {
        super(flexAgnoAlgorithmResource);
        this.auctionDayAheadService = auctionDayAheadService;
    }

    public void checkAlgorithmEvaluationConfig(AlgorithmEvaluationConfigDTO configDTO) throws ObjectValidationException {
        if (!CollectionUtils.isEmpty(configDTO.getOffers())) {
            Set<AuctionDayAheadOfferDTO> offers = auctionDayAheadService.findAllOffersById(configDTO.getOffers());
            AlgorithmType algorithmType = configDTO.getAlgorithmType();
            if (algorithmType.equals(DANO) && !offers.stream().allMatch(o -> o.getType().equals(algorithmType.getOfferType()))) {
                throw new ObjectValidationException("Selected offers is not compatible with algorithm with type: " + algorithmType,
                    AGNO_ALGORITHM_CANNOT_BE_RUN_BECAUSE_NOT_CHOOSE_ONLY_ENERGY_RELATED_BIDS);
            }
        }
    }

    public void checkValid(Set<AuctionDayAheadOfferDTO> offers, AlgorithmEvaluationConfigDTO configDTO) throws ObjectValidationException {
        checkOffers(offers);
        checkAlgorithmEvaluationConfig(configDTO);
        checkKdmModel(configDTO, offers);
    }

    public void checkInputFile(FileDTO fileDTO) throws ObjectValidationException {
        List<FileDTO> files = ZipUtil.zipToFiles(fileDTO.getBytesData());
        if (CollectionUtils.isEmpty(files)) {
            throw new ObjectValidationException("The DANO algorithm cannot be run because no bids have been submitted",
                AGNO_ALGORITHM_CANNOT_BE_RUN_BECAUSE_NO_BIDS_HAVE_BEEN_SUBMITTED);
        }
    }

    private void checkOffers(Set<AuctionDayAheadOfferDTO> offers) throws ObjectValidationException {
        if (CollectionUtils.isEmpty(offers)) {
            throw new ObjectValidationException("The DANO algorithm cannot be run because no bids have been submitted",
                AGNO_ALGORITHM_CANNOT_BE_RUN_BECAUSE_NO_BIDS_HAVE_BEEN_SUBMITTED);
        }
    }
}
