package pl.com.tt.flex.server.dataimport.factory.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pl.com.tt.flex.server.dataimport.DataImport;
import pl.com.tt.flex.server.dataimport.factory.DataImportFormat;
import pl.com.tt.flex.server.dataimport.factory.DataImportFactory;

import java.util.List;

@Component
public class DataImportFactoryImpl implements DataImportFactory {

    private List<DataImport> dataImports;

    @Autowired
    public DataImportFactoryImpl(List<DataImport> dataImports) {
        this.dataImports = dataImports;
    }


    @Override
    public DataImport getDataImport(Class clazz, DataImportFormat importFormat) {
        return dataImports.stream()
            .filter(dataImport -> dataImport.supportFormat(importFormat) && dataImport.supportClass(clazz)).findFirst()
            .orElseThrow(() -> new IllegalStateException(String.format("Cannot find DataGenerator for %s saving in %s format!", clazz.getSimpleName(), importFormat.name())));
    }
}
