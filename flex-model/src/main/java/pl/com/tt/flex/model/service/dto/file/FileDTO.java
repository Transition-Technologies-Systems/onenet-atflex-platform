package pl.com.tt.flex.model.service.dto.file;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.Serializable;
import java.util.Base64;

@Getter
@Setter
@FieldNameConstants
public class FileDTO implements Serializable {

    // name of file with extension (e.g. test.txt)
    private final String fileName;

    @JsonIgnore
    private final byte[] bytesData;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public FileDTO(@JsonProperty(Fields.fileName) String fileName,
                   @JsonProperty(Fields.bytesData) byte[] bytesData) {
        this.fileName = fileName;
        this.bytesData = bytesData;
    }

    public FileDTO(String fileName, String base64String) {
        this.fileName = fileName;
        this.bytesData = Base64.getDecoder().decode(base64String);
    }

    public FileDTO(MultipartFile multipartFile) throws IOException {
        this.fileName = multipartFile.getOriginalFilename();
        this.bytesData = multipartFile.getBytes();
    }

    public String getBase64StringData() {
        return Base64.getEncoder().encodeToString(bytesData);
    }
}