package io.github.transfusion.deployapp.auth;

import io.github.transfusion.deployapp.auth.oauth2_client.OAuth2UserInfo;
import io.github.transfusion.deployapp.auth.oauth2_client.OAuth2UserInfoFactory;
import io.github.transfusion.deployapp.db.entities.AuthProvider;
import io.github.transfusion.deployapp.db.entities.AuthProviderId;
import io.github.transfusion.deployapp.db.entities.User;
import io.github.transfusion.deployapp.db.repositories.AuthProviderRepository;
import io.github.transfusion.deployapp.db.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.Optional;

/**
 * Refer to "How to Add a Local User Database"
 * <a href="https://spring.io/guides/tutorials/spring-boot-oauth2/">https://spring.io/guides/tutorials/spring-boot-oauth2/</a>
 *
 * <a href="https://stackoverflow.com/questions/49715769/why-is-my-oauth2-config-not-using-my-custom-userservice">https://stackoverflow.com/questions/49715769/why-is-my-oauth2-config-not-using-my-custom-userservice</a>
 */
@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    Logger logger = LoggerFactory.getLogger(CustomOAuth2UserService.class);

    public CustomOAuth2UserService() {
        LoggerFactory.getLogger(CustomOAuth2UserService.class).info("initializing CustomOAuth2UserService");
    }

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthProviderRepository authProviderRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest oAuth2UserRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(oAuth2UserRequest);

        try {
            return processOAuth2User(oAuth2UserRequest, oAuth2User);
        } catch (AuthenticationException ex) {
            throw ex;
        } catch (Exception ex) {
            // Throwing an instance of AuthenticationException will trigger the OAuth2AuthenticationFailureHandler
            throw new InternalAuthenticationServiceException(ex.getMessage(), ex.getCause());
        }
    }

    private OAuth2User processOAuth2User(OAuth2UserRequest oAuth2UserRequest, OAuth2User oAuth2User) {
        OAuth2UserInfo oAuth2UserInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(oAuth2UserRequest.getClientRegistration().getRegistrationId(), oAuth2User.getAttributes());
        if (StringUtils.isEmpty(oAuth2UserInfo.getEmail())) {
            throw new OAuth2AuthenticationProcessingException("Email not found from OAuth2 provider");
        }

        Optional<AuthProvider> _authProvider = authProviderRepository.findByProviderKey(oAuth2UserInfo.getId());

        User user;
        if (_authProvider.isEmpty()) {
            // create new user
            user = registerNewUser(oAuth2UserRequest, oAuth2UserInfo);
        } else {
            AuthProvider authProvider = _authProvider.get();
            user = userRepository.findById(authProvider.getId().getUserId()).get();
        }
        // attempt to create user if not present
//        Optional<User> userOptional = userRepository.findByEmail(oAuth2UserInfo.getEmail());
//        if (userOptional.isPresent()) {
//            User user = userOptional.get();
//            if (!user.)
//        }

        CustomUserPrincipal g = CustomUserPrincipal.create(user, oAuth2User.getAttributes());
        return g;
    }

    private User registerNewUser(OAuth2UserRequest oAuth2UserRequest, OAuth2UserInfo oAuth2UserInfo) {

        User user = new User();
        user.setEmail(oAuth2UserInfo.getEmail());
        user.setAccountVerified(true);
        user.setCreatedOn(Instant.now());
        user.setName(oAuth2UserInfo.getName());

        userRepository.save(user);

        AuthProvider authProvider = new AuthProvider();
        AuthProviderId authProviderId = new AuthProviderId();
        authProviderId.setUserId(user.getId());
        authProviderId.setProviderName(oAuth2UserRequest.getClientRegistration().getRegistrationId());

        authProvider.setUser(user);
        authProvider.setId(authProviderId);
        authProvider.setProviderKey(oAuth2UserInfo.getId());

        authProviderRepository.save(authProvider);
        return userRepository.save(user);
    }

}
