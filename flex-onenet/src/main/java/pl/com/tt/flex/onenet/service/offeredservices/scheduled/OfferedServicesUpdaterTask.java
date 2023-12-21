package pl.com.tt.flex.onenet.service.offeredservices.scheduled;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;
import pl.com.tt.flex.onenet.service.offeredservices.OfferedServicesService;

@Slf4j
@Component
@ConditionalOnProperty("application.onenet.update-offered-services.enabled")
public class OfferedServicesUpdaterTask {

	private final OfferedServicesService offeredServicesService;

	public OfferedServicesUpdaterTask(final OfferedServicesService offeredServicesService) {
		this.offeredServicesService = offeredServicesService;
	}

	/**
	 * Aktualizuje oferowane usługi użytkowników danymi z systemu onenet
	 */
	@Transactional
	@Scheduled(cron = "${application.onenet.update-offered-services.cron}")
	public void execute() {
		log.trace("OfferedServicesUpdaterTask - Starting offered services update process");
		offeredServicesService.fetchAndSaveOfferedServicesForAllOnenetUsers();
		log.trace("OfferedServicesUpdaterTask - Finished offered services update process");
	}

}
