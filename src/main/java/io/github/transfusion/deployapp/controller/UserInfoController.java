package io.github.transfusion.deployapp.controller;

import io.github.transfusion.deployapp.auth.CustomUserDetailsService;
import io.github.transfusion.deployapp.auth.CustomUserPrincipal;
import io.github.transfusion.deployapp.db.repositories.AuthProviderRepository;
import io.github.transfusion.deployapp.dto.request.DeleteConnectedAccountRequest;
import io.github.transfusion.deployapp.dto.request.PatchProfileRequest;
import io.github.transfusion.deployapp.dto.response.AuthProviderDTO;
import io.github.transfusion.deployapp.dto.response.ProfileDTO;
import io.github.transfusion.deployapp.mappers.AuthProviderMapper;
import io.github.transfusion.deployapp.services.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.*;

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
}
