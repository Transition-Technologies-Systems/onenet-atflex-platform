package pl.com.tt.flex.admin.web.rest;

import org.springframework.security.access.prepost.PreAuthorize;
import pl.com.tt.flex.admin.web.rest.vm.RouteVM;

import java.util.ArrayList;
import java.util.List;

import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.netflix.zuul.filters.Route;
import org.springframework.cloud.netflix.zuul.filters.RouteLocator;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import static pl.com.tt.flex.model.security.permission.Authority.FLEX_SYS_ADMINISTRATE;

/**
 * REST controller for managing Gateway configuration.
 */
@RestController
@RequestMapping("/api/gateway")
public class GatewayResource {

    private final RouteLocator routeLocator;

    private final DiscoveryClient discoveryClient;


    public GatewayResource(RouteLocator routeLocator, DiscoveryClient discoveryClient) {
        this.routeLocator = routeLocator;
        this.discoveryClient = discoveryClient;
    }

    /**
     * {@code GET  /routes} : get the active routes.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the list of routes.
     */
    @GetMapping("/routes")
    @PreAuthorize("hasAuthority(\"" + FLEX_SYS_ADMINISTRATE + "\")")
    public ResponseEntity<List<RouteVM>> activeRoutes() {
        List<Route> routes = routeLocator.getRoutes();
        List<RouteVM> routeVMs = new ArrayList<>();
        routes.forEach(route -> {
            RouteVM routeVM = new RouteVM();
            routeVM.setPath(route.getFullPath());
            routeVM.setServiceId(route.getId());
            routeVM.setServiceInstances(discoveryClient.getInstances(route.getLocation()));
            routeVMs.add(routeVM);
        });
        return ResponseEntity.ok(routeVMs);
    }
}
