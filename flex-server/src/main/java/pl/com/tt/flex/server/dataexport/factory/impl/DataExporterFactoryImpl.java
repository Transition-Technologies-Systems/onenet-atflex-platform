package pl.com.tt.flex.server.dataexport.factory.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pl.com.tt.flex.server.dataexport.exporter.DataExporter;
import pl.com.tt.flex.server.dataexport.factory.DataExporterFactory;
import pl.com.tt.flex.server.dataexport.factory.DataExporterFormat;
import pl.com.tt.flex.server.domain.screen.enumeration.Screen;

import java.util.List;

@Component
public class DataExporterFactoryImpl implements DataExporterFactory {

    private List<DataExporter> dataExporters;

    @Autowired
    public DataExporterFactoryImpl(List<DataExporter> dataExporters) {
        this.dataExporters = dataExporters;
    }

    @Override
    public DataExporter getDataExporter(DataExporterFormat format, Class clazz, Screen screen) {
        return dataExporters.stream()
            .filter(dataExporter -> dataExporter.supportFormat(format) && dataExporter.supportClass(clazz) && dataExporter.supportScreen(screen)).findFirst()
            .orElseThrow(() -> new IllegalStateException(String.format("Cannot find DataExporter for DTO %s and Screen %s saving in %s format!", clazz.getSimpleName(), screen.name(), format.name())));
    }
}
