package pl.com.tt.flex.server.refreshView.listener;

import javax.persistence.PostPersist;
import javax.persistence.PostUpdate;

import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import pl.com.tt.flex.server.config.Constants;
import pl.com.tt.flex.server.domain.unit.UnitEntity;
import pl.com.tt.flex.server.service.unit.dto.UnitDTO;
import pl.com.tt.flex.server.service.unit.mapper.UnitMapper;
import pl.com.tt.flex.server.web.rest.websocket.FlexAdminRefreshViewWebsocketResource;
import pl.com.tt.flex.server.web.rest.websocket.FlexUserRefreshViewWebsocketResource;

@Slf4j
@Component
public class UnitListener {

    private final UnitMapper unitMapper;
    private final FlexAdminRefreshViewWebsocketResource adminRefreshViewWebsocketResource;
    private final FlexUserRefreshViewWebsocketResource userRefreshViewWebsocketResource;

    public UnitListener(UnitMapper unitMapper, @Lazy FlexAdminRefreshViewWebsocketResource adminRefreshViewWebsocketResource,
                        @Lazy FlexUserRefreshViewWebsocketResource userRefreshViewWebsocketResource) {
        this.unitMapper = unitMapper;
        this.adminRefreshViewWebsocketResource = adminRefreshViewWebsocketResource;
        this.userRefreshViewWebsocketResource = userRefreshViewWebsocketResource;
    }

    @Async
    @PostPersist
    public void onPostPersist(UnitEntity unitEntity) {
        log.info("onPostPersist() START - Send WebSocket messages with updated unit [unitId={}]", unitEntity.getId());
        UnitDTO unitDTO = unitMapper.toDto(unitEntity);
        postUnitToAdminApp(unitDTO);
        postUnitToUserApp(unitDTO);
        log.info("onPostPersist() END - Send WebSocket messages with updated unit [unitId={}]", unitEntity.getId());
    }

    @Async
    @PostUpdate
    public void onPostUpdate(UnitEntity unitEntity) {
        log.info("onPostUpdate() START - Send WebSocket messages with updated unit [unitId={}]", unitEntity.getId());
        UnitDTO unitDTO = unitMapper.toDto(unitEntity);
        postUnitToAdminApp(unitDTO);
        postUnitToUserApp(unitDTO);
        log.info("onPostUpdate() END - Send WebSocket messages with updated unit [unitId={}]", unitEntity.getId());
    }

    private void postUnitToAdminApp(UnitDTO offerDTO) {
        try {
            adminRefreshViewWebsocketResource.postModifiedUnit(offerDTO);
        } catch (Exception e) {
            log.debug("postUnitToAdminApp() Error while post unit {} to {} app\n{}", offerDTO.toString(), Constants.FLEX_ADMIN_APP_NAME, e.getMessage());
        }
    }

    private void postUnitToUserApp(UnitDTO offerDTO) {
        try {
            userRefreshViewWebsocketResource.postModifiedUnit(offerDTO);
        } catch (Exception e) {
            log.debug("postUnitToUserApp() Error while post unit {} to {} app\n{}", offerDTO.toString(), Constants.FLEX_USER_APP_NAME, e.getMessage());
        }
    }

}
