package pl.com.tt.flex.onenet.service.common;

import static pl.com.tt.flex.onenet.web.rest.errors.ErrorConstants.PROBLEM_CONNECTING_TO_ONENET;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.jasypt.util.text.StrongTextEncryptor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.io.Decoders;
import pl.com.tt.flex.onenet.service.connector.OnenetConnectorService;
import pl.com.tt.flex.onenet.web.rest.errors.OnenetSystemRequestException;

public abstract class AbstractOnenetService {

	protected final OnenetConnectorService onenetConnectorService;
	protected final StrongTextEncryptor encoder;

	public AbstractOnenetService(final OnenetConnectorService onenetConnectorService, final StrongTextEncryptor encoder) {
		this.onenetConnectorService = onenetConnectorService;
		this.encoder = encoder;
	}

	/**
	 * Pobiera z podanego tokenu datę jego wygaśnięcia
	 */
	protected Instant getTokenValidTo(String tokenHash) {
		String tokenHeader = new String(Decoders.BASE64.decode(tokenHash.split("\\.")[1]));
		try {
			Map<String, Object> tokenHeaderParams = new ObjectMapper().readValue(tokenHeader, HashMap.class);
			return Instant.ofEpochSecond(Long.parseLong(String.valueOf(tokenHeaderParams.get("exp"))));
		} catch (JsonProcessingException e) {
			throw new OnenetSystemRequestException("Error occured while processing access token",
					PROBLEM_CONNECTING_TO_ONENET);
		}
	}

}
