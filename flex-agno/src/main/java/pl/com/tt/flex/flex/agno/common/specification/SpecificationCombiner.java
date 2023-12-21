package pl.com.tt.flex.flex.agno.common.specification;

import io.github.jhipster.service.Criteria;
import io.github.jhipster.service.filter.Filter;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.metamodel.SingularAttribute;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Null safe specification combiner.
 *
 * @param <E> entity type
 */
public class SpecificationCombiner<E> {

    private final List<Specification<E>> specifications;
    private final boolean isCriteriaNotNull;

    public SpecificationCombiner(Criteria criteria) {
        this.isCriteriaNotNull = criteria != null;
        this.specifications = new LinkedList<>();
    }

    /**
     * When neither of filter or criteria is null then specification is added to be ANDed with other ones.
     *
     * @throws NullPointerException when either of parameters is null
     * @see Specification#and(Specification)
     */
    public <F extends Filter<T>, T, V extends SingularAttribute<?, ?>>
    SpecificationCombiner<E> and(Supplier<F> filter, V field, BiFunction<F, V, Specification<E>> specification) {
        Objects.requireNonNull(filter);
        Objects.requireNonNull(field);
        Objects.requireNonNull(specification);

        F f = filter.get();
        if (f != null && isCriteriaNotNull) {
            specifications.add(specification.apply(f, field));
        }

        return this;
    }

    /**
     * When neither of filter or criteria is null then specification is added to be ANDed with other ones.
     *
     * @throws NullPointerException when either of parameters is null
     * @see Specification#and(Specification)
     */
    public <F extends Filter<T>, T> SpecificationCombiner<E> and(Supplier<F> filter, Function<F, Specification<E>> specification) {
        Objects.requireNonNull(filter);
        Objects.requireNonNull(specification);

        F f = filter.get();
        if (f != null && isCriteriaNotNull) {
            specifications.add(specification.apply(f));
        }

        return this;
    }

    /**
     * ANDs all provided specification and returns combined one.
     *
     * @return All specification ANDed together or empty specification.
     * @see Specification#and(Specification)
     */
    public Specification<E> combine() {
        return specifications.stream().reduce(Specification.where(null), Specification::and);
    }

}
