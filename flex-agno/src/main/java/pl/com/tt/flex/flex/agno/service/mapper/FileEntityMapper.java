package pl.com.tt.flex.flex.agno.service.mapper;

import org.mapstruct.Named;
import pl.com.tt.flex.flex.agno.service.common.dto.FileDTOUtil;
import pl.com.tt.flex.flex.agno.util.ZipUtil;
import pl.com.tt.flex.model.service.dto.file.FileDTO;
import pl.com.tt.flex.model.service.dto.file.FileExtension;

import java.util.Collections;

public interface FileEntityMapper<D, E> extends EntityMapper<D, E> {

    @Named("fileExtensionDTOToEntity")
    default FileExtension fileExtensionDTOToEntity(FileDTO fileDTO) {
        return FileDTOUtil.getFileExtension(fileDTO.getFileName());
    }

    @Named("fileNameDTOToEntity")
    default String fileNameDTOToEntity(FileDTO fileDTO) {
        return fileDTO.getFileName();
    }

    @Named("fileBase64DataDTOToEntity")
    default byte[] fileBase64DataDTOToEntity(FileDTO fileDTO) {
        return ZipUtil.filesToZip(Collections.singletonList(fileDTO));
    }
}
