package pl.com.tt.flex.server.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletRequest;

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.apache.commons.lang3.StringUtils.isEmpty;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ServletRequestUtils {

    private static final String LOCALHOST_IP = "127.0.0.1";
    private static final String LOCALHOST_IP6 = "0:0:0:0:0:0:0:1";

    public static String getClientIpAddr(HttpServletRequest request) {
        String ip = !LOCALHOST_IP.equals(request.getHeader("X-Forwarded-For")) ? request.getHeader("X-Forwarded-For") : null;
        if (isBlankOrUnknown(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
            logInfoIfPresent("Proxy-Client-IP ip = {}", ip);
        }
        if (isBlankOrUnknown(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
            logInfoIfPresent("WL-Proxy-Client-IP ip = {}", ip);
        }
        if (isBlankOrUnknown(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
            logInfoIfPresent("HTTP_CLIENT_IP ip = {}", ip);
        }
        if (isBlankOrUnknown(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
            logInfoIfPresent("HTTP_X_FORWARDED_FOR ip = {}", ip);
        }
        if (isBlankOrUnknown(ip)) {
            ip = request.getRemoteAddr();
            logInfoIfPresent("getRemoteAddr() ip = {}", ip);
        }
        return ip;
    }

    private static void logInfoIfPresent(String message, String param) {
        if (!isEmpty(param)) {
            log.info(message, param);
        }
    }

    private static boolean isBlankOrUnknown(String ip) {
        return isNullOrEmpty(ip) || "unknown".equalsIgnoreCase(ip);
    }

}
