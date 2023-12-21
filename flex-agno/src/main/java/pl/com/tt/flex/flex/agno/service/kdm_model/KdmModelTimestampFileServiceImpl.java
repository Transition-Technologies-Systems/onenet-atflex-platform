package pl.com.tt.flex.flex.agno.service.kdm_model;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.com.tt.flex.flex.agno.common.errors.ObjectValidationException;
import pl.com.tt.flex.flex.agno.domain.kdm_model.KdmModelTimestampFileEntity;
import pl.com.tt.flex.flex.agno.repository.AbstractJpaRepository;
import pl.com.tt.flex.flex.agno.repository.kdm_model.KdmModelTimestampFileRepository;
import pl.com.tt.flex.flex.agno.service.common.AbstractServiceImpl;
import pl.com.tt.flex.flex.agno.service.kdm_model.mapper.KdmModelTimestampFileMapper;
import pl.com.tt.flex.flex.agno.service.mapper.EntityMapper;
import pl.com.tt.flex.flex.agno.util.ZipUtil;
import pl.com.tt.flex.flex.agno.validator.kdm_model.KdmModelTimestampFileValidator;
import pl.com.tt.flex.model.service.dto.file.FileDTO;
import pl.com.tt.flex.model.service.dto.kdm_model.KdmModelTimestampFileDTO;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
public class KdmModelTimestampFileServiceImpl extends AbstractServiceImpl<KdmModelTimestampFileEntity, KdmModelTimestampFileDTO, Long> implements KdmModelTimestampFileService {

	private final KdmModelTimestampFileRepository kdmModelTimestampFileRepository;
	private final KdmModelTimestampFileMapper kdmModelTimestampFileMapper;
	private final KdmModelTimestampFileValidator kdmModelTimestampFileValidator;

	public KdmModelTimestampFileServiceImpl(KdmModelTimestampFileRepository kdmModelTimestampFileRepository,
			KdmModelTimestampFileMapper kdmModelTimestampFileMapper,
			KdmModelTimestampFileValidator kdmModelTimestampFileValidator) {
		this.kdmModelTimestampFileRepository = kdmModelTimestampFileRepository;
		this.kdmModelTimestampFileMapper = kdmModelTimestampFileMapper;
		this.kdmModelTimestampFileValidator = kdmModelTimestampFileValidator;
	}

	@Override
	@Transactional(readOnly = true)
	public List<KdmModelTimestampFileDTO> findAllByKdmModelId(Long kdmModelId) {
		return kdmModelTimestampFileMapper.toDto(kdmModelTimestampFileRepository.findAllByKdmModelId(kdmModelId));
	}

	@Override
	@Transactional(readOnly = true)
	public FileDTO findKdmTimestampFileByTimestampAndKdmModelId(String timestamp, Long kdmModelId) {
		KdmModelTimestampFileEntity timestampKdm = kdmModelTimestampFileRepository.findByTimestampAndKdmModelId(timestamp, kdmModelId)
				.orElseThrow(() -> new IllegalStateException("Cannot find kdm file for timestamp=" + timestamp + " kdmModelId=" + kdmModelId));
		return ZipUtil.zipToFiles(timestampKdm.getFileZipData()).get(0);
	}

	@Override
	public AbstractJpaRepository<KdmModelTimestampFileEntity, Long> getRepository() {
		return this.kdmModelTimestampFileRepository;
	}

	@Override
	public EntityMapper<KdmModelTimestampFileDTO, KdmModelTimestampFileEntity> getMapper() {
		return this.kdmModelTimestampFileMapper;
	}

	@Override
	@Transactional
	public void updateAllTimestampsForKdmModel(String kdmModelId, List<KdmModelTimestampFileDTO> timestampFileDTOS)
			throws ObjectValidationException {
		saveUploadedTimestampFiles(kdmModelId, timestampFileDTOS);
		deleteOldTimestampFiles(kdmModelId, timestampFileDTOS);
	}

	/**
	 * Na listę do aktualizacji dodajemy obiekty, które zawierają DTO pliku kdm,
	 * aby następnie zapisać je w bazie danych (dodać jako nowe, lub zaktualizować istniejące).
	 *	Następnie sprawdzamy, czy w bazie danych istnieje obiekt KdmModelTimestampFile dla podanego timestampa i modelu Kdm.
	 * Jeżeli obiekt istnieje, przypisujemy jego Id nowemu obiektowi, aby podczas zapisu do bazy obecny plik kdm został nadpisany przez nowy.
	 */
	private void saveUploadedTimestampFiles(String kdmModelId, List<KdmModelTimestampFileDTO> timestampFileDTOS) throws ObjectValidationException {
		List<KdmModelTimestampFileDTO> timestampFilesToAddOrUpdate = timestampFileDTOS.stream()
				.filter(timestamp -> Objects.nonNull(timestamp.getFileDTO()))
				.collect(Collectors.toList());
		for (KdmModelTimestampFileDTO timestampFile : timestampFilesToAddOrUpdate) {
			Optional<KdmModelTimestampFileEntity> timestampEntity = kdmModelTimestampFileRepository.findByTimestampAndKdmModelId(timestampFile.getTimestamp(), Long.parseLong(kdmModelId));
			if(timestampEntity.isPresent() && Objects.isNull(timestampFile.getId())){
				timestampFile.setId(timestampEntity.get().getId());
			}
			kdmModelTimestampFileValidator.checkValid(timestampFile);
		}
		save(timestampFilesToAddOrUpdate);
	}

	/**
	 * Wybieramy obiekty, które nie zawierają id, ani DTO pliku kdm.
	 * Na listę dodajemy numery id pobranych z bazy plików, które odpowiadają
	 * numerowi timestampa zapisanego w obiekcie
	 * Następnie wszystkie pliki o id z listy zostaną usunięte z bazy danych
	 */
	private void deleteOldTimestampFiles(String kdmModelId, List<KdmModelTimestampFileDTO> timestampFileDTOS){
		List<Long> timestampFilesToDelete = timestampFileDTOS.stream()
				.filter(timestampFile -> Objects.isNull(timestampFile.getId()) && Objects.isNull(timestampFile.getFileDTO()))
				.filter(timestampFile -> findAllByKdmModelId(Long.parseLong(kdmModelId)).stream()
						.anyMatch(ts -> ts.getTimestamp().equals(timestampFile.getTimestamp())))
				.map(timestampFile -> findAllByKdmModelId(Long.parseLong(kdmModelId)).stream()
						.filter(stamp -> Objects.equals(stamp.getTimestamp(), timestampFile.getTimestamp()))
						.findFirst().get().getId())
				.collect(Collectors.toList());
		timestampFilesToDelete.forEach(this::delete);
	}
}
