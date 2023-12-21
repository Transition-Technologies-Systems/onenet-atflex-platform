package pl.com.tt.flex.server.repository.subportfolio;

import org.springframework.stereotype.Repository;
import pl.com.tt.flex.server.domain.subportfolio.SubportfolioFileEntity;
import pl.com.tt.flex.server.repository.AbstractJpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data  repository for the FlexPotentialFile entity.
 */
@SuppressWarnings("unused")
@Repository
public interface SubportfolioFileRepository extends AbstractJpaRepository<SubportfolioFileEntity, Long> {

	List<SubportfolioFileEntity> findAllBySubportfolioId(Long id);

	Optional<SubportfolioFileEntity> findByIdAndSubportfolioFspaId(Long fileId, Long fspaId);
}
