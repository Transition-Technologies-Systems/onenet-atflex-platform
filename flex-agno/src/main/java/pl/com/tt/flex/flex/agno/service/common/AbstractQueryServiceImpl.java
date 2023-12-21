package pl.com.tt.flex.flex.agno.service.common;

import io.github.jhipster.service.Criteria;
import io.github.jhipster.service.QueryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;
import pl.com.tt.flex.flex.agno.domain.EntityInterface;
import pl.com.tt.flex.flex.agno.service.AbstractQueryService;

import java.io.Serializable;
import java.util.List;

import static pl.com.tt.flex.flex.agno.service.common.QueryServiceUtil.setDefaultOrder;

@Slf4j
public abstract class AbstractQueryServiceImpl<ENTITY extends EntityInterface<ID>, DTO, ID extends Serializable, CRITERIA extends Criteria> extends QueryService<ENTITY> implements AbstractQueryService<ENTITY, DTO, ID, CRITERIA> {

    /**
     * Return a {@link List} of {@link DTO} which matches the criteria from the database.
     *
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public List<DTO> findByCriteria(CRITERIA criteria) {
        log.debug("find by criteria : {}", criteria);
        final Specification<ENTITY> specification = createSpecification(criteria);
        return getMapper().toDto(getRepository().findAll(specification));
    }

    /**
     * Return a {@link Page} of {@link DTO} which matches the criteria from the database.
     *
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page     The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<DTO> findByCriteria(CRITERIA criteria, Pageable page) {
        log.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<ENTITY> specification = createSpecification(criteria);
        page = setDefaultOrder(page, getDefaultOrderProperty());
        return getRepository().findAll(specification, page)
                .map(getMapper()::toDto);
    }

    /**
     * Return the number of matching entities in the database.
     *
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(CRITERIA criteria) {
        log.debug("count by criteria : {}", criteria);
        final Specification<ENTITY> specification = createSpecification(criteria);
        return getRepository().count(specification);
    }

    protected abstract Specification<ENTITY> createSpecification(CRITERIA criteria);
}
