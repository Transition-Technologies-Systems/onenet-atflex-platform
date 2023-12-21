package pl.com.tt.flex.flex.agno.service.common.dto;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.web.multipart.MultipartFile;
import pl.com.tt.flex.flex.agno.web.resource.error.file.FileParseException;
import pl.com.tt.flex.model.service.dto.file.FileDTO;
import pl.com.tt.flex.model.service.dto.file.FileExtension;

import java.util.Objects;

@Slf4j
public class FileDTOUtil {

    public static FileDTO parseMultipartFile(MultipartFile multipartFile) {
        try {
            return new FileDTO(multipartFile);
        } catch (Exception e) {
            log.error("Error occurred while parsing multipartFile file: {}", multipartFile.getOriginalFilename());
            log.error(e.getMessage(), e);
            throw new FileParseException();
        }
    }

    public static FileDTO parseMultipartFile(MultipartFile multipartFile, String filename) {
        try {
            return new FileDTO(filename, multipartFile.getBytes());
        } catch (Exception e) {
            log.error("Error occurred while parsing multipartFile file: {}", multipartFile.getOriginalFilename());
            log.error(e.getMessage(), e);
            throw new FileParseException();
        }
    }

    public static FileExtension getFileExtension(String filename) {
        String fileExtensionString = FilenameUtils.getExtension(filename);
        return FileExtension.valueOf(Objects.requireNonNull(fileExtensionString).toUpperCase());
    }
}
