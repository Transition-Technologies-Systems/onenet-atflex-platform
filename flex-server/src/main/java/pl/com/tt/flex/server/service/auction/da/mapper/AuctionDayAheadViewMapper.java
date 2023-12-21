package pl.com.tt.flex.server.service.auction.da.mapper;


import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pl.com.tt.flex.model.service.dto.auction.da.AuctionDayAheadDTO;
import pl.com.tt.flex.server.domain.auction.da.AuctionDayAheadEntity;
import pl.com.tt.flex.server.domain.auction.da.AuctionDayAheadViewEntity;
import pl.com.tt.flex.server.service.mapper.EntityMapper;

/**
 * Mapper for the entity {@link AuctionDayAheadEntity} and its DTO {@link AuctionDayAheadDTO}.
 */
@Mapper(componentModel = "spring")
public interface AuctionDayAheadViewMapper extends EntityMapper<AuctionDayAheadDTO, AuctionDayAheadViewEntity> {

    @Mapping(source = "productId", target = "product.id")
    @Mapping(source = "productMaxBidSize", target = "product.maxBidSize")
    @Mapping(source = "productMinBidSize", target = "product.minBidSize")
    AuctionDayAheadDTO toDto(AuctionDayAheadViewEntity auctionDayAheadViewEntity);

    AuctionDayAheadViewEntity toEntity(AuctionDayAheadDTO auctionDayAheadDTO);

    default AuctionDayAheadViewEntity fromId(Long id) {
        if (id == null) {
            return null;
        }
        AuctionDayAheadViewEntity auctionDayAheadViewEntity = new AuctionDayAheadViewEntity();
        auctionDayAheadViewEntity.setId(id);
        return auctionDayAheadViewEntity;
    }
}
