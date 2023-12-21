package pl.com.tt.flex.server.service.auction.cmvc.mapper;


import com.google.common.collect.Lists;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import pl.com.tt.flex.model.service.dto.auction.cmvc.AuctionCmvcDTO;
import pl.com.tt.flex.model.service.dto.localization.LocalizationType;
import pl.com.tt.flex.model.service.dto.localization.LocalizationTypeDTO;
import pl.com.tt.flex.server.domain.auction.cmvc.AuctionCmvcEntity;
import pl.com.tt.flex.server.domain.auction.cmvc.AuctionCmvcViewEntity;
import pl.com.tt.flex.server.service.mapper.EntityMapper;
import pl.com.tt.flex.server.service.product.mapper.ProductMapper;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Mapper for the entity {@link AuctionCmvcEntity} and its DTO {@link AuctionCmvcDTO}.
 */
@Mapper(componentModel = "spring", uses = {ProductMapper.class})
public interface AuctionCmvcViewMapper extends EntityMapper<AuctionCmvcDTO, AuctionCmvcViewEntity> {

    @Mapping(target = "localizationMerged", source = "localization")
    @Mapping(target = "localization", ignore = true)
    AuctionCmvcDTO toDto(AuctionCmvcViewEntity auctionCmvcEntity);

    @Mapping(target = "localization", ignore = true)
    AuctionCmvcViewEntity toEntity(AuctionCmvcDTO auctionCmvcDTO);

    default AuctionCmvcViewEntity fromId(Long id) {
        if (id == null) {
            return null;
        }
        AuctionCmvcViewEntity auctionCmvcViewEntity = new AuctionCmvcViewEntity();
        auctionCmvcViewEntity.setId(id);
        return auctionCmvcViewEntity;
    }

    /**
     * Poniższa implementacja rozdziela wygenerowany w widoku (auction_cmvc_view) string z lokalizacjami.
     * Napis wejściowy ma format "NazwaLokalizacji(TYP_LOKALIZACJI)". Aukcja może posiadać węcej niż jedną lokalizacje,
     * są one wtedy rozdzielone przecinkami. Dla każdej lokalizacji wycinana jest nazwa i typ i tworzony z nich obiekt LocalizationTypeDTO,
     * a następnie zapisywany do listy.
     */
    @AfterMapping
    default void setLocalization(AuctionCmvcViewEntity auctionCmvcEntity, @MappingTarget AuctionCmvcDTO auctionCmvcDTO) {
        if (Objects.nonNull(auctionCmvcEntity.getLocalization())) {
            List<LocalizationTypeDTO> localization = Lists.newArrayList();
            List<String> localizations = Arrays.asList(auctionCmvcEntity.getLocalization().split("\\s*,\\s*"));
            localizations.forEach(name -> {
                String[] result = name.split("\\(");
                LocalizationTypeDTO localizationType = new LocalizationTypeDTO();
                localizationType.setName(result[0]);
                localizationType.setType(LocalizationType.valueOf(result[1].split("\\)")[0]));
                localization.add(localizationType);
            });
            auctionCmvcDTO.setLocalization(localization);
        }
    }


}
