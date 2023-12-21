package pl.com.tt.flex.server.service.auction.cmvc.mapper;


import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pl.com.tt.flex.model.service.dto.auction.cmvc.AuctionCmvcDTO;
import pl.com.tt.flex.server.domain.auction.cmvc.AuctionCmvcEntity;
import pl.com.tt.flex.server.service.dictionary.localizationType.mapper.LocalizationTypeMapper;
import pl.com.tt.flex.server.service.mapper.EntityMapper;
import pl.com.tt.flex.server.service.product.mapper.ProductMapper;

/**
 * Mapper for the entity {@link AuctionCmvcEntity} and its DTO {@link AuctionCmvcDTO}.
 */
@Mapper(componentModel = "spring", uses = {ProductMapper.class, LocalizationTypeMapper.class})
public interface AuctionCmvcMapper extends EntityMapper<AuctionCmvcDTO, AuctionCmvcEntity> {

    AuctionCmvcDTO toDto(AuctionCmvcEntity auctionCmvcEntity);

    @Mapping(source = "product.id", target = "product")
    AuctionCmvcEntity toEntity(AuctionCmvcDTO auctionCmvcDTO);

    default AuctionCmvcEntity fromId(Long id) {
        if (id == null) {
            return null;
        }
        AuctionCmvcEntity auctionCmvcEntity = new AuctionCmvcEntity();
        auctionCmvcEntity.setId(id);
        return auctionCmvcEntity;
    }
}
