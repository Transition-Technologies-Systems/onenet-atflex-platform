package pl.com.tt.flex.server.service.auction.da.mapper;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import pl.com.tt.flex.model.service.dto.auction.offer.da.AuctionOfferDersDTO;
import pl.com.tt.flex.server.domain.auction.offer.da.AuctionOfferDersEntity;
import pl.com.tt.flex.server.service.mapper.EntityMapper;
import pl.com.tt.flex.server.service.unit.mapper.UnitMapper;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper for the entity {@link AuctionOfferDersEntity} and its DTO {@link AuctionOfferDersDTO}.
 */
@Mapper(componentModel = "spring", uses = {UnitMapper.class, AuctionOfferBandDataMapper.class})
public interface AuctionOfferDersMapper extends EntityMapper<AuctionOfferDersDTO, AuctionOfferDersEntity> {

    @Override
    @Mapping(source = "der.id", target = "unit.id")
    @Mapping(source = "der.name", target = "unit.name")
    AuctionOfferDersEntity toEntity(AuctionOfferDersDTO dto);

    @Override
    @Mapping(source = "unit", target = "der", qualifiedByName = "unitEntityToMinDerDto")
    AuctionOfferDersDTO toDto(AuctionOfferDersEntity entity);

    @AfterMapping
    default List<AuctionOfferDersDTO> sortOfferDers(@MappingTarget List<AuctionOfferDersDTO> auctionOfferDersDTO) {
        return auctionOfferDersDTO.stream().sorted(Comparator.comparing(AuctionOfferDersDTO::getId)).collect(Collectors.toList());
    }

    @AfterMapping
    default void linkBandData(@MappingTarget AuctionOfferDersEntity auctionOfferDersEntity) {
        auctionOfferDersEntity.getBandData().forEach(bandData -> bandData.setOfferDer(auctionOfferDersEntity));
    }

}
