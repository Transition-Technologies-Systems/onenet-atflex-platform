package pl.com.tt.flex.server.repository.user.registration;

import org.springframework.stereotype.Repository;
import pl.com.tt.flex.server.domain.user.registration.FspUserRegistrationCommentEntity;
import pl.com.tt.flex.server.repository.AbstractJpaRepository;

import java.util.List;

/**
 * Spring Data  repository for the FspUserRegCommentEntity entity.
 */
@SuppressWarnings("unused")
@Repository
public interface FspUserRegistrationCommentRepository extends AbstractJpaRepository<FspUserRegistrationCommentEntity, Long> {

    List<FspUserRegistrationCommentEntity> findAllByFspUserRegistrationIdOrderByIdAsc(Long fspUserRegId);
}
