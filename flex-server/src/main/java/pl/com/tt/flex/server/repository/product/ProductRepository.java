package pl.com.tt.flex.server.repository.product;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.com.tt.flex.server.domain.product.ProductEntity;
import pl.com.tt.flex.server.repository.AbstractJpaRepository;
import pl.com.tt.flex.server.service.product.dto.ProductNameMinDTO;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data  repository for the ProductEntity entity.
 */
@SuppressWarnings("unused")
@Repository
public interface ProductRepository extends AbstractJpaRepository<ProductEntity, Long> {

    @Query("select product from ProductEntity product left join fetch product.ssoUsers where product.id =:id")
    Optional<ProductEntity> findOneWithUsers(@Param("id") Long id);

    boolean existsByPsoUser_LoginAndActiveIsTrue(String login);

    Optional<ProductEntity> findByShortName(String productShortName);

    @Query("SELECT p FROM ProductEntity p WHERE p.active = true AND SYS_EXTRACT_UTC(SYSTIMESTAMP) NOT BETWEEN p.validFrom AND p.validTo")
	List<ProductEntity> findProductsToDeactivateByValidFromToDates();

    @Query("SELECT p FROM ProductEntity p WHERE p.active = false AND SYS_EXTRACT_UTC(SYSTIMESTAMP) BETWEEN p.validFrom AND p.validTo")
    List<ProductEntity> findProductsToActivateByValidFromToDates();

    boolean existsByShortNameIgnoreCase(String shortName);

    boolean existsByShortNameIgnoreCaseAndIdNot(String shortName, Long id);

    boolean existsByFullNameIgnoreCase(String fullName);

    boolean existsByFullNameIgnoreCaseAndIdNot(String fullName, Long id);

    @Query(value = "SELECT NEW pl.com.tt.flex.server.service.product.dto.ProductNameMinDTO(p.id, p.shortName) " +
        "FROM ProductEntity p WHERE p.id = :productId")
    ProductNameMinDTO getProductShortName(@Param("productId") Long productId);

    @Query("SELECT p.id FROM ProductEntity p WHERE p.fullName = :name OR p.shortName = :name")
    Long findByFullNameOrShortName(@Param("name") String name);

    @Query("SELECT p.id FROM ProductEntity p WHERE p.fullName = :name")
    Long findByFullName(@Param("name") String productName);
}
