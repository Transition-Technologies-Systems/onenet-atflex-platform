package pl.com.tt.flex.model.service.dto.chat;

import java.io.Serializable;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.com.tt.flex.model.service.dto.audit.AbstractAuditingDTO;
import pl.com.tt.flex.model.service.dto.chat.message.ChatMessageDTO;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ChatDTO extends AbstractAuditingDTO implements Serializable {

    private Long id;
    private ChatRecipientDTO respondent;
    private ChatMessageDTO latestMessage;

}
