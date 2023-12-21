package pl.com.tt.flex.onenet.service.consumedata;

import lombok.extern.slf4j.Slf4j;
import org.jasypt.util.text.StrongTextEncryptor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.com.tt.flex.model.service.dto.file.FileDTO;
import pl.com.tt.flex.onenet.domain.consumedata.ConsumeDataEntity;
import pl.com.tt.flex.onenet.domain.consumedata.ConsumeDataViewEntity;
import pl.com.tt.flex.onenet.domain.onenetuser.OnenetUserEntity;
import pl.com.tt.flex.onenet.repository.consumedata.ConsumeDataRepository;
import pl.com.tt.flex.onenet.repository.consumedata.ConsumeDataViewRepository;
import pl.com.tt.flex.onenet.repository.onenetuser.OnenetUserRepository;
import pl.com.tt.flex.onenet.service.common.AbstractOnenetJwtService;
import pl.com.tt.flex.onenet.service.connector.OnenetConnectorService;
import pl.com.tt.flex.onenet.service.onenetuser.OnenetUserService;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ConsumeDataService extends AbstractOnenetJwtService {
	// ponieważ nie przechowujemy nazwy plików w bazie (ani z OneNetu nie przychodzi nazwa), to nazwa pobranego pliku może być dowolna
	private static final String DOWNLOADED_FILE_NAME = "downloaded_file.txt";
	private final ConsumeDataRepository consumeDataRepository;
	private final ConsumeDataViewRepository consumeDataViewRepository;
	private final OnenetUserRepository onenetUserRepository;

	public ConsumeDataService(final OnenetConnectorService onenetConnectorService, final OnenetUserService onenetUserService, final StrongTextEncryptor encoder,
			final ConsumeDataRepository consumeDataRepository, final ConsumeDataViewRepository consumeDataViewRepository, final OnenetUserRepository onenetUserRepository) {
		super(onenetConnectorService, encoder, onenetUserService);
		this.consumeDataRepository = consumeDataRepository;
		this.consumeDataViewRepository = consumeDataViewRepository;
		this.onenetUserRepository = onenetUserRepository;
	}

	@Transactional(readOnly = true)
	public List<Long> getConsumedDataIdsForActiveUser() {
		String activeOnenetUsername = onenetUserService.getCurrentActiveUser().getUsername();
		return consumeDataViewRepository.findConsumeDataByActiveOnenetUsername(activeOnenetUsername).stream()
				.map(ConsumeDataViewEntity::getId).collect(Collectors.toList());
	}

    @Transactional
	public FileDTO getFile(String consumeDataId) {
		OnenetUserEntity activeOnenetUser = onenetUserService.getCurrentActiveUser();
		String fileData = onenetConnectorService.getConsumeDataFile(getToken(activeOnenetUser), consumeDataId).getFiledata();
		return new FileDTO(DOWNLOADED_FILE_NAME, fileData);
	}

	@Transactional
	public void fetchAndSaveConsumeDataForAllUsers() {
		onenetUserRepository.findAll().forEach(this::updateConsumeDataForUser);
	}

	private void updateConsumeDataForUser(OnenetUserEntity onenetUser) {
		Set<ConsumeDataEntity> usersConsumeData = onenetUser.getConsumeData();
		// trzeci parametr Collectors.toMap odpowiada za sytuację gdy zdarzą się zdublowane elementy (gdy Object.equals() zwróci true),
		// w tej sytuacji zostawiamy pierwotny klucz
		Map<String, ConsumeDataEntity> newConsumeDataByOnenetId = onenetConnectorService.getConsumedData(getToken(onenetUser)).stream()
				.collect(Collectors.toMap(ConsumeDataEntity::getOnenetId, Function.identity(), (consumeDataEntity, consumeDataEntity2) -> consumeDataEntity));
		updateAlreadyExistingConsumeData(usersConsumeData, newConsumeDataByOnenetId);
		assignNewConsumeData(usersConsumeData, newConsumeDataByOnenetId);
		removeUnusedConsumeData(usersConsumeData, newConsumeDataByOnenetId);
	}

	private void updateAlreadyExistingConsumeData(Set<ConsumeDataEntity> consumeData, Map<String, ConsumeDataEntity> newConsumeDataByOnenetId) {
		consumeData.stream().filter(consumeDataEntity -> newConsumeDataByOnenetId.containsKey(consumeDataEntity.getOnenetId()))
				.forEach(consumeDataEntity -> updateConsumeData(consumeDataEntity, newConsumeDataByOnenetId.get(consumeDataEntity.getOnenetId())));
	}

	private void assignNewConsumeData(Set<ConsumeDataEntity> usersConsumeData, Map<String, ConsumeDataEntity> newConsumeDataByOnenetId) {
		for (ConsumeDataEntity consumeDataEntity : newConsumeDataByOnenetId.values()) {
			if (!isConsumeDataAssignedToUser(usersConsumeData, consumeDataEntity)) {
				consumeDataRepository.findByOnenetIdEquals(consumeDataEntity.getOnenetId()).ifPresentOrElse(usersConsumeData::add, () -> {
					consumeDataRepository.save(consumeDataEntity);
					usersConsumeData.add(consumeDataEntity);
					log.info("Saved new consume data {} with id {}", consumeDataEntity.getTitle(), consumeDataEntity.getId());
				});
			}
		}
	}

	private void removeUnusedConsumeData(Set<ConsumeDataEntity> usersConsumeData, Map<String, ConsumeDataEntity> newConsumeDataByOnenetId) {
		Set<ConsumeDataEntity> dataToBeRemoved = new HashSet<>();
		usersConsumeData.stream().filter(consumeData -> !newConsumeDataByOnenetId.containsKey(consumeData.getOnenetId()))
				.forEach(dataToBeRemoved::add);
		usersConsumeData.removeAll(dataToBeRemoved);
	}

	private void updateConsumeData(ConsumeDataEntity dbConsumeData, ConsumeDataEntity newConsumeData) {
		if (!Objects.equals(dbConsumeData.getTitle(), newConsumeData.getTitle())) {
			dbConsumeData.setTitle(newConsumeData.getTitle());
		}
		if (!Objects.equals(dbConsumeData.getOnenetId(), newConsumeData.getOnenetId())) {
			dbConsumeData.setOnenetId(newConsumeData.getOnenetId());
		}
		if (!Objects.equals(dbConsumeData.getDataSupplier(), newConsumeData.getDataSupplier())) {
			dbConsumeData.setDataSupplier(newConsumeData.getDataSupplier());
		}
		if (!Objects.equals(dbConsumeData.getDataSupplierCompanyName(), newConsumeData.getDataSupplierCompanyName())) {
			dbConsumeData.setDataSupplierCompanyName(newConsumeData.getDataSupplierCompanyName());
		}
		if (!Objects.equals(dbConsumeData.getBusinessObjectId(), newConsumeData.getBusinessObjectId())) {
			dbConsumeData.setBusinessObjectId(newConsumeData.getBusinessObjectId());
		}
		if (!Objects.equals(dbConsumeData.getDescription(), newConsumeData.getDescription())) {
			dbConsumeData.setDescription(newConsumeData.getDescription());
		}
	}

	private boolean isConsumeDataAssignedToUser(Set<ConsumeDataEntity> usersConsumeData, ConsumeDataEntity consumeData) {
		return usersConsumeData.stream().anyMatch(consumeDataEntity -> consumeDataEntity.getOnenetId().equals(consumeData.getOnenetId()));
	}
}
