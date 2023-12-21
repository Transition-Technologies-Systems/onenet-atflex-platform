package pl.com.tt.flex.server.service.chat.message;

import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pl.com.tt.flex.model.service.dto.chat.message.ChatMessageDTO;
import pl.com.tt.flex.model.service.dto.file.FileDTO;
import pl.com.tt.flex.server.domain.chat.message.ChatMessageEntity;
import pl.com.tt.flex.server.domain.fsp.FspEntity;
import pl.com.tt.flex.server.domain.user.UserEntity;
import pl.com.tt.flex.server.repository.AbstractJpaRepository;
import pl.com.tt.flex.server.repository.chat.message.ChatMessageRepository;
import pl.com.tt.flex.server.service.chat.ChatService;
import pl.com.tt.flex.server.service.chat.message.mapper.ChatMessageMapper;
import pl.com.tt.flex.server.service.common.AbstractServiceImpl;
import pl.com.tt.flex.server.service.mapper.EntityMapper;
import pl.com.tt.flex.server.service.user.UserService;

import static pl.com.tt.flex.server.service.chat.ChatServiceImpl.NO_FSP_CHAT_RECIPIENT_ROLES;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ChatMessageServiceImpl extends AbstractServiceImpl<ChatMessageEntity, ChatMessageDTO, Long> implements ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatMessageMapper chatMessageMapper;
    private final UserService userService;
    private final ChatService chatService;

    @Override
    public List<ChatMessageDTO> getAllInChat(Long chatId) {
        UserEntity currentUser = userService.getCurrentUserFetchFsp();
        List<ChatMessageEntity> messages = chatMessageRepository.findAllByChatIdOrderByCreatedDateAsc(chatId);
        if(Objects.nonNull(currentUser.getFsp())) {
            Long currentCompanyId = currentUser.getFsp().getId();
            messages.stream().filter(msg -> !isSentByCurrentCompany(currentCompanyId, msg)).forEach(msg -> msg.setRead(true));
            return chatMessageMapper.toDtoForCompany(messages, currentCompanyId);
        } else {
            Long currentUserId = currentUser.getId();
            messages.stream().filter(msg -> !isSentByCurrentUser(currentUserId, msg)).forEach(msg -> msg.setRead(true));
            return chatMessageMapper.toDtoForUser(messages, currentUser);
        }
    }

    @Override
    public FileDTO getFileFromMessage(Long messageId) {
        return chatMessageRepository.findById(messageId)
            .filter(ChatMessageEntity::containsFile)
            .map(msg -> new FileDTO(msg.getFileName(), msg.getFile()))
            .orElseThrow(() -> new NoSuchElementException("File not found"));
    }

    @Override
    public void createMessage(Long chatId, String content, MultipartFile file) throws IOException {
        ChatMessageEntity messageToSave = new ChatMessageEntity();
        messageToSave.setText(content);
        if (file != null) {
            messageToSave.setFileParams(file);
        };
        messageToSave.setSendingUser(userService.getCurrentUser());
        messageToSave.setChat(chatService.getById(chatId));
        chatMessageRepository.save(messageToSave);
    }

    @Override
    public Long getNumberOfUnreadMessages() {
        UserEntity currentUser = userService.getCurrentUserFetchFsp();
        if(NO_FSP_CHAT_RECIPIENT_ROLES.stream().anyMatch(currentUser::hasRole)) {
            return chatMessageRepository.countUnreadByRole(currentUser.getRoles().stream().findAny().get());
        } else {
            return chatMessageRepository.countUnreadByCompanyId(currentUser.getFsp().getId());
        }
    }

    @Override
    public void markAllInChatAsRead(Long chatId) {
        UserEntity currentUser = userService.getCurrentUserFetchFsp();
        if(NO_FSP_CHAT_RECIPIENT_ROLES.stream().noneMatch(currentUser::hasRole)) {
            Long currentCompanyId = currentUser.getFsp().getId();
            chatMessageRepository.findUnreadByChatIdAndCompanyId(chatId, currentCompanyId).stream()
                .filter(msg -> !isSentByCurrentCompany(currentCompanyId, msg)).forEach(msg -> msg.setRead(true));
        } else {
            Long currentUserId = currentUser.getId();
            chatMessageRepository.findUnreadByChatIdAndRole(chatId, currentUser.getRoles().stream().findAny().get()).stream()
                .filter(msg -> !isSentByCurrentUser(currentUserId, msg)).forEach(msg -> msg.setRead(true));
        }
    }

    private Boolean isSentByCurrentCompany(Long currentCompanyId, ChatMessageEntity msg) {
        return Optional.ofNullable(msg.getSendingUser().getFsp()).map(FspEntity::getId)
            .map(id -> id.equals(currentCompanyId)).orElse(false);
    }

    private Boolean isSentByCurrentUser(Long currentUserId, ChatMessageEntity msg) {
        return Optional.ofNullable(msg.getSendingUser()).map(UserEntity::getId)
            .map(id -> id.equals(currentUserId)).orElse(false);
    }

    @Override
    public AbstractJpaRepository<ChatMessageEntity, Long> getRepository() {
        return chatMessageRepository;
    }

    @Override
    public EntityMapper<ChatMessageDTO, ChatMessageEntity> getMapper() {
        return chatMessageMapper;
    }
}
