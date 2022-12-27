package io.github.transfusion.deployapp.services;

import io.github.transfusion.deployapp.dto.internal.SendVerificationEmailEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

import java.util.HashMap;
import java.util.Map;

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

}
