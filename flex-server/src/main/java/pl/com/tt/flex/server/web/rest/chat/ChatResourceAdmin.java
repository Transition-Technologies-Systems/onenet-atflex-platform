package pl.com.tt.flex.server.web.rest.chat;

import static pl.com.tt.flex.model.security.permission.Authority.FLEX_ADMIN_CHAT_MANAGE;
import static pl.com.tt.flex.model.security.permission.Authority.FLEX_ADMIN_CHAT_MESSAGE_CREATE;
import static pl.com.tt.flex.model.security.permission.Authority.FLEX_ADMIN_CHAT_VIEW;
import static pl.com.tt.flex.server.config.Constants.FLEX_ADMIN_APP_NAME;

import java.io.IOException;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import lombok.extern.slf4j.Slf4j;
import pl.com.tt.flex.model.service.dto.chat.ChatDTO;
import pl.com.tt.flex.model.service.dto.chat.ChatRecipientDTO;
import pl.com.tt.flex.model.service.dto.chat.message.ChatMessageDTO;
import pl.com.tt.flex.model.service.dto.file.FileDTO;
import pl.com.tt.flex.server.common.errors.ObjectValidationException;
import pl.com.tt.flex.server.service.chat.ChatService;
import pl.com.tt.flex.server.service.chat.message.ChatMessageService;
import pl.com.tt.flex.server.validator.chat.ChatValidator;

@Slf4j
@RestController
@RequestMapping("/api/admin/chat")
public class ChatResourceAdmin extends ChatResource {

    public ChatResourceAdmin(ChatService chatService, ChatValidator chatValidator, ChatMessageService chatMessageService) {
        super(chatService, chatValidator, chatMessageService);
    }

    @GetMapping
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_CHAT_VIEW + "\")")
    public ResponseEntity<List<ChatDTO>> getAllMin() {
        log.debug("{} - REST request to get all ChatMinDTO for current user", FLEX_ADMIN_APP_NAME);
        return super.getAllForLoggedInUser();
    }

    @GetMapping("/recipients")
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_CHAT_VIEW + "\")")
    public ResponseEntity<List<ChatRecipientDTO>> getAllRecipientsDictionary() {
        log.debug("{} - REST request to get recipients dictionary", FLEX_ADMIN_APP_NAME);
        return super.getAllRecipientsDictionary();
    }

    @PostMapping
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_CHAT_MANAGE + "\")")
    @ResponseStatus(HttpStatus.CREATED)
    public void createNewChat(@RequestBody ChatDTO chatDTO) {
        log.debug("{} - REST request to save chat: {}", FLEX_ADMIN_APP_NAME, chatDTO);
        super.createNewChat(chatDTO);
    }

    @GetMapping("/{id}/messages")
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_CHAT_VIEW + "\")")
    public ResponseEntity<List<ChatMessageDTO>> getAllMessagesForChat(@PathVariable("id") Long chatId) {
        log.debug("{} - REST request to get all messages for chat id: {}", FLEX_ADMIN_APP_NAME, chatId);
        return super.getAllMessagesInChat(chatId);
    }

    @GetMapping("/message/{id}/file")
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_CHAT_VIEW + "\")")
    public ResponseEntity<FileDTO> downloadAttachedFile(@PathVariable("id") Long messageId) throws ObjectValidationException {
        log.debug("{} - REST request to download file attachment from message id: {}", FLEX_ADMIN_APP_NAME, messageId);
        return super.getFileFromMessage(messageId);
    }

    @PostMapping("/message")
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_CHAT_MESSAGE_CREATE + "\")")
    @ResponseStatus(HttpStatus.CREATED)
    public void createMessage(@NotNull @RequestPart("chatId") String chatIdString, @RequestPart(value = "content", required = false) String content,
                              @RequestPart(value = "file", required = false) MultipartFile file) throws ObjectValidationException, IOException {
        log.debug("{} - REST request to create a message in chat id: {}", FLEX_ADMIN_APP_NAME, chatIdString);
        Long chatId = Long.parseLong(chatIdString);
        super.createMessage(chatId, content, file);
    }

    @GetMapping("/unread")
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_CHAT_VIEW + "\")")
    public ResponseEntity<Long> getNumberOfUnreadMessages() {
        log.debug("{} - REST request to get the number of unread messages for current user", FLEX_ADMIN_APP_NAME);
        return super.getNumberOfUnreadMessages();
    }

    @PutMapping("/{id}/mark-as-read")
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_CHAT_VIEW + "\")")
    @ResponseStatus(HttpStatus.OK)
    public void markAllInChatAsRead(@PathVariable("id") Long chatId) {
        log.debug("{} - REST request to mark all messages in chat id: {} as read", FLEX_ADMIN_APP_NAME, chatId);
        super.markAllInChatAsRead(chatId);
    }

}
