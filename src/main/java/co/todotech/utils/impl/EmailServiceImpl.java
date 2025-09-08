package co.todotech.utils.impl;

import co.todotech.configuration.EmailConfig;
import co.todotech.utils.EmailService;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender javaMailSender; // Cambié el nombre a javaMailSender para ser consistente
    private final EmailConfig emailConfig;

    @Override
    public void enviarEmail(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(emailConfig.getFrom());
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);

            javaMailSender.send(message);
            log.info("Email enviado exitosamente a: {}", to);
        } catch (Exception e) {
            log.error("Error al enviar email a {}: {}", to, e.getMessage());
            throw new RuntimeException("Error al enviar email: " + e.getMessage());
        }
    }

    @Override
    public void enviarNotificacionAdminLogin(String adminEmail, String adminName, String fechaHora) {
        String subject = emailConfig.getAdminNotificationSubject(); // Usa el subject desde la configuración
        String text = String.format(
                "Hola %s,\n\n" +
                        "Se ha detectado un ingreso al sistema con tu cuenta de administrador.\n" +
                        "Fecha y hora: %s\n\n" +
                        "Si no fuiste tú, por favor contacta al soporte técnico inmediatamente.\n\n" +
                        "Saludos,\n" +
                        "Sistema de Gestión TodoTech",
                adminName, fechaHora
        );

        enviarEmail(adminEmail, subject, text);
    }

    @Override
    public void enviarRecordatorioContrasena(String destinatario, String nombre, String contrasena, String codigoVerificacion) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(emailConfig.getFrom());
            helper.setTo(destinatario);
            helper.setSubject("Recordatorio de Contraseña - TodoTech");

            String contenido = "<!DOCTYPE html>"
                    + "<html>"
                    + "<head>"
                    + "    <meta charset='UTF-8'>"
                    + "    <title>Recordatorio de Contraseña</title>"
                    + "    <style>"
                    + "        body { font-family: Arial, sans-serif; line-height: 1.6; margin: 0; padding: 20px; background-color: #f4f4f4; }"
                    + "        .container { max-width: 600px; margin: 0 auto; background-color: #ffffff; border-radius: 10px; overflow: hidden; box-shadow: 0 0 10px rgba(0,0,0,0.1); }"
                    + "        .header { background-color: #007bff; color: white; padding: 20px; text-align: center; }"
                    + "        .content { padding: 30px; }"
                    + "        .info-box { background-color: #f8f9fa; padding: 15px; border-radius: 5px; margin: 15px 0; border-left: 4px solid #007bff; }"
                    + "        .code { font-size: 24px; font-weight: bold; color: #007bff; padding: 15px; background-color: #e9ecef; text-align: center; margin: 20px 0; border-radius: 5px; letter-spacing: 3px; }"
                    + "        .footer { text-align: center; margin-top: 30px; color: #6c757d; font-size: 12px; padding: 20px; background-color: #f8f9fa; }"
                    + "        .warning { color: #dc3545; font-size: 14px; margin-top: 20px; padding: 10px; background-color: #fff3cd; border-radius: 5px; }"
                    + "    </style>"
                    + "</head>"
                    + "<body>"
                    + "    <div class='container'>"
                    + "        <div class='header'>"
                    + "            <h2>🔒 TodoTech - Recordatorio de Contraseña</h2>"
                    + "        </div>"
                    + "        <div class='content'>"
                    + "            <p>Hola <strong>" + nombre + "</strong>,</p>"
                    + "            <p>Has solicitado recordar tu contraseña. Aquí está tu información de acceso:</p>"
                    + "            "
                    + "            <div class='info-box'>"
                    + "                <p><strong>📧 Correo:</strong> " + destinatario + "</p>"
                    + "                <p><strong>🔑 Contraseña:</strong> " + contrasena + "</p>"
                    + "            </div>"
                    + "            "
                    + "            <p><strong>Código de verificación:</strong></p>"
                    + "            <div class='code'>" + codigoVerificacion + "</div>"
                    + "            "
                    + "            <div class='warning'>"
                    + "                <p>⚠️ <strong>Importante:</strong> Mantén esta información segura y no la compartas con nadie.</p>"
                    + "            </div>"
                    + "            "
                    + "            <p>Si no solicitaste este recordatorio, por favor ignora este mensaje y contacta con soporte.</p>"
                    + "        </div>"
                    + "        <div class='footer'>"
                    + "            <p>Este es un mensaje automático generado por el Sistema de Gestión TodoTech.</p>"
                    + "            <p>⚠️ Por favor no respondas a este correo.</p>"
                    + "        </div>"
                    + "    </div>"
                    + "</body>"
                    + "</html>";

            helper.setText(contenido, true);
            javaMailSender.send(message);

            log.info("✅ Email de recordatorio enviado exitosamente a: {}", destinatario);

        } catch (Exception e) {
            log.error("❌ Error enviando email de recordatorio a {}: {}", destinatario, e.getMessage());
            throw new RuntimeException("Error al enviar el email de recordatorio: " + e.getMessage());
        }
    }
}