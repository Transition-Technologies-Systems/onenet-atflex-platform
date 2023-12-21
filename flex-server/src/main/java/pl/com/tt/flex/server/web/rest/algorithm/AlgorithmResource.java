package pl.com.tt.flex.server.web.rest.algorithm;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/algorithm")
public class AlgorithmResource {

    protected static final String ENTITY_NAME = "algorithm";

    @Value("${jhipster.clientApp.name}")
    protected String applicationName;

    public AlgorithmResource() {
    }
}
