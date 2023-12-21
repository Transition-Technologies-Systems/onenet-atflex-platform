package pl.com.tt.flex.server.service.chat.message.mapper;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.mapstruct.Mapper;

import pl.com.tt.flex.model.service.dto.MinimalDTO;
import pl.com.tt.flex.model.service.dto.chat.ChatDTO;
import pl.com.tt.flex.model.service.dto.chat.ChatRecipientDTO;
import pl.com.tt.flex.model.service.dto.chat.message.ChatMessageDTO;
import pl.com.tt.flex.server.domain.chat.ChatEntity;
import pl.com.tt.flex.server.domain.chat.message.ChatMessageEntity;
import pl.com.tt.flex.server.domain.fsp.FspEntity;
import pl.com.tt.flex.server.domain.user.UserEntity;
import pl.com.tt.flex.server.service.mapper.EntityMapper;

import static pl.com.tt.flex.server.service.chat.ChatServiceImpl.NO_FSP_CHAT_RECIPIENT_ROLES;

@Mapper(componentModel = "spring")
public interface ChatMessageMapper extends EntityMapper<ChatMessageDTO, ChatMessageEntity> {

    default ChatMessageDTO toDtoForCompany(ChatMessageEntity message, Long currentCompanyId) {
        ChatMessageDTO messageDTO = toDto(message);
        boolean sentByCurrentCompany = Optional.ofNullable(message.getSendingUser()).map(UserEntity::getFsp).map(FspEntity::getId).map(id -> id.equals(currentCompanyId)).orElse(false);
        messageDTO.setMyCompanyMessage(sentByCurrentCompany);
        messageDTO.setContent(message.getText());
        messageDTO.setUserId(message.getSendingUser().getId());
        String senderLabel = message.getSendingUser().getUserName();
        if (Objects.nonNull(message.getSendingUser().getFsp())) {
            senderLabel = senderLabel + " (" + message.getSendingUser().getFsp().getCompanyName() + ")";
        }
        messageDTO.setCreatedBy(senderLabel);
        messageDTO.setAttachedFileName(message.getFileName());
        return messageDTO;
    }

    default ChatMessageDTO toDtoForUser(ChatMessageEntity message, UserEntity currentUser) {
        ChatMessageDTO messageDTO = toDto(message);
        boolean sentByCurrentRole = Optional.ofNullable(message.getSendingUser()).map(UserEntity::getRoles)
                        .map(roles -> roles.contains(currentUser.getRoles().stream().findAny().get())).orElse(false);
        messageDTO.setMyCompanyMessage(sentByCurrentRole);
        messageDTO.setContent(message.getText());
        messageDTO.setUserId(message.getSendingUser().getId());
        String senderLabel = message.getSendingUser().getUserName();
        if (Objects.nonNull(message.getSendingUser().getFsp())) {
            senderLabel = senderLabel + " (" + message.getSendingUser().getFsp().getCompanyName() + ")";
        }
        messageDTO.setCreatedBy(senderLabel);
        messageDTO.setAttachedFileName(message.getFileName());
        return messageDTO;
    }

    default List<ChatMessageDTO> toDtoForCompany(List<ChatMessageEntity> messages, Long currentCompanyId) {
        return messages.stream().map(msg -> toDtoForCompany(msg, currentCompanyId)).collect(Collectors.toList());
    }

    default List<ChatMessageDTO> toDtoForUser(List<ChatMessageEntity> messages, UserEntity currentUser) {
        return messages.stream().map(msg -> toDtoForUser(msg, currentUser)).collect(Collectors.toList());
    }

    default List<ChatDTO> toMinDtoForCompany(List<MinimalDTO<ChatEntity, ChatMessageEntity>> lastMessageForEachChatSorted, Long currentCompanyId) {
        return lastMessageForEachChatSorted.stream().map(msg -> toMinDtoForCompany(msg, currentCompanyId)).collect(Collectors.toList());
    }

    default List<ChatDTO> toMinDtoForUser(List<MinimalDTO<ChatEntity, ChatMessageEntity>> lastMessageForEachChatSorted, UserEntity currentUser) {
        return lastMessageForEachChatSorted.stream().map(msg -> toMinDtoForUser(msg, currentUser)).collect(Collectors.toList());
    }

    default ChatDTO toMinDtoForCompany(MinimalDTO<ChatEntity, ChatMessageEntity> lastMessageInChat, Long currentCompanyId) {
        ChatEntity chat = lastMessageInChat.getId();
        ChatDTO dto = new ChatDTO();
        dto.setId(chat.getId());
        dto.setCreatedBy(chat.getCreatedBy());
        dto.setCreatedDate(chat.getCreatedDate());
        dto.setLastModifiedBy(chat.getLastModifiedBy());
        dto.setLastModifiedDate(chat.getLastModifiedDate());
        ChatMessageEntity message = lastMessageInChat.getValue();
        dto.setLatestMessage(Optional.ofNullable(message).map(msg -> toDtoForCompany(message, currentCompanyId)).orElse(null));
        if (Optional.ofNullable(chat.getInitiatorCompany()).map(FspEntity::getId).map(id -> id.equals(currentCompanyId)).orElse(false)) {
            if (Objects.nonNull(chat.getRecipientCompany())) {
                FspEntity recipientCompany = chat.getRecipientCompany();
                dto.setRespondent(new ChatRecipientDTO(recipientCompany.getId(), recipientCompany.getCompanyName(), chat.getRecipientType()));
            } else {
                ChatRecipientDTO recipient = new ChatRecipientDTO();
                recipient.setRole(chat.getRecipientType());
                dto.setRespondent(recipient);
            }
        } else {
            if (Objects.nonNull(chat.getInitiatorCompany())) {
                FspEntity initiatorCompany = chat.getInitiatorCompany();
                dto.setRespondent(new ChatRecipientDTO(initiatorCompany.getId(), initiatorCompany.getCompanyName(), chat.getInitiatorType()));
            } else {
                ChatRecipientDTO recipient = new ChatRecipientDTO();
                recipient.setRole(chat.getInitiatorType());
                dto.setRespondent(recipient);
            }
        }
        return dto;
    }

    default ChatDTO toMinDtoForUser(MinimalDTO<ChatEntity, ChatMessageEntity> lastMessageInChat, UserEntity currentUser) {
        ChatEntity chat = lastMessageInChat.getId();
        ChatDTO dto = new ChatDTO();
        dto.setId(chat.getId());
        dto.setCreatedBy(chat.getCreatedBy());
        dto.setCreatedDate(chat.getCreatedDate());
        dto.setLastModifiedBy(chat.getLastModifiedBy());
        dto.setLastModifiedDate(chat.getLastModifiedDate());
        ChatMessageEntity message = lastMessageInChat.getValue();
        dto.setLatestMessage(Optional.ofNullable(message).map(msg -> toDtoForUser(message, currentUser)).orElse(null));
        if (currentUser.hasRole(chat.getInitiatorType())) {
            if (NO_FSP_CHAT_RECIPIENT_ROLES.contains(chat.getRecipientType())) {
                ChatRecipientDTO recipient = new ChatRecipientDTO();
                recipient.setRole(chat.getRecipientType());
                dto.setRespondent(recipient);
            } else {
                FspEntity recipientCompany = chat.getRecipientCompany();
                dto.setRespondent(new ChatRecipientDTO(recipientCompany.getId(), recipientCompany.getCompanyName(), chat.getRecipientType()));
            }
        } else {
            if (NO_FSP_CHAT_RECIPIENT_ROLES.contains(chat.getInitiatorType())) {
                ChatRecipientDTO recipient = new ChatRecipientDTO();
                recipient.setRole(chat.getInitiatorType());
                dto.setRespondent(recipient);
            } else {
                FspEntity initiatorCompany = chat.getInitiatorCompany();
                dto.setRespondent(new ChatRecipientDTO(initiatorCompany.getId(), initiatorCompany.getCompanyName(), chat.getInitiatorType()));
            }
        }
        return dto;
    }

}
