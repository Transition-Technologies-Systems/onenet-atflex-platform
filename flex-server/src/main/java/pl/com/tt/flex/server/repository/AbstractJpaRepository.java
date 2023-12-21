package pl.com.tt.flex.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;
import pl.com.tt.flex.server.domain.EntityInterface;

import java.io.Serializable;

@NoRepositoryBean
public interface AbstractJpaRepository<ENTITY extends EntityInterface, ID extends Serializable> extends JpaRepository<ENTITY, ID>, JpaSpecificationExecutor<ENTITY> {

}
