package pl.com.tt.flex.onenet.config;

import org.jasypt.util.text.StrongTextEncryptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.data.repository.query.SecurityEvaluationContextExtension;

import io.jsonwebtoken.io.Decoders;
import pl.com.tt.flex.onenet.security.jwt.JWTConfigurer;
import pl.com.tt.flex.onenet.security.jwt.TokenProvider;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

	private final TokenProvider tokenProvider;
	private final ApplicationProperties applicationProperties;

	public SecurityConfiguration(final TokenProvider tokenProvider,
								 final ApplicationProperties applicationProperties) {
		this.tokenProvider = tokenProvider;
		this.applicationProperties = applicationProperties;
	}

	@Override
	public void configure(WebSecurity web) throws Exception {
		web.ignoring()
				.antMatchers(HttpMethod.OPTIONS, "/**")
				.antMatchers("/app/**/*.{js,html}")
				.antMatchers("/bower_components/**")
				.antMatchers("/i18n/**")
				.antMatchers("/content/**")
				.antMatchers("/swagger-ui/index.html")
				.antMatchers("/test/**")
				.antMatchers("/h2-console/**");
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http
				.csrf()
				.disable()
				.headers()
				.frameOptions()
				.disable()
				.and()
				.sessionManagement()
				.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
				.and()
				.authorizeRequests()
				.antMatchers(HttpMethod.TRACE).denyAll()
				.antMatchers("/api/**").authenticated()
				.antMatchers("/management/health").permitAll()
				.and()
				.apply(securityConfigurerAdapter());
	}

	private JWTConfigurer securityConfigurerAdapter() {
		return new JWTConfigurer(tokenProvider);
	}

	@Bean
	public SecurityEvaluationContextExtension securityEvaluationContextExtension() {
		return new SecurityEvaluationContextExtension();
	}

	@Bean
	public StrongTextEncryptor textEncoder() {
		byte[] keyBytes = Decoders.BASE64.decode(applicationProperties.getOnenet().getBase64Secret());
		StrongTextEncryptor encryptor = new StrongTextEncryptor();
		encryptor.setPassword(new String(keyBytes));
		return encryptor;
	}
}
