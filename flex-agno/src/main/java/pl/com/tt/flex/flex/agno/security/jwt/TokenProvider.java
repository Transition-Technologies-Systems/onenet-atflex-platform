package pl.com.tt.flex.flex.agno.security.jwt;

import java.security.Key;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import pl.com.tt.flex.flex.agno.config.ApplicationProperties;

@Slf4j
@Component
public class TokenProvider {

	private static final String AUTHORITIES_KEY = "auth";

	private Key key;

	private final ApplicationProperties applicationProperties;

	private long tokenValidityInMilliseconds;


	public TokenProvider(ApplicationProperties applicationProperties) {
		this.applicationProperties = applicationProperties;
	}

	@PostConstruct
	public void init() {
		byte[] keyBytes;
		log.debug("Using a Base64-encoded JWT secret key");
		keyBytes = Decoders.BASE64.decode(applicationProperties.getJwt().getBase64Secret());
		this.key = Keys.hmacShaKeyFor(keyBytes);
		this.tokenValidityInMilliseconds = 1000 * applicationProperties.getJwt().getTokenValidityInSeconds();
	}

	public Authentication getAuthentication(String token) {
		Claims claims = Jwts.parserBuilder()
				.setSigningKey(key)
				.build()
				.parseClaimsJws(token)
				.getBody();

		Collection<? extends GrantedAuthority> authorities =
				Arrays.stream(claims.get(AUTHORITIES_KEY).toString().split(","))
						.map(SimpleGrantedAuthority::new)
						.collect(Collectors.toList());

		User principal = new User(claims.getSubject(), "", authorities);

		return new UsernamePasswordAuthenticationToken(principal, token, authorities);
	}

	public String createToken(Authentication authentication) {
		String authorities = authentication.getAuthorities().stream()
				.map(GrantedAuthority::getAuthority)
				.collect(Collectors.joining(","));

		long now = (new Date()).getTime();
		Date validity = new Date(now + this.tokenValidityInMilliseconds);
		return Jwts.builder()
				.setSubject(authentication.getName())
				.claim(AUTHORITIES_KEY, authorities)
				.signWith(key, SignatureAlgorithm.HS512)
				.setExpiration(validity)
				.compact();
	}

	public boolean validateToken(String authToken) {
		try {
			Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(authToken);
			return true;
		} catch (JwtException | IllegalArgumentException e) {
			log.info("Invalid JWT token.");
			log.trace("Invalid JWT token trace.", e);
			e.printStackTrace();
		}
		return false;
	}
}
