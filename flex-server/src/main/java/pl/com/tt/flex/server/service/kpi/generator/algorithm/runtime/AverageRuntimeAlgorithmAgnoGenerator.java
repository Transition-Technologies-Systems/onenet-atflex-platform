package pl.com.tt.flex.server.service.kpi.generator.algorithm.runtime;

import lombok.AllArgsConstructor;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import pl.com.tt.flex.model.service.dto.kpi.KpiDTO;
import pl.com.tt.flex.model.service.dto.kpi.KpiType;
import pl.com.tt.flex.server.dataexport.util.CellUtils;
import pl.com.tt.flex.server.service.kpi.generator.KpiGenerator;

import java.util.concurrent.atomic.AtomicInteger;

import static pl.com.tt.flex.server.util.DurationFormatUtil.withHourMinutesAndSeconds;
import static pl.com.tt.flex.server.util.InstantFormatUtil.DD_MM_YYYY_HH_MM_SS;
import static pl.com.tt.flex.server.util.InstantFormatUtil.format;

/**
 * KPI - Średni czas trwania obliczeń algorytmu AGNO/Average runtime of aggregated network offer algorithm
 */
@Component
@AllArgsConstructor
public class AverageRuntimeAlgorithmAgnoGenerator extends KpiGenerator {

	private static final int START_ROW_INDEX = 1;
	private static final int ID = 0;
	private static final int ALGORITHM_TYPE = 1;
	private static final int START_DATE = 2;
	private static final int DELTA = 3;

	private static final int AVG_ALGORITHM_TYPE = 6;
	private static final int AVG_AVERAGE_DURATION = 7;

	private final AverageRuntimeAlgorithmAgnoDataFactory averageRuntimeAlgorithmAgnoDataFactory;

	@Override
	protected void fillSheet(XSSFWorkbook workbook, KpiDTO kpiDTO) {
		AverageRuntimeAlgorithmAgnoData averageRuntimeAlgorithmAgnoData = averageRuntimeAlgorithmAgnoDataFactory.create();
		XSSFSheet sheet = workbook.getSheetAt(0);
		fillAverageAlgorithmDuration(averageRuntimeAlgorithmAgnoData, sheet);
		fillAlgorithmDuration(averageRuntimeAlgorithmAgnoData, sheet);
	}

	private void fillAlgorithmDuration(AverageRuntimeAlgorithmAgnoData averageRuntimeAlgorithmAgnoData, XSSFSheet sheet) {
		AtomicInteger rowIndex = new AtomicInteger(START_ROW_INDEX);
		averageRuntimeAlgorithmAgnoData.getAlgorithmRuntime().forEach(algorithmRuntime -> {
			XSSFRow row = CellUtils.getRow(sheet, rowIndex.getAndIncrement());
			row.createCell(ID).setCellValue(algorithmRuntime.getId());
			row.createCell(ALGORITHM_TYPE).setCellValue(algorithmRuntime.getAlgorithmDescription());
			row.createCell(START_DATE).setCellValue(format(algorithmRuntime.getStartDate(), DD_MM_YYYY_HH_MM_SS));
			row.createCell(DELTA).setCellValue(withHourMinutesAndSeconds(algorithmRuntime.getDelta()));
		});
	}

	private void fillAverageAlgorithmDuration(AverageRuntimeAlgorithmAgnoData averageRuntimeAlgorithmAgnoData, XSSFSheet sheet) {
		AtomicInteger rowIndex = new AtomicInteger(START_ROW_INDEX);
		averageRuntimeAlgorithmAgnoData.getAlgorithmTypeDuration().forEach((key, duration) -> {
			XSSFRow row = CellUtils.getRow(sheet, rowIndex.getAndIncrement());
			row.createCell(AVG_ALGORITHM_TYPE).setCellValue(key.getSecond());
			row.createCell(AVG_AVERAGE_DURATION).setCellValue(withHourMinutesAndSeconds(duration));
		});
		XSSFRow row = CellUtils.getRow(sheet, rowIndex.getAndIncrement());
		row.createCell(AVG_AVERAGE_DURATION).setCellValue(withHourMinutesAndSeconds(averageRuntimeAlgorithmAgnoData.getAverageDuration()));
	}

	@Override
	protected String getTemplate() {
		return "templates/xlsx/kpi/algorithm/runtime/AverageRuntimeAlgorithm.xlsx";
	}

	@Override
	protected String getFilename(KpiDTO kpiDTO) {
		return "Average_runtime_of_aggregated_network_offer_algorithm.xlsx";
	}

	@Override
	public boolean isSupported(KpiType kpiType) {
		return kpiType.equals(KpiType.AVERAGE_RUNTIME_AGNO_ALGORITHM);
	}
}
