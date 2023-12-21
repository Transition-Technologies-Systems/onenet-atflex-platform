package pl.com.tt.flex.server.dataexport.exporter;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.context.MessageSource;
import pl.com.tt.flex.model.security.permission.ViewWithAuthority;
import pl.com.tt.flex.model.service.dto.file.FileDTO;
import pl.com.tt.flex.server.dataexport.exception.ScreenColumnConfigurationException;
import pl.com.tt.flex.server.dataexport.exporter.offer.detail.enumeration.LevelOfDetail;
import pl.com.tt.flex.server.dataexport.util.CellValueHelper;
import pl.com.tt.flex.server.dataexport.util.header.Header;
import pl.com.tt.flex.server.dataexport.util.header.LocaleTranslation;
import pl.com.tt.flex.server.domain.screen.enumeration.Screen;
import pl.com.tt.flex.server.security.SecurityUtils;
import pl.com.tt.flex.server.service.user.config.screen.UserScreenConfigService;
import pl.com.tt.flex.server.service.user.config.screen.dto.ScreenColumnDTO;
import pl.com.tt.flex.server.service.user.config.screen.dto.UserScreenConfigDTO;
import pl.com.tt.flex.server.util.InstantUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
public abstract class AbstractDataExporter<DTO> implements DataExporter<DTO> {

    private boolean isOnlyVisibleColumn;
    protected Locale locale;
    private CellStyle headerCellStyle;

    private static final int INDEX_OF_SHEET = 0;
    private static final int INITIAL_ROW_INDEX = 0;
    private static final int INITIAL_COL_INDEX = 0;

    private final UserScreenConfigService userScreenConfigService;
    protected final MessageSource messageSource;
    private final CellValueHelper cellValueHelper;

    protected AbstractDataExporter(UserScreenConfigService userScreenConfigService, MessageSource messageSource,
                                   CellValueHelper cellValueHelper) {
        this.userScreenConfigService = userScreenConfigService;
        this.messageSource = messageSource;
        this.cellValueHelper = cellValueHelper;
    }

    @Override
    public FileDTO export(List<DTO> valueToSave, Locale locale, Screen screen, boolean isOnlyVisibleColumn, LevelOfDetail detail) throws IOException {
        this.isOnlyVisibleColumn = isOnlyVisibleColumn;
        this.locale = locale;
        return new FileDTO(getFilename(), Base64.getEncoder().encodeToString(getData(valueToSave, screen, detail)));
    }

    /**
     * Metoda zwraca liste kolumn ktore sa dodawane w eksportowanym pliku.
     * Zwracany jest obiekt Header ktory zawiera informacje o:
     * <p>
     * - columnCode: kod kolumny (nazwa pola z DTO)
     * </p>
     * <p>
     * - columnTranslations: lista dostepnych tlumaczen dla danej kolumny (zalezna od wybranych jezykow)
     * </p>
     * <p>
     * - index: numer kolumny w pliku eksportowanym
     * </p>
     *
     * @param locales Lista jezykow z ktore maja byc brane pod uwage do sporzadzenia listy dostepnych nazw kolumn
     * @param object  instanacja obiektu (dowolny obiekt DTO, np new DTO())
     */
    @Override
    public List<Header> getHeaderList(List<Locale> locales, DTO object) {
        List<Header> headerList = Lists.newArrayList();
        AtomicInteger colIndex = new AtomicInteger(INITIAL_COL_INDEX);
        for (ScreenColumnDTO screenColumn : getColumnList()) {
            if (isVisible(screenColumn, object) && screenColumn.isExport()) {
                Header header = Header.builder()
                    .index(colIndex.getAndIncrement())
                    .columnCode(screenColumn.getColumnName())
                    .columnTranslations(locales.stream()
                        .map(l -> new LocaleTranslation(l, messageSource.getMessage(getPrefix() + screenColumn.getColumnName(), null, l)))
                        .collect(Collectors.toList())
                    )
                    .build();
                headerList.add(header);
            }
        }
        return headerList;
    }

    protected byte[] getData(List<DTO> valueToSave, Screen screen, LevelOfDetail detail) throws IOException {
        List<ScreenColumnDTO> screenColumns = getScreenColumnConfig(screen);
        Workbook workbook = new XSSFWorkbook();
        workbook.createSheet();
        setHeaderCellStyle(workbook);
        try {
            fillData(workbook, valueToSave, screenColumns);
        } catch (ReflectiveOperationException e) {
            log.error("getData() The column definition is not valid, exception: {}", e.getMessage());
            e.printStackTrace();
            throw new ScreenColumnConfigurationException("The column definition for screen " + screen + " is not valid!");
        }
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);

        return outputStream.toByteArray();
    }

    protected List<ScreenColumnDTO> getScreenColumnConfig(Screen screen) {
        Optional<UserScreenConfigDTO> screenConfig = userScreenConfigService.getForCurrentUserByScreen(screen);
        if (screenConfig.isPresent() && isOnlyVisibleColumn) {
            Map<String, List<ScreenColumnDTO>> usersCustomColumnsByName = screenConfig.get().getScreenColumns().stream().collect(Collectors.groupingBy(ScreenColumnDTO::getColumnName));
            return getColumnList().stream().map(defaultColumn -> usersCustomColumnsByName.containsKey(defaultColumn.getColumnName()) ?
                usersCustomColumnsByName.get(defaultColumn.getColumnName()).get(0) : defaultColumn).collect(Collectors.toList());
        } else {
            return getColumnList();
        }
    }

    protected void fillData(Workbook workbook, List<DTO> valueToSave, List<ScreenColumnDTO> screenColumns) throws ReflectiveOperationException {
        Sheet sheet = workbook.getSheetAt(INDEX_OF_SHEET);
        AtomicInteger rowIndex = new AtomicInteger(INITIAL_ROW_INDEX);
        fillHeader(sheet.createRow(rowIndex.getAndIncrement()), screenColumns, valueToSave.get(0));
        for (DTO value : valueToSave) {
            Row row = sheet.createRow(rowIndex.getAndIncrement());
            fillRow(row, value, screenColumns);
        }
        autoSizeColumn(sheet, screenColumns);
    }

    protected void fillHeader(Row row, List<ScreenColumnDTO> screenColumns, DTO object) {
        AtomicInteger colIndex = new AtomicInteger(INITIAL_COL_INDEX);
        for (ScreenColumnDTO screenColumn : screenColumns) {
            if (isVisible(screenColumn, object) && screenColumn.isExport()) {
                Cell cell = row.createCell(colIndex.getAndIncrement());
                updateCell(cell, screenColumn);
                cell.setCellStyle(getHeaderCellStyle());
            }
        }
    }

    protected void fillRow(Row row, DTO data, List<ScreenColumnDTO> screenColumns) throws ReflectiveOperationException {
        AtomicInteger colIndex = new AtomicInteger(INITIAL_COL_INDEX);
        Object object;
        for (ScreenColumnDTO screenColumn : screenColumns) {
            String columnName = screenColumn.getColumnName();
            String[] split = columnName.split("\\.");
            if (isVisible(screenColumn, data) && screenColumn.isExport()) {
                Optional<String> maybyNotStandardFill = notStandardColumnFill(screenColumn.getColumnName(), data);
                if (maybyNotStandardFill.isPresent()) {
                    object = maybyNotStandardFill.get();
                } else if (split.length > 1) {
                    Field field = data.getClass().getDeclaredField(split[0]);
                    field.setAccessible(true);
                    object = field.get(data);
                    if (!Objects.isNull(object)) {
                        for (int i = 1; i < split.length; i++) {
                            object = findObject(split[i], object);
                        }
                    }
                } else {
                    object = findObject(columnName, data);
                }
                updateCell(row.createCell(colIndex.getAndIncrement()), object);
            }
        }
    }

    /**
     * Nie standardowe tworzenie wartości w kolumnach
     * np. Gdy istnieje potrzeba użycia dwóch pól z DTO (String name, String surname) do stworzenia wartości kolumny o nazwie "nameAndSurname"
     * <p>
     * if(columnName.equals("nameAndSurname") {
     * return Optional.of(data.getName + " " + data.getSurname);
     * }
     * return Optional.empty();
     */
    protected abstract Optional<String> notStandardColumnFill(String columnName, DTO data);

    protected Object findObject(String fieldName, Object o) {
        Field field = findField(fieldName, o);
        if (Objects.nonNull(field)) {
            try {
                return field.get(o);
            } catch (IllegalAccessException e) {
                return null;
            }
        }
        return null;
    }

    protected Field findField(String fieldName, Object o) {
        Class<?> current = o.getClass();
        do {
            try {
                Field field = current.getDeclaredField(fieldName);
                field.setAccessible(true);
                return field;
            } catch (Exception e) {
            }
        } while ((current = current.getSuperclass()) != null);
        return null;
    }

    protected void updateCell(Cell cell, Object o) {
        String cellValue = cellValueHelper.getCellValue(o, messageSource, locale);
        cell.setCellValue(cellValue);
    }


    protected void updateCell(Cell cell, ScreenColumnDTO screenColumn) {
        cell.setCellValue(messageSource.getMessage(getPrefix() + screenColumn.getColumnName(), null, locale));
    }


    private boolean isVisible(ScreenColumnDTO screenColumn, Object object) {
        boolean isAuthorityToExport = isAuthorityToExport(screenColumn, object);
        return (screenColumn.isVisible() || !isOnlyVisibleColumn) && isAuthorityToExport;
    }

    private boolean isAuthorityToExport(ScreenColumnDTO screenColumn, Object object) {
        if (notStandardColumnFill(screenColumn.getColumnName(), (DTO) object).isEmpty()) {
            Field field = findField(screenColumn.getColumnName(), object);
            if (Objects.nonNull(field) && field.isAnnotationPresent(ViewWithAuthority.class)) {
                ViewWithAuthority annotation = field.getAnnotation(ViewWithAuthority.class);
                if (annotation.ignoreInXlsxExport()) {
                    return true;
                }
                return Arrays.stream(annotation.values())
                    .anyMatch(SecurityUtils::isCurrentUserInAuthority);
            }
        }
        return true;
    }

    protected String getFilename() {
        String filenameFormat = "%s_%s.xlsx"; // filename_date.xlsx
        String datePattern = "yyyyMMddHHmm";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(datePattern).withZone(ZoneId.systemDefault());
        String date = formatter.format(InstantUtil.now());
        String filename = messageSource.getMessage(getPrefix() + "fileName", null, locale);
        return String.format(filenameFormat, filename, date);
    }

    protected void autoSizeColumn(Sheet sheet, List<ScreenColumnDTO> screenColumnEntity) {
        for (int i = 0; i < screenColumnEntity.size(); i++) {
            sheet.autoSizeColumn(i, true);
        }
    }

    protected void setHeaderCellStyle(Workbook workbook) {
        CellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setWrapText(true);
        cellStyle.setAlignment(HorizontalAlignment.LEFT);
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        headerCellStyle = cellStyle;
    }

    protected CellStyle getHeaderCellStyle() {
        return headerCellStyle;
    }

    protected abstract List<ScreenColumnDTO> getColumnList();
}
