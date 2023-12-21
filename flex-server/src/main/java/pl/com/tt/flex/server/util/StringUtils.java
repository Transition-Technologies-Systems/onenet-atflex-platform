package pl.com.tt.flex.server.util;

import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StringUtils {

    public static String substringNullSafe(String value, int length) {
        String str = Strings.nullToEmpty(value);
        return str.length() <= length ? str : str.substring(0, length);
    }

    public static String createErrorMessage(Throwable e) {
        String exception = String.valueOf(e);
        return exception.substring(0, Math.min(exception.length(), 1000));
    }
}
