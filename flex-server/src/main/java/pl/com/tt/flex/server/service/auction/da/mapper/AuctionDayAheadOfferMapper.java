package pl.com.tt.flex.server.service.auction.da.mapper;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Set;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

import pl.com.tt.flex.model.service.dto.auction.offer.AuctionOfferDTO;
import pl.com.tt.flex.model.service.dto.auction.offer.AuctionOfferMinDTO;
import pl.com.tt.flex.model.service.dto.auction.offer.da.AuctionDayAheadOfferDTO;
import pl.com.tt.flex.server.domain.auction.offer.da.AuctionDayAheadOfferEntity;
import pl.com.tt.flex.server.service.mapper.EntityMapper;
import pl.com.tt.flex.server.service.schedulingUnit.mapper.SchedulingUnitMapper;
import pl.com.tt.flex.server.service.unit.mapper.UnitMapper;

/**
 * Mapper for the entity {@link AuctionDayAheadOfferEntity} and its DTO {@link AuctionOfferDTO}.
 */
@Mapper(componentModel = "spring", uses = {AuctionDayAheadMapper.class,
    SchedulingUnitMapper.class, UnitMapper.class, AuctionOfferDersMapper.class})
public interface AuctionDayAheadOfferMapper extends EntityMapper<AuctionOfferDTO, AuctionDayAheadOfferEntity> {

    @Override
    @Mapping(source = "auctionDayAhead.id", target = "auctionDayAhead")
    @Mapping(source = "schedulingUnit.id", target = "schedulingUnit")
    @Mapping(source = "ders", target = "units")
    AuctionDayAheadOfferEntity toEntity(AuctionOfferDTO dto);

    @Mapping(source = "units", target = "ders")
    @Mapping(source = "entity", target = "volume", qualifiedByName = "getVolumeRange")
    @Mapping(source = "entity", target = "acceptedVolume", qualifiedByName = "getAcceptedVolumeRange")
    @Mapping(source = "entity", target = "volumeTooltipVisible", qualifiedByName = "checkVolumeTootipVisible")
    @Mapping(source = "entity", target = "acceptedVolumeTooltipVisible", qualifiedByName = "checkAcceptedVolumeTootipVisible")
    AuctionOfferDTO toDto(AuctionDayAheadOfferEntity entity);

    @Mapping(source = "schedulingUnit.id", target = "schedulingUnit")
    @Mapping(source = "schedulingUnit.bsp.id", target = "schedulingUnit.bsp.id")
    @Mapping(source = "ders", target = "units")
    AuctionDayAheadOfferEntity toEntityFromDayAhead(AuctionDayAheadOfferDTO dto);

    Set<AuctionDayAheadOfferEntity> toEntityFromDayAhead(Set<AuctionDayAheadOfferDTO> dto);

    @Mapping(target = "ders", source = "units")
    @Mapping(target = "schedulingUnit.schedulingUnitType", ignore = true)
    @Mapping(target = "auctionStatus", source = "auctionDayAhead.status")
    AuctionDayAheadOfferDTO toDayAheadDto(AuctionDayAheadOfferEntity entity);

    @Mapping(target = "potentialId", source = "schedulingUnit.id")
    @Mapping(target = "potentialName", source = "schedulingUnit.name")
    @Mapping(target = "companyName", source = "schedulingUnit.bsp.companyName")
    @Mapping(target = "volume", source = "entity", qualifiedByName = "getVolumeRange")
    AuctionOfferMinDTO toOfferMinDto(AuctionDayAheadOfferEntity entity);

    @Named("getVolumeRange")
    default String getVolumeRange(AuctionDayAheadOfferEntity entity) {
        if(Objects.equals(entity.getVolumeFrom(), BigDecimal.ZERO)){
            return entity.getVolumeTo().toString();
        } else if (Objects.equals(entity.getVolumeTo(), BigDecimal.ZERO)){
            return entity.getVolumeFrom().toString();
        }
        return entity.getVolumeTo().toString() + " / " + entity.getVolumeFrom().toString();
    }

    @Named("getAcceptedVolumeRange")
    default String getAcceptedVolumeRange(AuctionDayAheadOfferEntity entity) {
        if(Objects.equals(entity.getAcceptedVolumeFrom(), BigDecimal.ZERO)){
            return entity.getAcceptedVolumeTo().toString();
        } else if (Objects.equals(entity.getAcceptedVolumeTo(), BigDecimal.ZERO)){
            return entity.getAcceptedVolumeFrom().toString();
        }
        return entity.getAcceptedVolumeTo().toString() + " / " + entity.getAcceptedVolumeFrom().toString();
    }

    @Named("checkVolumeTootipVisible")
    default Boolean checkVolumeTootipVisible(AuctionDayAheadOfferEntity entity) {
        if(Objects.equals(entity.getVolumeFrom(), BigDecimal.ZERO) || Objects.equals(entity.getVolumeTo(), BigDecimal.ZERO)){
            return false;
        }
        return true;
    }

    @Named("checkAcceptedVolumeTootipVisible")
    default Boolean checkAcceptedVolumeTootipVisible(AuctionDayAheadOfferEntity entity) {
        if(Objects.equals(entity.getAcceptedVolumeFrom(), BigDecimal.ZERO) || Objects.equals(entity.getAcceptedVolumeTo(), BigDecimal.ZERO)){
            return false;
        }
        return true;
    }

    @AfterMapping
    default void linkDers(@MappingTarget AuctionDayAheadOfferEntity auctionOfferEntity) {
        auctionOfferEntity.getUnits().forEach(der -> der.setOffer(auctionOfferEntity));
    }

    default AuctionDayAheadOfferEntity fromId(Long id) {
        if (id == null) {
            return null;
        }
        AuctionDayAheadOfferEntity offerEntity = new AuctionDayAheadOfferEntity();
        offerEntity.setId(id);
        return offerEntity;
    }

    default BigDecimal fromString(String string) {
        if (string == null) {
            return null;
        }
        double stringParsed;
        try {
            stringParsed = Double.parseDouble(string);
        } catch (NumberFormatException nfe) {
            return null;
        }
        return BigDecimal.valueOf(stringParsed);
    }

    default String fromBigDecimal(BigDecimal bigDecimal) {
        return bigDecimal.toString();
    }
}
