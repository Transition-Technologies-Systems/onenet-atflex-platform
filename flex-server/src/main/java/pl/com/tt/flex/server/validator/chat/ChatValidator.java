package pl.com.tt.flex.server.validator.chat;

import static pl.com.tt.flex.server.service.chat.ChatServiceImpl.NO_FSP_CHAT_RECIPIENT_ROLES;
import static pl.com.tt.flex.server.web.rest.errors.ErrorConstants.*;

import java.util.Objects;
import java.util.Set;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import pl.com.tt.flex.model.security.permission.Role;
import pl.com.tt.flex.model.service.dto.chat.ChatRecipientDTO;
import pl.com.tt.flex.server.common.errors.ObjectValidationException;
import pl.com.tt.flex.server.domain.chat.ChatEntity;
import pl.com.tt.flex.server.domain.chat.message.ChatMessageEntity;
import pl.com.tt.flex.server.domain.fsp.FspEntity;
import pl.com.tt.flex.server.domain.user.UserEntity;
import pl.com.tt.flex.server.repository.chat.ChatRepository;
import pl.com.tt.flex.server.repository.chat.message.ChatMessageRepository;
import pl.com.tt.flex.server.service.user.UserService;

@Component
@RequiredArgsConstructor
public class ChatValidator {

    ObjectValidationException CHAT_ACCESS_FORBIDDEN_EXCEPTION = new ObjectValidationException("Current user does not have access to requested chat", CHAT_ACCESS_FORBIDDEN);

    private final UserService userService;
    private final ChatRepository chatRepository;
    private final ChatMessageRepository chatMessageRepository;

    public void checkChatAccess(Long chatId) {
        UserEntity currentUser = userService.getCurrentUserFetchFsp();
        ChatEntity chat = chatRepository.findById(chatId).orElseThrow(() -> CHAT_ACCESS_FORBIDDEN_EXCEPTION);
        if (NO_FSP_CHAT_RECIPIENT_ROLES.stream().anyMatch(currentUser::hasRole)) {
            checkChatAccessForUser(currentUser, chat);
        } else {
            checkChatAccessForCompany(currentUser, chat);
        }
    }

    public void checkChatAccessForCompany(UserEntity currentUser, ChatEntity chat) {
        Long currentCompanyId = currentUser.getFsp().getId();
        boolean isChatInitiator = currentUser.hasRole(chat.getInitiatorType()) &&
            (Objects.nonNull(chat.getInitiatorCompany()) && chat.getInitiatorCompany().getId().equals(currentCompanyId));
        boolean isChatRecipient = currentUser.hasRole(chat.getRecipientType()) &&
            (Objects.nonNull(chat.getRecipientCompany()) && chat.getRecipientCompany().getId().equals(currentCompanyId));
        if(!isChatInitiator && !isChatRecipient) {
            throw CHAT_ACCESS_FORBIDDEN_EXCEPTION;
        }
    }

    public void checkChatAccessForUser(UserEntity currentUser, ChatEntity chat) {
        boolean isChatInitiator = currentUser.hasRole(chat.getInitiatorType());
        boolean isChatRecipient = currentUser.hasRole(chat.getRecipientType());
        if(!isChatInitiator && !isChatRecipient) {
            throw CHAT_ACCESS_FORBIDDEN_EXCEPTION;
        }
    }

    public void checkFileAccess(Long messageId) {
        ObjectValidationException fileNotFoundException = new ObjectValidationException("Requested file does not exist", CHAT_FILE_DOES_NOT_EXIST);
        ChatMessageEntity message = chatMessageRepository.findById(messageId).orElseThrow(() -> fileNotFoundException);
        checkChatAccess(message.getChat().getId());
        if (!message.containsFile()) {
            throw fileNotFoundException;
        }
    }

    public boolean messageCreateRequestValid(String content, MultipartFile file) {
        return (Objects.nonNull(content) && !content.isBlank()) || (Objects.nonNull(file) && !file.isEmpty());
    }

    public void checkChatDoesNotExist(ChatRecipientDTO recipientDTO) {
        UserEntity currentUser = userService.getCurrentUserFetchFsp();
        boolean chatExists;
        if (NO_FSP_CHAT_RECIPIENT_ROLES.stream().anyMatch(currentUser::hasRole)) {
            chatExists = checkChatExistsForRoles(currentUser.getRoles(), recipientDTO);
        } else {
            chatExists = checkChaExistsForCompany(currentUser.getFsp(), recipientDTO);
        }
        if(chatExists) {
            throw new ObjectValidationException("Chat already exists", CHAT_ALREADY_EXISTS);
        }
    }

    private boolean checkChaExistsForCompany(FspEntity currentUsersFsp, ChatRecipientDTO recipientDTO) {
        if (NO_FSP_CHAT_RECIPIENT_ROLES.stream().anyMatch(recipientDTO.getRole()::equals)) {
            return chatRepository.existsByParticipatingCompanyIdAndRole(currentUsersFsp.getId(), Set.of(recipientDTO.getRole()));
        } else {
            return chatRepository.existsByParticipatingCompanyIds(currentUsersFsp.getId(), recipientDTO.getId());
        }
    }

    private boolean checkChatExistsForRoles(Set<Role> currentUsersRoles, ChatRecipientDTO recipientDTO) {
        if (NO_FSP_CHAT_RECIPIENT_ROLES.stream().anyMatch(recipientDTO.getRole()::equals)) {
            return chatRepository.existsByParticipatingRoles(currentUsersRoles, Set.of(recipientDTO.getRole()));
        } else {
            return chatRepository.existsByParticipatingCompanyIdAndRole(recipientDTO.getId(), currentUsersRoles);
        }
    }
}
