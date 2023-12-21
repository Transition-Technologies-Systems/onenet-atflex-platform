package pl.com.tt.flex.server.repository.product;

import org.springframework.stereotype.Repository;
import pl.com.tt.flex.server.domain.product.ProductFileEntity;
import pl.com.tt.flex.server.repository.AbstractJpaRepository;

import java.util.List;

/**
 * Spring Data  repository for the ProductFile entity.
 */
@SuppressWarnings("unused")
@Repository
public interface ProductFileRepository extends AbstractJpaRepository<ProductFileEntity, Long> {

	List<ProductFileEntity> findAllByProductId(Long id);
}
