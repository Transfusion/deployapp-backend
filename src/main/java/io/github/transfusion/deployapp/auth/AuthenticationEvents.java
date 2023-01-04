package io.github.transfusion.deployapp.auth;

import io.github.transfusion.deployapp.services.StorageCredentialsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.authentication.event.InteractiveAuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * https://github.com/spring-projects/spring-security/issues/3857
 */
@Component
public class AuthenticationEvents {

    @Autowired
    private StorageCredentialsService storageCredentialsService;

    /**
     * @param event
     */
    @EventListener
    public void onAuthenticationSuccess(AuthenticationSuccessEvent event) {
        UUID userId = ((CustomUserPrincipal) event.getAuthentication().getPrincipal()).getId();
        storageCredentialsService.migrateAnonymousData(userId);
    }

//    session is already cleared at this point; see OAuth2AuthenticationSuccessHandler
//    @EventListener
//    public void onInteractiveAuthenticationSuccess(InteractiveAuthenticationSuccessEvent event) {
//        UUID userId = ((CustomUserPrincipal) event.getAuthentication().getPrincipal()).getId();
//        storageCredentialsService.migrateAnonymousData(userId);
//    }

//    @EventListener
//    public void onFailure(AbstractAuthenticationFailureEvent failures) {
//        // ...
//    }
}