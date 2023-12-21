package pl.com.tt.flex.server.config;

import lombok.Getter;

@Getter
public enum AppModuleName {

    FLEX_ADMIN(Constants.FLEX_ADMIN_APP_NAME),
    FLEX_USER(Constants.FLEX_USER_APP_NAME),
    FLEX_SERVER(Constants.FLEX_SERVER_APP_NAME);

    private final String stringValue;

    AppModuleName(String stringValue) {
        this.stringValue = stringValue;
    }

    public static AppModuleName fromStringName(String stringValue) {
        for (AppModuleName type : AppModuleName.values()) {
            if (type.stringValue.equals(stringValue)) {
                return type;
            }
        }
        throw new RuntimeException("Wrong AppModuleName value: " + stringValue);
    }
}
