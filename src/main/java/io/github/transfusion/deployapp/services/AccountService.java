package io.github.transfusion.deployapp.services;

import io.github.transfusion.deployapp.auth.CustomUserPrincipal;
import io.github.transfusion.deployapp.db.entities.User;
import io.github.transfusion.deployapp.db.entities.VerificationToken;
import io.github.transfusion.deployapp.db.repositories.AuthProviderRepository;
import io.github.transfusion.deployapp.db.repositories.UserRepository;
import io.github.transfusion.deployapp.db.repositories.VerificationTokenRepository;
import io.github.transfusion.deployapp.dto.internal.SendVerificationEmailEvent;
import io.github.transfusion.deployapp.dto.request.PatchProfileRequest;
import io.github.transfusion.deployapp.dto.response.LoginResultDTO;
import io.github.transfusion.deployapp.dto.response.RegisterResultDTO;
import io.github.transfusion.deployapp.dto.response.ResendVerificationResultDTO;
import io.github.transfusion.deployapp.exceptions.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
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

        if (request.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        return userRepository.save(user);
    }

    @Autowired
    private AuthProviderRepository authProviderRepository;

    @Transactional
    public long deleteConnectedAccount(UUID userId, String providerName) {
        return authProviderRepository.deleteByUserIdAndId_ProviderName(userId, providerName);
    }

    // Delegating email sending to the NotificationService is the better separation of concerns.

    @Autowired
    private VerificationTokenRepository verificationTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    ApplicationEventPublisher eventPublisher;

    @Value("${custom_app.token_validity_duration}")
    private Integer tokenValidityDuration;

    public RegisterResultDTO registerByEmail(String email, String password, String redirectBaseUrl) {
        Optional<User> _user = userRepository.findByEmail(email);
        if (_user.isPresent()) {
            // if account is NOT verified that means account is pending verification
            return new RegisterResultDTO(false, true, !_user.get().getAccountVerified());
        }

        // encode the password and register!
        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setAccountVerified(false);
        user.setCreatedOn(Instant.now());
        userRepository.save(user);

        // generate and save a random token
        UUID randomUUID = UUID.randomUUID();
        VerificationToken token = new VerificationToken();
        token.setId(randomUUID);
        token.setUserId(user.getId());
        token.setCreatedOn(Instant.now());
//        token.setEmail(user.getEmail());
        token.setExpiry(Instant.now().plusSeconds(tokenValidityDuration));
        verificationTokenRepository.save(token);

        // fire off the registration email
        eventPublisher.publishEvent(new SendVerificationEmailEvent(user.getId(), email, redirectBaseUrl, token.getId().toString()));
        return new RegisterResultDTO(true, false, false);
    }

    public ResendVerificationResultDTO resendVerification(String email, String newEmail, String redirectBaseUrl) {
        // check if user exists and is in pending verification status.
        Optional<User> _user = userRepository.findByEmail(email);
        if (_user.isEmpty())
            return new ResendVerificationResultDTO(false, false); // can't resend a verification if one never existed

        User user = _user.get();
        if (user.getAccountVerified()) return new ResendVerificationResultDTO(false, false); // already verified!;

        boolean emailChanged = newEmail != null && !newEmail.equals(email);
        if (emailChanged) {
            user.setEmail(newEmail);
            userRepository.save(user);
        }

        Optional<VerificationToken> _token = verificationTokenRepository.findByUserId(user.getId());
        if (_token.isEmpty()) {
            // edge case which should never be encountered: no record of having sent verif but user is already present & unverified

            // generate and save a random token
            UUID randomUUID = UUID.randomUUID();
            VerificationToken token = new VerificationToken();
            token.setId(randomUUID);
            token.setUserId(user.getId());
            token.setCreatedOn(Instant.now());
//            token.setEmail(user.getEmail());
            token.setExpiry(Instant.now().plusSeconds(tokenValidityDuration));
            verificationTokenRepository.save(token);

            // fire off the registration email
            eventPublisher.publishEvent(new SendVerificationEmailEvent(user.getId(),
                    user.getEmail(), redirectBaseUrl, token.getId().toString()));
        } else {
            // token is not empty, check whether 10 mins have passed
            VerificationToken token = _token.get();
            if (token.getCreatedOn().until(Instant.now(), ChronoUnit.MINUTES) < 10)
                return new ResendVerificationResultDTO(false, newEmail != null);

            token.setCreatedOn(Instant.now());
            token.setExpiry(Instant.now().plusSeconds(tokenValidityDuration));
            verificationTokenRepository.save(token);

            eventPublisher.publishEvent(new SendVerificationEmailEvent(user.getId(),
                    user.getEmail(), redirectBaseUrl, token.getId().toString()));
        }
        return new ResendVerificationResultDTO(true, emailChanged);
    }

    @Autowired
    private AuthenticationManager authenticationManager;

    public LoginResultDTO login(String email, String password) {
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(email, password);
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(token);
            UUID userId = ((CustomUserPrincipal) authentication.getPrincipal()).getId();
            SecurityContextHolder.getContext().setAuthentication(authentication);
            return new LoginResultDTO(true, userId, false, false, null);
        } catch (BadCredentialsException | NullPointerException ex) {
            return new LoginResultDTO(false, null, true, false, null);
        } catch (DisabledException ex) {
            return new LoginResultDTO(false, null, false, true, null);
        } catch (Exception e) {
            return new LoginResultDTO(false, null, null, null, e.getMessage());
        }
    }

    public boolean verify(UUID tokenId) {
        Optional<VerificationToken> _token = verificationTokenRepository.findById(tokenId);
        if (_token.isEmpty()) return false;
        VerificationToken token = _token.get();
        // if already expired
        if (token.getExpiry().isBefore(Instant.now())) return false;
        token.getUser().setAccountVerified(true);
        userRepository.save(token.getUser());
        verificationTokenRepository.delete(token);
        return true;
    }
}
