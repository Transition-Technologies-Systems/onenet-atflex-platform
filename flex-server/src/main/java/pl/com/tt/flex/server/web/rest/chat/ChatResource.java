package pl.com.tt.flex.server.web.rest.chat;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pl.com.tt.flex.model.service.dto.chat.ChatDTO;
import pl.com.tt.flex.model.service.dto.chat.ChatRecipientDTO;
import pl.com.tt.flex.model.service.dto.chat.message.ChatMessageDTO;
import pl.com.tt.flex.model.service.dto.file.FileDTO;
import pl.com.tt.flex.server.service.chat.ChatService;
import pl.com.tt.flex.server.service.chat.message.ChatMessageService;
import pl.com.tt.flex.server.validator.chat.ChatValidator;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ChatResource {

    public static final String ENTITY_NAME = "chat";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final ChatService chatService;
    private final ChatValidator chatValidator;
    private final ChatMessageService chatMessageService;

    protected ResponseEntity<List<ChatDTO>> getAllForLoggedInUser() {
        List<ChatDTO> result = chatService.getAllForLoggedInUser();
        return ResponseEntity.ok().body(result);
    }

    protected ResponseEntity<List<ChatRecipientDTO>> getAllRecipientsDictionary() {
        List<ChatRecipientDTO> result = chatService.getAllRecipientsDictionary();
        return ResponseEntity.ok().body(result);
    }

    protected void createNewChat(ChatDTO chatDTO) {
        chatValidator.checkChatDoesNotExist(chatDTO.getRespondent());
        chatService.save(chatDTO);
    }

    protected ResponseEntity<List<ChatMessageDTO>> getAllMessagesInChat(Long chatId) {
        chatValidator.checkChatAccess(chatId);
        List<ChatMessageDTO> result = chatMessageService.getAllInChat(chatId);
        return ResponseEntity.ok().body(result);
    }

    protected ResponseEntity<FileDTO> getFileFromMessage(Long messageId) {
        chatValidator.checkFileAccess(messageId);
        FileDTO file = chatMessageService.getFileFromMessage(messageId);
        return ResponseEntity.ok().body(file);
    }

    protected void createMessage(Long chatId, String content, MultipartFile file) throws IOException {
        chatValidator.checkChatAccess(chatId);
        if(chatValidator.messageCreateRequestValid(content, file)) {
            chatMessageService.createMessage(chatId, content, file);
        }
    }

    protected ResponseEntity<Long> getNumberOfUnreadMessages() {
        return ResponseEntity.ok().body(chatMessageService.getNumberOfUnreadMessages());
    }

    protected void markAllInChatAsRead(Long chatId) {
        chatValidator.checkChatAccess(chatId);
        chatMessageService.markAllInChatAsRead(chatId);
    }
}
