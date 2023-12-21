package pl.com.tt.flex.server.service.chat;

import java.util.List;

import pl.com.tt.flex.model.service.dto.chat.ChatDTO;
import pl.com.tt.flex.model.service.dto.chat.ChatRecipientDTO;
import pl.com.tt.flex.server.domain.chat.ChatEntity;
import pl.com.tt.flex.server.service.AbstractService;

public interface ChatService extends AbstractService<ChatEntity, ChatDTO, Long> {

	List<ChatDTO> getAllForLoggedInUser();

	List<ChatRecipientDTO> getAllRecipientsDictionary();

	ChatEntity getById(Long chatId);

}
