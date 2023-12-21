package pl.com.tt.flex.server.domain.user;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.id.enhanced.SequenceStyleGenerator;
import pl.com.tt.flex.server.domain.EntityInterface;

import javax.persistence.*;
import java.time.Instant;

@Data
@Entity
@Table(name = "users_online")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@GenericGenerator(name = "users_online_id_generator", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator", parameters = {
    @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "users_online_seq"),
    @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.INITIAL_PARAM, value = "1"),
    @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.INCREMENT_PARAM, value = "1")})
@ToString
@EqualsAndHashCode
public class UserOnlineEntity implements EntityInterface<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "users_online_id_generator")
    private Long id;

    @ManyToOne
    private UserEntity user;

    @Basic(fetch = FetchType.LAZY)
    @Column(name = "token", nullable = false, columnDefinition = "clob")
    private String token;

    @Column(name = "ip_address", nullable = false)
    private String addressId;

    @Column(name = "created_date", nullable = false)
    private Instant createdDate;

}
