package pl.com.tt.flex.server.domain.chat.message;

import java.io.IOException;
import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.id.enhanced.SequenceStyleGenerator;
import org.springframework.web.multipart.MultipartFile;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.com.tt.flex.server.domain.audit.AbstractAuditingEntity;
import pl.com.tt.flex.server.domain.chat.ChatEntity;
import pl.com.tt.flex.server.domain.user.UserEntity;
import pl.com.tt.flex.server.refreshView.listener.ChatMessageListener;

@Getter
@Setter
@Entity
@Table(name = "chat_message")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@EntityListeners(ChatMessageListener.class)
@GenericGenerator(
    name = "chat_message_id_generator",
    strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
    parameters = {
        @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "chat_message_seq"),
        @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.INITIAL_PARAM, value = "1"),
        @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.INCREMENT_PARAM, value = "1")
    }
)
@NoArgsConstructor
public class ChatMessageEntity extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "chat_message_id_generator")
    private Long id;

    @Size(min = 1, max = 1000)
    @Column(name = "text", length = 1000)
    private String text;

    @ManyToOne(optional = false)
    @JoinColumn(name = "chat_id")
    private ChatEntity chat;

    @NotNull
    @Column(name = "message_read", nullable = false)
    private boolean read = false;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "sending_user_id")
    private UserEntity sendingUser;

    @Size(max = 100)
    @Column(name = "file_name", length = 100)
    private String fileName;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "attachment_file")
    private byte[] file;

    public boolean containsFile() {
        return Objects.nonNull(file) && Objects.nonNull(fileName);
    }

    public void setFileParams(MultipartFile file) throws IOException {
        this.fileName = file.getOriginalFilename();
        this.file = file.getBytes();
    }

}
