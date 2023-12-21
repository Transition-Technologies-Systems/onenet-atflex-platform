package pl.com.tt.flex.server.dataimport;

import org.springframework.web.multipart.MultipartFile;
import pl.com.tt.flex.model.service.dto.file.FileDTO;
import pl.com.tt.flex.server.dataimport.factory.DataImportFormat;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public interface DataImport<U> {

    List<U> doImport(MultipartFile file, Locale locale) throws IOException, ImportDataException;

    List<U> doImport(FileDTO fileDTO, Locale locale) throws IOException, ImportDataException;

    boolean supportClass(Class clazz);

    boolean supportFormat(DataImportFormat format);

}
