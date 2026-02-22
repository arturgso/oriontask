package br.com.oriontask.backend.shared.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

  private final JavaMailSender mailSender;

  @Value("${spring.mail.username}")
  private String fromEmail;

  @Value("${app.frontend-url}")
  private String frontendUrl;

  @Async
  public void sendConfirmationEmail(String to, String token) {
    log.info("Sending confirmation email to {}", to);
    String confirmationUrl = frontendUrl + "/confirm-email?token=" + token;

    SimpleMailMessage message = new SimpleMailMessage();
    message.setFrom(fromEmail);
    message.setTo(to);
    message.setSubject("Confirm your email - OrionTask");
    message.setText(
        "Welcome to OrionTask! Please confirm your email by clicking the link below:\n\n"
            + confirmationUrl
            + "\n\nThis link will expire in 24 hours.");

    try {
      mailSender.send(message);
      log.info("Confirmation email sent to {}", to);
    } catch (Exception e) {
      log.error("Failed to send confirmation email to {}", to, e);
    }
  }
}
