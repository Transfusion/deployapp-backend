package io.github.transfusion.deployapp.controller;

import io.github.transfusion.deployapp.dto.response.ProfileDTO;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/user")
public class UserInfoController {
    @GetMapping("profile")
    public ProfileDTO profile() {
//        Object g = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authentication = context.getAuthentication();
        if (authentication instanceof AnonymousAuthenticationToken)
            return new ProfileDTO(false, null, "anonymous", "Anonymous", null);
//        authentication.getPrincipal()

        return new ProfileDTO(true, UUID.randomUUID(), "Sample", "foobar", "sample@sample.com");
    }
}
