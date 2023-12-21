package pl.com.tt.flex.flex.agno.common.criteria;

import io.github.jhipster.service.filter.Filter;

import java.util.Optional;

/**
 * Provides null safe ability to copy filters.
 */
public interface FilterCopyableCriteria {

    /**
     * When provided filter is not null then copy operation occurs, otherwise returns null.
     *
     * @return copied filter or null.
     * @see Filter#copy()
     */
    default <F extends Filter<FT>, FT> F copyFilter(F filter) {
        return (F) Optional.ofNullable(filter).map(F::copy).orElse(null);
    }

}
