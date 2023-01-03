package io.github.transfusion.deployapp.auth;

import io.github.transfusion.deployapp.db.entities.StorageCredential;
import io.github.transfusion.deployapp.db.repositories.StorageCredentialRepository;
import io.github.transfusion.deployapp.exceptions.ResourceNotFoundException;
import io.github.transfusion.deployapp.session.SessionData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Optional;
import java.util.UUID;

@Component
public class CustomPermissionEvaluator implements PermissionEvaluator {

    @Autowired
    private StorageCredentialRepository storageCredentialRepository;

    @Autowired
    private SessionData sessionData;

    private boolean checkStorageCredentialEdit(Authentication authentication, UUID id) {
        Optional<StorageCredential> _credential = storageCredentialRepository.findById(id);
        if (_credential.isEmpty()) throw new ResourceNotFoundException("StorageCredential", "id", id);
        StorageCredential credential = _credential.get();

        if (authentication instanceof AnonymousAuthenticationToken)
            // can't join organizations anonymously...
            return sessionData.getAnonymousCredentials().contains(id);

        // TODO: handle organizations!
        return credential.getUser().getId().equals(((CustomUserPrincipal) authentication.getPrincipal()).getId());
    }

    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        if (permission.equals("STORECRED_EDIT")) {
            return checkStorageCredentialEdit(authentication, (UUID) targetDomainObject);
        }
        return false;
    }

    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
        return false;
    }
}
