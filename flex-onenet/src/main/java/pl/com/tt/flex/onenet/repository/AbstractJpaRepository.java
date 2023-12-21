package pl.com.tt.flex.onenet.repository;

import java.io.Serializable;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;

import pl.com.tt.flex.onenet.domain.EntityInterface;

@NoRepositoryBean
public interface AbstractJpaRepository<ENTITY extends EntityInterface, ID extends Serializable> extends JpaRepository<ENTITY, ID>, JpaSpecificationExecutor<ENTITY> {

}
