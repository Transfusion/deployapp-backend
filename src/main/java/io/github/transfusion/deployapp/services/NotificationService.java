package io.github.transfusion.deployapp.services;

//import io.github.transfusion.deployapp.db.repositories.VerificationTokenRepository;

import io.github.transfusion.deployapp.dto.internal.SendChangeEmailEvent;
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

    @Autowired
    private EmailService emailService;

    @Value("${custom_app.token_validity_duration}")
    private Integer tokenValidityDuration;

    @Autowired
    private SpringTemplateEngine thymeleafTemplateEngine;

    private void sendVerificationEmail(String email, String targetUrl) {
        // send out an email
        Context thymeleafContext = new Context();
        Map<String, Object> templateModel = new HashMap<>();

        templateModel.put("link", targetUrl);
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

    private void sendChangeEmail(String email, String targetUrl) {
        Context thymeleafContext = new Context();
        Map<String, Object> templateModel = new HashMap<>();

        templateModel.put("link", targetUrl);
        templateModel.put("valid_minutes", tokenValidityDuration / 60);
        thymeleafContext.setVariables(templateModel);

        String htmlBody = thymeleafTemplateEngine.process("change_email_verification.html", thymeleafContext);
        try {
            emailService.sendHtmlMessage(email, "DeployApp Email Change", htmlBody);
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

    @EventListener
    private void initiateChangeEmail(SendChangeEmailEvent event) {
        String targetUrl = event.getRedirectBaseUrl();
        targetUrl = UriComponentsBuilder.fromUriString(targetUrl)
                .queryParam("change_email", event.getToken())
                .queryParam("new_email", event.getNewEmail())
                .build().toUriString();

        sendChangeEmail(event.getEmail(), targetUrl);
    }

}
