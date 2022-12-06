package io.github.transfusion.deployapp.session;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Component
@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS, value = WebApplicationContext.SCOPE_SESSION)
public class SessionData implements Serializable {
    public Set<UUID> getAnonymousCredentials() {
        return anonymousCredentials;
    }

    public Set<UUID> getAnonymousAppBinaries() {
        return anonymousAppBinaries;
    }

    private final Set<UUID> anonymousCredentials = new HashSet<>();
    private final Set<UUID> anonymousAppBinaries = new HashSet<>();

}
