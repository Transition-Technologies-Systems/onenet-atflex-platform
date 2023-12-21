package pl.com.tt.flex.onenet.service.consumedata.scheduled;

import org.jasypt.util.text.StrongTextEncryptor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;
import pl.com.tt.flex.onenet.service.common.AbstractOnenetJwtService;
import pl.com.tt.flex.onenet.service.connector.OnenetConnectorService;
import pl.com.tt.flex.onenet.service.consumedata.ConsumeDataService;
import pl.com.tt.flex.onenet.service.onenetuser.OnenetUserService;

@Slf4j
@Component
@ConditionalOnProperty("application.onenet.update-consume-data.enabled")
public class ConsumeDataUpdateTask extends AbstractOnenetJwtService {
	private final ConsumeDataService consumeDataService;

	public ConsumeDataUpdateTask(final OnenetConnectorService onenetConnectorService, final StrongTextEncryptor encoder, final OnenetUserService onenetUserService,
								 final ConsumeDataService consumeDataService) {
		super(onenetConnectorService, encoder, onenetUserService);
		this.consumeDataService = consumeDataService;
	}

	/**
	 * Metoda aktualizująca skonsumowane dane użytkowników danymi z Onenet API. Dane są odświeżane co 10 sekund.
	 */
	@Scheduled(cron = "${application.onenet.update-consume-data.cron}")
	@Transactional
	public void execute() {
		log.trace("ConsumeDataUpdateTask - updating users consume data started");
		consumeDataService.fetchAndSaveConsumeDataForAllUsers();
		log.trace("ConsumeDataUpdateTask - updating users consume data ended");
	}

}
