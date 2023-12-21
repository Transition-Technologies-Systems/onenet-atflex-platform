package pl.com.tt.flex.server.service.chat.message;

import java.io.IOException;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import pl.com.tt.flex.model.service.dto.chat.message.ChatMessageDTO;
import pl.com.tt.flex.model.service.dto.file.FileDTO;
import pl.com.tt.flex.server.domain.chat.message.ChatMessageEntity;
import pl.com.tt.flex.server.service.AbstractService;

public interface ChatMessageService extends AbstractService<ChatMessageEntity, ChatMessageDTO, Long> {

    List<ChatMessageDTO> getAllInChat(Long chatId);

	FileDTO getFileFromMessage(Long messageId);

    void createMessage(Long chatId, String content, MultipartFile file) throws IOException;

    Long getNumberOfUnreadMessages();

    void markAllInChatAsRead(Long chatId);
}
