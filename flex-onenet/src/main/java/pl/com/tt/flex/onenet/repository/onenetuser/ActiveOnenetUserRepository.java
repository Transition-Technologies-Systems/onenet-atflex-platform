package pl.com.tt.flex.onenet.repository.onenetuser;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import pl.com.tt.flex.onenet.domain.onenetuser.ActiveOnenetUserEntiy;
import pl.com.tt.flex.onenet.domain.onenetuser.OnenetUserEntity;
import pl.com.tt.flex.onenet.repository.AbstractJpaRepository;

@Repository
public interface ActiveOnenetUserRepository extends AbstractJpaRepository<ActiveOnenetUserEntiy, Long> {

	Optional<ActiveOnenetUserEntiy> findByFlexUsernameEquals(String flexUsername);

	boolean existsByActiveOnenetUser(OnenetUserEntity onenetUser);


}
