package pl.com.tt.flex.server.service.importData.flexPotential;

import org.springframework.web.multipart.MultipartFile;
import pl.com.tt.flex.server.dataimport.ImportDataException;

import java.io.IOException;

public interface FlexPotentialImportService {

    void importFlexPotential(MultipartFile file, String langKey) throws IOException, ImportDataException;

}
