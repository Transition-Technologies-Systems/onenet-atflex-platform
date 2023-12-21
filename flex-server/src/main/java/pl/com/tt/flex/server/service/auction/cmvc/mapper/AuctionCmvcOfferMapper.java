package pl.com.tt.flex.server.service.auction.cmvc.mapper;


import java.math.BigDecimal;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pl.com.tt.flex.model.service.dto.auction.offer.AuctionOfferDTO;
import pl.com.tt.flex.model.service.dto.auction.offer.AuctionOfferMinDTO;
import pl.com.tt.flex.model.service.dto.auction.offer.cmvc.AuctionCmvcOfferDTO;
import pl.com.tt.flex.server.domain.auction.offer.cmvc.AuctionCmvcOfferEntity;
import pl.com.tt.flex.server.service.auction.da.mapper.AuctionDayAheadMapper;
import pl.com.tt.flex.server.service.auction.da.mapper.AuctionOfferDersMapper;
import pl.com.tt.flex.server.service.mapper.EntityMapper;
import pl.com.tt.flex.server.service.potential.mapper.FlexPotentialMapper;
import pl.com.tt.flex.server.service.schedulingUnit.mapper.SchedulingUnitMapper;
import pl.com.tt.flex.server.service.unit.mapper.UnitMapper;

/**
 * Mapper for the entity {@link AuctionCmvcOfferEntity} and its DTO {@link AuctionOfferDTO}.
 */
@Mapper(componentModel = "spring", uses = {AuctionCmvcMapper.class, FlexPotentialMapper.class, AuctionDayAheadMapper.class,
    SchedulingUnitMapper.class, UnitMapper.class, AuctionOfferDersMapper.class})
public interface AuctionCmvcOfferMapper extends EntityMapper<AuctionOfferDTO, AuctionCmvcOfferEntity> {

    @Override
    @Mapping(source = "auctionCmvc.id", target = "auctionCmvc")
    @Mapping(source = "flexPotential.id", target = "flexPotential")
    AuctionCmvcOfferEntity toEntity(AuctionOfferDTO dto);

    AuctionOfferDTO toDto(AuctionCmvcOfferEntity entity);

    @Mapping(source = "auctionCmvc.id", target = "auctionCmvc")
    @Mapping(source = "flexPotential.id", target = "flexPotential")
    @Mapping(source = "flexPotential.fsp.id", target = "flexPotential.fsp.id")
    AuctionCmvcOfferEntity toEntityFromCmvc(AuctionCmvcOfferDTO dto);

    @Mapping(source = "flexPotential.product.shortName", target = "flexPotential.productName")
    AuctionCmvcOfferDTO toCmvcDto(AuctionCmvcOfferEntity entity);

    @Mapping(target = "potentialId", source = "flexPotential.id")
    @Mapping(target = "companyName", source = "flexPotential.fsp.companyName")
    AuctionOfferMinDTO toOfferMinDto(AuctionCmvcOfferEntity entity);

    default AuctionCmvcOfferEntity fromId(Long id) {
        if (id == null) {
            return null;
        }
        AuctionCmvcOfferEntity offerEntity = new AuctionCmvcOfferEntity();
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

    default String fromBigDecimal(BigDecimal number) {
        if (number == null) {
            return null;
        }
        return number.toString();
    }
}
