package pl.com.tt.flex.server.service.auction.da.series.mapper;


import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pl.com.tt.flex.model.service.dto.auction.da.AuctionsSeriesDTO;
import pl.com.tt.flex.server.domain.auction.da.AuctionsSeriesEntity;
import pl.com.tt.flex.server.service.mapper.EntityMapper;
import pl.com.tt.flex.server.service.product.mapper.ProductMapper;

/**
 * Mapper for the entity {@link AuctionsSeriesEntity} and its DTO {@link AuctionsSeriesDTO}.
 */
@Mapper(componentModel = "spring", uses = {ProductMapper.class})
public interface AuctionsSeriesMapper extends EntityMapper<AuctionsSeriesDTO, AuctionsSeriesEntity> {

    @Mapping(source = "product", target = "product")
    AuctionsSeriesDTO toDto(AuctionsSeriesEntity auctionsSeriesEntity);

    @Mapping(source = "product.id", target = "product")
    AuctionsSeriesEntity toEntity(AuctionsSeriesDTO auctionsSeriesDTO);

    default AuctionsSeriesEntity fromId(Long id) {
        if (id == null) {
            return null;
        }
        AuctionsSeriesEntity auctionsSeriesEntity = new AuctionsSeriesEntity();
        auctionsSeriesEntity.setId(id);
        return auctionsSeriesEntity;
    }
}
