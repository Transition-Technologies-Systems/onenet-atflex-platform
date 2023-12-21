package pl.com.tt.flex.onenet.repository.onenetuser;

import org.springframework.stereotype.Repository;

import pl.com.tt.flex.onenet.domain.onenetuser.OnenetUserEntity;
import pl.com.tt.flex.onenet.repository.AbstractJpaRepository;

@Repository
public interface OnenetUserRepository extends AbstractJpaRepository<OnenetUserEntity, Long> {

	boolean existsByUsername(String username);

}
