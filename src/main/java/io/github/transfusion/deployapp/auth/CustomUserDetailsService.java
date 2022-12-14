package io.github.transfusion.deployapp.auth;

import io.github.transfusion.deployapp.db.entities.User;
import io.github.transfusion.deployapp.db.repositories.UserRepository;
import io.github.transfusion.deployapp.exceptions.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    @Autowired
    UserRepository userRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsernameIgnoreCaseOrEmailIgnoreCase(username, username).orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));


        return CustomUserPrincipalBuilder.create(user);
    }


    @Transactional
    public UserDetails loadUserById(UUID id) {
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        return CustomUserPrincipalBuilder.create(user);
    }

    /**
     * Reloads the {@link CustomUserPrincipal} from the {@link User} entity in the database.
     */
    public void reloadUserPrincipal() {
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authentication = context.getAuthentication();

        if (authentication == null || authentication instanceof AnonymousAuthenticationToken) return;

        UUID id = ((CustomUserPrincipal) authentication.getPrincipal()).getId();
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        CustomUserPrincipal newUserPrincipal = CustomUserPrincipalBuilder.create(user);

        // https://stackoverflow.com/questions/60005431/how-to-update-data-on-principal-object-on-spring-boot
        // https://stackoverflow.com/questions/29434209/change-username-in-spring-security-when-logged-in
        if (authentication instanceof OAuth2AuthenticationToken) {
            context.setAuthentication(new OAuth2AuthenticationToken(newUserPrincipal,
                    authentication.getAuthorities(),
                    ((OAuth2AuthenticationToken) authentication).getAuthorizedClientRegistrationId()
            ));
            SecurityContextHolder.setContext(context);
        } else if (authentication instanceof UsernamePasswordAuthenticationToken) {
            context.setAuthentication(new UsernamePasswordAuthenticationToken(
                    newUserPrincipal, authentication.getCredentials(),
                    authentication.getAuthorities()
            ));
            SecurityContextHolder.setContext(context);
        } else {
            throw new RuntimeException(String.format("Unknown Authentication method! %s", authentication.getClass().getName()));
        }
    }
}
