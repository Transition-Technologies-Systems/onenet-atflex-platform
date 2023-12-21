package pl.com.tt.flex.server.service.dictionary.derType;

import pl.com.tt.flex.server.domain.unit.DerTypeEntity;
import pl.com.tt.flex.server.domain.unit.enumeration.DerType;
import pl.com.tt.flex.server.service.AbstractService;
import pl.com.tt.flex.server.service.dictionary.derType.dto.DerTypeDTO;

/**
 * Service Interface for managing {@link DerTypeEntity}.
 */
public interface DerTypeService extends AbstractService<DerTypeEntity, DerTypeDTO, Long> {

    boolean existsByIdAndType(Long id, DerType reception);

    void sendNotificationInformingAboutCreated(DerTypeDTO derTypeDTO);

    void sendNotificationInformingAboutModification(DerTypeDTO modifyDerType);

}
