package pl.com.tt.flex.server.service.auction.da.file;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.Range;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import pl.com.tt.flex.model.service.dto.auction.da.AuctionDayAheadDTO;
import pl.com.tt.flex.model.service.dto.auction.offer.da.AuctionDayAheadOfferDTO;
import pl.com.tt.flex.model.service.dto.file.FileDTO;
import pl.com.tt.flex.model.service.dto.product.type.Direction;
import pl.com.tt.flex.server.common.errors.ObjectValidationException;
import pl.com.tt.flex.server.service.auction.da.AuctionDayAheadService;
import pl.com.tt.flex.server.service.schedulingUnit.SchedulingUnitService;
import pl.com.tt.flex.server.service.schedulingUnit.dto.SchedulingUnitDTO;
import pl.com.tt.flex.server.service.unit.dto.UnitMinDTO;
import pl.com.tt.flex.server.service.unit.selfSchedule.UnitSelfScheduleService;
import pl.com.tt.flex.server.service.user.UserService;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static pl.com.tt.flex.model.service.dto.auction.type.AuctionDayAheadType.CAPACITY;
import static pl.com.tt.flex.model.service.dto.auction.type.AuctionDayAheadType.ENERGY;
import static pl.com.tt.flex.server.service.auction.da.file.factory.DACapacityOfferImportTemplateFactory.getCapacityOfferImportTemplate;
import static pl.com.tt.flex.server.service.auction.da.file.factory.DAEnergyOfferImportTemplateFactory.getEnergyOfferImportTemplate;
import static pl.com.tt.flex.server.service.auction.da.file.reader.DACapacityOfferImportFileReader.readCapacityOffer;
import static pl.com.tt.flex.server.service.auction.da.file.reader.DAEnergyOfferImportFileReader.readEnergyOffer;

@Service
@Slf4j
public class AuctionDayAheadOfferFileServiceImpl implements AuctionDayAheadOfferFileService {

    private final AuctionDayAheadService auctionDayAheadService;
    private final SchedulingUnitService schedulingUnitService;
    private final UserService userService;
    private final UnitSelfScheduleService unitSelfScheduleService;

    public AuctionDayAheadOfferFileServiceImpl(final AuctionDayAheadService auctionDayAheadService,
                                               final SchedulingUnitService schedulingUnitService,
                                               final UserService userService,
                                               final UnitSelfScheduleService unitSelfScheduleService) {
        this.auctionDayAheadService = auctionDayAheadService;
        this.schedulingUnitService = schedulingUnitService;
        this.userService = userService;
        this.unitSelfScheduleService = unitSelfScheduleService;
    }

    @Override
    public FileDTO getOfferImportTemplate(Long auctionId, Long schedulingUnitId, Long fspId) throws IOException {
        log.debug("getOfferImportTemplate() Get auction offer import template for auction id: {}, scheduling unit id: {}", auctionId, schedulingUnitId);
        AuctionDayAheadDTO dbAuction = auctionDayAheadService.findById(auctionId)
            .orElseThrow(() -> new RuntimeException("Cannot find day ahead auction with id: " + auctionId));
        SchedulingUnitDTO dbSchedulingUnit = Optional.ofNullable(fspId).map(id -> schedulingUnitService.findByIdAndBspId(schedulingUnitId, id))
            .orElse(schedulingUnitService.findById(schedulingUnitId))
            .orElseThrow(() -> new RuntimeException("Cannot find scheduling unit with id: " + schedulingUnitId));
        String userLang = userService.getLangKeyForCurrentLoggedUser();

        Map<UnitMinDTO, Map<String, BigDecimal>> derSelfSchedules = unitSelfScheduleService.findVolumesForDersAndDateMap(
            dbSchedulingUnit.getUnits().stream().map(UnitMinDTO::getId).collect(Collectors.toUnmodifiableList()),
            dbAuction.getDeliveryDate()
        );
        Map<Long, Map<String, BigDecimal>> derSelfSchedulesWithDerIdKey = derSelfSchedules.entrySet().stream()
            .collect(Collectors.toMap(s -> s.getKey().getId(), Map.Entry::getValue));
        return getTemplateForAuctionType(dbAuction, dbSchedulingUnit, userLang, derSelfSchedulesWithDerIdKey);
    }

    private FileDTO getTemplateForAuctionType(AuctionDayAheadDTO dbAuction, SchedulingUnitDTO dbSchedulingUnit, String userLang, Map<Long, Map<String, BigDecimal>> derSelfSchedules) throws IOException {
        if (dbAuction.getType().equals(ENERGY)) {
            return getEnergyOfferImportTemplate(dbAuction, dbSchedulingUnit, derSelfSchedules, userLang);
        } else if (dbAuction.getType().equals(CAPACITY)) {
            Direction productDirection = dbAuction.getProduct().getDirection();
            return getCapacityOfferImportTemplate(dbAuction, dbSchedulingUnit, userLang, derSelfSchedules, productDirection);
        }
        throw new IllegalStateException("Unsupported auction type");
    }

    @Override
    public AuctionDayAheadOfferDTO importDayAheadOffer(MultipartFile multipartFile, AuctionDayAheadDTO dbAuction, SchedulingUnitDTO dbSchedulingUnit,
                                                       Long offerId, Range<Instant> deliveryPeriod) throws ObjectValidationException, IOException {
        log.debug("importDayAheadOffer() Import day ahead offer for auction id: {}, scheduling unit id: {}", dbAuction.getId(), dbSchedulingUnit.getId());
        XSSFWorkbook workbook = new XSSFWorkbook(multipartFile.getInputStream());
        AuctionDayAheadOfferDTO dbOffer = Optional.ofNullable(offerId)
            .map(auctionDayAheadService::findOfferById)
            .map(Optional::get)
            .orElse(null);
        return readOfferForAuctionType(dbAuction, dbSchedulingUnit, workbook, dbOffer, deliveryPeriod);
    }

    private AuctionDayAheadOfferDTO readOfferForAuctionType(AuctionDayAheadDTO dbAuction, SchedulingUnitDTO dbSchedulingUnit, XSSFWorkbook workbook,
                                                            AuctionDayAheadOfferDTO dbOffer, Range<Instant> deliveryPeriod) throws ObjectValidationException {
        if (dbAuction.getType().equals(ENERGY)) {
            return readEnergyOffer(workbook, dbAuction, dbSchedulingUnit, dbOffer, deliveryPeriod);
        } else if (dbAuction.getType().equals(CAPACITY)) {
            Direction productDirection = dbAuction.getProduct().getDirection();
            return readCapacityOffer(dbAuction, dbSchedulingUnit, workbook, dbOffer, deliveryPeriod, productDirection);
        }
        throw new IllegalStateException("Unsupported auction type");
    }

}
