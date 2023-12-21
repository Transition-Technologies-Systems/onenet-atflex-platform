package pl.com.tt.flex.server.service.auction.offer.mapper;


import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import pl.com.tt.flex.model.service.dto.auction.offer.AuctionOfferDTO;
import pl.com.tt.flex.model.service.dto.auction.offer.AuctionOfferViewDTO;
import pl.com.tt.flex.model.service.dto.der.DerMinDTO;
import pl.com.tt.flex.server.domain.auction.offer.AuctionOfferViewEntity;
import pl.com.tt.flex.server.service.mapper.EntityMapper;

/**
 * Mapper for the entity {@link AuctionOfferViewEntity} and its DTO {@link AuctionOfferDTO}.
 */
@Mapper(componentModel = "spring")
public interface AuctionOfferViewMapper extends EntityMapper<AuctionOfferViewDTO, AuctionOfferViewEntity> {

    @Mapping(qualifiedByName = "toListOfDerMin", target = "derMinDTOs", source = "entity")
    @Mapping(qualifiedByName = "toFlexPotentialVolumeString", target = "flexibilityPotentialVolume", source = "entity")
    AuctionOfferViewDTO toDto(AuctionOfferViewEntity entity);

    AuctionOfferViewEntity toEntity(AuctionOfferViewDTO dto);

    default AuctionOfferViewEntity fromId(Long id) {
        if (id == null) {
            return null;
        }
        AuctionOfferViewEntity offerEntity = new AuctionOfferViewEntity();
        offerEntity.setId(id);
        return offerEntity;
    }

    @Named("toListOfDerMin")
    default List<DerMinDTO> toListOfDerMin(AuctionOfferViewEntity offerView) {
        return offerView.getSchedulingUnitOrPotentialDers().stream().map(entity -> {
                DerMinDTO derMinDTO = new DerMinDTO();
                derMinDTO.setId(entity.getId());
                derMinDTO.setName(entity.getName());
                derMinDTO.setSourcePower(entity.getSourcePower());
                derMinDTO.setPMin(entity.getPMin());
                return derMinDTO;
            }).collect(Collectors.toList());
    }

    @Named("toFlexPotentialVolumeString")
    default String toFlexPotentialVolumeString(AuctionOfferViewEntity offerView) {
        return Optional.ofNullable(offerView.getFlexibilityPotentialVolume())
            .map(volume -> volume.toString() + " " + offerView.getFlexibilityPotentialVolumeUnit()).orElse(null);
    }
}
