package io.github.transfusion.deployapp.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.List;

@Service
public class EmailService {

    @Value("${custom_app.email_from}")
    private String emailFrom;

    @Autowired
    private JavaMailSender emailSender;

    public void sendHtmlMessage(String to, String subject, String htmlBody) throws MessagingException {
        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setFrom(emailFrom);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlBody, true);
        emailSender.send(message);
    }

//    public void sendSimpleEmail(String subject, String text, List<String> to) {
//        SimpleMailMessage message = new SimpleMailMessage();
//        message.setSubject(subject);
//        message.setText(text);
//        message.setTo(to.toArray(new String[0]));
//
//        mailSender.send(message);
//    }
}
