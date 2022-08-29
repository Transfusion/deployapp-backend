package io.github.transfusion.deployapp.controller;

import io.github.transfusion.deployapp.auth.CustomUserPrincipal;
import io.github.transfusion.deployapp.dto.response.ProfileDTO;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/v1/user")
public class UserInfoController {
    @Operation(summary = "Gets the profile of the currently logged in user", description = "Used in the AuthContext of the React frontend", tags = {"auth"})
    @GetMapping("profile")
    public ProfileDTO profile() {
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authentication = context.getAuthentication();
        if (authentication instanceof AnonymousAuthenticationToken)
            return new ProfileDTO(false, null, false, "anonymous", "Anonymous", null);
//        authentication.getPrincipal()
        else {
            CustomUserPrincipal principal = (CustomUserPrincipal) authentication.getPrincipal();
            return new ProfileDTO(true, principal.getId(), principal.hasUsername(), principal.getUsername(), principal.getName(), principal.getEmail());
        }
    }
}
