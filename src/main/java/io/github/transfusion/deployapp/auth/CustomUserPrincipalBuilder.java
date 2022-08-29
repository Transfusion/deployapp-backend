package io.github.transfusion.deployapp.auth;

import io.github.transfusion.deployapp.db.entities.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class CustomUserPrincipalBuilder {
    public static CustomUserPrincipal create(User user) {
        List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));

        return new CustomUserPrincipal(user.getId(), user.getUsername(), user.getEmail(), user.getPassword(), user.getName(), user.getAccountVerified(), authorities);
    }

    public static CustomUserPrincipal create(User user, Map<String, Object> attributes) {
        CustomUserPrincipal userPrincipal = CustomUserPrincipalBuilder.create(user);
        userPrincipal.setAttributes(attributes);
        return userPrincipal;
    }

}
