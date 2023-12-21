package pl.com.tt.flex.server.validator;

import static pl.com.tt.flex.server.web.rest.errors.ErrorConstants.FILE_EXTENSION_NOT_SUPPORTED;

import java.util.List;
import java.util.Set;

import org.springframework.web.multipart.MultipartFile;

import pl.com.tt.flex.server.common.errors.ObjectValidationException;
import pl.com.tt.flex.server.domain.common.enumeration.FileExtension;
import pl.com.tt.flex.server.service.common.dto.FileDTOUtil;

public abstract class AbstractFileValidator {

    public void checkFileExtensionValid(MultipartFile multipartFile) throws ObjectValidationException {
        FileExtension fileExtension = FileDTOUtil.getFileExtension(multipartFile.getOriginalFilename());
        if (!getSupportedFileExtensions().contains(fileExtension)) {
            throw new ObjectValidationException("Not supported file extension", FILE_EXTENSION_NOT_SUPPORTED, getEntityName());
        }
    }

    public void checkFileExtensionValid(List<MultipartFile> multipartFiles) throws ObjectValidationException {
        for (MultipartFile multipartFile : multipartFiles) {
            checkFileExtensionValid(multipartFile);
        }
    }

    protected abstract Set<FileExtension> getSupportedFileExtensions();

    protected abstract String getEntityName();

}
