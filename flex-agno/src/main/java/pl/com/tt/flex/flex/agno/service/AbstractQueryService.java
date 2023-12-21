package pl.com.tt.flex.flex.agno.service;

import io.github.jhipster.service.Criteria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import pl.com.tt.flex.flex.agno.domain.EntityInterface;
import pl.com.tt.flex.flex.agno.repository.AbstractJpaRepository;
import pl.com.tt.flex.flex.agno.service.mapper.EntityMapper;

import java.io.Serializable;
import java.util.List;

public interface AbstractQueryService<ENTITY extends EntityInterface<ID>, DTO, ID extends Serializable, CRITERIA extends Criteria> {

    @Transactional(readOnly = true)
    List<DTO> findByCriteria(CRITERIA criteria);

    @Transactional(readOnly = true)
    Page<DTO> findByCriteria(CRITERIA criteria, Pageable page);

    long countByCriteria(CRITERIA criteria);

    String getDefaultOrderProperty();

    AbstractJpaRepository<ENTITY, ID> getRepository();

    EntityMapper<DTO, ENTITY> getMapper();
}
