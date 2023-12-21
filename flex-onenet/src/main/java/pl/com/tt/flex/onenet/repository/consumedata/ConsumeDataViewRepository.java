package pl.com.tt.flex.onenet.repository.consumedata;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.com.tt.flex.onenet.domain.consumedata.ConsumeDataViewEntity;
import pl.com.tt.flex.onenet.repository.AbstractJpaRepository;

import java.util.List;

@Repository
public interface ConsumeDataViewRepository extends AbstractJpaRepository<ConsumeDataViewEntity, Long> {
	@Query(value = "SELECT * FROM CONSUME_DATA_VIEW cdv JOIN ONENET_USER ou ON ou.username = cdv.data_supplier WHERE cdv.data_supplier = :username", nativeQuery = true)
	List<ConsumeDataViewEntity> findConsumeDataByActiveOnenetUsername(@Param("username") String username);
}
