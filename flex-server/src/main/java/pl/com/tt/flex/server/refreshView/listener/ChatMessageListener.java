package pl.com.tt.flex.server.refreshView.listener;

import java.util.List;
import java.util.Objects;

import javax.persistence.PostPersist;
import javax.persistence.PostUpdate;

import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;
import pl.com.tt.flex.model.service.dto.MinimalDTO;
import pl.com.tt.flex.model.service.dto.chat.ChatDTO;
import pl.com.tt.flex.model.service.dto.chat.message.ChatMessageDTO;
import pl.com.tt.flex.server.config.Constants;
import pl.com.tt.flex.server.domain.chat.ChatEntity;
import pl.com.tt.flex.server.domain.chat.message.ChatMessageEntity;
import pl.com.tt.flex.server.domain.fsp.FspEntity;
import pl.com.tt.flex.server.domain.user.UserEntity;
import pl.com.tt.flex.server.repository.chat.ChatRepository;
import pl.com.tt.flex.server.repository.chat.message.ChatMessageRepository;
import pl.com.tt.flex.server.repository.user.UserRepository;
import pl.com.tt.flex.server.service.chat.message.mapper.ChatMessageMapper;
import pl.com.tt.flex.server.web.rest.websocket.FlexAdminRefreshViewWebsocketResource;
import pl.com.tt.flex.server.web.rest.websocket.FlexUserRefreshViewWebsocketResource;

import static pl.com.tt.flex.server.service.chat.ChatServiceImpl.NO_FSP_CHAT_RECIPIENT_ROLES;

@Slf4j
@Component
@Transactional(readOnly = true)
public class ChatMessageListener {

    private final ChatRepository chatRepository;
    private final UserRepository userRepository;
    private final ChatMessageMapper chatMessageMapper;
    private final ChatMessageRepository chatMessageRepository;
    private final FlexUserRefreshViewWebsocketResource userRefreshViewWebsocketResource;
    private final FlexAdminRefreshViewWebsocketResource adminRefreshViewWebsocketResource;

    public ChatMessageListener(@Lazy ChatRepository chatRepository, @Lazy UserRepository userRepository, ChatMessageMapper chatMessageMapper, @Lazy ChatMessageRepository chatMessageRepository,
                               @Lazy FlexUserRefreshViewWebsocketResource userRefreshViewWebsocketResource, @Lazy FlexAdminRefreshViewWebsocketResource adminRefreshViewWebsocketResource) {
        this.chatRepository = chatRepository;
        this.userRepository = userRepository;
        this.chatMessageMapper = chatMessageMapper;
        this.chatMessageRepository = chatMessageRepository;
        this.userRefreshViewWebsocketResource = userRefreshViewWebsocketResource;
        this.adminRefreshViewWebsocketResource = adminRefreshViewWebsocketResource;
    }

    @Async
    @PostPersist
    public void onPostPersist(ChatMessageEntity chatMessageEntity) {
        log.info("onPostPersist() START - Send WebSocket messages with created message [messageId={}]", chatMessageEntity.getId());
        sendAllNotifications(chatMessageEntity);
        log.info("onPostPersist() END - Send WebSocket messages with created message [messageId={}]", chatMessageEntity.getId());
    }

    @Async
    @PostUpdate
    public void onPostUpdate(ChatMessageEntity chatMessageEntity) {
        log.info("onPostUpdate() START - Send WebSocket messages with updated message [messageId={}]", chatMessageEntity.getId());
        sendAllNotifications(chatMessageEntity);
        log.info("onPostUpdate() END - Send WebSocket messages with updated message [messageId={}]", chatMessageEntity.getId());
    }

    public void sendAllNotifications(ChatMessageEntity chatMessageEntity) {
        ChatEntity chatEntity = chatRepository.findByIdFetchAll(chatMessageEntity.getChat().getId()).get();
        ChatMessageEntity messageWithSendingFsp = chatMessageRepository.findChatMessageFetchSendingUserFsp(chatMessageEntity.getId());
        List<UserEntity> sendingUsers = getUsersToNotify(chatEntity, messageWithSendingFsp.getSendingUser(), true);
        notifyChatMessageChange(messageWithSendingFsp, sendingUsers);
        notifyChatChange(chatEntity, messageWithSendingFsp, sendingUsers);

        List<UserEntity> respondentUsers = getUsersToNotify(chatEntity, messageWithSendingFsp.getSendingUser(), false);
        notifyChatMessageChange(messageWithSendingFsp, respondentUsers);
        notifyChatChange(chatEntity, messageWithSendingFsp, respondentUsers);
        notifyUnreadNumberChange(respondentUsers);
    }

    private List<UserEntity> getUsersToNotify(ChatEntity chat, UserEntity sendingUser, boolean senderSideUsers) {
        boolean isSenderOfChatInitiatorCompany = Objects.nonNull(sendingUser.getFsp()) && Objects.nonNull(chat.getInitiatorCompany()) && chat.getInitiatorCompany().getId().equals(sendingUser.getFsp().getId());
        boolean isSenderOfChatInitiatorRole = NO_FSP_CHAT_RECIPIENT_ROLES.stream().filter(role -> chat.getInitiatorType().equals(role)).anyMatch(sendingUser::hasRole);
        if (isSenderOfChatInitiatorCompany || isSenderOfChatInitiatorRole) {
            if (senderSideUsers) {
                return getChatInitiatorUsers(chat);
            } else {
                return getChatRecipientUsers(chat);
            }
        } else {
            if (senderSideUsers) {
                return getChatRecipientUsers(chat);
            } else {
                return getChatInitiatorUsers(chat);
            }
        }
    }

    private void notifyChatMessageChange(ChatMessageEntity messageEntity, List<UserEntity> usersToNotify) {
        ChatMessageDTO message;
        FspEntity companyToNotify = usersToNotify.get(0).getFsp();
        if (Objects.nonNull(companyToNotify)) {
            message = chatMessageMapper.toDtoForCompany(messageEntity, companyToNotify.getId());
            for (UserEntity user : usersToNotify) {
                postMessage(messageEntity, message, user);
            }
        } else {
            for (UserEntity user : usersToNotify) {
                message = chatMessageMapper.toDtoForUser(messageEntity, user);
                postMessage(messageEntity, message, user);
            }
        }
    }

    private void postMessage(ChatMessageEntity messageEntity, ChatMessageDTO message, UserEntity user) {
        if (NO_FSP_CHAT_RECIPIENT_ROLES.stream().anyMatch(user::hasRole)) {
            postChatMessageToAdminApp(user.getLogin(), messageEntity.getChat().getId(), message);
        } else {
            postChatMessageToUserApp(user.getLogin(), messageEntity.getChat().getId(), message);
        }
    }

    private void postChatMessageToUserApp(String login, Long chatId, ChatMessageDTO chatMessageDTO) {
        try {
            userRefreshViewWebsocketResource.postChatMessage(login, chatId, chatMessageDTO);
        } catch (Exception e) {
            log.debug("postChatMessageToUserApp() Error while posting chat message {} to {} app\n{}", chatMessageDTO.toString(), Constants.FLEX_USER_APP_NAME, e.getMessage());
        }
    }

    private void postChatMessageToAdminApp(String login, Long chatId, ChatMessageDTO chatMessageDTO) {
        try {
            adminRefreshViewWebsocketResource.postChatMessage(login, chatId, chatMessageDTO);
        } catch (Exception e) {
            log.debug("postChatMessageToAdminApp() Error while posting chat message {} to {} app\n{}", chatMessageDTO.toString(), Constants.FLEX_USER_APP_NAME, e.getMessage());
        }
    }

    private void notifyUnreadNumberChange(List<UserEntity> usersToNotify) {
        if (NO_FSP_CHAT_RECIPIENT_ROLES.stream().anyMatch(usersToNotify.get(0)::hasRole)) {
            usersToNotify.forEach(usr -> {
                Long unreadMessageNumber = chatMessageRepository.countUnreadByRole(usr.getRoles().stream().findAny().get());
                postUnreadMessagesNumberToAdminApp(usr.getLogin(), unreadMessageNumber);
            });
        } else {
            usersToNotify.forEach(usr -> {
                Long unreadMessageNumber = chatMessageRepository.countUnreadByCompanyId(usr.getFsp().getId());
                postUnreadMessagesNumberToUserApp(usr.getLogin(), unreadMessageNumber);
            });
        }
    }

    private void postUnreadMessagesNumberToUserApp(String login, Long unreadNumber) {
        try {
            userRefreshViewWebsocketResource.postUnreadMessagesNumber(login, unreadNumber);
        } catch (Exception e) {
            log.debug("postUnreadMessagesNumberToUserApp() Error while posting unreadNumber {} for user {} to {} app\n{}", unreadNumber, login, Constants.FLEX_USER_APP_NAME, e.getMessage());
        }
    }

    private void postUnreadMessagesNumberToAdminApp(String login, Long unreadNumber) {
        try {
            adminRefreshViewWebsocketResource.postUnreadMessagesNumber(login, unreadNumber);
        } catch (Exception e) {
            log.debug("postUnreadMessagesNumberToAdminApp() Error while posting unreadNumber {} for user {} to {} app\n{}", unreadNumber, login, Constants.FLEX_USER_APP_NAME, e.getMessage());
        }
    }

    public void notifyChatChange(ChatEntity chat, ChatMessageEntity message, List<UserEntity> usersToNotify) {
        usersToNotify.forEach(user -> postChat(new MinimalDTO<>(chat, message), user));
    }

    public void postChat(MinimalDTO<ChatEntity, ChatMessageEntity> minDTO, UserEntity user) {
        ChatDTO chatDTO;
        if(NO_FSP_CHAT_RECIPIENT_ROLES.stream().anyMatch(user::hasRole)) {
            chatDTO = chatMessageMapper.toMinDtoForUser(minDTO, user);
            postChatToAdminApp(user.getLogin(), chatDTO);
        } else {
            chatDTO = chatMessageMapper.toMinDtoForCompany(minDTO, user.getFsp().getId());
            postChatToUserApp(user.getLogin(), chatDTO);
        }
    }

    private List<UserEntity> getChatInitiatorUsers(ChatEntity chatEntity) {
        if (Objects.nonNull(chatEntity.getInitiatorCompany())) {
            return userRepository.findAllByFspIdFetchFsp(chatEntity.getInitiatorCompany().getId());
        }
        return userRepository.findAllWithRole(chatEntity.getInitiatorType());
    }

    private List<UserEntity> getChatRecipientUsers(ChatEntity chatEntity) {
        if (Objects.nonNull(chatEntity.getRecipientCompany())) {
            return userRepository.findAllByFspIdFetchFsp(chatEntity.getRecipientCompany().getId());
        }
        return userRepository.findAllWithRole(chatEntity.getRecipientType());
    }

    private void postChatToUserApp(String login, ChatDTO chatDTO) {
        try {
            userRefreshViewWebsocketResource.postChat(login, chatDTO);
        } catch (Exception e) {
            log.debug("postChatToUserApp() Error while posting chat {} for user {} to {} app\n{}", chatDTO.toString(), login, Constants.FLEX_USER_APP_NAME, e.getMessage());
        }
    }

    private void postChatToAdminApp(String login, ChatDTO chatDTO) {
        try {
            adminRefreshViewWebsocketResource.postChat(login, chatDTO);
        } catch (Exception e) {
            log.debug("postChatToAdminApp() Error while posting chat {} for user {} to {} app\n{}", chatDTO.toString(), login, Constants.FLEX_USER_APP_NAME, e.getMessage());
        }
    }

}

