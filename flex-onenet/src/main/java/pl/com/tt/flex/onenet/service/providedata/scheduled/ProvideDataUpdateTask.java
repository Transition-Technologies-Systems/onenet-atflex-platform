package pl.com.tt.flex.onenet.service.providedata.scheduled;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;
import pl.com.tt.flex.onenet.service.providedata.ProvideDataService;

@Slf4j
@Component
@ConditionalOnProperty("application.onenet.update-provide-data.enabled")
public class ProvideDataUpdateTask {

	private final ProvideDataService provideDataService;

	public ProvideDataUpdateTask(final ProvideDataService provideDataService) {
		this.provideDataService = provideDataService;
	}

	/**
	 * Aktualizuje oferowane usługi użytkowników danymi z systemu onenet
	 */
	@Transactional
	@Scheduled(cron = "${application.onenet.update-provide-data.cron}")
	public void execute() {
		log.trace("ProvideDataUpdateTask - Starting provide data update process");
		provideDataService.fetchAndSaveProvideDataForAllOnenetUsers();
		log.trace("ProvideDataUpdateTask - Finished provide data update process");
	}

}
