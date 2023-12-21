package pl.com.tt.flex.server.repository.chat.message;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import pl.com.tt.flex.model.security.permission.Role;
import pl.com.tt.flex.server.domain.chat.message.ChatMessageEntity;
import pl.com.tt.flex.server.repository.AbstractJpaRepository;

@Repository
public interface ChatMessageRepository extends AbstractJpaRepository<ChatMessageEntity, Long> {

    List<ChatMessageEntity> findAllByChatIdOrderByCreatedDateAsc(Long chatId);

    @Query("SELECT COUNT(m) FROM ChatMessageEntity m JOIN m.chat c JOIN m.sendingUser su LEFT JOIN su.fsp fsp WHERE m.read = false AND "
            + "(fsp.id != :companyId OR fsp IS NULL) AND (c.recipientCompany.id = :companyId OR c.initiatorCompany.id = :companyId)")
    Long countUnreadByCompanyId(@Param(value = "companyId") Long companyId);

    @Query("SELECT COUNT(m) FROM ChatMessageEntity m JOIN m.chat c JOIN m.sendingUser su JOIN su.roles r WHERE m.read = false AND r != :role " +
        "AND (c.recipientType IN :role OR c.initiatorType IN :role)")
    Long countUnreadByRole(@Param(value = "role") Role role);

    @Query("SELECT m FROM ChatMessageEntity m JOIN m.chat c JOIN m.sendingUser su LEFT JOIN su.fsp fsp WHERE c.id = :chatId AND m.read = false AND " +
        "(fsp.id != :companyId OR fsp IS NULL) AND (c.recipientCompany.id = :companyId OR c.initiatorCompany.id = :companyId)")
    List<ChatMessageEntity> findUnreadByChatIdAndCompanyId(@Param(value = "chatId") Long chatId, @Param(value = "companyId") Long companyId);

    @Query("SELECT m FROM ChatMessageEntity m JOIN m.chat c JOIN m.sendingUser su JOIN su.roles r WHERE c.id = :chatId AND m.read = false AND " +
        "r != :role AND (c.recipientType = :role OR c.initiatorType = :role)")
    List<ChatMessageEntity> findUnreadByChatIdAndRole(@Param(value = "chatId") Long chatId, @Param(value = "role") Role role);

    @Query("SELECT m FROM ChatMessageEntity m JOIN FETCH m.sendingUser u LEFT JOIN FETCH u.fsp WHERE m.id = :messageId")
    ChatMessageEntity findChatMessageFetchSendingUserFsp(@Param("messageId") Long messageId);
}
