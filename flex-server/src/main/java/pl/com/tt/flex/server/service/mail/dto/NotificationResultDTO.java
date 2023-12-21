package pl.com.tt.flex.server.service.mail.dto;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResultDTO implements Serializable {
    private String notifiedEmailAdress;
}
