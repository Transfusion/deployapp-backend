package io.github.transfusion.deployapp.services;

import io.github.transfusion.deployapp.auth.CustomUserPrincipal;
import io.github.transfusion.deployapp.db.entities.User;
import io.github.transfusion.deployapp.db.repositories.AuthProviderRepository;
import io.github.transfusion.deployapp.db.repositories.UserRepository;
import io.github.transfusion.deployapp.dto.request.PatchProfileRequest;
import io.github.transfusion.deployapp.exceptions.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class AccountService {

    @Autowired
    private UserRepository userRepository;

    public User getUserById(UUID id) {
        Optional<User> _user = userRepository.findById(id);
        if (_user.isEmpty()) throw new ResourceNotFoundException("User", "id", id);
        return _user.get();
    }

    /**
     * @param request {@link PatchProfileRequest}
     * @return The {@link User} object, after it has been updated and saved
     */
    public User patchUserProfile(PatchProfileRequest request) {
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authentication = context.getAuthentication();

        CustomUserPrincipal principal = (CustomUserPrincipal) authentication.getPrincipal();

        UUID id = principal.getId();
        User user = getUserById(id);

        if (request.getUsername() != null) {
            if (request.getUsername().isEmpty()) user.setUsername(null);
            else user.setUsername(request.getUsername());
        }

        if (request.getName() != null) {
            if (request.getName().isEmpty()) user.setName(null);
            else user.setName(request.getName());
        }

        return userRepository.save(user);
    }

    @Autowired
    private AuthProviderRepository authProviderRepository;

    @Transactional
    public long deleteConnectedAccount(UUID userId, String providerName) {
        return authProviderRepository.deleteByUserIdAndId_ProviderName(userId, providerName);
    }
}
