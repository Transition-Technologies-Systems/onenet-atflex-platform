package pl.com.tt.flex.server.repository.potential;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.com.tt.flex.model.service.dto.MinimalDTO;
import pl.com.tt.flex.model.service.dto.potential.FlexPotentialMinDTO;
import pl.com.tt.flex.server.domain.potential.FlexPotentialEntity;
import pl.com.tt.flex.server.repository.AbstractJpaRepository;

import java.time.Instant;
import java.util.List;

/**
 * Spring Data  repository for the FlexPotentialEntity entity.
 */
@SuppressWarnings("unused")
@Repository
public interface FlexPotentialRepository extends AbstractJpaRepository<FlexPotentialEntity, Long> {

    @Query("SELECT fp FROM FlexPotentialEntity fp WHERE fp.fsp.id = :fspId AND fp.active = true")
    List<FlexPotentialEntity> findActiveFlexPotentialsOfFsp(@Param("fspId") Long fspId);

    boolean existsByIdAndFspCompanyName(Long id, String companyName);

    @Query("SELECT NEW pl.com.tt.flex.model.service.dto.MinimalDTO(f.id, f.fsp.companyName) FROM FlexPotentialEntity f WHERE f.id = :id")
    MinimalDTO<Long, String> findFlexPotentialMinWithFspCompanyName(@Param("id") Long id);

    @Query("SELECT fp FROM FlexPotentialEntity fp WHERE fp.active = true AND SYS_EXTRACT_UTC(SYSTIMESTAMP) NOT BETWEEN fp.validFrom AND fp.validTo")
    List<FlexPotentialEntity> findFlexPotentialsToDeactivateByValidFromToDates();

    @Query("SELECT fp FROM FlexPotentialEntity fp WHERE fp.active = false AND SYS_EXTRACT_UTC(SYSTIMESTAMP) BETWEEN fp.validFrom AND fp.validTo")
    List<FlexPotentialEntity> findFlexPotentialsToActivateByValidFromToDates();

    @Query(value = "SELECT DISTINCT u.name FROM FlexPotentialEntity fp " +
        "JOIN fp.units u " +
        "order by u.name")
    List<String> findAllDerNamesJoinedToFP();

    @Query(value = "SELECT DISTINCT u.name FROM FlexPotentialEntity fp " +
        "JOIN fp.units u " +
        "WHERE fp.registered = false AND fp.fsp.id = :fspId " +
        "order by u.name")
    List<String> findAllDerNamesJoinedToFPByFspId(@Param("fspId") Long fspId);

    @Query(value = "SELECT DISTINCT u.name FROM FlexPotentialEntity fp " +
        "JOIN fp.units u " +
        "WHERE fp.registered = true " +
        "order by u.name")
    List<String> findAllDerNamesJoinedToFlexRegister();

    @Query(value = "SELECT COUNT(DISTINCT u.name) FROM FlexPotentialEntity fp " +
        "JOIN fp.units u " +
        "WHERE fp.registered = true ")
    Long countDerJoinedToFlexRegister();

    @Query(value = "SELECT COUNT(DISTINCT u.name) FROM FlexPotentialEntity fp " +
        "JOIN fp.units u " +
        "JOIN fp.product p " +
        "WHERE p.balancing = true")
    Long countDerJoinedToFlexPotentialWithBalancingProduct();

    @Query(value = "SELECT DISTINCT u.name FROM FlexPotentialEntity fp " +
        "JOIN fp.units u " +
        "WHERE fp.registered = true AND fp.fsp.id = :fspId " +
        "order by u.name")
    List<String> findAllDerNamesJoinedToFlexRegisterByFspId(@Param("fspId") Long fspId);

    @Query(value = "SELECT DISTINCT FP.id FROM FLEX_POTENTIAL FP " +
        "JOIN FLEX_POTENTIAL_UNITS FPU ON FPU.FLEX_POTENTIAL_ID = FP.ID " +
        "WHERE FPU.UNIT_ID = :unitId AND FP.ACTIVE = 1 ORDER BY FP.id", nativeQuery = true)
    List<Long> findActiveByUnit(@Param("unitId") Long unitId);

    @Query(value = "SELECT DISTINCT FP.id FROM FLEX_POTENTIAL FP " +
        "JOIN FLEX_POTENTIAL_UNITS FPU ON FPU.FLEX_POTENTIAL_ID = FP.ID " +
        "WHERE FPU.UNIT_ID = :unitId ORDER BY FP.id", nativeQuery = true)
    List<Long> findByUnit(@Param("unitId") Long unitId);

    @Query("SELECT NEW pl.com.tt.flex.model.service.dto.potential.FlexPotentialMinDTO(fp.id, fp.volume, fp.volumeUnit, fp.product.shortName, fp.fullActivationTime, fp.minDeliveryDuration) FROM FlexPotentialEntity fp " +
        "WHERE fp.fsp.id = :fspId AND fp.product.id = :productId AND fp.registered = true AND fp.active = true")
    List<FlexPotentialMinDTO> findAllByFspIdAndProductIdAndRegisteredIsTrueAndActiveIsTrue(@Param("fspId") Long fspId, @Param("productId") Long productId);

    @Query("SELECT fp FROM FlexPotentialEntity fp " +
        "WHERE fp.fsp.id = :fspId AND fp.product.id = :productId AND fp.registered = true AND fp.active = true")
    List<FlexPotentialEntity> findAllEntitiesByFspIdAndProductIdAndRegisteredIsTrueAndActiveIsTrue(@Param("fspId") Long fspId, @Param("productId") Long productId);

    @Query("SELECT fp FROM FlexPotentialEntity fp " +
        "WHERE fp.product.id = :productId AND fp.registered = true AND fp.active = true AND fsp.active = true AND (fsp.role = 'ROLE_FLEX_SERVICE_PROVIDER' OR fsp.role = 'ROLE_FLEX_SERVICE_PROVIDER_AGGREGATED')")
    List<FlexPotentialEntity> findAllByProductIdAndRegisteredIsTrueAndActiveIsTrueAndFspRole(@Param("productId") Long productId);

    @Query("SELECT fp FROM FlexPotentialEntity fp " +
        "WHERE fp.product.balancing = true AND fp.registered = true")
    List<FlexPotentialEntity> findAllByProductBalancingIsTrueAndRegisterIsTrue();

    @Query("SELECT fp FROM FlexPotentialEntity fp " +
        "WHERE fp.product.cmvc = true AND fp.registered = true AND fp.active = true")
    List<FlexPotentialEntity> findAllByRegisteredIsTrueAndActiveIsTrueAndProductCmvcIsTrue();

    @Query("SELECT fp FROM FlexPotentialEntity fp " +
        "WHERE fp.product.id in (:productIds) AND fp.registered = true " +
        "AND ( (validFrom <= :validFrom AND validTo >= :validFrom) OR (validFrom <= :validTo AND validTo >= :validTo) OR (validFrom >= :validFrom AND validTo <= :validTo) ) " +
        "AND (fsp.role = 'ROLE_FLEX_SERVICE_PROVIDER' OR fsp.role = 'ROLE_FLEX_SERVICE_PROVIDER_AGGREGATED')")
    List<FlexPotentialEntity> findAllByProductIdsAndRegisteredIsTrueAndValidFromAndValidTo(@Param("productIds") List<Long> productIds,
                                                                                           @Param("validFrom") Instant validFrom,
                                                                                           @Param("validTo") Instant validTo);

    boolean existsByIdAndFspId(Long flexPotentialId, Long fspId);

    boolean existsByFspIdAndRegisteredIsTrueAndActiveIsTrueAndProductBalancingIsTrue(Long fspId);

    /**
     * Aby BSP mógł zaprosić DERa do jednostki grafikowej, to ten DER musi posiadać "Flex register" na produkt z flagą "Balancing".
     */
    boolean existsByRegisteredIsTrueAndActiveIsTrueAndProductBalancingIsTrueAndUnitsId(Long derId);
}
