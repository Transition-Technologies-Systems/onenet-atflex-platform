package pl.com.tt.flex.server.service.kpi.generator.active.participation;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import pl.com.tt.flex.model.service.dto.file.FileDTO;
import pl.com.tt.flex.model.service.dto.kpi.KpiDTO;
import pl.com.tt.flex.model.service.dto.kpi.KpiType;
import pl.com.tt.flex.server.service.kpi.generator.KpiGenerateException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;

import static pl.com.tt.flex.server.util.WorkbookUtils.verifyThatTwoWorkbookAreSame;

class ActiveParticipationGeneratorTest {

    private final ActiveParticipationDataFactory activeParticipationDataFactory;
    private final ActiveParticipationGenerator activeParticipationGenerator;

    ActiveParticipationGeneratorTest() {
        activeParticipationDataFactory = Mockito.mock(ActiveParticipationDataFactory.class);
        activeParticipationGenerator = new ActiveParticipationGenerator(activeParticipationDataFactory);
    }

    @Test
    void givenSimpleData_expectedGenerateFile() throws KpiGenerateException, IOException {
        //before
        ActiveParticipationData activeParticipationData = new ActiveParticipationData(BigDecimal.valueOf(43), BigDecimal.valueOf(120));
        Mockito.doReturn(activeParticipationData).when(activeParticipationDataFactory).create();

        //given
        KpiDTO kpiDTO = KpiDTO.builder()
            .type(KpiType.ACTIVE_PARTICIPATION)
            .id(0L)
            .build();

        //when
        FileDTO generate = activeParticipationGenerator.generate(kpiDTO);

        //then
        XSSFWorkbook expectedWorkbook = getExpectedWorkbook(new ClassPathResource("/templates/kpi/active/participation/ActiveParticipation_1.xlsx"));
        verifyThatTwoWorkbookAreSame(expectedWorkbook, new XSSFWorkbook(new ByteArrayInputStream(generate.getBytesData())));
    }

    @NotNull
    private static XSSFWorkbook getExpectedWorkbook(Resource resource) throws IOException {
        InputStream expectedFile = resource.getInputStream();
        return new XSSFWorkbook(new ByteArrayInputStream(expectedFile.readAllBytes()));
    }
}
