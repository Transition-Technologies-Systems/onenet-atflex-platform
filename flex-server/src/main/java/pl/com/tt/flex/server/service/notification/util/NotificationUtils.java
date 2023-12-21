package pl.com.tt.flex.server.service.notification.util;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pl.com.tt.flex.model.service.dto.audit.AbstractAuditingDTO;
import pl.com.tt.flex.server.domain.audit.AbstractAuditingEntity;
import pl.com.tt.flex.server.domain.enumeration.NotificationEvent;
import pl.com.tt.flex.server.domain.enumeration.NotificationParam;
import pl.com.tt.flex.server.domain.notification.NotificationParamEntity;
import pl.com.tt.flex.model.service.dto.MinimalDTO;
import pl.com.tt.flex.server.service.notification.dto.NotificationDTO;
import pl.com.tt.flex.server.service.notification.dto.NotificationParamValue;
import pl.com.tt.flex.server.service.notification.factory.NotifierFactory;
import pl.com.tt.flex.server.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public class NotificationUtils {

    public static void registerNewNotification(NotifierFactory notifierFactory, NotificationEvent event, Map<NotificationParam, NotificationParamValue> params) {
        NotificationDTO notificationDTO = createDTO(event, params);
        notifierFactory.getNotifier(event).notify(notificationDTO);
    }

    public static void registerNewNotificationForSpecifiedUsers(NotifierFactory notifierFactory, NotificationEvent event, Map<NotificationParam, NotificationParamValue> params,
                                                                List<MinimalDTO<Long, String>> users) {
        NotificationDTO notificationDTO = createDTO(event, params);
        notificationDTO.setUsers(users);
        notifierFactory.getNotifier(event).notify(notificationDTO);
    }

    private static NotificationDTO createDTO(NotificationEvent event, Map<NotificationParam, NotificationParamValue> params) {
        NotificationDTO notificationDTO = new NotificationDTO();
        notificationDTO.setEventType(event);
        cutParamValuesIfNecessary(params);
        notificationDTO.setParams(params);
        return notificationDTO;
    }

    private static void cutParamValuesIfNecessary(Map<NotificationParam, NotificationParamValue> params) {
        params.keySet().forEach(key -> {
            NotificationParamValue notificationParamValue = params.get(key);
            notificationParamValue.setValue(StringUtils.substringNullSafe(notificationParamValue.getValue(), NotificationParamEntity.MAX_LENGTH_FOR_PARAM_VALUE));
            params.put(key, notificationParamValue);
        });
    }

    /**
     * Komunikat wyswietlany jest dla uzytkownika ktory stworzyl, ostatnio zmodyfikowal i aktualnie modyfikuje danego FP,
     */
    public static Set<String> getLoginsOfUsersToBeNotified(String currentUserLogin, AbstractAuditingEntity auditingEntity) {
        return Sets.newHashSet(currentUserLogin, auditingEntity.getCreatedBy(), auditingEntity.getLastModifiedBy());
    }

    /**
     * Komunikat wyswietlany jest dla uzytkownika ktory stworzyl, ostatnio zmodyfikowal i aktualnie modyfikuje danego FP,
     */
    public static Set<String> getLoginsOfUsersToBeNotified(String currentUserLogin, AbstractAuditingDTO auditingDTO) {
        return Sets.newHashSet(currentUserLogin, auditingDTO.getCreatedBy(), auditingDTO.getLastModifiedBy());
    }

    public static class ParamsMapBuilder {
        private Map<NotificationParam, NotificationParamValue> paramsToValue = Maps.newHashMap();

        private ParamsMapBuilder() {
        }

        public static ParamsMapBuilder create() {
            return new ParamsMapBuilder();
        }

        // Zapis jako paramsow obiektu np. jsona
        // Wartosc ta trzymana jest w kolumnie OBJECT w tabeli NOTIFICATION_PARAM
        public ParamsMapBuilder addObjectsParam(NotificationParam param, String objects) {
            if (org.springframework.util.StringUtils.isEmpty(objects)) {
                log.debug("addObjectsParam() Cannot add empty string {}", param.name());
                return this;
            }
            paramsToValue.put(param, NotificationParamValue.ParamValueBuilder.create().addObject(objects).build());
            return this;
        }

        public ParamsMapBuilder addParam(NotificationParam param, Object value) {
            if (Objects.isNull(value)) {
                log.debug("addParam() Cannot add param {} with null value", param.name());
                return this;
            }
            paramsToValue.put(param, NotificationParamValue.ParamValueBuilder.create().addParam(String.valueOf(value)).build());
            return this;
        }

        // Dodaje parametr do listy tylko wtedy gdy: oldValue != newValue.
        public ParamsMapBuilder addModificationParam(NotificationParam param, Object oldValue, Object newValue) {
            if (Objects.equals(oldValue, newValue)) {
                return this;
            }
            addParam(param, newValue);
            return this;
        }

        public Map<NotificationParam, NotificationParamValue> build() {
            return paramsToValue;
        }
    }

}
