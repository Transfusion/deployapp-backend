package io.github.transfusion.deployapp.auth;

import io.github.transfusion.deployapp.services.MigrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * https://github.com/spring-projects/spring-security/issues/3857
 */
@Component
public class AuthenticationEvents {

    @Autowired
    private MigrationService migrationService;

    /**
     * @param event
     */
    @EventListener
    public void onAuthenticationSuccess(AuthenticationSuccessEvent event) {
        UUID userId = ((CustomUserPrincipal) event.getAuthentication().getPrincipal()).getId();
        migrationService.migrateAnonymousData(userId);
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