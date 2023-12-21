package pl.com.tt.flex.server.converter;

import pl.com.tt.flex.server.domain.EntityInterface;
import pl.com.tt.flex.server.web.dto.AbstractDTO;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Mariusz Batyra on 18.07.2017.
 */
public abstract class Converter<ENTITY extends EntityInterface, DTO extends AbstractDTO> {

    public abstract ENTITY convertToEntity(DTO dto);

    public abstract DTO convertToDto(ENTITY entity);

    public List<ENTITY> convertToEntities(List<DTO> dtos) {
        return dtos.stream().map(this::convertToEntity).collect(Collectors.toList());
    }

    public List<DTO> convertToDtos(List<ENTITY> entities) {
        return entities.stream().map(this::convertToDto).collect(Collectors.toList());
    }

}
