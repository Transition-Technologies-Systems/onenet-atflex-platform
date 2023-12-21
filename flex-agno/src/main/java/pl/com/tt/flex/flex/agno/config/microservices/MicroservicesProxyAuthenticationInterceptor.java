package pl.com.tt.flex.flex.agno.config.microservices;

import com.google.common.collect.Lists;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import pl.com.tt.flex.flex.agno.security.jwt.TokenProvider;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;


@Component
@Slf4j
public class MicroservicesProxyAuthenticationInterceptor implements RequestInterceptor {

    private static final String AUTHORIZATION = "Authorization";
    private static final String BEARER = "Bearer ";
    private static final String SYSTEM_ROLE = "ROLE_SYSTEM";
    private static final String USERNAME = "system";
    private static final String PASSWORD = "system";

    @Autowired
    private TokenProvider tokenProvider;

    @Override
    public void apply(RequestTemplate requestTemplate) {
        if (requestTemplateHasToken(requestTemplate)) {
            requestTemplate.removeHeader(AUTHORIZATION);
        }
        log.info("apply() Generate new Barer Token to authenticate with Microservices");
        requestTemplate.header(AUTHORIZATION, generateToken());
    }

    private boolean requestTemplateHasToken(RequestTemplate requestTemplate) {
        return isNotEmpty(requestTemplate.headers().get(AUTHORIZATION));
    }

    private String generateToken() {
        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(USERNAME, PASSWORD,
            Lists.newArrayList(new SimpleGrantedAuthority(SYSTEM_ROLE)));
        return BEARER + tokenProvider.createToken(usernamePasswordAuthenticationToken);
    }

}
