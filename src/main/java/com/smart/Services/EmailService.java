package com.smart.Services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender javaMailSender;

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    public boolean sendEmail(String subject, String message, String to) {
        boolean isEmailSent = false;
        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(message, true); // true indicates this is HTML

            javaMailSender.send(mimeMessage);
            isEmailSent = true;
        } catch (MessagingException e) {
            log.error("Error while sending email: {}", e.getMessage());
            isEmailSent = false;
        }
        return isEmailSent;
    }
}