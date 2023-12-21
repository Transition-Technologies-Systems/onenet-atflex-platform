package pl.com.tt.flex.server.repository.user.registration;

import org.springframework.stereotype.Repository;
import pl.com.tt.flex.server.domain.user.registration.FspUserRegistrationFileEntity;
import pl.com.tt.flex.server.repository.AbstractJpaRepository;

import java.util.List;

/**
 * Spring Data  repository for the FspUserRegistrationFileEntity entity.
 */
@SuppressWarnings("unused")
@Repository
public interface FspUserRegistrationFileRepository extends AbstractJpaRepository<FspUserRegistrationFileEntity, Long> {

}
