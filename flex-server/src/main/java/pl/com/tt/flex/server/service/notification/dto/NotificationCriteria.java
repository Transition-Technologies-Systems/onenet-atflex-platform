package pl.com.tt.flex.server.service.notification.dto;

import java.io.Serializable;
import java.util.Objects;
import io.github.jhipster.service.Criteria;
import pl.com.tt.flex.server.domain.enumeration.NotificationEvent;
import io.github.jhipster.service.filter.Filter;
import io.github.jhipster.service.filter.LongFilter;
import io.github.jhipster.service.filter.InstantFilter;
import pl.com.tt.flex.server.domain.notification.NotificationEntity;

/**
 * Criteria class for the {@link NotificationEntity} entity. This class is used
 * in {@link pl.com.tt.flex.server.web.rest.NotificationResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /notifications?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
public class NotificationCriteria implements Serializable, Criteria {
    /**
     * Class for filtering NotificationEventType
     */
    public static class NotificationEventTypeFilter extends Filter<NotificationEvent> {

        public NotificationEventTypeFilter() {
        }

        public NotificationEventTypeFilter(NotificationEventTypeFilter filter) {
            super(filter);
        }

        @Override
        public NotificationEventTypeFilter copy() {
            return new NotificationEventTypeFilter(this);
        }

    }

    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private NotificationEventTypeFilter eventType;

    private InstantFilter createdDate;

    private LongFilter notificationUserId;

    private LongFilter notificationParamId;

    public NotificationCriteria() {
    }

    public NotificationCriteria(NotificationCriteria other) {
        this.id = other.id == null ? null : other.id.copy();
        this.eventType = other.eventType == null ? null : other.eventType.copy();
        this.createdDate = other.createdDate == null ? null : other.createdDate.copy();
        this.notificationUserId = other.notificationUserId == null ? null : other.notificationUserId.copy();
        this.notificationParamId = other.notificationParamId == null ? null : other.notificationParamId.copy();
    }

    @Override
    public NotificationCriteria copy() {
        return new NotificationCriteria(this);
    }

    public LongFilter getId() {
        return id;
    }

    public void setId(LongFilter id) {
        this.id = id;
    }

    public NotificationEventTypeFilter getEventType() {
        return eventType;
    }

    public void setEventType(NotificationEventTypeFilter eventType) {
        this.eventType = eventType;
    }

    public InstantFilter getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(InstantFilter createdDate) {
        this.createdDate = createdDate;
    }

    public LongFilter getNotificationUserId() {
        return notificationUserId;
    }

    public void setNotificationUserId(LongFilter notificationUserId) {
        this.notificationUserId = notificationUserId;
    }

    public LongFilter getNotificationParamId() {
        return notificationParamId;
    }

    public void setNotificationParamId(LongFilter notificationParamId) {
        this.notificationParamId = notificationParamId;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final NotificationCriteria that = (NotificationCriteria) o;
        return
            Objects.equals(id, that.id) &&
            Objects.equals(eventType, that.eventType) &&
            Objects.equals(createdDate, that.createdDate) &&
            Objects.equals(notificationUserId, that.notificationUserId) &&
            Objects.equals(notificationParamId, that.notificationParamId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
        id,
        eventType,
        createdDate,
        notificationUserId,
        notificationParamId
        );
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "NotificationCriteria{" +
                (id != null ? "id=" + id + ", " : "") +
                (eventType != null ? "eventType=" + eventType + ", " : "") +
                (createdDate != null ? "createdDate=" + createdDate + ", " : "") +
                (notificationUserId != null ? "notificationUserId=" + notificationUserId + ", " : "") +
                (notificationParamId != null ? "notificationParamId=" + notificationParamId + ", " : "") +
            "}";
    }

}
