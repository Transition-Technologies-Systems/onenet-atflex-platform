package pl.com.tt.flex.server.refreshView.listener;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.persistence.PostPersist;

import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;
import pl.com.tt.flex.model.service.dto.chat.ChatDTO;
import pl.com.tt.flex.server.config.Constants;
import pl.com.tt.flex.server.domain.chat.ChatEntity;
import pl.com.tt.flex.server.domain.user.UserEntity;
import pl.com.tt.flex.server.repository.user.UserRepository;
import pl.com.tt.flex.server.service.chat.mapper.ChatMapper;
import pl.com.tt.flex.server.web.rest.websocket.FlexAdminRefreshViewWebsocketResource;
import pl.com.tt.flex.server.web.rest.websocket.FlexUserRefreshViewWebsocketResource;

import static pl.com.tt.flex.server.service.chat.ChatServiceImpl.NO_FSP_CHAT_RECIPIENT_ROLES;

@Slf4j
@Component
@Transactional(readOnly = true)
public class ChatListener {

    private final ChatMapper chatMapper;
    private final UserRepository userRepository;
    private final FlexUserRefreshViewWebsocketResource userRefreshViewWebsocketResource;
    private final FlexAdminRefreshViewWebsocketResource adminRefreshViewWebsocketResource;

    public ChatListener(ChatMapper chatMapper, @Lazy UserRepository userRepository, @Lazy FlexUserRefreshViewWebsocketResource userRefreshViewWebsocketResource,
                        @Lazy FlexAdminRefreshViewWebsocketResource adminRefreshViewWebsocketResource) {
        this.chatMapper = chatMapper;
        this.userRepository = userRepository;
        this.userRefreshViewWebsocketResource = userRefreshViewWebsocketResource;
        this.adminRefreshViewWebsocketResource = adminRefreshViewWebsocketResource;
    }

    @Async
    @PostPersist
    public void onPostPersist(ChatEntity chatEntity) {
        log.info("onPostPersist() START - Send WebSocket messages with new chat [chatID={}]", chatEntity.getId());
        Set<UserEntity> usersToNotify = new HashSet<>();
        usersToNotify.addAll(getChatInitiatorUsers(chatEntity));
        usersToNotify.addAll(getChatRecipientUsers(chatEntity));
        usersToNotify.stream().map(user -> userRepository.findOneByLoginAndDeletedFalseFetchFsp(user.getLogin()).get()).forEach(user -> postChat(chatEntity, user));
        log.info("onPostPersist() END - Send WebSocket messages with new chat [chatID={}]", chatEntity.getId());
    }

    public void postChat(ChatEntity chatEntity, UserEntity user) {
        ChatDTO chatDTO;
        if(NO_FSP_CHAT_RECIPIENT_ROLES.stream().anyMatch(user::hasRole)) {
            chatDTO = chatMapper.toDtoForInitiatorUser(chatEntity, user);
            postChatToAdminApp(user.getLogin(), chatDTO);
        } else {
            chatDTO = chatMapper.toDtoForCompany(chatEntity, user.getFsp().getId());
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
