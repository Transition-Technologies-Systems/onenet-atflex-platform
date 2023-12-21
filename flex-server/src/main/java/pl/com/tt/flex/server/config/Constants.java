package pl.com.tt.flex.server.config;

/**
 * Application constants.
 */
public final class Constants {

    public static final String FLEX_ADMIN_APP_NAME = "FLEX-ADMIN";
    public static final String FLEX_USER_APP_NAME = "FLEX-USER";
    public static final String FLEX_SERVER_APP_NAME = "FLEX-SERVER";
    public static final String FLEX_APP_NAME_HEADER = "Gateway";

    // Regex for acceptable logins
    public static final String LOGIN_REGEX = "^(?>[a-zA-Z0-9!$&*+=?^_`{|}~.-]+@[a-zA-Z0-9-]+(?:\\.[a-zA-Z0-9-]+)*)|(?>[_.@A-Za-z0-9-]+)$";

    public static final String SYSTEM_ACCOUNT = "system";
    public static final String ANONYMOUS_USER = "anonymoususer";
    public static final String DEFAULT_LANGUAGE = "en";

    private Constants() {
    }
}
