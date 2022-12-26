package io.github.transfusion.deployapp.services;

import io.github.transfusion.deployapp.db.repositories.VerificationTokenRepository;
import io.github.transfusion.deployapp.dto.internal.SendVerificationEmailEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

import java.util.*;

@Service
public class NotificationService {

//    @Autowired
//    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private VerificationTokenRepository tokenRepository;

    @Value("${custom_app.token_validity_duration}")
    private Integer tokenValidityDuration;

    @Autowired
    private SpringTemplateEngine thymeleafTemplateEngine;

    private void sendVerificationEmail(String email, String targetUrl) {
        // send out an email
        Context thymeleafContext = new Context();
        Map<String, Object> templateModel = new HashMap<>();

        templateModel.put("verification_link", targetUrl);
        templateModel.put("valid_minutes", tokenValidityDuration / 60);
//        templateModel.put("timestamp", "");
        thymeleafContext.setVariables(templateModel);
        String htmlBody = thymeleafTemplateEngine.process("signup_confirmation.html", thymeleafContext);

        try {
            emailService.sendHtmlMessage(email, "DeployApp Account Confirmation", htmlBody);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @EventListener
    private void registrationCompleted(SendVerificationEmailEvent event) {
        String targetUrl = event.getRedirectBaseUrl();
        targetUrl = UriComponentsBuilder.fromUriString(targetUrl)
                .queryParam("verify", event.getToken())
                .build().toUriString();

        sendVerificationEmail(event.getEmail(), targetUrl);
    }

//    @Autowired
//    ApplicationEventPublisher eventPublisher;
//
//    public boolean verifyUser(UUID tokenId) {
//        Optional<VerificationToken> _token = tokenRepository.findById(tokenId);
//        if (_token.isEmpty()) return false;
//
//        VerificationToken token = _token.get();
//        if (token.getExpiry().isAfter(Instant.now())) return false;
//
//        tokenRepository.delete(token);
//        eventPublisher.publishEvent(new UserVerifiedEvent(token.getUserId()));
//
//        return true;
//    }
//
//    public boolean resendVerification(UUID userId, String newEmail) {
//        // resend after 10 mins
//        Optional<VerificationToken> _token = tokenRepository.findByUserId(userId);
//        if (_token.isEmpty()) return false;
//
//        VerificationToken token = _token.get();
//
//        // check the date..
//        if (token.getCreatedOn().until(Instant.now(), ChronoUnit.MINUTES) >= 10) {
//            if (newEmail != null) {
//                eventPublisher.publishEvent(new PreVerificationEmailChangeEvent(userId, newEmail));
//                token.setEmail(newEmail);
//            }
//
//            UUID randomUUID = UUID.randomUUID();
////            VerificationToken token = new VerificationToken();
//            token.setId(randomUUID);
//            // set the user id without having access to the actual user entity.
//            token.setUserId(userId);
//            token.setCreatedOn(Instant.now());
//            token.setExpiry(Instant.now().plusSeconds(tokenValidityDuration));
//
//            tokenRepository.save(token);
//
//            sendVerificationEmail(token.getEmail(), );
//            return true;
//        }
//
//        return false;
//    }
}
