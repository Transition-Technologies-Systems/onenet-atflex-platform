package pl.com.tt.flex.server.service.notification.emitter;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pl.com.tt.flex.server.config.Constants;
import pl.com.tt.flex.server.domain.enumeration.NotificationEvent;
import pl.com.tt.flex.server.service.notification.dto.SocketEventDTO;
import pl.com.tt.flex.server.web.rest.websocket.FlexAdminWebsocketResource;
import pl.com.tt.flex.server.web.rest.websocket.FlexUserWebsocketResource;

@Slf4j
@Component
@RequiredArgsConstructor
public class SocketEventEmitter {

    private final FlexAdminWebsocketResource flexAdminWebsocketResource;
    private final FlexUserWebsocketResource flexUserWebsocketResource;

    public void postEvent(SocketEventDTO socketEventDTO, String login) {
        log.debug("WebSocket -> Prepare message for user {} to send with event {}", login, socketEventDTO.toString());
        NotificationEvent event = socketEventDTO.getEvent();
        if (event.isNotifyAdmin()) {
            postEventToAdminApp(socketEventDTO, login);
        }
        if (event.isNotifyUser()) {
            postEventToUserApp(socketEventDTO, login);
        }
    }

    private void postEventToAdminApp(SocketEventDTO socketEventDTO, String login) {
        try {
            log.debug("Post event to {} app", Constants.FLEX_ADMIN_APP_NAME);
            ObjectMapper objectMapper = new ObjectMapper();
            String socketEventDTOString = objectMapper.writeValueAsString(socketEventDTO);
            flexAdminWebsocketResource.postNewEvent(login, socketEventDTOString);
        } catch (Exception e) {
            log.error("Error while post event {} to {} app to user {}\n{}", socketEventDTO.toString(), Constants.FLEX_ADMIN_APP_NAME, login, e.getMessage());
            e.printStackTrace();
        }
    }

    private void postEventToUserApp(SocketEventDTO socketEventDTO, String login) {
        try {
            log.debug("Post event to {} app", Constants.FLEX_USER_APP_NAME);
            ObjectMapper objectMapper = new ObjectMapper();
            String socketEventDTOString = objectMapper.writeValueAsString(socketEventDTO);
            flexUserWebsocketResource.postNewEvent(login, socketEventDTOString);
        } catch (Exception e) {
            log.error("Error while post event {} to {} app to user {}\n{}", socketEventDTO.toString(), Constants.FLEX_ADMIN_APP_NAME, login, e.getMessage());
            e.printStackTrace();
        }
    }

}
