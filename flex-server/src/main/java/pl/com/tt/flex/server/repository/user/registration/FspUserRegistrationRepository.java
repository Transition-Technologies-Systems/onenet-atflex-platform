package pl.com.tt.flex.server.repository.user.registration;

import org.springframework.stereotype.Repository;
import pl.com.tt.flex.server.domain.user.registration.FspUserRegistrationEntity;
import pl.com.tt.flex.server.domain.user.registration.enumeration.FspUserRegistrationStatus;
import pl.com.tt.flex.server.repository.AbstractJpaRepository;

import java.util.Optional;
import java.util.Set;

/**
 * Spring Data  repository for the FspUserRegistrationEntity entity.
 */
@SuppressWarnings("unused")
@Repository
public interface FspUserRegistrationRepository extends AbstractJpaRepository<FspUserRegistrationEntity, Long> {

    boolean existsByEmailIgnoreCaseAndStatusNotIn(String email, Set<FspUserRegistrationStatus> statuses);

    Optional<FspUserRegistrationEntity> findOneBySecurityKey(String key);

    Optional<FspUserRegistrationEntity> findOneByFspUserActivationKey(String key);

    Optional<FspUserRegistrationEntity> findOneByFspUserId(Long fspUserId);
}
