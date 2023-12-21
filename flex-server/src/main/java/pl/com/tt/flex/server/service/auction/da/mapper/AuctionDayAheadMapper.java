package pl.com.tt.flex.server.service.auction.da.mapper;


import org.mapstruct.Mapper;
import pl.com.tt.flex.model.service.dto.auction.da.AuctionDayAheadDTO;
import pl.com.tt.flex.server.domain.auction.da.AuctionDayAheadEntity;
import pl.com.tt.flex.server.service.auction.da.series.mapper.AuctionsSeriesMapper;
import pl.com.tt.flex.server.service.mapper.EntityMapper;

/**
 * Mapper for the entity {@link AuctionDayAheadEntity} and its DTO {@link AuctionDayAheadDTO}.
 */
@Mapper(componentModel = "spring", uses = {AuctionsSeriesMapper.class})
public interface AuctionDayAheadMapper extends EntityMapper<AuctionDayAheadDTO, AuctionDayAheadEntity> {

    AuctionDayAheadDTO toDto(AuctionDayAheadEntity auctionDayAheadEntity);

    AuctionDayAheadEntity toEntity(AuctionDayAheadDTO auctionDayAheadDTO);

    default AuctionDayAheadEntity fromId(Long id) {
        if (id == null) {
            return null;
        }
        AuctionDayAheadEntity auctionDayAheadEntity = new AuctionDayAheadEntity();
        auctionDayAheadEntity.setId(id);
        return auctionDayAheadEntity;
    }
}
