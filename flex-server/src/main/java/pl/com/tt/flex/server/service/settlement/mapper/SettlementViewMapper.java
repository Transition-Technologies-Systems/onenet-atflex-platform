package pl.com.tt.flex.server.service.settlement.mapper;

import org.mapstruct.Mapper;

import org.mapstruct.Mapping;
import org.mapstruct.Named;

import pl.com.tt.flex.server.domain.auction.offer.AuctionOfferViewType;
import pl.com.tt.flex.server.domain.settlement.SettlementViewEntity;
import pl.com.tt.flex.server.service.mapper.EntityMapper;
import pl.com.tt.flex.server.service.settlement.dto.SettlementEditDTO;
import pl.com.tt.flex.server.service.settlement.dto.SettlementMinDTO;
import pl.com.tt.flex.server.service.settlement.dto.SettlementViewDTO;

@Mapper(componentModel = "spring")
public interface SettlementViewMapper extends EntityMapper<SettlementViewDTO, SettlementViewEntity> {

    SettlementEditDTO toSettlementEditDTO(SettlementViewEntity settlementViewEntity);

    SettlementMinDTO toSettlementMinDTO(SettlementViewDTO SettlementViewDTO);

    @Mapping(source = "offerCategory", target = "acceptedVolumeCmvcTooltipVisible")
    @Mapping(source = "settlementViewEntity", target = "acceptedVolumeTooltipVisible", qualifiedByName = "checkAcceptedVolumeTootipVisible")
    SettlementViewDTO toDto(SettlementViewEntity settlementViewEntity);

    @Named("checkAcceptedVolumeTootipVisible")
    default Boolean checkAcceptedVolumeTootipVisible(SettlementViewEntity settlementViewEntity) {
        return settlementViewEntity.getAcceptedVolume().contains("/");
    }

    default Boolean getAcceptedVolumeCmvcTooltipVisibleFlag (AuctionOfferViewType offerCategory) {
        return AuctionOfferViewType.CMVC.equals(offerCategory);
    }
}
