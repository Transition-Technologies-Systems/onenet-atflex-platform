package pl.com.tt.flex.server.dataimport.impl;

import static pl.com.tt.flex.server.service.common.XlsxUtil.getBigDecimalValueOrNullFromCellWithUnit;
import static pl.com.tt.flex.server.service.common.XlsxUtil.getCellStringValue;
import static pl.com.tt.flex.server.util.DateUtil.getFromDate;
import static pl.com.tt.flex.server.util.DateUtil.getToDate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import lombok.extern.slf4j.Slf4j;
import pl.com.tt.flex.model.service.dto.file.FileDTO;
import pl.com.tt.flex.server.dataimport.DataImport;
import pl.com.tt.flex.server.dataimport.factory.DataImportFormat;
import pl.com.tt.flex.server.domain.settlement.SettlementEntity;
import pl.com.tt.flex.server.service.settlement.dto.SettlementViewDTO;

@Slf4j
@Component
public class SettlementImport implements DataImport<SettlementViewDTO> {

    public List<SettlementViewDTO> doImport(MultipartFile[] files) throws IOException {
        List<SettlementViewDTO> settlements = new ArrayList<>();
        for (MultipartFile file : files) {
            settlements.addAll(doImport(file, null));
        }
        return settlements;
    }

    @Override
    public List<SettlementViewDTO> doImport(MultipartFile file, Locale locale) throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook(file.getInputStream());
        Sheet sheet = workbook.getSheetAt(0);
        Iterator<Row> rowIterator = sheet.rowIterator();
        rowIterator.next(); //Pomiń wiersz z nazwami kolumn
        List<SettlementViewDTO> importedSettlements = new ArrayList<>();
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            SettlementViewDTO importedSettlement = SettlementViewDTO.builder()
                .id(Long.parseLong(getCellStringValue(row.getCell(0))))
                .derName(getCellStringValue(row.getCell(1)))
                .offerId(Long.parseLong(getCellStringValue(row.getCell(2))))
                .auctionName(getCellStringValue(row.getCell(3)))
                .companyName(getCellStringValue(row.getCell(4)))
                .acceptedDeliveryPeriodFrom(getFromDate(getCellStringValue(row.getCell(5))))
                .acceptedDeliveryPeriodTo(getToDate(getCellStringValue(row.getCell(5))))
                .acceptedVolume(getCellStringValue(row.getCell(6)).split(" ")[0])
                .activatedVolume(getBigDecimalValueOrNullFromCellWithUnit(row.getCell(7)))
                .settlementAmount(getBigDecimalValueOrNullFromCellWithUnit(row.getCell(8)))
                .build();
            importedSettlements.add(importedSettlement);
        }
        return importedSettlements;
    }

    @Override
    public List<SettlementViewDTO> doImport(FileDTO fileDTO, Locale locale) {
        throw new NotImplementedException("This method is currently not used");
    }

    @Override
    public boolean supportClass(Class clazz) {
        return SettlementEntity.class.equals(clazz);
    }

    @Override
    public boolean supportFormat(DataImportFormat format) {
        return format.equals(DataImportFormat.XLSX);
    }

}
