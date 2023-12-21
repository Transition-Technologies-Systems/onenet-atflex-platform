package pl.com.tt.flex.onenet.service.common;

import java.time.Instant;

import org.jasypt.util.text.StrongTextEncryptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;

import pl.com.tt.flex.onenet.domain.onenetuser.OnenetUserEntity;
import pl.com.tt.flex.onenet.service.connector.OnenetConnectorService;
import pl.com.tt.flex.onenet.service.onenetuser.OnenetUserService;

public abstract class AbstractOnenetJwtService extends AbstractOnenetService {

	@Value("${application.onenet.authorize-always}")
	private boolean authorizeAlways;

	protected final OnenetUserService onenetUserService;

	public AbstractOnenetJwtService(final OnenetConnectorService onenetConnectorService, final StrongTextEncryptor encoder, final OnenetUserService onenetUserService) {
		super(onenetConnectorService, encoder);
		this.onenetUserService = onenetUserService;
	}

	/**
	 * Pobiera aktywny token dostępu dla podanego użytkownika onenet,
	 * dokładna funkcjonalność zależy od wartości ustawionej w application.onenet.authorize-always
	 */
	@Transactional
	public String getToken() {
		return getToken(onenetUserService.getCurrentActiveUser());
	}

	@Transactional
	public String getToken(OnenetUserEntity onenetUser) {
		if (authorizeAlways) {
			return getNewTokenFromOnenet(onenetUser);
		}
		renewTokenIfExpired(onenetUser);
		return encoder.decrypt(onenetUser.getTokenHash());
	}

	private void renewTokenIfExpired(OnenetUserEntity activeOnenetUser) {
		if (activeOnenetUser.getTokenValidTo().isBefore(Instant.now())) {
			String newToken = getNewTokenFromOnenet(activeOnenetUser);
			activeOnenetUser.setTokenHash(encoder.encrypt(newToken));
			activeOnenetUser.setTokenValidTo(getTokenValidTo(newToken));
		}
	}

	private String getNewTokenFromOnenet(OnenetUserEntity activeOnenetUser) {
		return onenetConnectorService.authOnenetUser(activeOnenetUser.getUsername(), encoder.decrypt(activeOnenetUser.getPasswordHash())).getAccessToken();
	}

}
