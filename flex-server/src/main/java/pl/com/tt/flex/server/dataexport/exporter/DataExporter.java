package pl.com.tt.flex.server.dataexport.exporter;

import pl.com.tt.flex.model.service.dto.file.FileDTO;
import pl.com.tt.flex.server.dataexport.exporter.offer.detail.enumeration.LevelOfDetail;
import pl.com.tt.flex.server.dataexport.factory.DataExporterFormat;
import pl.com.tt.flex.server.dataexport.util.header.Header;
import pl.com.tt.flex.server.domain.screen.enumeration.Screen;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public interface DataExporter<U> {

    boolean supportFormat(DataExporterFormat format);

    boolean supportClass(Class clazz);

    boolean supportScreen(Screen screen);

    String getPrefix();

    FileDTO export(List<U> objects, Locale locale, Screen screen, boolean isOnlyVisibleColumn, LevelOfDetail detail) throws IOException;

    List<Header> getHeaderList(List<Locale> locale, U object);
}
