package pl.com.tt.flex.server.service.common.dto;

import java.util.Objects;

import org.apache.commons.io.FilenameUtils;
import org.springframework.web.multipart.MultipartFile;

import lombok.extern.slf4j.Slf4j;
import pl.com.tt.flex.model.service.dto.file.FileDTO;
import pl.com.tt.flex.server.domain.common.enumeration.FileExtension;
import pl.com.tt.flex.server.web.rest.errors.file.FileParseException;

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

    public static FileExtension getFileExtension(String filename) {
        String fileExtensionString = FilenameUtils.getExtension(filename);
        return FileExtension.valueOf(Objects.requireNonNull(fileExtensionString).toUpperCase());
    }
}
