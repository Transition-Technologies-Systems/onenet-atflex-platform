package pl.com.tt.flex.onenet.util;

import java.util.Optional;

public class StringUtil {

	public static String getStringOrNull(Object value) {
		return Optional.ofNullable(value).map(String::valueOf).filter(string -> !string.isBlank()).orElse(null);
	}

}
