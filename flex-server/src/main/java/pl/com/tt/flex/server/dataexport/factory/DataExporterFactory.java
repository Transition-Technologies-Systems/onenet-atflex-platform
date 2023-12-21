package pl.com.tt.flex.server.dataexport.factory;

import pl.com.tt.flex.server.dataexport.exporter.DataExporter;
import pl.com.tt.flex.server.domain.screen.enumeration.Screen;

public interface DataExporterFactory {

    DataExporter getDataExporter(DataExporterFormat format, Class clazz, Screen screen);

}
