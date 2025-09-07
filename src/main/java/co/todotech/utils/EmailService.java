package co.todotech.utils;

import co.todotech.configuration.EmailConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final EmailConfig emailConfig;

    public void enviarEmail(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(emailConfig.getFrom());
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);

            mailSender.send(message);
            log.info("Email enviado exitosamente a: {}", to);
        } catch (Exception e) {
            log.error("Error al enviar email a {}: {}", to, e.getMessage());
            // Puedes decidir si lanzar la excepción o solo loggear el error
        }
    }

    public void enviarNotificacionAdminLogin(String adminEmail, String adminName, String fechaHora) {
        String subject = emailConfig.getAdminNotificationSubject();
        String text = String.format(
                "Hola %s,\n\n" +
                        "Se ha detectado un ingreso al sistema con tu cuenta de administrador.\n" +
                        "Fecha y hora: %s\n\n" +
                        "Si no fuiste tú, por favor contacta al soporte técnico inmediatamente.\n\n" +
                        "Saludos,\n" +
                        "Sistema de Gestión",
                adminName, fechaHora
        );

        enviarEmail(adminEmail, subject, text);
    }
}