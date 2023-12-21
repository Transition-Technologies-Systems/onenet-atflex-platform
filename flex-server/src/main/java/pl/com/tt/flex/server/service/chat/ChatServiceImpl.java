package pl.com.tt.flex.server.service.chat;

import static pl.com.tt.flex.model.security.permission.Role.ROLE_ADMIN;
import static pl.com.tt.flex.model.security.permission.Role.ROLE_BALANCING_SERVICE_PROVIDER;
import static pl.com.tt.flex.model.security.permission.Role.ROLE_DISTRIBUTION_SYSTEM_OPERATOR;
import static pl.com.tt.flex.model.security.permission.Role.ROLE_FLEX_SERVICE_PROVIDER;
import static pl.com.tt.flex.model.security.permission.Role.ROLE_FLEX_SERVICE_PROVIDER_AGGREGATED;
import static pl.com.tt.flex.model.security.permission.Role.ROLE_MARKET_OPERATOR;
import static pl.com.tt.flex.model.security.permission.Role.ROLE_TRANSMISSION_SYSTEM_OPERATOR;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pl.com.tt.flex.model.security.permission.Role;
import pl.com.tt.flex.model.service.dto.MinimalDTO;
import pl.com.tt.flex.model.service.dto.chat.ChatDTO;
import pl.com.tt.flex.model.service.dto.chat.ChatRecipientDTO;
import pl.com.tt.flex.server.domain.chat.ChatEntity;
import pl.com.tt.flex.server.domain.chat.message.ChatMessageEntity;
import pl.com.tt.flex.server.domain.fsp.FspEntity;
import pl.com.tt.flex.server.domain.user.UserEntity;
import pl.com.tt.flex.server.repository.AbstractJpaRepository;
import pl.com.tt.flex.server.repository.chat.ChatRepository;
import pl.com.tt.flex.server.repository.fsp.FspRepository;
import pl.com.tt.flex.server.service.chat.mapper.ChatMapper;
import pl.com.tt.flex.server.service.chat.message.mapper.ChatMessageMapper;
import pl.com.tt.flex.server.service.common.AbstractServiceImpl;
import pl.com.tt.flex.server.service.mapper.EntityMapper;
import pl.com.tt.flex.server.service.user.UserService;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ChatServiceImpl extends AbstractServiceImpl<ChatEntity, ChatDTO, Long> implements ChatService {

    public static final List<Role> NO_FSP_CHAT_RECIPIENT_ROLES = List.of(ROLE_TRANSMISSION_SYSTEM_OPERATOR, ROLE_DISTRIBUTION_SYSTEM_OPERATOR, ROLE_MARKET_OPERATOR, ROLE_ADMIN);

    private final ChatRepository chatRepository;
    private final ChatMapper chatMapper;
    private final UserService userService;
    private final FspRepository fspRepository;
    private final ChatMessageMapper chatMessageMapper;

    @Override
    public List<ChatDTO> getAllForLoggedInUser() {
        UserEntity currentUser = userService.getCurrentUserFetchFsp();
        if(NO_FSP_CHAT_RECIPIENT_ROLES.stream().anyMatch(currentUser::hasRole)) {
            List<MinimalDTO<ChatEntity, ChatMessageEntity>> userRoleChats = chatRepository.findAllChatLastMessageByUserRole(currentUser.getRoles());
            return chatMessageMapper.toMinDtoForUser(userRoleChats, currentUser);
        } else {
            Long currentCompanyId = currentUser.getFsp().getId();
            List<MinimalDTO<ChatEntity, ChatMessageEntity>> userCompanyChats = chatRepository.findAllChatLastMessageByCompanyId(currentCompanyId);
            return chatMessageMapper.toMinDtoForCompany(userCompanyChats, currentCompanyId);
        }
    }

    @Override
    public List<ChatRecipientDTO> getAllRecipientsDictionary() {
        UserEntity currentUser = userService.getCurrentUser();
        List<ChatRecipientDTO> dictionary = new ArrayList<>();
        for (Role recipient_role : NO_FSP_CHAT_RECIPIENT_ROLES) {
            if (!currentUser.hasRole(recipient_role)) {
                userService.findUsersByRole(recipient_role).stream().findAny().ifPresent(user -> dictionary.add(new ChatRecipientDTO(user.getId(), user.getUserName(), recipient_role)));
            }
        }
        if (!currentUser.hasRole(ROLE_FLEX_SERVICE_PROVIDER) && !currentUser.hasRole(ROLE_FLEX_SERVICE_PROVIDER_AGGREGATED)) {
            dictionary.addAll(fspRepository.findAllFspMin());
        }
        if (!currentUser.hasRole(ROLE_BALANCING_SERVICE_PROVIDER)) {
            dictionary.addAll(fspRepository.findAllBspMin());
        }
        return dictionary;
    }

    @Override
    public ChatEntity getById(Long id) {
        return chatRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Cannnot find chat with id " + id));
    }

    @Override
    public ChatDTO save(ChatDTO chatDTO) {
        UserEntity currentUser = userService.getCurrentUserFetchFsp();
        if (Objects.nonNull(currentUser.getFsp())) {
            return saveAsCompany(chatDTO, currentUser.getFsp());
        } else {
            return saveAsUser(chatDTO, currentUser);
        }
    }

    public ChatDTO saveAsCompany(ChatDTO chatDTO, FspEntity currentCompany) {
        ChatEntity chatToSave;
        Role recipientRole = chatDTO.getRespondent().getRole();
        if(NO_FSP_CHAT_RECIPIENT_ROLES.contains(recipientRole)) {
            chatToSave = chatMapper.toEntityForInitiatorCompany(chatDTO, currentCompany);
        } else {
            Long recipientCompanyId = chatDTO.getRespondent().getId();
            FspEntity recipientCompany = fspRepository.findById(recipientCompanyId).orElseThrow(() -> new NoSuchElementException("Cannnot find company with id " + recipientCompanyId));
            chatToSave = chatMapper.toEntity(chatDTO, currentCompany, recipientCompany);
        }
        chatToSave = chatRepository.save(chatToSave);
        return chatMapper.toDtoForCompany(chatToSave, currentCompany.getId());
    }

    public ChatDTO saveAsUser(ChatDTO chatDTO, UserEntity currentUser) {
        ChatEntity chatToSave;
        Role recipientRole = chatDTO.getRespondent().getRole();
        if(NO_FSP_CHAT_RECIPIENT_ROLES.contains(recipientRole)) {
            chatToSave = chatMapper.toEntityForInitiatorUser(chatDTO, currentUser);
        } else {
            Long recipientCompanyId = chatDTO.getRespondent().getId();
            FspEntity recipientCompany = fspRepository.findById(recipientCompanyId).orElseThrow(() -> new NoSuchElementException("Cannnot find company with id " + recipientCompanyId));
            chatToSave = chatMapper.toEntity(chatDTO, currentUser, recipientCompany);
        }
        chatToSave = chatRepository.save(chatToSave);
        return chatMapper.toDtoForInitiatorUser(chatToSave, currentUser);
    }

    @Override
    public AbstractJpaRepository<ChatEntity, Long> getRepository() {
        return chatRepository;
    }

    @Override
    public EntityMapper<ChatDTO, ChatEntity> getMapper() {
        return chatMapper;
    }

}
