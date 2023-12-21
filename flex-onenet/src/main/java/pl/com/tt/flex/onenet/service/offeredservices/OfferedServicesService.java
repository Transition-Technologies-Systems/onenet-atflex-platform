package pl.com.tt.flex.onenet.service.offeredservices;

import java.util.Arrays;
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

import lombok.extern.slf4j.Slf4j;
import pl.com.tt.flex.model.service.dto.file.FileDTO;
import pl.com.tt.flex.onenet.domain.offeredservices.OfferedServiceEntity;
import pl.com.tt.flex.onenet.domain.onenetuser.OnenetUserEntity;
import pl.com.tt.flex.onenet.repository.offeredservices.OfferedServicesRepository;
import pl.com.tt.flex.onenet.service.common.AbstractOnenetJwtService;
import pl.com.tt.flex.onenet.service.connector.OnenetConnectorService;
import pl.com.tt.flex.onenet.service.offeredservices.dto.OfferedServiceDTO;
import pl.com.tt.flex.onenet.service.offeredservices.dto.OfferedServiceFullDTO;
import pl.com.tt.flex.onenet.service.offeredservices.dto.OfferedServiceMinDTO;
import pl.com.tt.flex.onenet.service.offeredservices.mapper.OfferedServicesMapper;
import pl.com.tt.flex.onenet.service.onenetuser.OnenetUserService;
import pl.com.tt.flex.onenet.util.FileUtil;

@Slf4j
@Service
public class OfferedServicesService extends AbstractOnenetJwtService {

	private final OfferedServicesRepository offeredServicesRepository;
	private final OfferedServicesMapper offeredServicesMapper;

	public OfferedServicesService(final OnenetConnectorService onenetConnectorService, final OnenetUserService onenetUserService,
								  final StrongTextEncryptor encoder, final OfferedServicesRepository offeredServicesRepository,
								  final OfferedServicesMapper offeredServicesMapper) {
		super(onenetConnectorService, encoder, onenetUserService);
		this.offeredServicesRepository = offeredServicesRepository;
		this.offeredServicesMapper = offeredServicesMapper;
	}

	public FileDTO getFileSchema(Long offeredServiceId) {
		return offeredServicesRepository.findById(offeredServiceId)
				.map(OfferedServiceEntity::getFileSchemaZip)
				.map(FileUtil::zipToFiles)
				.map(fileList -> fileList.get(0))
				.orElseThrow(() -> new IllegalStateException("Cannot find offered service with id: " + offeredServiceId));
	}

	public FileDTO getFileSchemaSample(Long offeredServiceId) {
		return offeredServicesRepository.findById(offeredServiceId)
				.map(OfferedServiceEntity::getFileSchemaSampleZip)
				.map(FileUtil::zipToFiles)
				.map(fileList -> fileList.get(0))
				.orElseThrow(() -> new IllegalStateException("Cannot find offered service with id: " + offeredServiceId));
	}

	@Transactional
	public void fetchAndSaveOfferedServicesForAllOnenetUsers() {
		onenetUserService.getAllOnenetUsers().forEach(this::updateOfferedServicesForUser);
	}

	@Transactional(readOnly = true)
	public List<OfferedServiceMinDTO> getAllMinDtoForActiveUser() {
		return onenetUserService.getCurrentActiveUser().getOfferedServices().stream()
			.map(offeredServicesMapper::toMinDto)
			.collect(Collectors.toList());
	}

	private void updateOfferedServicesForUser(OnenetUserEntity onenetUser) {
		Set<OfferedServiceEntity> usersOfferedServices = onenetUser.getOfferedServices();
		Map<String, OfferedServiceFullDTO> newOfferedServicesByOnenetId = onenetConnectorService.getOfferedServices(getToken(onenetUser)).stream()
				.collect(Collectors.toMap(OfferedServiceDTO::getOnenetId, Function.identity()));
		updateAlreadyAssignedServices(usersOfferedServices, newOfferedServicesByOnenetId);
		assignNewOfferedServices(usersOfferedServices, newOfferedServicesByOnenetId);
		removeUnusedOfferedServicesAssignment(usersOfferedServices, newOfferedServicesByOnenetId);
	}

	private void updateAlreadyAssignedServices(Set<OfferedServiceEntity> usersOfferedServices, Map<String, OfferedServiceFullDTO> newOfferedServicesByOnenetId) {
		usersOfferedServices.stream()
				.filter(offeredService -> newOfferedServicesByOnenetId.containsKey(offeredService.getOnenetId()))
				.forEach(offeredService -> applyChangesToDbEntity(offeredService, newOfferedServicesByOnenetId.get(offeredService.getOnenetId())));
	}

	private void assignNewOfferedServices(Set<OfferedServiceEntity> usersOfferedServices, Map<String, OfferedServiceFullDTO> newOfferedServicesByOnenetId) {
		for (OfferedServiceFullDTO newOfferedService : newOfferedServicesByOnenetId.values()) {
			if (!isServiceAssignedToUser(usersOfferedServices, newOfferedService)) {
				offeredServicesRepository.findByOnenetIdEquals(newOfferedService.getOnenetId())
						.ifPresentOrElse(usersOfferedServices::add, () -> {
							OfferedServiceEntity newOfferedServiceEntity = offeredServicesMapper.toEntity(newOfferedService);
							offeredServicesRepository.save(newOfferedServiceEntity);
							usersOfferedServices.add(newOfferedServiceEntity);
							log.info("Saved new offered service {} with id {}", newOfferedServiceEntity.getTitle(), newOfferedServiceEntity.getId());
						});
			}
		}
	}

	private void removeUnusedOfferedServicesAssignment(Set<OfferedServiceEntity> usersOfferedServices, Map<String, OfferedServiceFullDTO> newOfferedServicesByOnenetId) {
		Set<OfferedServiceEntity> servicesToRemove = new HashSet<>();
		usersOfferedServices.stream()
				.filter(offeredService -> !newOfferedServicesByOnenetId.containsKey(offeredService.getOnenetId()))
				.forEach(servicesToRemove::add);
		usersOfferedServices.removeAll(servicesToRemove);
	}

	private void applyChangesToDbEntity(OfferedServiceEntity dbOfferedService, OfferedServiceFullDTO newOfferedService) {
		if (!Objects.equals(dbOfferedService.getTitle(), newOfferedService.getTitle())) {
			dbOfferedService.setTitle(newOfferedService.getTitle());
			log.info("Updated title {} in offered service id {}", dbOfferedService.getTitle(), dbOfferedService.getId());
		}
		if (!Objects.equals(dbOfferedService.getBusinessObjectId(), newOfferedService.getBusinessObjectId())) {
			dbOfferedService.setBusinessObjectId(newOfferedService.getBusinessObjectId());
			log.info("Updated business object id {} in offered service id {}", dbOfferedService.getBusinessObjectId(), dbOfferedService.getId());
		}
		if (!Objects.equals(dbOfferedService.getBusinessObject(), newOfferedService.getBusinessObject())) {
			dbOfferedService.setBusinessObject(newOfferedService.getBusinessObject());
			log.info("Updated business object {} in offered service id {}", dbOfferedService.getBusinessObject(), dbOfferedService.getId());
		}
		if (!Objects.equals(dbOfferedService.getServiceCode(), newOfferedService.getServiceCode())) {
			dbOfferedService.setServiceCode(newOfferedService.getServiceCode());
			log.info("Updated service code {} in offered service id {}", dbOfferedService.getServiceCode(), dbOfferedService.getId());
		}
		if (!Arrays.equals(dbOfferedService.getFileSchemaZip(), newOfferedService.getFileSchemaZip())) {
			dbOfferedService.setFileSchemaZip(newOfferedService.getFileSchemaZip());
			log.info("Updated file schema in offered service id {}", dbOfferedService.getId());
		}
		if (!Arrays.equals(dbOfferedService.getFileSchemaSampleZip(), newOfferedService.getFileSchemaSampleZip())) {
			dbOfferedService.setFileSchemaSampleZip(newOfferedService.getFileSchemaSampleZip());
			log.info("Updated file schema sample in offered service id {}", dbOfferedService.getId());
		}
		if (!Objects.equals(dbOfferedService.getDescription(), newOfferedService.getDescription())) {
			dbOfferedService.setDescription(newOfferedService.getDescription());
			log.info("Updated description {} in offered service id {}", dbOfferedService.getDescription(), dbOfferedService.getId());
		}
	}

	private boolean isServiceAssignedToUser(Set<OfferedServiceEntity> usersOfferedServices, OfferedServiceFullDTO newOfferedService) {
		return usersOfferedServices.stream().anyMatch(usersOfferedService -> usersOfferedService.getOnenetId().equals(newOfferedService.getOnenetId()));
	}

}
