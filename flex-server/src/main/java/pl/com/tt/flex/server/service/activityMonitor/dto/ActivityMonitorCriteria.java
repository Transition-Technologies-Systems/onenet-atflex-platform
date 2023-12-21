package pl.com.tt.flex.server.service.activityMonitor.dto;

import io.github.jhipster.service.Criteria;
import io.github.jhipster.service.filter.Filter;
import io.github.jhipster.service.filter.InstantFilter;
import io.github.jhipster.service.filter.LongFilter;
import io.github.jhipster.service.filter.StringFilter;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.com.tt.flex.server.config.AppModuleName;
import pl.com.tt.flex.server.domain.activityMonitor.ActivityEvent;
import pl.com.tt.flex.server.domain.fsp.FspEntity;
import pl.com.tt.flex.server.web.rest.fsp.FspResourceAdmin;

import java.io.Serializable;

/**
 * Criteria class for the {@link FspEntity} entity. This class is used
 * in {@link FspResourceAdmin} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /fsps?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
public class ActivityMonitorCriteria implements Serializable, Criteria {

    /**
     * Class for filtering ActivityEvent
     */
    @NoArgsConstructor
    public static class ActivityEventFilter extends Filter<ActivityEvent> {

        public ActivityEventFilter(ActivityMonitorCriteria.ActivityEventFilter filter) {
            super(filter);
        }

        @Override
        public ActivityMonitorCriteria.ActivityEventFilter copy() {
            return new ActivityMonitorCriteria.ActivityEventFilter(this);
        }

    }

    @NoArgsConstructor
    public static class AppModuleNameFilter extends Filter<AppModuleName> {

        public AppModuleNameFilter(AppModuleNameFilter filter) {
            super(filter);
        }

        @Override
        public AppModuleNameFilter copy() {
            return new AppModuleNameFilter(this);
        }
    }

    private static final long serialVersionUID = 1L;
    private LongFilter id;
    private InstantFilter createdDate;
    private ActivityEventFilter event;
    private StringFilter login;
    private StringFilter httpRequestUriPath;
    private StringFilter httpResponseStatus;
    private AppModuleNameFilter appModuleName;

    ActivityMonitorCriteria(ActivityMonitorCriteria other) {
        this.id = other.id == null ? null : other.id.copy();
        this.createdDate = other.createdDate == null ? null : other.createdDate.copy();
        this.event = other.event == null ? null : other.event.copy();
        this.login = other.login == null ? null : other.login.copy();
        this.httpRequestUriPath = other.httpRequestUriPath == null ? null : other.httpRequestUriPath.copy();
        this.httpResponseStatus = other.httpResponseStatus == null ? null : other.httpResponseStatus.copy();
        this.appModuleName = other.appModuleName == null ? null : other.appModuleName.copy();
    }

    @Override
    public ActivityMonitorCriteria copy() {
        return new ActivityMonitorCriteria(this);
    }
}
