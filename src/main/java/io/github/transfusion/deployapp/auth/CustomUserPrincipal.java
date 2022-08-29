package io.github.transfusion.deployapp.auth;

//import io.github.transfusion.deployapp.db.entities.User;
import org.springframework.security.core.GrantedAuthority;
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.*;

public class CustomUserPrincipal implements OAuth2User, UserDetails {
    private final UUID id;

    private final boolean hasUsername;
    private final String username;
    private final String email;
    private final String password;

    private final String name;
    private final boolean enabled;
    private final Collection<? extends GrantedAuthority> authorities;
    private Map<String, Object> attributes;

    public CustomUserPrincipal(UUID id, String username, String email, String password, String name, Boolean accountVerified, Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.hasUsername = username != null;
        this.username = username == null ? email : username;
        this.email = email;
        this.password = password;
        this.name = name;
        this.enabled = accountVerified;
        this.authorities = authorities;
    }

//    public static CustomUserPrincipal create(User user) {
//        List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
//
//        return new CustomUserPrincipal(user.getId(), user.getUsername(), user.getEmail(), user.getPassword(), user.getName(), user.getAccountVerified(), authorities);
//    }
//
//    public static CustomUserPrincipal create(User user, Map<String, Object> attributes) {
//        CustomUserPrincipal userPrincipal = CustomUserPrincipal.create(user);
//        userPrincipal.setAttributes(attributes);
//        return userPrincipal;
//    }

    public UUID getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    @Override
    public String getName() {
        return name;
//        return String.format("%s-%s-%s", String.valueOf(id), email, name);
    }

    public boolean hasUsername() {
        return hasUsername;
    }
}
