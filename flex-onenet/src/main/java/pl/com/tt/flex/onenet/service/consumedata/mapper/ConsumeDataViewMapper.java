package pl.com.tt.flex.onenet.service.consumedata.mapper;

import org.mapstruct.Mapper;
import pl.com.tt.flex.onenet.domain.consumedata.ConsumeDataViewEntity;
import pl.com.tt.flex.onenet.service.consumedata.dto.ConsumeDataViewDTO;
import pl.com.tt.flex.onenet.service.mapper.EntityMapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ConsumeDataViewMapper extends EntityMapper<ConsumeDataViewDTO, ConsumeDataViewEntity> {

	ConsumeDataViewDTO toDto(ConsumeDataViewEntity consumeDataViewEntity);

	List<ConsumeDataViewDTO> toDto(List<ConsumeDataViewEntity> consumeDataViewEntities);

	ConsumeDataViewEntity toEntity(ConsumeDataViewDTO consumeDataViewDTO);
}
