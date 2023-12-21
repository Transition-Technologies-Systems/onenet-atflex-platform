package pl.com.tt.flex.server.repository.user;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.com.tt.flex.server.domain.user.UserEntity;
import pl.com.tt.flex.server.repository.AbstractJpaRepository;
import pl.com.tt.flex.model.security.permission.Role;
import pl.com.tt.flex.model.service.dto.MinimalDTO;
import pl.com.tt.flex.server.service.user.dto.UserMinDTO;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Spring Data JPA repository for the {@link UserEntity} entity.
 */
@Repository
public interface UserRepository extends AbstractJpaRepository<UserEntity, Long> {

    String USERS_BY_LOGIN_CACHE = "usersByLogin";
    String USERS_BY_EMAIL_CACHE = "usersByEmail";

    Optional<UserEntity> findOneByActivationKey(String activationKey);
    Optional<UserEntity> findOneByResetKey(String resetKey);
    Optional<UserEntity> findOneByEmailAndActivatedTrueIgnoreCase(String email);
    Optional<UserEntity> findOneByLoginAndDeletedIsFalse(String login);
    Page<UserEntity> findAllByLoginNotAndDeletedIsFalse(Pageable pageable, String login);

//    @EntityGraph(attributePaths = "roles")
//    @Cacheable(cacheNames = USERS_BY_LOGIN_CACHE)
    Optional<UserEntity> findOneByLoginAndDeletedFalse(String login);

    @Query("SELECT u FROM UserEntity u LEFT JOIN FETCH u.fsp WHERE u.login = :login")
    Optional<UserEntity> findOneByLoginAndDeletedFalseFetchFsp(@Param("login") String login);

//    @EntityGraph(attributePaths = "roles")
//    @Cacheable(cacheNames = USERS_BY_EMAIL_CACHE)
    Optional<UserEntity> findOneByEmailAndDeletedFalse(String email);

	List<UserEntity> findDistinctByActivatedTrueAndDeletedFalseAndRolesIn(Set<Role> roles);

    List<UserEntity> findAllByFspId(Long fspId);

    @Query("SELECT u FROM UserEntity u JOIN FETCH u.fsp fsp WHERE fsp.id = :fspId")
    List<UserEntity> findAllByFspIdFetchFsp(@Param("fspId") Long fspId);

	boolean existsByEmailIgnoreCaseAndDeleted(String email, boolean deleted);
    boolean existsByEmailIgnoreCaseAndDeletedAndIdNot(String email, boolean deleted, Long id);

    boolean existsByLoginIgnoreCase(String login);
    boolean existsByLoginIgnoreCaseAndIdNot(String login, Long id);

    @Query("SELECT NEW pl.com.tt.flex.server.service.user.dto.UserMinDTO(u) FROM UserEntity u JOIN u.roles ur WHERE ur IN :roles AND ur NOT IN ('ROLE_ADMIN') AND u.activated = 1")
    List<UserMinDTO> findNotAdminUsersByRole(@Param("roles") Set<Role> roles);

    @Query("SELECT NEW pl.com.tt.flex.server.service.user.dto.UserMinDTO(u) FROM UserEntity u WHERE u.id IN :ids AND u.activated = true")
    List<UserMinDTO> getUsersByIds(@Param("ids") Set<Long> ids);

    @Query("SELECT NEW pl.com.tt.flex.model.service.dto.MinimalDTO(u.id, u.login) FROM UserEntity u JOIN u.roles r WHERE r IN :roles AND u.activated = 1")
    List<MinimalDTO<Long, String>> getUsersByRolesMinimal(@Param("roles") Set<Role> roles);

    @Query("SELECT NEW pl.com.tt.flex.model.service.dto.MinimalDTO(u.id, u.login) FROM UserEntity u WHERE u.fsp.companyName = :companyName AND u.activated = true")
    List<MinimalDTO<Long, String>> getUsersByCompanyName(@Param("companyName") String companyName);

    @Query("SELECT NEW pl.com.tt.flex.model.service.dto.MinimalDTO(u.id, u.login) FROM UserEntity u WHERE u.login IN :userLogins AND u.activated = true")
    List<MinimalDTO<Long, String>> getUsersByLogin(@Param("userLogins") Set<String> userLogins);
    /**
     * Verify that user has at least one of provided roles
     */
    boolean existsByIdAndRolesIn(Long userId, List<Role> roles);

    @Query("SELECT u FROM UserEntity u JOIN u.roles r WHERE r = :role AND u.activated = true")
    List<UserEntity> findAllWithRole(@Param("role") Role role);
}
