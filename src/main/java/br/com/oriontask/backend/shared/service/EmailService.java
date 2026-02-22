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
    message.setSubject("Confirme seu e-mail - OrionTask");
    message.setText(
        "Bem-vindo(a) ao OrionTask! Por favor, confirme seu e-mail clicando no link abaixo:\n\n"
            + confirmationUrl
            + "\n\nEste link expira em 24 horas.");

    mailSender.send(message);
    log.info("Confirmation email sent to {}", to);
  }

  @Async
  public void sendPasswordResetEmail(String to, String token) {
    log.info("Sending password reset email to {}", to);
    String resetUrl = frontendUrl + "/reset-password?token=" + token;

    SimpleMailMessage message = new SimpleMailMessage();
    message.setFrom(fromEmail);
    message.setTo(to);
    message.setSubject("Redefinição de Senha - OrionTask");
    message.setText(
        "Você solicitou a redefinição de senha para sua conta OrionTask. Clique no link abaixo para criar uma nova senha:\n\n"
            + resetUrl
            + "\n\nEste link expira em 2 horas.");

    mailSender.send(message);
    log.info("Password reset email sent to {}", to);
  }
}
