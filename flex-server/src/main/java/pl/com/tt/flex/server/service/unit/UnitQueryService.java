package pl.com.tt.flex.server.service.unit;

import static pl.com.tt.flex.server.domain.unit.UnitEntity_.ID;
import static pl.com.tt.flex.server.domain.unit.UnitEntity_.active;
import static pl.com.tt.flex.server.domain.unit.UnitEntity_.aggregated;
import static pl.com.tt.flex.server.domain.unit.UnitEntity_.certified;
import static pl.com.tt.flex.server.domain.unit.UnitEntity_.code;
import static pl.com.tt.flex.server.domain.unit.UnitEntity_.connectionPower;
import static pl.com.tt.flex.server.domain.unit.UnitEntity_.derTypeGeneration;
import static pl.com.tt.flex.server.domain.unit.UnitEntity_.directionOfDeviation;
import static pl.com.tt.flex.server.domain.unit.UnitEntity_.fsp;
import static pl.com.tt.flex.server.domain.unit.UnitEntity_.id;
import static pl.com.tt.flex.server.domain.unit.UnitEntity_.mridDso;
import static pl.com.tt.flex.server.domain.unit.UnitEntity_.mridTso;
import static pl.com.tt.flex.server.domain.unit.UnitEntity_.name;
import static pl.com.tt.flex.server.domain.unit.UnitEntity_.pMin;
import static pl.com.tt.flex.server.domain.unit.UnitEntity_.powerStationTypes;
import static pl.com.tt.flex.server.domain.unit.UnitEntity_.qMax;
import static pl.com.tt.flex.server.domain.unit.UnitEntity_.qMin;
import static pl.com.tt.flex.server.domain.unit.UnitEntity_.sourcePower;
import static pl.com.tt.flex.server.domain.unit.UnitEntity_.subportfolio;
import static pl.com.tt.flex.server.domain.unit.UnitEntity_.validFrom;
import static pl.com.tt.flex.server.domain.unit.UnitEntity_.validTo;
import static pl.com.tt.flex.server.domain.unit.UnitEntity_.version;

import java.time.Instant;
import java.util.List;

import javax.persistence.criteria.JoinType;
import javax.persistence.metamodel.SingularAttribute;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.jhipster.service.filter.InstantFilter;
import io.github.jhipster.service.filter.StringFilter;
import lombok.extern.slf4j.Slf4j;
import pl.com.tt.flex.server.common.specification.SpecificationCombiner;
import pl.com.tt.flex.server.domain.audit.AbstractAuditingEntity_;
import pl.com.tt.flex.server.domain.fsp.FspEntity_;
import pl.com.tt.flex.server.domain.subportfolio.SubportfolioEntity_;
import pl.com.tt.flex.server.domain.unit.DerTypeEntity_;
import pl.com.tt.flex.server.domain.unit.LocalizationTypeEntity_;
import pl.com.tt.flex.server.domain.unit.UnitEntity;
import pl.com.tt.flex.server.repository.AbstractJpaRepository;
import pl.com.tt.flex.server.repository.unit.UnitRepository;
import pl.com.tt.flex.server.service.common.AbstractQueryServiceImpl;
import pl.com.tt.flex.server.service.mapper.EntityMapper;
import pl.com.tt.flex.server.service.unit.dto.UnitCriteria;
import pl.com.tt.flex.server.service.unit.dto.UnitDTO;
import pl.com.tt.flex.server.service.unit.dto.UnitMinDTO;
import pl.com.tt.flex.server.service.unit.mapper.UnitMapper;
import pl.com.tt.flex.server.util.CriteriaUtils;

/**
 * Service for executing complex queries for {@link UnitEntity} entities in the database.
 * The main input is a {@link UnitCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link List} of {@link UnitDTO} or a {@link Page} of {@link UnitDTO} which fulfills the criteria.
 */
@Slf4j
@Service
@Transactional(readOnly = true)
public class UnitQueryService extends AbstractQueryServiceImpl<UnitEntity, UnitDTO, Long, UnitCriteria> {

    private final UnitRepository unitRepository;

    private final UnitMapper unitMapper;

    public UnitQueryService(final UnitRepository unitRepository, final UnitMapper unitMapper) {
        this.unitRepository = unitRepository;
        this.unitMapper = unitMapper;
    }

    @Transactional(readOnly = true)
    public List<UnitMinDTO> findMinByCriteria(UnitCriteria criteria, Sort sort) {
        log.debug("find min by criteria : {}", criteria);
        final Specification<UnitEntity> specification = createSpecification(criteria);
        return unitMapper.toMinDto(unitRepository.findAll(specification, sort));
    }

    /**
     * Return a {@link List} of {@link UnitDTO} which matches the criteria from the database.
     *
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param sort     The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public List<UnitDTO> findByCriteria(UnitCriteria criteria, Sort sort) {
        log.debug("find by criteria : {}, sort: {}", criteria, sort);
        final Specification<UnitEntity> specification = createSpecification(criteria);
        return unitMapper.toDto(unitRepository.findAll(specification, sort));
    }

    /**
     * Function to convert {@link UnitCriteria} to a {@link Specification}
     *
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<UnitEntity> createSpecification(UnitCriteria criteria) {
        return new SpecificationCombiner<UnitEntity>(criteria)
            .and(criteria::getId, id, this::buildRangeSpecification)
            .and(criteria::getName, name, this::buildStringSpecification)
            .and(criteria::getCode, code, this::buildStringSpecification)
            .and(criteria::getMridTso, mridTso, this::buildStringSpecification)
            .and(criteria::getMridDso, mridDso, this::buildStringSpecification)
            .and(criteria::getAggregated, aggregated, this::buildSpecification)
            .and(criteria::getValidFrom, validFrom, this::buildInstantFilterRangeSpecification)
            .and(criteria::getValidTo, validTo, this::buildInstantFilterRangeSpecification)
            .and(criteria::getActive, active, this::buildSpecification)
            .and(criteria::getCertified, certified, this::buildSpecification)
            .and(criteria::getVersion, version, this::buildRangeSpecification)
            .and(criteria::getCreatedBy, AbstractAuditingEntity_.createdBy, this::buildStringSpecification)
            .and(criteria::getCreatedDate, AbstractAuditingEntity_.createdDate, this::buildInstantFilterRangeSpecification)
            .and(criteria::getLastModifiedBy, AbstractAuditingEntity_.lastModifiedBy, this::buildStringSpecification)
            .and(criteria::getLastModifiedDate, AbstractAuditingEntity_.lastModifiedDate, this::buildInstantFilterRangeSpecification)
            .and(criteria::getFspId, longFilter -> buildSpecification(longFilter, root -> root.join(fsp, JoinType.INNER).get(FspEntity_.id)))
            .and(criteria::getFspActive, booleanFilter -> buildReferringEntitySpecification(booleanFilter, fsp, FspEntity_.active))
            .and(criteria::getFspRepresentativeCompanyName, stringFilter ->
                buildSpecification(stringFilter, root -> root.join(fsp, JoinType.INNER).get(FspEntity_.companyName)))
            .and(criteria::getDirectionOfDeviation, directionOfDeviation, this::buildSpecification)
            .and(criteria::getSourcePower, sourcePower, this::buildRangeSpecification)
            .and(criteria::getConnectionPower, connectionPower, this::buildRangeSpecification)
            .and(criteria::getPowerStation, stringFilter ->
                buildSpecification(stringFilter, root -> root.join(powerStationTypes, JoinType.INNER).get(LocalizationTypeEntity_.name)))
            .and(criteria::getSubportfolioId, longFilter -> buildSpecification(longFilter, root -> root.join(subportfolio, JoinType.INNER).get(SubportfolioEntity_.id)))
            .and(criteria::getSubportfolioName, stringFilter ->
                buildSubportfolioNameSpecification(criteria, stringFilter))
            .and(criteria::getDerTypeId, longFilter -> buildSpecification(longFilter, root -> root.join(derTypeGeneration, JoinType.INNER).get(DerTypeEntity_.id)))
            .and(criteria::getPmin, pMin, this::buildRangeSpecification)
            .and(criteria::getQmin, qMin, this::buildRangeSpecification)
            .and(criteria::getQmax, qMax, this::buildRangeSpecification)
            .combine();
    }

    private Specification<UnitEntity> buildInstantFilterRangeSpecification(InstantFilter instantFilter,
                                                                           SingularAttribute<? super UnitEntity, Instant> instantField) {
        CriteriaUtils.setOnlyDayIfEqualsFilter(instantFilter);
        return buildRangeSpecification(instantFilter, instantField);
    }

    // jeżeli użytkownik korzysta z filtra "not contains" to pobierane są również dery niepodpięte pod subportfolio,
    // w przeciwnym wypadku filtrować normalnie
    private Specification<UnitEntity> buildSubportfolioNameSpecification(UnitCriteria criteria, StringFilter stringFilter) {
        if (criteria.getSubportfolioName().getDoesNotContain() != null) {
            return (root, criteriaQuery, criteriaBuilder) ->
                criteriaBuilder.or(criteriaBuilder.notLike(root.join(subportfolio, JoinType.LEFT).get(SubportfolioEntity_.name), "%" +
                    criteria.getSubportfolioName().getDoesNotContain() + "%"), criteriaBuilder.isNull(root.join(subportfolio, JoinType.LEFT).get(SubportfolioEntity_.name)));
        }

        return buildSpecification(stringFilter, root -> root.join(subportfolio, JoinType.LEFT).get(SubportfolioEntity_.name));
    }

    @Override
    public String getDefaultOrderProperty() {
        return ID;
    }

    @Override
    public AbstractJpaRepository<UnitEntity, Long> getRepository() {
        return this.unitRepository;
    }

    @Override
    public EntityMapper<UnitDTO, UnitEntity> getMapper() {
        return this.unitMapper;
    }
}
