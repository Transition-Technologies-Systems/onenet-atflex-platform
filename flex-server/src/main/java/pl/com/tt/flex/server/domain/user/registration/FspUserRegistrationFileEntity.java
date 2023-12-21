package pl.com.tt.flex.server.domain.user.registration;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.id.enhanced.SequenceStyleGenerator;
import pl.com.tt.flex.server.domain.audit.AbstractAuditingEntity;
import pl.com.tt.flex.server.domain.common.enumeration.FileExtension;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Objects;

/**
 * A FspUserRegistrationFileEntity - attached file to FspUserRegistrationEntity.
 * @see FspUserRegistrationEntity
 */
@Getter
@Setter
@Entity
@Table(name = "fsp_user_registration_file")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@GenericGenerator(
    name = "fsp_user_reg_file_id_generator",
    strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
    parameters = {
        @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "fsp_user_reg_file_seq"),
        @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.INITIAL_PARAM, value = "1"),
        @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.INCREMENT_PARAM, value = "1")
    }
)
public class FspUserRegistrationFileEntity extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "fsp_user_reg_file_id_generator")
    private Long id;

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
    @Column(name = "file_zip_data", nullable = false)
    private byte[] fileZipData;

    @NotNull
    @ManyToOne(optional = false)
    private FspUserRegistrationCommentEntity comment;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof FspUserRegistrationFileEntity)) {
            return false;
        }
        return Objects.nonNull(id) && Objects.equals(id, ((FspUserRegistrationFileEntity) o).id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
