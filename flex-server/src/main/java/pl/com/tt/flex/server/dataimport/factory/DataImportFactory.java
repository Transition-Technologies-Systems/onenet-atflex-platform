package pl.com.tt.flex.server.dataimport.factory;

import pl.com.tt.flex.server.dataimport.DataImport;

public interface DataImportFactory {

    DataImport getDataImport(Class clazz, DataImportFormat importFormat);
}
