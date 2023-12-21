package pl.com.tt.flex.model.service.dto.chat.message;

import java.io.Serializable;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.com.tt.flex.model.service.dto.audit.AbstractAuditingDTO;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ChatMessageDTO extends AbstractAuditingDTO implements Serializable {

    private Long id;
    private String content;
    private Long userId;
    private boolean myCompanyMessage;
    private boolean read;
    private String attachedFileName;

}
