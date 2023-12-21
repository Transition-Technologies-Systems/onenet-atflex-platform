package pl.com.tt.flex.server.service.chat.mapper;

import java.util.Objects;
import java.util.Optional;

import org.mapstruct.Mapper;

import pl.com.tt.flex.model.service.dto.chat.ChatDTO;
import pl.com.tt.flex.model.service.dto.chat.ChatRecipientDTO;
import pl.com.tt.flex.server.domain.chat.ChatEntity;
import pl.com.tt.flex.server.domain.fsp.FspEntity;
import pl.com.tt.flex.server.domain.user.UserEntity;
import pl.com.tt.flex.server.service.mapper.EntityMapper;

@Mapper(componentModel = "spring")
public interface ChatMapper extends EntityMapper<ChatDTO, ChatEntity> {

    default ChatDTO toDtoForCompany(ChatEntity chat, Long currentCompanyId) {
        ChatDTO dto = toDto(chat);
        if (Optional.ofNullable(chat.getRecipientCompany()).map(FspEntity::getId).map(id -> id.equals(currentCompanyId)).orElse(false)) {
            setChatInitiatorAsRespondent(chat, dto);
        } else {
            setChatRecipientAsRespondent(chat, dto);
        }
        return dto;
    }

    default ChatDTO toDtoForInitiatorUser(ChatEntity chat, UserEntity currentUser) {
        ChatDTO dto = toDto(chat);
        if (currentUser.hasRole(chat.getRecipientType())) {
            setChatInitiatorAsRespondent(chat, dto);
        } else {
            setChatRecipientAsRespondent(chat, dto);
        }
        return dto;
    }

    default ChatEntity toEntity(ChatDTO chat, FspEntity currentCompany, FspEntity respondentCompany) {
        ChatEntity chatEntity = toEntity(chat);
        chatEntity.setInitiatorType(currentCompany.getRole());
        chatEntity.setInitiatorCompany(currentCompany);
        chatEntity.setRecipientType(chat.getRespondent().getRole());
        chatEntity.setRecipientCompany(respondentCompany);
        return chatEntity;
    }

    default ChatEntity toEntityForInitiatorCompany(ChatDTO chat, FspEntity initiatorCompany) {
        ChatEntity chatEntity = toEntity(chat);
        chatEntity.setInitiatorType(initiatorCompany.getRole());
        chatEntity.setInitiatorCompany(initiatorCompany);
        chatEntity.setRecipientType(chat.getRespondent().getRole());
        return chatEntity;
    }

    default ChatEntity toEntity(ChatDTO chat, UserEntity currentUser, FspEntity respondentCompany) {
        ChatEntity chatEntity = toEntity(chat);
        chatEntity.setInitiatorType(currentUser.getRoles().stream().findAny().get());
        chatEntity.setRecipientType(chat.getRespondent().getRole());
        chatEntity.setRecipientCompany(respondentCompany);
        return chatEntity;
    }

    default ChatEntity toEntityForInitiatorUser(ChatDTO chat, UserEntity initiatorUser) {
        ChatEntity chatEntity = toEntity(chat);
        chatEntity.setInitiatorType(initiatorUser.getRoles().stream().findAny().get());
        chatEntity.setRecipientType(chat.getRespondent().getRole());
        return chatEntity;
    }

    private void setChatInitiatorAsRespondent(ChatEntity chat, ChatDTO dto) {
        if (Objects.nonNull(chat.getInitiatorCompany())) {
            FspEntity initiatorCompany = chat.getInitiatorCompany();
            dto.setRespondent(new ChatRecipientDTO(initiatorCompany.getId(), initiatorCompany.getCompanyName(), chat.getInitiatorType()));
        } else {
            ChatRecipientDTO recipient = new ChatRecipientDTO();
            recipient.setRole(chat.getInitiatorType());
            dto.setRespondent(recipient);
        }
    }

    private void setChatRecipientAsRespondent(ChatEntity chat, ChatDTO dto) {
        if (Objects.nonNull(chat.getRecipientCompany())) {
            FspEntity recipientCompany = chat.getRecipientCompany();
            dto.setRespondent(new ChatRecipientDTO(recipientCompany.getId(), recipientCompany.getCompanyName(), chat.getRecipientType()));
        } else {
            ChatRecipientDTO recipient = new ChatRecipientDTO();
            recipient.setRole(chat.getRecipientType());
            dto.setRespondent(recipient);
        }
    }

}
