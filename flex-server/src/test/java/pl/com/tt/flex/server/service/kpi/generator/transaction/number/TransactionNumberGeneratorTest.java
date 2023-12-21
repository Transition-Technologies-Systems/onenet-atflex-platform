package pl.com.tt.flex.server.service.kpi.generator.transaction.number;

import org.apache.commons.lang3.tuple.Pair;
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
import java.time.Instant;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static pl.com.tt.flex.server.util.WorkbookUtils.verifyThatTwoWorkbookAreSame;

class TransactionNumberGeneratorTest {
    private final TransactionNumberDataFactory transactionNumberDataFactory;
    private final TransactionNumberGenerator transactionNumberGenerator;

    TransactionNumberGeneratorTest() {
        transactionNumberDataFactory = Mockito.mock(TransactionNumberDataFactory.class);
        transactionNumberGenerator = new TransactionNumberGenerator(transactionNumberDataFactory);
    }

    @Test
    void givenSimpleData_expectedGenerateFile() throws KpiGenerateException, IOException {
        //before
        TransactionNumberData transactionNumberData = getTransactionNumberData();
        Mockito.doReturn(transactionNumberData).when(transactionNumberDataFactory).create(Mockito.any(), Mockito.any());

        //given
        KpiDTO kpiDTO = KpiDTO.builder()
                              .type(KpiType.NUMBER_OF_TRANSACTIONS)
                              .dateFrom(Instant.parse("2022-10-06T22:00:00.00Z"))
                              .dateTo(Instant.parse("2022-10-05T22:00:00.00Z"))
                              .id(0L)
                              .build();

        //when
        FileDTO generate = transactionNumberGenerator.generate(kpiDTO);

        //then
        XSSFWorkbook expectedWorkbook = getExpectedWorkbook(new ClassPathResource("/templates/kpi/transaction/number/NumberOfTransactions_06_10_2022-07_10_2022.xlsx"));
        verifyThatTwoWorkbookAreSame(expectedWorkbook, new XSSFWorkbook(new ByteArrayInputStream(generate.getBytesData())));
    }

    @NotNull
    private TransactionNumberData getTransactionNumberData() {
        final Map<Pair<String, LocalDate>, Long> numberOfTransactionGroupingByProductNameAndDeliveryDate = new HashMap<>();
        numberOfTransactionGroupingByProductNameAndDeliveryDate.put(Pair.of("MocnyFull3", LocalDate.of(2022, 10, 6)), 1L);
        numberOfTransactionGroupingByProductNameAndDeliveryDate.put(Pair.of("Up", LocalDate.of(2022, 10, 6)), 1L);
        final Map<String, Long> numberOfTransactionGroupingByProductName = new HashMap<>();
        numberOfTransactionGroupingByProductName.put("MocnyFull3", 1L);
        numberOfTransactionGroupingByProductName.put("Up", 1L);
        return new TransactionNumberData(numberOfTransactionGroupingByProductNameAndDeliveryDate, numberOfTransactionGroupingByProductName);
    }

    @NotNull
    private static XSSFWorkbook getExpectedWorkbook(Resource resource) throws IOException {
        InputStream expectedFile = resource.getInputStream();
        return new XSSFWorkbook(new ByteArrayInputStream(expectedFile.readAllBytes()));
    }
}
