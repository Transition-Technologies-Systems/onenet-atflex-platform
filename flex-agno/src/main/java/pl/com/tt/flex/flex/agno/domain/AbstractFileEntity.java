package pl.com.tt.flex.flex.agno.domain;

import lombok.Getter;
import lombok.Setter;
import pl.com.tt.flex.flex.agno.domain.audit.AbstractAuditingEntity;
import pl.com.tt.flex.model.service.dto.file.FileExtension;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * An abstract entity for files
 */
@Getter
@Setter
@MappedSuperclass
public abstract class AbstractFileEntity extends AbstractAuditingEntity {

    /**
     * File name with extension e.g. test.txt
     */
    @NotNull
    @Size(max = 100)
    @Column(name = "file_name", length = 100, nullable = false)
    private String fileName;

    /**
     * File extension type e.g. DOC, DOCX, PDF, TXT, XLS, XLSX
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "file_extension", nullable = false)
    private FileExtension fileExtension;

    /**
     * File is kept in zip archive
     */
    @Lob
    @NotNull
    @Column(name = "file_zip_data", nullable = false)
    private byte[] fileZipData;
}
