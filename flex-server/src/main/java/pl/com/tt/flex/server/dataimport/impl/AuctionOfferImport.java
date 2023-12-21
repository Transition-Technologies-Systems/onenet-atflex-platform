package pl.com.tt.flex.server.dataimport.impl;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import pl.com.tt.flex.model.service.dto.auction.offer.AuctionOfferViewDTO;
import pl.com.tt.flex.model.service.dto.file.FileDTO;
import pl.com.tt.flex.server.common.errors.ObjectValidationException;
import pl.com.tt.flex.server.dataexport.exporter.offer.BidsEvaluationExporter;
import pl.com.tt.flex.server.dataexport.util.header.Header;
import pl.com.tt.flex.server.dataexport.util.header.LocaleTranslation;
import pl.com.tt.flex.server.dataimport.DataImport;
import pl.com.tt.flex.server.dataimport.factory.DataImportFormat;
import pl.com.tt.flex.server.service.importData.auctionOffer.dto.AuctionOfferImportData;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

import static pl.com.tt.flex.server.dataexport.util.header.HeaderUtils.getIndexByColumnCode;
import static pl.com.tt.flex.server.web.rest.errors.ErrorConstants.IMPORT_OFFERS_TEMPLATE_INCORRECT;

@Component
@Slf4j
public class AuctionOfferImport implements DataImport<AuctionOfferImportData> {
    /**
     * Column code uzywane do wygenerowania pliku do eksportu
     * Patrz: ${@link BidsEvaluationExporter} pole: defaultColumnList
     */
    private static final String BID_ID_COLUMN_CODE = "id";
    private static final String STATUS_COLUMN_CODE = "status";
    private static final String ACCEPTED_VOLUME_COLUMN_CODE = "acceptedVolume";
    private static final String ACCEPTED_DELIVERY_PERIOD_COLUMN_CODE = "acceptedDeliveryPeriod";

    private final BidsEvaluationExporter bidsEvaluationExporter;

    public AuctionOfferImport(BidsEvaluationExporter bidsEvaluationExporter) {
        this.bidsEvaluationExporter = bidsEvaluationExporter;
    }

    @Override
    public List<AuctionOfferImportData> doImport(MultipartFile multipartFile, Locale locale) throws IOException {
        // Pobranie listy dozwolonych nazw kolumn dla jezyka PL i EN
        List<Header> headerList = bidsEvaluationExporter.getHeaderList(Lists.newArrayList(Locale.forLanguageTag("pl"), Locale.forLanguageTag("en")), new AuctionOfferViewDTO());
        Map<Integer, List<XSSFCell>> stringListMap = readFile(multipartFile.getInputStream(), headerList);
        List<AuctionOfferImportData> importedBids = new ArrayList<>();
        for (Map.Entry<Integer, List<XSSFCell>> sheet : stringListMap.entrySet()) {
            importedBids.add(parseToDTO(sheet.getValue(), headerList));
        }
        log.info("doImport() Find {} BIDS to import in file {}.", importedBids.size(), multipartFile.getOriginalFilename());
        return importedBids;
    }

    @Override
    public List<AuctionOfferImportData> doImport(FileDTO fileDTO, Locale locale) {
        throw new NotImplementedException("This method is currently not used");
    }

    private Map<Integer, List<XSSFCell>> readFile(InputStream inputStream, List<Header> headerList) throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
        XSSFSheet sheet = workbook.getSheetAt(0);
        Map<Integer, List<XSSFCell>> sheetData = new HashMap<>();
        Iterator rowIterator = sheet.rowIterator();
        if (rowIterator.hasNext()) {
            XSSFRow headerRow = (XSSFRow) rowIterator.next(); // pobieramy pierwszy wiersz z nagłówkiem
            checkHeadersValid(headerRow, headerList);
            while (rowIterator.hasNext()) {
                XSSFRow row = (XSSFRow) rowIterator.next();
                ArrayList<XSSFCell> rows = getCellsFromRaw(row);
                if (!rows.isEmpty()) {
                    sheetData.put(row.getRowNum(), rows);
                }
            }
        }
        return sheetData;
    }

    /**
     * Metoda sprawdza dla każdej etykiety z wiersza nagłówkowego pliku,
     * czy istnieje tłumaczenie, które jest zgodne z treścią nagłówka.
     *
     * @param headerList Przekazana lista z dostepnymi dla danej kolumny tlumaczeniami
     * @param headerRow  Wiersz w ktorym znajduja sie nazwy kolumn
     */
    private void checkHeadersValid(XSSFRow headerRow, List<Header> headerList) {
        boolean isAnyTranslationValid = headerList.stream()
            .allMatch(header -> header.getColumnTranslations().stream()
                .map(LocaleTranslation::getTranslation)
                .collect(Collectors.toList())
                .contains(headerRow.getCell(header.getIndex()).getStringCellValue())
            );
        if (!isAnyTranslationValid) {
            throw new ObjectValidationException("Incorrect labels in header row", IMPORT_OFFERS_TEMPLATE_INCORRECT);
        }
    }

    private ArrayList<XSSFCell> getCellsFromRaw(XSSFRow row) {
        ArrayList<XSSFCell> rows = new ArrayList<>();
        Iterator cells = row.cellIterator();
        while (cells.hasNext()) {
            XSSFCell cell = (XSSFCell) cells.next();
            cell.setCellType(XSSFCell.CELL_TYPE_STRING);
            rows.add(cell);
        }
        return rows;
    }

    private AuctionOfferImportData parseToDTO(List<XSSFCell> data, List<Header> headerList) {
        log.info("parseToDTO() -- BEGIN");
        AuctionOfferImportData auctionOfferImportData = AuctionOfferImportData.builder()
            .id(data.get(getIndexByColumnCode(headerList, BID_ID_COLUMN_CODE)).toString())
            .status(data.get(getIndexByColumnCode(headerList, STATUS_COLUMN_CODE)).toString())
            .acceptedVolume(data.get(getIndexByColumnCode(headerList, ACCEPTED_VOLUME_COLUMN_CODE)).toString().replace(",", "."))
            .acceptedDeliveryPeriod(data.get(getIndexByColumnCode(headerList, ACCEPTED_DELIVERY_PERIOD_COLUMN_CODE)).toString())
            .build();
        log.info("parseToDTO() - Imported Bid data: \n {} \n", auctionOfferImportData);
        log.info("parseToDTO() -- END");
        return auctionOfferImportData;
    }

    @Override
    public boolean supportClass(Class clazz) {
        return clazz.equals(AuctionOfferImportData.class);
    }

    @Override
    public boolean supportFormat(DataImportFormat format) {
        return format.equals(DataImportFormat.XLSX);
    }
}
