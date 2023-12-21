package pl.com.tt.flex.onenet.service.providedata;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.jasypt.util.text.StrongTextEncryptor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import io.jsonwebtoken.io.Encoders;
import lombok.extern.slf4j.Slf4j;
import pl.com.tt.flex.model.service.dto.file.FileDTO;
import pl.com.tt.flex.onenet.domain.consumedata.ConsumeDataEntity;
import pl.com.tt.flex.onenet.domain.offeredservices.OfferedServiceEntity;
import pl.com.tt.flex.onenet.domain.onenetuser.OnenetUserEntity;
import pl.com.tt.flex.onenet.repository.consumedata.ConsumeDataRepository;
import pl.com.tt.flex.onenet.repository.offeredservices.OfferedServicesRepository;
import pl.com.tt.flex.onenet.service.common.AbstractOnenetJwtService;
import pl.com.tt.flex.onenet.service.connector.OnenetConnectorService;
import pl.com.tt.flex.onenet.service.consumedata.mapper.ConsumeDataMapper;
import pl.com.tt.flex.onenet.service.onenetuser.OnenetUserService;
import pl.com.tt.flex.onenet.service.providedata.dto.ProvideDataResponseDTO;
import pl.com.tt.flex.onenet.util.FileUtil;

@Slf4j
@Service
public class ProvideDataService extends AbstractOnenetJwtService {

	private final ConsumeDataRepository consumeDataRepository;
	private final ConsumeDataMapper consumeDataMapper;
	private final OfferedServicesRepository offeredServicesRepository;

	public ProvideDataService(final OnenetConnectorService onenetConnectorService, final StrongTextEncryptor encoder,
                              final OnenetUserService onenetUserService, final ConsumeDataRepository consumeDataRepository,
                              final ConsumeDataMapper consumeDataMapper, final OfferedServicesRepository offeredServicesRepository) {
		super(onenetConnectorService, encoder, onenetUserService);
		this.consumeDataRepository = consumeDataRepository;
		this.consumeDataMapper = consumeDataMapper;
        this.offeredServicesRepository = offeredServicesRepository;
    }

	@Transactional
	public void provideData(MultipartFile multipartFile, String title, String description, String filename,
							String dataOfferingId, String code) throws IOException {
		String encodedFile = Encoders.BASE64.encode(multipartFile.getBytes());
		String onenetId = onenetConnectorService.postProvideData(getToken(), encodedFile, title, description, filename, dataOfferingId, code);
		String businessObjectId = offeredServicesRepository.findByOnenetIdEquals(dataOfferingId).map(OfferedServiceEntity::getBusinessObjectId).orElse(null);
		ConsumeDataEntity entity = consumeDataMapper.toEntity(multipartFile, title, description, filename, onenetId,
                onenetUserService.getCurrentActiveUser().getUsername(), businessObjectId);
		consumeDataRepository.save(entity);
	}

	@Transactional
	public void fetchAndSaveProvideDataForAllOnenetUsers() {
		onenetUserService.getAllOnenetUsers().forEach(this::updateProvideDataForUser);
	}

	@Transactional(readOnly = true)
	public FileDTO getFile(Long id) {
		return consumeDataRepository.findById(id)
				.map(ConsumeDataEntity::getFileZip)
				.map(FileUtil::zipToFiles)
				.map(fileList -> fileList.get(0))
				.orElseThrow(() -> new IllegalStateException("Cannot find provide data with id: " + id));
	}

	private void updateProvideDataForUser(OnenetUserEntity onenetUserEntity) {
		Map<String, ProvideDataResponseDTO> fetchedProvideDataByOnenetId = onenetConnectorService.getProvideData(getToken(onenetUserEntity)).stream()
				.collect(Collectors.toMap(ProvideDataResponseDTO::getOnenetId, Function.identity()));
		Map<String, ProvideDataResponseDTO> providedDataToUpdate = new HashMap<>();
		Set<ProvideDataResponseDTO> providedDataToSave = new HashSet<>();
		List<ConsumeDataEntity> databaseEntities = consumeDataRepository.findAllByOnenetIdIn(fetchedProvideDataByOnenetId.keySet());
		sortFetchedData(fetchedProvideDataByOnenetId.values(), databaseEntities, providedDataToUpdate, providedDataToSave);
		updateExistingProvideData(databaseEntities, providedDataToUpdate);
		addNew(providedDataToSave, onenetUserEntity.getUsername());
	}

	private void sortFetchedData(Collection<ProvideDataResponseDTO> fetchedProvideData, List<ConsumeDataEntity> databaseEntities,
								 Map<String, ProvideDataResponseDTO> providedDataToUpdate, Set<ProvideDataResponseDTO> providedDataToSave) {
		Set<String> onenetIdsPresentInDb = databaseEntities.stream()
				.map(ConsumeDataEntity::getOnenetId)
				.collect(Collectors.toSet());
		fetchedProvideData.forEach(dto -> {
			if (onenetIdsPresentInDb.contains(dto.getOnenetId())) {
				providedDataToUpdate.put(dto.getOnenetId(), dto);
			} else {
				providedDataToSave.add(dto);
			}
		});
	}

	private void updateExistingProvideData(List<ConsumeDataEntity> databaseEntities, Map<String, ProvideDataResponseDTO> providedDataToUpdate) {
		for (ConsumeDataEntity dbEntity : databaseEntities) {
			ProvideDataResponseDTO updatedData = providedDataToUpdate.get(dbEntity.getOnenetId());
			if (!Objects.equals(dbEntity.getTitle(), updatedData.getTitle())) {
				dbEntity.setTitle(updatedData.getTitle());
				log.info("Updated title {} in provide data id {}", dbEntity.getTitle(), dbEntity.getId());
			}
			if (!Objects.equals(dbEntity.getBusinessObjectId(), updatedData.getBusinessObjectId())) {
				dbEntity.setBusinessObjectId(updatedData.getBusinessObjectId());
				log.info("Updated business object id {} in provide data id {}", dbEntity.getBusinessObjectId(), dbEntity.getId());
			}
			if (!Objects.equals(dbEntity.getDescription(), updatedData.getDescription())) {
				dbEntity.setDescription(updatedData.getDescription());
				log.info("Updated description {} in provide data id {}", dbEntity.getDescription(), dbEntity.getId());
			}
		}
	}

	private void addNew(Set<ProvideDataResponseDTO> providedDataToSave, String username) {
		Set<ConsumeDataEntity> entitiesToSave = new HashSet<>();
		for (ProvideDataResponseDTO provideDataDTO : providedDataToSave) {
			ConsumeDataEntity newDataEntity = new ConsumeDataEntity();
			newDataEntity.setDataSupplier(username);
			newDataEntity.setOnenetId(provideDataDTO.getOnenetId());
			newDataEntity.setDescription(provideDataDTO.getDescription());
			newDataEntity.setBusinessObjectId(provideDataDTO.getBusinessObjectId());
			entitiesToSave.add(newDataEntity);
		}
		if (entitiesToSave.size() > 0) {
			consumeDataRepository.saveAll(entitiesToSave);
			log.info("Saved {} provide data entities", entitiesToSave.size());
		}
	}
}
