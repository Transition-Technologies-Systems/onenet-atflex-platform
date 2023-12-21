package pl.com.tt.flex.server.service.notification.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode
public class NotificationParamValue {

    private String value;
    private String object;

    public static class ParamValueBuilder {
        private final NotificationParamValue paramValue = new NotificationParamValue();

        private ParamValueBuilder() {
        }

        public static NotificationParamValue.ParamValueBuilder create() {
            return new NotificationParamValue.ParamValueBuilder();
        }

        public NotificationParamValue.ParamValueBuilder addParam(Object value) {
            paramValue.setValue(String.valueOf(value));
            return this;
        }

        public NotificationParamValue.ParamValueBuilder addObject(String object) {
            paramValue.setObject(object);
            return this;
        }

        public NotificationParamValue.ParamValueBuilder addObject(byte[] object) {
            if (object != null) {
                paramValue.setObject(new String(object));
            }
            return this;
        }

        public NotificationParamValue build() {
            return paramValue;
        }
    }
}
