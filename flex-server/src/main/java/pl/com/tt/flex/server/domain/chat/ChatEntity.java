package pl.com.tt.flex.server.domain.chat;

import java.io.Serializable;
import java.util.Objects;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.id.enhanced.SequenceStyleGenerator;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.com.tt.flex.model.security.permission.Role;
import pl.com.tt.flex.server.domain.audit.AbstractAuditingEntity;
import pl.com.tt.flex.server.domain.chat.message.ChatMessageEntity;
import pl.com.tt.flex.server.domain.fsp.FspEntity;
import pl.com.tt.flex.server.refreshView.listener.ChatListener;

import static pl.com.tt.flex.server.service.chat.ChatServiceImpl.NO_FSP_CHAT_RECIPIENT_ROLES;

@Getter
@Setter
@Entity
@Table(name = "chat")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@EntityListeners(ChatListener.class)
@GenericGenerator(
    name = "chat_id_generator",
    strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
    parameters = {
        @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "chat_seq"),
        @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.INITIAL_PARAM, value = "1"),
        @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.INCREMENT_PARAM, value = "1")
    }
)
@NoArgsConstructor
public class ChatEntity extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "chat_id_generator")
    private Long id;

    @NotNull
    @Column(name = "recipient_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private Role recipientType;

    @ManyToOne(fetch = FetchType.LAZY)
    private FspEntity recipientCompany;

    @NotNull
    @Column(name = "initiator_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private Role initiatorType;

    @ManyToOne(fetch = FetchType.LAZY)
    private FspEntity initiatorCompany;

    @OneToMany(mappedBy = "chat", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<ChatMessageEntity> messages;

    @AssertTrue(message = "RecipientCompany must be set unless chat recipient is of type TSO, DSO, MO or TA")
    private boolean isRecipientFieldSet() {
        return NO_FSP_CHAT_RECIPIENT_ROLES.contains(recipientType) == Objects.isNull(recipientCompany);
    }

    @AssertTrue(message = "InitiatorCompany must be set unless chat initiator is of type TSO, DSO, MO or TA")
    private boolean isInitiatorFieldSet() {
        return NO_FSP_CHAT_RECIPIENT_ROLES.contains(initiatorType) == Objects.isNull(initiatorCompany);
    }

}
