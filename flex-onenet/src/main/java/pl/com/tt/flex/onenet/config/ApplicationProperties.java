package pl.com.tt.flex.onenet.config;


import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Component
@NoArgsConstructor
@ConfigurationProperties(prefix = "application", ignoreUnknownFields = false)
public class ApplicationProperties {

	private final Jwt jwt = new Jwt();
	private final Onenet onenet = new Onenet();

	@Getter
	@Setter
	@NoArgsConstructor
	public static class Jwt {
		private String base64Secret;
		private Long tokenValidityInSeconds;
	}

	@Getter
	@Setter
	@NoArgsConstructor
	public static class Onenet {
		private String url;
		private String base64Secret;
		private boolean authorizeAlways;
		private UpdateOfferedServices updateOfferedServices = new UpdateOfferedServices();
		private UpdateConsumeData updateConsumeData = new UpdateConsumeData();
		private UpdateProvideData updateProvideData = new UpdateProvideData();

		@Getter
		@Setter
		@NoArgsConstructor
		public static class UpdateOfferedServices {
			private boolean enabled;
			private String cron;
		}

		@Getter
		@Setter
		@NoArgsConstructor
		public static class UpdateConsumeData {
			private boolean enabled;
			private String cron;
		}

		@Getter
		@Setter
		@NoArgsConstructor
		public static class UpdateProvideData {
			private boolean enabled;
			private String cron;
		}
	}
}
