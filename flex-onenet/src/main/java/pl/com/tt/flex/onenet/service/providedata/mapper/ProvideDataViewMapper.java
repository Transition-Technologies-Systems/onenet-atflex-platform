package pl.com.tt.flex.onenet.service.providedata.mapper;

import org.mapstruct.Mapper;

import pl.com.tt.flex.onenet.domain.consumedata.ConsumeDataViewEntity;
import pl.com.tt.flex.onenet.service.mapper.EntityMapper;
import pl.com.tt.flex.onenet.service.providedata.dto.ProvideDataViewDTO;

@Mapper(componentModel = "spring")
public interface ProvideDataViewMapper extends EntityMapper<ProvideDataViewDTO, ConsumeDataViewEntity> {
}
