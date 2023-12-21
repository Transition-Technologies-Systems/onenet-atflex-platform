package pl.com.tt.flex.server.service.schedulingUnit.mapper;


import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import pl.com.tt.flex.server.domain.schedulingUnit.SchedulingUnitProposalEntity;
import pl.com.tt.flex.server.domain.unit.UnitEntity;
import pl.com.tt.flex.server.service.dictionary.derType.dto.DerTypeMinDTO;
import pl.com.tt.flex.server.service.fsp.mapper.FspMapper;
import pl.com.tt.flex.server.service.mapper.EntityMapper;
import pl.com.tt.flex.server.service.schedulingUnit.dto.ProposalDetailsDTO;
import pl.com.tt.flex.server.service.schedulingUnit.dto.SchedulingUnitProposalDTO;
import pl.com.tt.flex.server.service.schedulingUnit.dto.SchedulingUnitProposalMinDTO;
import pl.com.tt.flex.server.service.unit.mapper.UnitMapper;
import pl.com.tt.flex.server.service.user.mapper.UserMapper;
import pl.com.tt.flex.server.util.DictionaryUtils;

import java.util.List;

import static java.util.Objects.nonNull;

/**
 * Mapper for the entity {@link SchedulingUnitProposalEntity} and its DTO {@link SchedulingUnitProposalDTO}.
 */
@Mapper(componentModel = "spring", uses = {SchedulingUnitMapper.class, UnitMapper.class, UserMapper.class, FspMapper.class})
public interface SchedulingUnitProposalMapper extends EntityMapper<SchedulingUnitProposalDTO, SchedulingUnitProposalEntity> {

    //napisane pod modal z zaproszeniem
    @Mapping(source = "schedulingUnit.id", target = "schedulingUnitId")
    @Mapping(source = "bsp.id", target = "bspId")
    @Mapping(source = "unit.id", target = "unitId")
    @Mapping(source = "sender.id", target = "senderId")
    @Mapping(source = ".", target = "details")
    SchedulingUnitProposalDTO toDto(SchedulingUnitProposalEntity schedulingUnitProposalEntity);

    default ProposalDetailsDTO fillUnitDetails(SchedulingUnitProposalEntity schedulingUnitProposalEntity) {
        if (schedulingUnitProposalEntity.getUnit() == null) {
            return null;
        }
        UnitEntity unitEntity = schedulingUnitProposalEntity.getUnit();
        ProposalDetailsDTO detailsDTO = new ProposalDetailsDTO();
        detailsDTO.setDerName(unitEntity.getName());
        detailsDTO.setDerSourcePower(unitEntity.getSourcePower());
        detailsDTO.setDerConnectionPower(unitEntity.getConnectionPower());
        if (nonNull(unitEntity.getFsp())) {
            detailsDTO.setFspName(unitEntity.getFsp().getCompanyName());
        }
        if (nonNull(schedulingUnitProposalEntity.getBsp())) {
            detailsDTO.setBspName(schedulingUnitProposalEntity.getBsp().getCompanyName());
        }
        if (nonNull(schedulingUnitProposalEntity.getSchedulingUnit())) {
            detailsDTO.setSchedulingUnitName(schedulingUnitProposalEntity.getSchedulingUnit().getName());
            if (nonNull(schedulingUnitProposalEntity.getSchedulingUnit().getSchedulingUnitType())) {
                detailsDTO.setSchedulingUnitType(DictionaryUtils.getDictionaryDTO(schedulingUnitProposalEntity.getSchedulingUnit().getSchedulingUnitType()));
            }
        }
        if (nonNull(unitEntity.getDerTypeReception())) {
            detailsDTO.setDerTypeReception(new DerTypeMinDTO(unitEntity.getDerTypeReception().getId(), unitEntity.getDerTypeReception().getType(),
                unitEntity.getDerTypeReception().getDescriptionEn(), unitEntity.getDerTypeReception().getDescriptionPl(), DictionaryUtils.getKey(unitEntity.getDerTypeReception()),
                DictionaryUtils.getNlsCode(unitEntity.getDerTypeReception())));
        }
        if (nonNull(unitEntity.getDerTypeEnergyStorage())) {
            detailsDTO.setDerTypeEnergyStorage(new DerTypeMinDTO(unitEntity.getDerTypeEnergyStorage().getId(), unitEntity.getDerTypeEnergyStorage().getType(),
                unitEntity.getDerTypeEnergyStorage().getDescriptionEn(), unitEntity.getDerTypeEnergyStorage().getDescriptionPl(), DictionaryUtils.getKey(unitEntity.getDerTypeEnergyStorage()),
                DictionaryUtils.getNlsCode(unitEntity.getDerTypeEnergyStorage())));
        }
        if (nonNull(unitEntity.getDerTypeGeneration())) {
            detailsDTO.setDerTypeGeneration(new DerTypeMinDTO(unitEntity.getDerTypeGeneration().getId(), unitEntity.getDerTypeGeneration().getType(),
                unitEntity.getDerTypeGeneration().getDescriptionEn(), unitEntity.getDerTypeGeneration().getDescriptionPl(), DictionaryUtils.getKey(unitEntity.getDerTypeGeneration()),
                DictionaryUtils.getNlsCode(unitEntity.getDerTypeGeneration())));
        }
        return detailsDTO;
    }

    @Mapping(source = "schedulingUnit.id", target = "schedulingUnitId")
    @Mapping(source = "schedulingUnit.name", target = "schedulingUnitName")
    @Mapping(source = ".", target = "bspId", qualifiedByName = "getSchedulingUnitBspIdFromEntity")
    @Mapping(source = ".", target = "bspName", qualifiedByName = "getSchedulingUnitBspNameFromEntity")
    @Mapping(source = "unit.id", target = "derId")
    @Mapping(source = "unit.name", target = "derName")
    @Mapping(source = "unit.fsp.id", target = "fspId")
    @Mapping(source = "unit.fsp.companyName", target = "fspName")
    SchedulingUnitProposalMinDTO toMinDto(SchedulingUnitProposalEntity schedulingUnitProposalEntity);

    //napisane pod wiersze z tabeli w oknie "Partnership invitations/propositions"
    List<SchedulingUnitProposalMinDTO> toMinDto(List<SchedulingUnitProposalEntity> schedulingUnitProposalEntities);

    @Mapping(source = "bspId", target = "bsp")
    @Mapping(source = "schedulingUnitId", target = "schedulingUnit")
    @Mapping(source = "unitId", target = "unit")
    @Mapping(source = "senderId", target = "sender")
    SchedulingUnitProposalEntity toEntity(SchedulingUnitProposalDTO schedulingUnitProposalDTO);

    default SchedulingUnitProposalEntity fromId(Long id) {
        if (id == null) {
            return null;
        }
        SchedulingUnitProposalEntity schedulingUnitProposalEntity = new SchedulingUnitProposalEntity();
        schedulingUnitProposalEntity.setId(id);
        return schedulingUnitProposalEntity;
    }

    @Named("getSchedulingUnitBspIdFromEntity")
    default Long getSchedulingUnitBspIdFromEntity(SchedulingUnitProposalEntity schedulingUnitProposalEntity) {
        if (nonNull(schedulingUnitProposalEntity.getSchedulingUnit())) {
            return schedulingUnitProposalEntity.getSchedulingUnit().getId();
        } else if (nonNull(schedulingUnitProposalEntity.getBsp())) {
            return schedulingUnitProposalEntity.getBsp().getId();
        }
        return null;
    }

    @Named("getSchedulingUnitBspNameFromEntity")
    default String getSchedulingUnitBspNameFromEntity(SchedulingUnitProposalEntity schedulingUnitProposalEntity) {
        if (nonNull(schedulingUnitProposalEntity.getSchedulingUnit()) && nonNull(schedulingUnitProposalEntity.getSchedulingUnit().getBsp())) {
            return schedulingUnitProposalEntity.getSchedulingUnit().getBsp().getCompanyName();
        } else if (nonNull(schedulingUnitProposalEntity.getBsp())) {
            return schedulingUnitProposalEntity.getBsp().getCompanyName();
        }
        return null;
    }
}
