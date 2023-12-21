package pl.com.tt.flex.server.repository.derType;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pl.com.tt.flex.server.domain.unit.DerTypeEntity;
import pl.com.tt.flex.server.domain.unit.enumeration.DerType;
import pl.com.tt.flex.server.repository.AbstractJpaRepository;
import pl.com.tt.flex.server.service.dictionary.derType.dto.DerTypeMinDTO;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data  repository for the DerTypeEntity entity.
 */
@SuppressWarnings("unused")
@Repository
public interface DerTypeRepository extends AbstractJpaRepository<DerTypeEntity, Long> {

    Optional<DerTypeEntity> findByDescriptionEn(String descriptionEn);

    @Query("SELECT NEW pl.com.tt.flex.server.service.dictionary.derType.dto.DerTypeMinDTO(dt.id, dt.type, dt.descriptionEn) FROM DerTypeEntity dt")
    List<DerTypeMinDTO> findAllMin();

	boolean existsByIdAndType(Long derId, DerType derType);
}
