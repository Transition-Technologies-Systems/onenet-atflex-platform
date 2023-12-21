package pl.com.tt.flex.model.service.dto.file;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@FieldNameConstants
public class FileContentDTO implements Serializable {

    // name of file with extension (e.g. test.txt)
    private final String fileName;
    // the contents of the file line by line
    private final List<String> content;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public FileContentDTO(@JsonProperty(Fields.fileName) String fileName,
                          @JsonProperty(Fields.content) List<String> content) {
        this.fileName = fileName;
        this.content = content;
    }

    public FileContentDTO(String fileName, String base64String) {
        this.fileName = fileName;
        this.content = getContentFromArrayByte(Base64.getDecoder().decode(base64String));
    }

    public FileContentDTO(MultipartFile multipartFile) throws IOException {
        this.fileName = multipartFile.getOriginalFilename();
        this.content = getContentFromArrayByte(Base64.getDecoder().decode(multipartFile.getBytes()));
    }

    public FileContentDTO(FileDTO fileDTO) {
        this.fileName = fileDTO.getFileName();
        this.content = getContentFromArrayByte(fileDTO.getBytesData());
    }

    private List<String> getContentFromArrayByte(byte[] bytes) {
        InputStream inputStream = new ByteArrayInputStream(bytes);
        BufferedReader buffReader = new BufferedReader(new InputStreamReader(inputStream));
        return buffReader.lines().collect(Collectors.toList());
    }
}
