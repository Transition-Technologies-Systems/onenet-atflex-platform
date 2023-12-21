package pl.com.tt.flex.server.service.dictionary;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import pl.com.tt.flex.model.service.dto.dictionary.DictionaryUpdateDTO;
import pl.com.tt.flex.server.config.Constants;
import pl.com.tt.flex.server.domain.AbstractDictionaryEntity;
import pl.com.tt.flex.server.web.rest.websocket.FlexAdminWebsocketResource;
import pl.com.tt.flex.server.web.rest.websocket.FlexUserWebsocketResource;

import javax.persistence.PostPersist;
import javax.persistence.PostUpdate;

@Component
@Slf4j
public class DictionaryListener {

    private final FlexAdminWebsocketResource flexAdminWebsocketResource;
    private final FlexUserWebsocketResource flexUserWebsocketResource;

    public DictionaryListener(@Lazy FlexAdminWebsocketResource flexAdminWebsocketResource, @Lazy FlexUserWebsocketResource flexUserWebsocketResource) {
        this.flexAdminWebsocketResource = flexAdminWebsocketResource;
        this.flexUserWebsocketResource = flexUserWebsocketResource;
    }

    @PostPersist
    @Async
    public void onPostPersist(AbstractDictionaryEntity entity) {
        log.info("onPostPersist() START - Send WebSocket message informing that the dictionary has been updated - dictionary type: {}", entity.getDictionaryType());
        DictionaryUpdateDTO dictionaryUpdateDTO = new DictionaryUpdateDTO(entity.getDictionaryType());
        postDictionaryToAdminApp(dictionaryUpdateDTO);
        postDictionaryToUserApp(dictionaryUpdateDTO);
        log.info("onPostPersist() END - Send WebSocket message informing that the dictionary has been updated - dictionary type: {}", entity.getDictionaryType());
    }

    @PostUpdate
    @Async
    public void onPostUpdate(AbstractDictionaryEntity entity) {
        log.info("onPostUpdate() START - Send WebSocket message informing that the dictionary has been updated - dictionary type: {}", entity.getDictionaryType());
        DictionaryUpdateDTO dictionaryUpdateDTO = new DictionaryUpdateDTO(entity.getDictionaryType());
        postDictionaryToAdminApp(dictionaryUpdateDTO);
        postDictionaryToUserApp(dictionaryUpdateDTO);
        log.info("onPostUpdate() END - Send WebSocket message informing that the dictionary has been updated - dictionary type: {}", entity.getDictionaryType());
    }

    private void postDictionaryToAdminApp(DictionaryUpdateDTO dictionaryUpdateDTO) {
        try {
            flexAdminWebsocketResource.postDictionaryUpdate(dictionaryUpdateDTO);
        } catch (Exception e) {
            log.debug("postDictionaryToAdminApp() Error while post dictionary update {} to {} app\n{}", dictionaryUpdateDTO.toString(), Constants.FLEX_ADMIN_APP_NAME, e.getMessage());
        }
    }

    private void postDictionaryToUserApp(DictionaryUpdateDTO dictionaryUpdateDTO) {
        try {
            flexUserWebsocketResource.postDictionaryUpdate(dictionaryUpdateDTO);
        } catch (Exception e) {
            log.debug("postDictionaryToUserApp() Error while post dictionary update {} to {} app\n{}", dictionaryUpdateDTO.toString(), Constants.FLEX_USER_APP_NAME, e.getMessage());
        }
    }
}
