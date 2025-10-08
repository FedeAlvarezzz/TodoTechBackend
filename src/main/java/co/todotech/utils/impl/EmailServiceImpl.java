package co.todotech.utils.impl;

import co.todotech.utils.EmailService;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;


import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.email.from}")
    private String fromEmail;

    @Value("${app.email.admin-subject}")
    private String adminSubject;

    @Value("${app.email.password-reminder-subject}")
    private String passwordReminderSubject;

    // Patrón para validar emails
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$"
    );

    @Override
    public void sendAdminLoginNotification(String email, String nombre, String fechaHora) throws Exception {
        // Validaciones estrictas
        validateSingleEmail(email);

        try {
            log.info("=== ENVIANDO NOTIFICACIÓN ADMIN A: {} ===", email);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            // Configuración simple y directa
            helper.setFrom(fromEmail);
            helper.setTo(email.trim()); // SOLO un destinatario
            helper.setSubject(adminSubject);

            String htmlContent = buildAdminLoginNotificationHtml(nombre, fechaHora);
            helper.setText(htmlContent, true);

            // Enviar mensaje
            mailSender.send(message);

            log.info("✅ Notificación admin enviada EXITOSAMENTE a: {}", email);

        } catch (Exception e) {
            log.error("❌ ERROR al enviar notificación admin a {}: {}", email, e.getMessage(), e);
            throw new Exception("Error al enviar notificación por correo: " + e.getMessage());
        }
    }

    @Override
    public void sendPasswordReminder(String email, String nombre, String nombreUsuario, String contrasena) throws Exception {
        // Validaciones estrictas
        validateSingleEmail(email);

        try {
            log.info("=== INICIANDO ENVÍO DE RECORDATORIO ===");
            log.info("📧 Destinatario: {}", email);
            log.info("👤 Nombre: {}", nombre);
            log.info("👤 Nombre de usuario: {}", nombreUsuario);
            log.info("🔒 Contraseña: [PROTEGIDA]"); // No loggear contraseñas reales por seguridad

            long startTime = System.currentTimeMillis();

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            // Configuración simple y directa
            helper.setFrom(fromEmail);
            log.debug("📨 Remitente configurado: {}", fromEmail);

            helper.setTo(email.trim()); // SOLO un destinatario
            log.debug("✅ Destinatario configurado: {}", email.trim());

            helper.setSubject(passwordReminderSubject);
            log.debug("📝 Asunto configurado: {}", passwordReminderSubject);

            String htmlContent = buildPasswordReminderHtml(nombre, nombreUsuario, contrasena);
            log.debug("📄 Contenido HTML generado (tamaño aprox.): {} caracteres", htmlContent.length());

            helper.setText(htmlContent, true);
            log.debug("✅ Contenido del email configurado");

            // Enviar mensaje
            log.info("🚀 Enviando email...");
            mailSender.send(message);

            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;

            log.info("✅ Recordatorio enviado EXITOSAMENTE a: {}", email);
            log.info("⏰ Tiempo de ejecución: {} ms", duration);
            log.info("=== ENVÍO COMPLETADO ===\n");

        } catch (Exception e) {
            log.error("❌ ERROR CRÍTICO al enviar recordatorio");
            log.error("📧 Destinatario fallido: {}", email);
            log.error("🔍 Mensaje de error: {}", e.getMessage());
            log.error("📋 Stack trace completo:", e);
            log.error("=== ENVÍO FALLIDO ===");

            throw new Exception("Error al enviar recordatorio por correo: " + e.getMessage());
        }
    }

    /**
     * Valida que el email sea único y válido
     */
    private void validateSingleEmail(String email) throws Exception {
        if (email == null || email.trim().isEmpty()) {
            throw new Exception("El email no puede estar vacío");
        }

        String cleanEmail = email.trim();

        // Verificar que no contenga múltiples emails (separados por comas, punto y coma, etc.)
        if (cleanEmail.contains(",") || cleanEmail.contains(";") || cleanEmail.contains(" ")) {
            throw new Exception("Solo se permite un email por envío. Email recibido: " + cleanEmail);
        }

        // Validar formato del email
        if (!EMAIL_PATTERN.matcher(cleanEmail).matches()) {
            throw new Exception("Formato de email inválido: " + cleanEmail);
        }

        log.info("✅ Email validado: {}", cleanEmail);
    }

    private String buildAdminLoginNotificationHtml(String nombre, String fechaHora) {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "    <meta charset='UTF-8'>" +
                "    <style>" +
                "        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }" +
                "        .container { max-width: 600px; margin: 0 auto; padding: 20px; }" +
                "        .header { background-color: #007bff; color: white; text-align: center; padding: 20px; border-radius: 5px 5px 0 0; }" +
                "        .content { background-color: #f8f9fa; padding: 20px; border: 1px solid #dee2e6; }" +
                "        .footer { background-color: #6c757d; color: white; text-align: center; padding: 10px; border-radius: 0 0 5px 5px; }" +
                "        .info-box { background-color: white; padding: 15px; margin: 10px 0; border-left: 4px solid #007bff; }" +
                "    </style>" +
                "</head>" +
                "<body>" +
                "    <div class='container'>" +
                "        <div class='header'>" +
                "            <h2>🔐 Notificación de Ingreso al Sistema</h2>" +
                "        </div>" +
                "        <div class='content'>" +
                "            <h3>¡Hola " + nombre + "!</h3>" +
                "            <p>Se ha detectado un nuevo ingreso a tu cuenta de administrador en el sistema TodoTech.</p>" +
                "            <div class='info-box'>" +
                "                <p><strong>📅 Fecha y hora de ingreso:</strong> " + fechaHora + "</p>" +
                "                <p><strong>👤 Usuario:</strong> " + nombre + "</p>" +
                "            </div>" +
                "            <p>Si fuiste tú quien ingresó, puedes ignorar este mensaje. Si no reconoces este acceso, por favor contacta al soporte técnico inmediatamente.</p>" +
                "            <p><strong>Por tu seguridad, revisa regularmente los accesos a tu cuenta.</strong></p>" +
                "        </div>" +
                "        <div class='footer'>" +
                "            <p>© 2024 TodoTech - Sistema de Gestión</p>" +
                "        </div>" +
                "    </div>" +
                "</body>" +
                "</html>";
    }

    private String buildPasswordReminderHtml(String nombre, String nombreUsuario, String contrasena) {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "    <meta charset='UTF-8'>" +
                "    <style>" +
                "        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }" +
                "        .container { max-width: 600px; margin: 0 auto; padding: 20px; }" +
                "        .header { background-color: #28a745; color: white; text-align: center; padding: 20px; border-radius: 5px 5px 0 0; }" +
                "        .content { background-color: #f8f9fa; padding: 20px; border: 1px solid #dee2e6; }" +
                "        .footer { background-color: #6c757d; color: white; text-align: center; padding: 10px; border-radius: 0 0 5px 5px; }" +
                "        .credentials-box { background-color: white; padding: 15px; margin: 15px 0; border-left: 4px solid #28a745; border-radius: 4px; }" +
                "        .warning { background-color: #fff3cd; border: 1px solid #ffeeba; color: #856404; padding: 10px; border-radius: 4px; margin: 15px 0; }" +
                "        .password { font-family: 'Courier New', monospace; font-weight: bold; color: #dc3545; font-size: 16px; }" +
                "    </style>" +
                "</head>" +
                "<body>" +
                "    <div class='container'>" +
                "        <div class='header'>" +
                "            <h2>🔑 Recordatorio de Contraseña</h2>" +
                "        </div>" +
                "        <div class='content'>" +
                "            <h3>¡Hola " + nombre + "!</h3>" +
                "            <p>Has solicitado un recordatorio de tus credenciales de acceso al sistema TodoTech.</p>" +
                "            <div class='credentials-box'>" +
                "                <h4>📝 Tus credenciales son:</h4>" +
                "                <p><strong>👤 Usuario:</strong> " + nombreUsuario + "</p>" +
                "                <p><strong>🔐 Contraseña:</strong> <span class='password'>" + contrasena + "</span></p>" +
                "            </div>" +
                "            <div class='warning'>" +
                "                <p><strong>⚠️ Importante:</strong></p>" +
                "                <ul>" +
                "                    <li>Guarda esta información en un lugar seguro</li>" +
                "                    <li>No compartas tus credenciales con nadie</li>" +
                "                    <li>Considera cambiar tu contraseña después de iniciar sesión</li>" +
                "                    <li>Si no solicitaste este recordatorio, contacta al administrador</li>" +
                "                </ul>" +
                "            </div>" +
                "            <p>Puedes iniciar sesión en el sistema usando las credenciales proporcionadas arriba.</p>" +
                "        </div>" +
                "        <div class='footer'>" +
                "            <p>© 2024 TodoTech - Sistema de Gestión</p>" +
                "        </div>" +
                "    </div>" +
                "</body>" +
                "</html>";
    }
}