package io.github.transfusion.deployapp.controller;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import io.github.transfusion.deployapp.auth.CustomUserDetailsService;
import io.github.transfusion.deployapp.auth.CustomUserPrincipal;
import io.github.transfusion.deployapp.db.repositories.AuthProviderRepository;
import io.github.transfusion.deployapp.dto.request.*;
import io.github.transfusion.deployapp.dto.response.*;
import io.github.transfusion.deployapp.mappers.AuthProviderMapper;
import io.github.transfusion.deployapp.services.AccountService;
import io.github.transfusion.deployapp.services.RateLimitService;
import io.github.transfusion.deployapp.utils.RateLimitUtils;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/api/v1/user")
public class UserInfoController {
    @Operation(summary = "Gets the profile of the currently logged in user", description = "Used in the AuthContext of the React frontend", tags = {"auth"})
    @GetMapping("profile")
    public ProfileDTO profile() {
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authentication = context.getAuthentication();
        if (authentication instanceof AnonymousAuthenticationToken)
            return new ProfileDTO(false, null, false, "anonymous", "Anonymous", null, false, false, null);
//        authentication.getPrincipal()
        else {
            boolean oauth_login = authentication instanceof OAuth2AuthenticationToken;
            String oauth_registration_id = null;
            if (oauth_login)
                oauth_registration_id = ((OAuth2AuthenticationToken) authentication).getAuthorizedClientRegistrationId();
            CustomUserPrincipal principal = (CustomUserPrincipal) authentication.getPrincipal();
            return new ProfileDTO(true, principal.getId(), principal.hasUsername(), principal.getUsername(), principal.getName(), principal.getEmail(), principal.hasPassword(), authentication instanceof OAuth2AuthenticationToken, oauth_registration_id);
        }
    }

    @Autowired
    private AccountService accountService;
//    allow changing of the username and email

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @PatchMapping("profile")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ProfileDTO patchProfile(@RequestBody PatchProfileRequest request) {
        accountService.patchUserProfile(request);
        customUserDetailsService.reloadUserPrincipal();
        return profile();
    }

    @Autowired
    private RateLimitService rateLimitService;

    @PostMapping("change_email")
    @PreAuthorize("hasRole('ROLE_USER')") // cannot be anonymous
    public ResponseEntity<Void> changeEmail(HttpServletRequest request, @RequestBody ChangeEmailRequest changeEmailRequest) throws URISyntaxException {
        baseUrlCheck(request, changeEmailRequest.getRedirectBaseUrl());

        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authentication = context.getAuthentication();
        CustomUserPrincipal principal = (CustomUserPrincipal) authentication.getPrincipal();
        UUID id = principal.getId();

        Bucket bucket = rateLimitService.resolveBucket("change_email",
                id + "_" + changeEmailRequest.getEmail(), RateLimitService.AVAILABLE_CONFIGURATIONS.EMAIL_RATELIMIT);
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        if (!probe.isConsumed()) {
            return RateLimitUtils.tooManyRequestsResponse(probe, null);
        } else {
            accountService.changeEmail(changeEmailRequest.getEmail(), changeEmailRequest.getRedirectBaseUrl());
            return new ResponseEntity<>(null, HttpStatus.OK);
        }
    }

    @PostMapping("confirm_change_email")
    @PreAuthorize("hasRole('ROLE_USER')") // cannot be anonymous
    public ResponseEntity<Void> confirmChangeEmail(@RequestBody ConfirmEmailChangeRequest request) {
        boolean ok = accountService.confirmChangeEmail(request.getToken(), request.getEmail());
        if (ok) customUserDetailsService.reloadUserPrincipal();
        return new ResponseEntity<>(ok ? HttpStatus.OK : HttpStatus.FORBIDDEN);
    }


    @Autowired
    private AuthProviderRepository authProviderRepository;

    @Autowired
    private AuthProviderMapper authProviderMapper;

    @GetMapping("connectedAccounts")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<List<AuthProviderDTO>> getOwnConnectedAccounts() {
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authentication = context.getAuthentication();

        CustomUserPrincipal principal = (CustomUserPrincipal) authentication.getPrincipal();
        UUID id = principal.getId();

        return new ResponseEntity<>(authProviderRepository.findByUserId(id).stream().map(authProviderMapper::toDTO).collect(Collectors.toList()), HttpStatus.OK);
    }

    @DeleteMapping("connectedAccounts/{providerName}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<Void> deleteConnectedAccount(@PathVariable String providerName) {
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authentication = context.getAuthentication();

        CustomUserPrincipal principal = (CustomUserPrincipal) authentication.getPrincipal();
        UUID id = principal.getId();

        accountService.deleteConnectedAccount(id, providerName);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    private void baseUrlCheck(HttpServletRequest request, String baseUrl) throws URISyntaxException {
        // if we get to this point, we have passed the CorsFilter.
        // simply check if the origin is equal to the given base url, and whether they are present in custom_cors.origins.

        String origin = request.getHeader("origin");
        String baseUrlOrigin = ServletUriComponentsBuilder.fromUri(new URI(baseUrl)).replacePath(null).build().toString();
        if (origin == null || !origin.equals(baseUrlOrigin))
            throw new AccessDeniedException("redirectBaseUrl must be from the same origin.");
    }

    @PostMapping("register")
    public ResponseEntity<RegisterResultDTO> register(HttpServletRequest request, @RequestBody RegisterRequest registerRequest) throws URISyntaxException {
        baseUrlCheck(request, registerRequest.getRedirectBaseUrl());

        RegisterResultDTO resultDTO = accountService.registerByEmail(registerRequest.getEmail(), registerRequest.getPassword(), registerRequest.getRedirectBaseUrl());
        return new ResponseEntity<>(resultDTO, resultDTO.getSuccess() ? HttpStatus.OK : HttpStatus.BAD_REQUEST);
    }

    @PostMapping("resend_verification")
    public ResponseEntity<ResendVerificationResultDTO> resendVerification(HttpServletRequest request, @RequestBody ResendVerificationRequest resendVerificationRequest) throws URISyntaxException {
        baseUrlCheck(request, resendVerificationRequest.getRedirectBaseUrl());

        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authentication = context.getAuthentication();
        CustomUserPrincipal principal = (CustomUserPrincipal) authentication.getPrincipal();
        UUID id = principal.getId();

        Bucket bucket = rateLimitService.resolveBucket("resend_verification",
                id + "_" + resendVerificationRequest.getNewEmail(), RateLimitService.AVAILABLE_CONFIGURATIONS.EMAIL_RATELIMIT);
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        if (!probe.isConsumed()) {
            return RateLimitUtils.tooManyRequestsResponse(probe, null);
        } else {
            ResendVerificationResultDTO resultDTO = accountService.resendVerification(resendVerificationRequest.getEmail(),
                    resendVerificationRequest.getNewEmail(), resendVerificationRequest.getRedirectBaseUrl());

            return new ResponseEntity<>(resultDTO, resultDTO.getSuccess() ? HttpStatus.OK : HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("verify")
    public ResponseEntity<Void> verify(@RequestBody VerificationRequest verificationRequest) {
        boolean ok = accountService.verify(verificationRequest.getToken());
        return new ResponseEntity<>(ok ? HttpStatus.OK : HttpStatus.FORBIDDEN);
    }

    @PostMapping("login")
    public ResponseEntity<LoginResultDTO> login(HttpServletRequest request,
                                                @RequestBody LoginRequest loginRequest) {
        return new ResponseEntity<>
                (accountService.login(loginRequest.getEmail(), loginRequest.getPassword()), HttpStatus.OK);
    }
}
