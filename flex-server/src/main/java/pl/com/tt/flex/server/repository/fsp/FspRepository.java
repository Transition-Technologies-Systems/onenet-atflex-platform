package pl.com.tt.flex.server.repository.fsp;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.com.tt.flex.model.security.permission.Role;
import pl.com.tt.flex.model.service.dto.chat.ChatRecipientDTO;
import pl.com.tt.flex.model.service.dto.fsp.FspCompanyMinDTO;
import pl.com.tt.flex.server.domain.fsp.FspEntity;
import pl.com.tt.flex.server.repository.AbstractJpaRepository;
import pl.com.tt.flex.model.service.dto.MinimalDTO;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Spring Data  repository for the FspEntity.
 */
@SuppressWarnings("unused")
@Repository
public interface FspRepository extends AbstractJpaRepository<FspEntity, Long> {

    @Query("SELECT fsp FROM FspEntity fsp WHERE fsp.active = true AND SYS_EXTRACT_UTC(SYSTIMESTAMP) NOT BETWEEN fsp.validFrom AND fsp.validTo")
    List<FspEntity> findFspsToDeactivateByValidFromToDates();

    @Query("SELECT fsp FROM FspEntity fsp WHERE fsp.active = false AND SYS_EXTRACT_UTC(SYSTIMESTAMP) BETWEEN fsp.validFrom AND fsp.validTo")
    List<FspEntity> findFspsToActivateByValidFromToDates();

    boolean existsByOwner_Login(String login);

    Optional<FspEntity> findByOwnerId(Long id);

    Optional<FspEntity> findByUsersId(Long id);

    Optional<FspEntity> findByCompanyName(String companyName);

    @Query("SELECT NEW pl.com.tt.flex.model.service.dto.fsp.FspCompanyMinDTO(fsp.id, fsp.companyName, fsp.role) FROM FspEntity fsp " +
        "WHERE fsp.active = true AND fsp.role IN :roles AND fsp.deleted = false ORDER BY fsp.companyName")
    List<FspCompanyMinDTO> findAllFspCompanyNameMinimalByActiveAndRoles(@Param(value = "roles") List<Role> roles);

    @Query("SELECT NEW pl.com.tt.flex.model.service.dto.fsp.FspCompanyMinDTO(fsp.id, fsp.companyName, fsp.role) FROM FspEntity fsp " +
        "WHERE fsp.role IN :roles AND fsp.deleted = false ORDER BY fsp.companyName")
    List<FspCompanyMinDTO> findAllFspCompanyNameMinimalByRoles(@Param(value = "roles") List<Role> roles);

    /**
     * @see Role#ROLE_FLEX_SERVICE_PROVIDER_AGGREGATED
     */
    @Query("SELECT NEW pl.com.tt.flex.model.service.dto.fsp.FspCompanyMinDTO(fsp.id, fsp.companyName, fsp.role) FROM FspEntity fsp WHERE EXISTS (SELECT u FROM UnitEntity u WHERE u.fsp.id = fsp.id " +
        "AND fsp.active = true AND fsp.role = 'ROLE_FLEX_SERVICE_PROVIDER_AGGREGATED' AND fsp.deleted = false AND u.certified = true AND u.subportfolio.id IS NULL) ORDER BY fsp.companyName")
    List<FspCompanyMinDTO> findAllFspaWithDersNotConnectedToSubportfolio();

    @Query("SELECT DISTINCT NEW pl.com.tt.flex.model.service.dto.fsp.FspCompanyMinDTO(ue.fsp.id, ue.fsp.companyName, ue.fsp.role) FROM UnitEntity ue " +
        "WHERE fsp.active = true AND fsp.role IN :roles AND ue.active = true AND ue.certified = true " +
        "AND ue.validTo >= SYS_EXTRACT_UTC(SYSTIMESTAMP) ORDER BY ue.fsp.companyName")
    Set<FspCompanyMinDTO> findAllFspActiveCompanyNameMinimalByRolesWhereAttachedUnitIsActiveAndCertified(@Param(value = "roles") List<Role> roles);

    /**
     * @see Role#ROLE_BALANCING_SERVICE_PROVIDER
     */
    @Query("SELECT NEW pl.com.tt.flex.model.service.dto.fsp.FspCompanyMinDTO(fsp.id, fsp.companyName, fsp.role) FROM FspEntity fsp " +
        "WHERE fsp.active = true AND fsp.role = 'ROLE_BALANCING_SERVICE_PROVIDER' AND fsp.schedulingUnits IS NOT EMPTY ORDER BY fsp.companyName")
    List<FspCompanyMinDTO> findAllActiveBspWithNotEmptySchedulingUnitsMinimal();

    boolean existsByCompanyNameIgnoreCase(String companyName);

    /**
     * @see Role#ROLE_FLEX_SERVICE_PROVIDER
     * @see Role#ROLE_FLEX_SERVICE_PROVIDER_AGGREGATED
     */
    @Query("SELECT NEW pl.com.tt.flex.model.service.dto.fsp.FspCompanyMinDTO(fsp.id, fsp.companyName, fsp.role) FROM FspEntity fsp " +
        "WHERE fsp.active = true AND (fsp.role = 'ROLE_FLEX_SERVICE_PROVIDER' OR fsp.role = 'ROLE_FLEX_SERVICE_PROVIDER_AGGREGATED') " +
        "AND EXISTS (SELECT id FROM FlexPotentialEntity fp " +
        "WHERE fp.fsp = fsp.id AND fp.product.id = :productId AND fp.registered = true)")
    List<FspCompanyMinDTO> findFspsWithRegisteredPotentialsForProduct(@Param(value = "productId") Long productId);

    @Query("SELECT fsp.role FROM FspEntity fsp WHERE fsp.id = :fspId")
    Role findFspRole(@Param(value = "fspId") Long fspId);

    @Query("SELECT NEW pl.com.tt.flex.model.service.dto.MinimalDTO(u.id, u.login) FROM UserEntity u WHERE fsp.id = :fspId AND u.activated = 1 AND u.deleted = 0")
    List<MinimalDTO<Long, String>> findFspUsersMin(@Param(value = "fspId") Long fspId);

    @Query("SELECT NEW pl.com.tt.flex.model.service.dto.MinimalDTO(u.id, u.login) FROM UserEntity u WHERE fsp.id in :fspIds AND u.activated = 1 AND u.deleted = 0")
    List<MinimalDTO<Long, String>> findFspUsersMin(@Param(value = "fspIds") Set<Long> fspIds);

    @Query("SELECT NEW pl.com.tt.flex.model.service.dto.chat.ChatRecipientDTO(bsp.id, bsp.companyName, bsp.role) FROM FspEntity bsp " +
        "WHERE bsp.role = 'ROLE_BALANCING_SERVICE_PROVIDER' AND bsp.active = 1 AND bsp.deleted = 0 ORDER BY bsp.companyName ASC")
    List<ChatRecipientDTO> findAllBspMin();

    @Query("SELECT NEW pl.com.tt.flex.model.service.dto.chat.ChatRecipientDTO(fsp.id, fsp.companyName, fsp.role) FROM FspEntity fsp " +
        "WHERE (fsp.role = 'ROLE_FLEX_SERVICE_PROVIDER' OR fsp.role = 'ROLE_FLEX_SERVICE_PROVIDER_AGGREGATED') AND fsp.active = 1 AND fsp.deleted = 0 ORDER BY fsp.companyName ASC")
    List<ChatRecipientDTO> findAllFspMin();
}
