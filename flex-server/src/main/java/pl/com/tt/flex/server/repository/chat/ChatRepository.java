package pl.com.tt.flex.server.repository.chat;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import pl.com.tt.flex.model.security.permission.Role;
import pl.com.tt.flex.model.service.dto.MinimalDTO;
import pl.com.tt.flex.server.domain.chat.ChatEntity;
import pl.com.tt.flex.server.domain.chat.message.ChatMessageEntity;
import pl.com.tt.flex.server.repository.AbstractJpaRepository;

@Repository
public interface ChatRepository extends AbstractJpaRepository<ChatEntity, Long> {

    @Query("SELECT NEW pl.com.tt.flex.model.service.dto.MinimalDTO(c, m) FROM ChatEntity c LEFT JOIN ChatMessageEntity m ON m.chat.id = c.id WHERE " +
            "(c.recipientCompany.id = :companyId OR c.initiatorCompany.id = :companyId) AND " +
            "(m IS NULL OR m.createdDate = (SELECT MAX(m2.createdDate) FROM ChatMessageEntity m2 WHERE m2.chat.id = c.id))" +
            "ORDER BY m.createdDate DESC")
    List<MinimalDTO<ChatEntity, ChatMessageEntity>> findAllChatLastMessageByCompanyId(@Param(value = "companyId") Long companyId);

    @Query("SELECT NEW pl.com.tt.flex.model.service.dto.MinimalDTO(c, m) FROM ChatEntity c LEFT JOIN ChatMessageEntity m ON m.chat.id = c.id WHERE " +
            "(c.initiatorType IN :roles OR c.recipientType IN :roles) AND " +
            "(m IS NULL OR m.createdDate = (SELECT MAX(m2.createdDate) FROM ChatMessageEntity m2 WHERE m2.chat.id = c.id)) " +
            "ORDER BY m.createdDate DESC")
    List<MinimalDTO<ChatEntity, ChatMessageEntity>> findAllChatLastMessageByUserRole(@Param(value = "roles") Set<Role> roles);

    @Query("SELECT c FROM ChatEntity c LEFT JOIN FETCH c.recipientCompany LEFT JOIN FETCH c.initiatorCompany WHERE c.id = :id")
    Optional<ChatEntity> findByIdFetchAll(@Param(value = "id") Long id);

    @Query("SELECT CASE WHEN count(*) > 0 THEN true ELSE false END FROM ChatEntity c WHERE " +
            "(c.recipientCompany.id = :companyAId AND c.initiatorCompany.id = :companyBId) OR " +
            "(c.recipientCompany.id = :companyBId AND c.initiatorCompany.id = :companyAId)")
    boolean existsByParticipatingCompanyIds(@Param(value = "companyAId") Long companyAId, @Param(value = "companyBId") Long companyBId);

    @Query("SELECT CASE WHEN count(*) > 0 THEN true ELSE false END FROM ChatEntity c WHERE " +
            "(c.recipientType IN :roles AND c.initiatorCompany.id = :companyId) OR " +
            "(c.recipientCompany.id = :companyId AND c.initiatorType IN :roles)")
    boolean existsByParticipatingCompanyIdAndRole(@Param(value = "companyId") Long companyAId, @Param(value = "roles") Set<Role> roles);

    @Query("SELECT CASE WHEN count(*) > 0 THEN true ELSE false END FROM ChatEntity c WHERE " +
            "(c.recipientType IN :rolesA AND c.initiatorType IN :rolesB) OR " +
            "(c.recipientType IN :rolesB AND c.initiatorType IN :rolesA)")
    boolean existsByParticipatingRoles(@Param(value = "rolesA") Set<Role> rolesA, @Param(value = "rolesB") Set<Role> rolesB);
}
