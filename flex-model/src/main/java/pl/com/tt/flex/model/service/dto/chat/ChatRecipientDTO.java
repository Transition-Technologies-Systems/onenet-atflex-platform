package pl.com.tt.flex.model.service.dto.chat;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.com.tt.flex.model.security.permission.Role;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ChatRecipientDTO implements Serializable {

    private Long id;
    private String name;
    private Role role;

}
