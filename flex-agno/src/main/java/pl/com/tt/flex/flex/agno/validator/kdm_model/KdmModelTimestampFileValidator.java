package pl.com.tt.flex.flex.agno.validator.kdm_model;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import pl.com.tt.flex.flex.agno.common.errors.ObjectValidationException;
import pl.com.tt.flex.flex.agno.repository.kdm_model.KdmModelRepository;
import pl.com.tt.flex.flex.agno.repository.kdm_model.KdmModelTimestampFileRepository;
import pl.com.tt.flex.flex.agno.service.common.dto.FileDTOUtil;
import pl.com.tt.flex.flex.agno.util.TimestampFileUtil;
import pl.com.tt.flex.flex.agno.validator.ObjectValidator;
import pl.com.tt.flex.model.service.dto.file.FileDTO;
import pl.com.tt.flex.model.service.dto.file.FileExtension;
import pl.com.tt.flex.model.service.dto.kdm_model.KdmModelTimestampFileDTO;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static pl.com.tt.flex.flex.agno.web.resource.error.ErrorConstants.*;
import static pl.com.tt.flex.flex.agno.web.resource.kdm_model.KdmModelResource.ENTITY_NAME;

@Slf4j
@Component
public class KdmModelTimestampFileValidator implements ObjectValidator<KdmModelTimestampFileDTO, Long> {

	private final boolean isGenValidationEnabled;
	@Autowired
	private final KdmModelTimestampFileRepository kdmModelTimestampFileRepository;
	@Autowired
	private final KdmModelRepository kdmModelRepository;
	private final String UNKNOWN_LENGTH_WHITESPACE_FIELD = "\\s+";
	public static final List<String> sortedHourNumbers = Arrays.asList("1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "2a");

	public KdmModelTimestampFileValidator(@Value("${application.kdm-import.enable-gen-section-validation}") boolean isGenValidationEnabled, KdmModelTimestampFileRepository kdmModelTimestampFileRepository, KdmModelRepository kdmModelRepository) {
		this.kdmModelTimestampFileRepository = kdmModelTimestampFileRepository;
		this.isGenValidationEnabled = isGenValidationEnabled;
		this.kdmModelRepository = kdmModelRepository;
	}

	@Override
	public void checkValid(KdmModelTimestampFileDTO kdmModelTimestampFileDTO) throws ObjectValidationException {
		checkFileExtension(kdmModelTimestampFileDTO);
		checkTimestampNumber(kdmModelTimestampFileDTO);
		checkKdmModelExists(kdmModelTimestampFileDTO.getKdmModelId());
		checkFileContent(kdmModelTimestampFileDTO.getFileDTO());
	}

	private void checkKdmModelExists(Long kdmModelId) throws ObjectValidationException {
		if(!kdmModelRepository.existsById(kdmModelId)){
			throw new ObjectValidationException("Kdm model", KDM_MODEL_DOES_NOT_EXIST);
		}
	}

	/**
	 * Metoda sprawdza poprawność struktury wewnętrznej przesyłanego pliku KDM.
	 */
	private void checkFileContent(FileDTO fileDTO) throws ObjectValidationException {
		log.debug("checkFileContent() Start - validate correctness of structure in timestamp file {}", fileDTO.getFileName());
		checkSectionHeaders(fileDTO);
		checkNodesSection(fileDTO);
		checkBranchesSection(fileDTO);
		if (isGenValidationEnabled) {
			checkGenSection(fileDTO);
		}
		checkReceptionSection(fileDTO);
		log.debug("checkFileContent() End");
	}

	/**
	 * Metoda sprawdza czy w pliku znajdują się wymagane nagłówki.
	 */
	private void checkSectionHeaders(FileDTO fileDTO) throws ObjectValidationException {
		Pattern nodesHeader = Pattern.compile("W[EĘeę]Z[LŁlł]Y", Pattern.CASE_INSENSITIVE);
		//Pattern powinien zignorować nagłówki GALEZIE-ST i GALEZIE-TT
		Pattern branchesHeader = Pattern.compile("GA[LŁlł][EĘeę]ZIE$", Pattern.CASE_INSENSITIVE);
		if (checkMissingSectionHeader(nodesHeader, fileDTO)) {
			throw new ObjectValidationException("WEZLY label not found", NODES_LABEL_NOT_FOUND);
		}
		if (checkMissingSectionHeader(branchesHeader, fileDTO)) {
			throw new ObjectValidationException("GALEZIE label not found", BRANCHES_LABEL_NOT_FOUND);
		}
		checkGenHeader(fileDTO);
		checkReceptionHeader(fileDTO);
	}

	/**
	 * Metoda sprawdza poprawność nagłówka sekcji *Gen. Nagłówek wykrywany jest na podstawie zawartych w nim unikalnych etykiet,
	 * a następnie weryfikowane jest, czy zawiera on wszystkie wymagane etykiety.
	 */
	private void checkGenHeader(FileDTO fileDTO) throws ObjectValidationException {
		List<Pattern> genUniqueLabels = Stream.of("\\*Gen\\.", "Pg", "Qg", "Pw", "Qw", "Rgt", "Xtb", "X\"g", "X'g", "Xg")
				.map(pattern -> Pattern.compile(pattern, Pattern.CASE_INSENSITIVE)).collect(Collectors.toList());
		List<Pattern> genRequiredLabels = Stream.of("\\*Gen\\.", "W[eęEĘ]ze[lłLŁ]", "Sn", "Typ", "Pg", "Pmin", "Pmax", "Qg", "Qmin", "QMax", "Pw")
				.map(pattern -> Pattern.compile(pattern, Pattern.CASE_INSENSITIVE)).collect(Collectors.toList());
		try (BufferedReader br = TimestampFileUtil.getFileBufferedReader(fileDTO)) {
			String line;
			while ((line = br.readLine()) != null) {
				List<Pattern> missingLabels = getMissingLabels(genRequiredLabels, line);
				if (isHeaderRow(genUniqueLabels, missingLabels, line)  && !missingLabels.isEmpty()) {
					String missingLabel = missingLabels.stream().findFirst().get().toString();
					if (Objects.equals(missingLabel, "\\*Gen\\.")) {
						missingLabel = "*Gen.";
					} else if (Objects.equals(missingLabel, "W[eęEĘ]ze[lłLŁ]")) {
						missingLabel = "Węzeł";
					}
					throw new ObjectValidationException("Missing required label", GEN_MISSING_LABEL, missingLabel);
				}
				if (isHeaderRow(genUniqueLabels, missingLabels, line)  && missingLabels.isEmpty()) {
					return;
				}
			}
		} catch (IOException e) {
			throw new ObjectValidationException(e.getMessage(), FILE_PARSE_ERROR);
		}
		throw new ObjectValidationException("*Gen. section labels not found", GEN_LABELS_NOT_FOUND);
	}

	/**
	 * Metoda sprawdza poprawność nagłówka sekcji *Odbiór. Nagłówek wykrywany jest na podstawie zawartych w nim unikalnych etykiet,
	 * a następnie weryfikowane jest, czy zawiera on wszystkie wymagane etykiety.
	 */
	private void checkReceptionHeader(FileDTO fileDTO) throws ObjectValidationException {
		List<Pattern> receptionUniqueLabels = Stream.of("\\*Odbi[oóOÓ]r", "Pl", "CP", "Ql", "CQ", "Rt")
				.map(pattern -> Pattern.compile(pattern, Pattern.CASE_INSENSITIVE)).collect(Collectors.toList());
		List<Pattern> receptionRequiredLabels = Stream.of("\\*Odbi[oóOÓ]r", "W[eęEĘ]ze[lłLŁ]", "Typ", "Pl", "Pmin", "Pmax", "CP", "Ql")
				.map(pattern -> Pattern.compile(pattern, Pattern.CASE_INSENSITIVE)).collect(Collectors.toList());
		try (BufferedReader br = TimestampFileUtil.getFileBufferedReader(fileDTO)) {
			String line;
			while ((line = br.readLine()) != null) {
				List<Pattern> missingLabels = getMissingLabels(receptionRequiredLabels, line);
				if (isHeaderRow(receptionUniqueLabels, missingLabels, line)  && !missingLabels.isEmpty()) {
					String missingLabel = missingLabels.stream().findFirst().get().toString();
					if (Objects.equals(missingLabel, "\\*Odbi[oóOÓ]r")) {
						missingLabel = "*Odbiór";
					} else if (Objects.equals(missingLabel, "W[eęEĘ]ze[lłLŁ]")) {
						missingLabel = "Węzeł";
					}
					throw new ObjectValidationException("Missing required label", RECEPTION_MISSING_LABEL, missingLabel);
				}
				if (isHeaderRow(receptionUniqueLabels, missingLabels, line)  && missingLabels.isEmpty()) {
					return;
				}
			}
		} catch (IOException e) {
			throw new ObjectValidationException(e.getMessage(), FILE_PARSE_ERROR);
		}
		throw new ObjectValidationException("*Odbiór section labels not found", RECEPTION_LABELS_NOT_FOUND);
	}

	/**
	 * Metoda zwraca listę brakujących w wierszu etykiet wymaganych dla danego nagłówka
	 */
	private static List<Pattern> getMissingLabels(List<Pattern> headerRequiredLabels, String line) {
		return headerRequiredLabels.stream().filter(label -> !label.matcher(line).find()).collect(Collectors.toList());
	}

	/**
	 * Metoda sprawdza czy linia zawiera jakąkolwiek etykietę unikalną dla danego nagłówka.
	 */
	private static boolean isHeaderRow(List<Pattern> headerUniqueLabels, List<Pattern> missingLabels, String line) {
		return headerUniqueLabels.stream().anyMatch(label -> label.matcher(line).find());
	}

	/**
	 * Metoda sprawdza czy przekazany w parametrze nagłówek znajduje się w pliku.
	 */
	private boolean checkMissingSectionHeader(Pattern header, FileDTO fileDTO) throws ObjectValidationException {
		try (BufferedReader br = TimestampFileUtil.getFileBufferedReader(fileDTO)) {
			String line;
			while ((line = br.readLine()) != null) {
				if (header.matcher(line.trim()).find()) {
					return false;
				}
			}
		} catch (IOException e) {
			throw new ObjectValidationException(e.getMessage(), FILE_PARSE_ERROR);
		}
		return true;
	}

	/**
	 * Metoda sprawdza poprawność struktury sekcji "WEZLY".
	 */
	private void checkNodesSection(FileDTO fileDTO) throws ObjectValidationException {
		checkCommasAndDotsForNodes(fileDTO);
		Pattern startLine = Pattern.compile("W[EĘeę]Z[LŁlł]Y", Pattern.CASE_INSENSITIVE);
		Pattern endLine = Pattern.compile("GA[LŁlł][EĘeę]ZIE", Pattern.CASE_INSENSITIVE);
		if (checkSectionHasNoContent(startLine, endLine, fileDTO)) {
			throw new ObjectValidationException("WEZLY section has no content", NODES_CONTENT_NOT_FOUND);
		}
	}

	/**
	 * metoda sprawdza czy dane w sekcji WEZLY zawierają przecinki i kropki.
	 */
	private void checkCommasAndDotsForNodes(FileDTO fileDTO) throws ObjectValidationException {
		Pattern startLine = Pattern.compile("W[EĘeę]Z[LŁlł]Y", Pattern.CASE_INSENSITIVE);
		Pattern endLine = Pattern.compile("GA[LŁlł][EĘeę]ZIE", Pattern.CASE_INSENSITIVE);
		try (BufferedReader br = TimestampFileUtil.getFileBufferedReader(fileDTO)) {
			String line;
			while ((line = br.readLine()) != null) {
				if (startLine.matcher(line).find()) {
					while ((line = br.readLine()) != null && !endLine.matcher(line).find()) {
						if (line.indexOf(',') == -1) {
							throw new ObjectValidationException("No commas in line", MISSING_COMMAS);
						}
						if (line.indexOf('.') == -1) {
							throw new ObjectValidationException("No dots in line", MISSING_DOTS);
						}
					}
					break;
				}
			}
		} catch (IOException e) {
			throw new ObjectValidationException(e.getMessage(), FILE_PARSE_ERROR);
		}
	}

	/**
	 * Metoda sprawdza poprawność struktury sekcji "GALEZIE".
	 */
	private void checkBranchesSection(FileDTO fileDTO) throws ObjectValidationException {
		Pattern startLine = Pattern.compile("GA[LŁlł][EĘeę]ZIE", Pattern.CASE_INSENSITIVE);
		Pattern endLine = Pattern.compile("GA[LŁlł][EĘeę]ZIE-ST", Pattern.CASE_INSENSITIVE);
		if (checkSectionHasNoContent(startLine, endLine, fileDTO)) {
			throw new ObjectValidationException("GALEZIE section has no content", BRANCHES_CONTENT_NOT_FOUND);
		}
	}

	/**
	 * Metoda sprawdza poprawność struktury sekcji "*Gen.".
	 */
	private void checkGenSection(FileDTO fileDTO) throws ObjectValidationException {
		int lineLength = 23;
		Pattern startLine = Pattern.compile("\\*Gen\\.", Pattern.CASE_INSENSITIVE);
		Pattern endLine = Pattern.compile("\\*Odbi[oóOÓ]r", Pattern.CASE_INSENSITIVE);
		if (checkSectionHasNoContent(startLine, endLine, fileDTO)) {
			throw new ObjectValidationException("*Gen. section has no content", GEN_CONTENT_NOT_FOUND);
		}
		if (checkMissingColumns(startLine, endLine, lineLength, fileDTO)) {
			throw new ObjectValidationException("Invalid columns count in *Gen. section", GEN_MISSING_COLUMNS);
		}
	}

	/**
	 * Metoda sprawdza poprawność struktury sekcji "*Odbiór".
	 */
	private void checkReceptionSection(FileDTO fileDTO) throws ObjectValidationException {
		int lineLength = 19;
		Pattern startLine = Pattern.compile("\\*Odbi[oóOÓ]r", Pattern.CASE_INSENSITIVE);
		Pattern endLine = Pattern.compile("\\*Lin_In", Pattern.CASE_INSENSITIVE);
		if (checkSectionHasNoContent(startLine, endLine, fileDTO)) {
			throw new ObjectValidationException("*Odbiór section has no content", RECEPTION_CONTENT_NOT_FOUND);
		}
		if (checkMissingColumns(startLine, endLine, lineLength, fileDTO)) {
			throw new ObjectValidationException("Invalid columns count in *Odbiór section", RECEPTION_MISSING_COLUMNS);
		}
	}

	/**
	 * Metoda sprawdza czy w zawartości danej sekcji nie brakuje żadnych kolumn.
	 */
	private boolean checkMissingColumns(Pattern startLine, Pattern endLine, int lineLength, FileDTO fileDTO) throws ObjectValidationException {
		try (BufferedReader br = TimestampFileUtil.getFileBufferedReader(fileDTO)) {
			String line;
			while ((line = br.readLine()) != null) {
				if (startLine.matcher(line).find()) {
					while ((line = br.readLine()) != null && !endLine.matcher(line).find()) {
						if (line.trim().split(UNKNOWN_LENGTH_WHITESPACE_FIELD).length != lineLength) {
							return true;
						}
					}
				}
			}
		} catch (IOException e) {
			throw new ObjectValidationException(e.getMessage(), FILE_PARSE_ERROR);
		}
		return false;
	}

	/**
	 * Metoda sprawdza czy liczba linii z danymi wewnątrz podanej sekcji jest równa 0.
	 */
	private boolean checkSectionHasNoContent(Pattern startLine, Pattern endLine, FileDTO fileDTO) throws ObjectValidationException {
		int sectionSize = 0;
		try (BufferedReader br = TimestampFileUtil.getFileBufferedReader(fileDTO)) {
			String line;
			while ((line = br.readLine()) != null) {
				if (startLine.matcher(line).find()) {
					while ((line = br.readLine()) != null && !endLine.matcher(line).find()) {
						sectionSize++;
					}
					return sectionSize == 0;
				}
			}
		} catch (IOException e) {
			throw new ObjectValidationException(e.getMessage(), FILE_PARSE_ERROR);
		}
		return true;
	}

	private void checkTimestampNumber(KdmModelTimestampFileDTO kdmModelTimestampFileDTO) throws ObjectValidationException {
		if (!sortedHourNumbers.contains(kdmModelTimestampFileDTO.getTimestamp())) {
			throw new ObjectValidationException("Wrong timestamp number",
					WRONG_KDM_MODEL_TIMESTAMP_NUMBER, ENTITY_NAME,
					kdmModelTimestampFileDTO.getId());
		}
	}

	private void checkFileExtension(KdmModelTimestampFileDTO kdmModelTimestampFileDTO) throws ObjectValidationException {
		FileExtension fileExtension = FileDTOUtil.getFileExtension(kdmModelTimestampFileDTO.getFileDTO().getFileName());
		if (!FileExtension.KDM.equals(fileExtension)) {
			throw new ObjectValidationException("Cannot add file because wrong extension",
					CANNOT_ADD_KDM_MODEL_TIMESTAMP_FILE_BECAUSE_WRONG_EXTENSION, ENTITY_NAME,
					kdmModelTimestampFileDTO.getId());
		}
	}

	@Override
	public void checkModifiable(KdmModelTimestampFileDTO kdmModelTimestampFileDTO) throws ObjectValidationException {
		checkValid(kdmModelTimestampFileDTO);
	}

	@Override
	public void checkDeletable(Long aLong) throws ObjectValidationException {

	}
}
