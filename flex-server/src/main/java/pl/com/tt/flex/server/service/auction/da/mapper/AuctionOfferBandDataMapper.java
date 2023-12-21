package pl.com.tt.flex.server.service.auction.da.mapper;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import pl.com.tt.flex.model.service.dto.auction.offer.da.AuctionOfferBandDataDTO;
import pl.com.tt.flex.server.domain.auction.offer.da.AuctionOfferBandDataEntity;
import pl.com.tt.flex.server.service.mapper.EntityMapper;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static pl.com.tt.flex.server.util.DateUtil.sortedHourNumbers;

/**
 * Mapper for the entity {@link AuctionOfferBandDataEntity} and its DTO {@link AuctionOfferBandDataDTO}.
 */
@Mapper(componentModel = "spring", uses = {})
public interface AuctionOfferBandDataMapper extends EntityMapper<AuctionOfferBandDataDTO, AuctionOfferBandDataEntity> {

    @AfterMapping
    default List<AuctionOfferBandDataDTO> sortOfferBand(@MappingTarget List<AuctionOfferBandDataDTO> bandDataDTOList) {
        return bandDataDTOList.stream()
            .sorted(Comparator.comparing(AuctionOfferBandDataDTO::getBandNumber)
                .thenComparing(bandData -> sortedHourNumbers.indexOf(bandData.getHourNumber())))
            .collect(Collectors.toList());
    }
}
