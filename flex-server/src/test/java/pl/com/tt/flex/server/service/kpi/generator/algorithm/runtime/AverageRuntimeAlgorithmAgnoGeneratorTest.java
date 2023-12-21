package pl.com.tt.flex.server.service.kpi.generator.algorithm.runtime;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import pl.com.tt.flex.model.service.dto.algorithm.AlgorithmType;
import pl.com.tt.flex.model.service.dto.file.FileDTO;
import pl.com.tt.flex.model.service.dto.kpi.KpiDTO;
import pl.com.tt.flex.model.service.dto.kpi.KpiType;
import pl.com.tt.flex.server.service.kpi.generator.KpiGenerateException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static pl.com.tt.flex.server.util.WorkbookUtils.verifyThatTwoWorkbookAreSame;

class AverageRuntimeAlgorithmAgnoGeneratorTest {
	private final AverageRuntimeAlgorithmAgnoDataFactory averageRuntimeAlgorithmAgnoDataFactory;
	private final AverageRuntimeAlgorithmAgnoGenerator averageRuntimeAlgorithmAgnoGenerator;

	AverageRuntimeAlgorithmAgnoGeneratorTest() {
		averageRuntimeAlgorithmAgnoDataFactory = Mockito.mock(AverageRuntimeAlgorithmAgnoDataFactory.class);
		averageRuntimeAlgorithmAgnoGenerator = new AverageRuntimeAlgorithmAgnoGenerator(averageRuntimeAlgorithmAgnoDataFactory);
	}

	@Test
	void givenSimpleData_expectedGenerateFile() throws KpiGenerateException, IOException {
		//before
		AverageRuntimeAlgorithmAgnoData averageRuntimeAlgorithmAgnoData = getAverageRuntimeAlgorithmAgnoDataFactory();
		Mockito.doReturn(averageRuntimeAlgorithmAgnoData).when(averageRuntimeAlgorithmAgnoDataFactory).create();

		//given
		KpiDTO kpiDTO = KpiDTO.builder()
									 .type(KpiType.AVERAGE_RUNTIME_AGNO_ALGORITHM)
									 .id(0L)
									 .build();

		//when
		FileDTO generate = averageRuntimeAlgorithmAgnoGenerator.generate(kpiDTO);

		//then
		XSSFWorkbook expectedWorkbook = getExpectedWorkbook(new ClassPathResource("/templates/kpi/algorithm/runtime/AverageRuntimeAlgorithm.xlsx"));
		verifyThatTwoWorkbookAreSame(expectedWorkbook, new XSSFWorkbook(new ByteArrayInputStream(generate.getBytesData())));
	}

	@NotNull
	private AverageRuntimeAlgorithmAgnoData getAverageRuntimeAlgorithmAgnoDataFactory() {
		final List<AlgorithmRuntime> algorithmRuntimeList = new ArrayList<>();
		algorithmRuntimeList.add(new AlgorithmRuntime(
				611L, AlgorithmType.BM, "Pure AGNO",
				Instant.parse("2022-11-03T12:00:00Z"), Instant.parse("2022-11-03T12:01:38Z")
		));
		algorithmRuntimeList.add(new AlgorithmRuntime(
				612L, AlgorithmType.DISAGGREGATION, "AGNO DISAGGREGATION",
				Instant.parse("2022-11-03T12:00:00Z"), Instant.parse("2022-11-03T12:00:11Z")
		));
		algorithmRuntimeList.add(new AlgorithmRuntime(
				613L, AlgorithmType.DISAGGREGATION, "AGNO DISAGGREGATION",
				Instant.parse("2022-11-03T12:00:00Z"), Instant.parse("2022-11-03T12:00:11Z")
		));
		algorithmRuntimeList.add(new AlgorithmRuntime(
				614L, AlgorithmType.DISAGGREGATION, "AGNO DISAGGREGATION",
				Instant.parse("2022-11-03T12:00:00Z"), Instant.parse("2022-11-03T12:00:11Z")
		));
		algorithmRuntimeList.add(new AlgorithmRuntime(
				615L, AlgorithmType.BM, "Pure AGNO",
				Instant.parse("2022-11-03T12:00:00Z"), Instant.parse("2022-11-03T12:01:20Z")
		));
		algorithmRuntimeList.add(new AlgorithmRuntime(
				616L, AlgorithmType.BM, "Pure AGNO",
				Instant.parse("2022-11-03T13:00:00Z"), Instant.parse("2022-11-03T13:01:17Z")
		));
		algorithmRuntimeList.add(new AlgorithmRuntime(
				617L, AlgorithmType.BM, "Pure AGNO",
				Instant.parse("2022-11-03T13:00:00Z"), Instant.parse("2022-11-03T13:01:18Z")
		));
		algorithmRuntimeList.add(new AlgorithmRuntime(
				618L, AlgorithmType.BM, "Pure AGNO",
				Instant.parse("2022-11-03T13:00:00Z"), Instant.parse("2022-11-03T13:01:38Z")
		));
		algorithmRuntimeList.add(new AlgorithmRuntime(
				619L, AlgorithmType.BM, "Pure AGNO",
				Instant.parse("2022-11-03T14:00:00Z"), Instant.parse("2022-11-03T14:02:44Z")
		));
		algorithmRuntimeList.add(new AlgorithmRuntime(
				620L, AlgorithmType.BM, "Pure AGNO",
				Instant.parse("2022-11-03T14:00:00Z"), Instant.parse("2022-11-03T14:02:51Z")
		));
		algorithmRuntimeList.add(new AlgorithmRuntime(
				621L, AlgorithmType.PBCM, "AGNO for reserves",
				Instant.parse("2022-11-03T14:00:00Z"), Instant.parse("2022-11-03T14:01:04Z")
		));
		algorithmRuntimeList.add(new AlgorithmRuntime(
				622L, AlgorithmType.DANO, "AGNO for DGIA",
				Instant.parse("2022-11-03T14:00:00Z"), Instant.parse("2022-11-03T14:07:56Z")
		));
		algorithmRuntimeList.add(new AlgorithmRuntime(
				623L, AlgorithmType.DANO, "AGNO for DGIA",
				Instant.parse("2022-11-03T14:00:00Z"), Instant.parse("2022-11-03T15:04:01Z")
		));
		return new AverageRuntimeAlgorithmAgnoData(algorithmRuntimeList);
	}

	@NotNull
	private static XSSFWorkbook getExpectedWorkbook(Resource resource) throws IOException {
		InputStream expectedFile = resource.getInputStream();
		return new XSSFWorkbook(new ByteArrayInputStream(expectedFile.readAllBytes()));
	}
}
