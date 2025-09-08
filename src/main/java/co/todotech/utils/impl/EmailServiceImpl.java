package co.todotech.utils.impl;

import co.todotech.configuration.EmailConfig;
import co.todotech.utils.EmailService;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender javaMailSender;
    private final EmailConfig emailConfig;

    @Override
    public void enviarEmail(String to, String subject, String text) {
        // Este método ahora será para texto plano, pero los otros usarán HTML
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(emailConfig.getFrom());
            helper.setTo(to);
            helper.setSubject(subject);

            // Convertir texto plano a HTML básico
            String htmlContent = convertirTextoAHtml(text);
            helper.setText(htmlContent, true);

            javaMailSender.send(message);
            log.info("📧 Email enviado exitosamente a: {}", to);
        } catch (Exception e) {
            log.error("❌ Error al enviar email a {}: {}", to, e.getMessage());
            throw new RuntimeException("Error al enviar email: " + e.getMessage());
        }
    }

    @Override
    public void enviarNotificacionAdminLogin(String adminEmail, String adminName, String fechaHora) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(emailConfig.getFrom());
            helper.setTo(adminEmail);
            helper.setSubject("🔐 " + emailConfig.getAdminNotificationSubject());

            String contenido = "<!DOCTYPE html>"
                    + "<html>"
                    + "<head>"
                    + "    <meta charset='UTF-8'>"
                    + "    <title>Notificación de Ingreso</title>"
                    + "    <style>"
                    + "        * { margin: 0; padding: 0; box-sizing: border-box; }"
                    + "        body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); margin: 0; padding: 20px; }"
                    + "        .container { max-width: 600px; margin: 20px auto; background: white; border-radius: 20px; overflow: hidden; box-shadow: 0 20px 40px rgba(0,0,0,0.1); }"
                    + "        .header { background: linear-gradient(135deg, #ff6b6b 0%, #ee5a24 100%); color: white; padding: 30px; text-align: center; }"
                    + "        .header h1 { font-size: 28px; margin-bottom: 10px; }"
                    + "        .content { padding: 40px; }"
                    + "        .alert-box { background: #fff3e0; border: 2px solid #ff9800; border-radius: 15px; padding: 25px; margin: 20px 0; text-align: center; }"
                    + "        .alert-icon { font-size: 48px; margin-bottom: 15px; }"
                    + "        .info-card { background: #f8f9fa; border-radius: 15px; padding: 25px; margin: 20px 0; border-left: 5px solid #007bff; }"
                    + "        .info-item { display: flex; justify-content: space-between; margin: 12px 0; padding: 12px; background: white; border-radius: 10px; box-shadow: 0 2px 8px rgba(0,0,0,0.1); }"
                    + "        .footer { background: #2c3e50; color: white; text-align: center; padding: 25px; }"
                    + "        .warning { background: #ffebee; border: 2px solid #f44336; border-radius: 10px; padding: 20px; margin: 20px 0; text-align: center; }"
                    + "        .button { display: inline-block; background: #007bff; color: white; padding: 15px 30px; text-decoration: none; border-radius: 25px; margin: 20px 0; font-weight: bold; }"
                    + "        .gradient-text { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); -webkit-background-clip: text; -webkit-text-fill-color: transparent; font-weight: bold; }"
                    + "    </style>"
                    + "</head>"
                    + "<body>"
                    + "    <div class='container'>"
                    + "        <div class='header'>"
                    + "            <h1>🚨 Notificación de Seguridad</h1>"
                    + "            <p>Sistema de Gestión TodoTech</p>"
                    + "        </div>"
                    + "        <div class='content'>"
                    + "            <div class='alert-box'>"
                    + "                <div class='alert-icon'>⚠️</div>"
                    + "                <h2>Actividad de Ingreso Detectada</h2>"
                    + "                <p>Se ha identificado un acceso a tu cuenta administrativa</p>"
                    + "            </div>"
                    + "            "
                    + "            <div class='info-card'>"
                    + "                <h3>📋 Detalles del Ingreso</h3>"
                    + "                <div class='info-item'>"
                    + "                    <span>👤 Administrador:</span>"
                    + "                    <span class='gradient-text'>" + adminName + "</span>"
                    + "                </div>"
                    + "                <div class='info-item'>"
                    + "                    <span>📧 Email:</span>"
                    + "                    <span>" + adminEmail + "</span>"
                    + "                </div>"
                    + "                <div class='info-item'>"
                    + "                    <span>🕐 Fecha y Hora:</span>"
                    + "                    <span>" + fechaHora + "</span>"
                    + "                </div>"
                    + "            </div>"
                    + "            "
                    + "            <div class='warning'>"
                    + "                <h3>🔒 Acción Requerida</h3>"
                    + "                <p>Si no reconoces esta actividad, contacta inmediatamente al equipo de soporte</p>"
                    + "                <a href='mailto:soporte@todotech.com' class='button'>🆘 Contactar Soporte</a>"
                    + "            </div>"
                    + "        </div>"
                    + "        <div class='footer'>"
                    + "            <p>© 2024 TodoTech - Sistema de Gestión</p>"
                    + "            <p>Este es un mensaje automático de seguridad</p>"
                    + "        </div>"
                    + "    </div>"
                    + "</body>"
                    + "</html>";

            helper.setText(contenido, true);
            javaMailSender.send(message);

            log.info("✅ Notificación de admin enviada exitosamente a: {}", adminEmail);

        } catch (Exception e) {
            log.error("❌ Error enviando notificación a admin {}: {}", adminEmail, e.getMessage());
            throw new RuntimeException("Error al enviar notificación: " + e.getMessage());
        }
    }

    @Override
    public void enviarRecordatorioContrasena(String destinatario, String nombre, String contrasena, String codigoVerificacion) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(emailConfig.getFrom());
            helper.setTo(destinatario);
            helper.setSubject("🔑 Recordatorio de Contraseña - TodoTech");

            String contenido = "<!DOCTYPE html>"
                    + "<html>"
                    + "<head>"
                    + "    <meta charset='UTF-8'>"
                    + "    <title>Recordatorio de Contraseña</title>"
                    + "    <style>"
                    + "        * { margin: 0; padding: 0; box-sizing: border-box; }"
                    + "        body { font-family: 'Arial', sans-serif; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); margin: 0; padding: 20px; }"
                    + "        .container { max-width: 600px; margin: 20px auto; background: white; border-radius: 20px; overflow: hidden; box-shadow: 0 20px 40px rgba(0,0,0,0.1); }"
                    + "        .header { background: linear-gradient(135deg, #4ecdc4 0%, #44a08d 100%); color: white; padding: 30px; text-align: center; }"
                    + "        .header h1 { font-size: 28px; margin-bottom: 10px; }"
                    + "        .content { padding: 40px; }"
                    + "        .welcome { text-align: center; margin-bottom: 30px; }"
                    + "        .credentials-box { background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%); color: white; border-radius: 15px; padding: 30px; margin: 30px 0; }"
                    + "        .credential-item { background: rgba(255,255,255,0.1); padding: 15px; margin: 15px 0; border-radius: 10px; border: 2px solid rgba(255,255,255,0.2); }"
                    + "        .security-note { background: #fff3e0; border: 2px solid #ff9800; border-radius: 15px; padding: 25px; margin: 20px 0; text-align: center; }"
                    + "        .footer { background: #2c3e50; color: white; text-align: center; padding: 25px; }"
                    + "        .button { display: inline-block; background: #ff6b6b; color: white; padding: 15px 30px; text-decoration: none; border-radius: 25px; margin: 20px 0; font-weight: bold; }"
                    + "        .icon { font-size: 24px; margin-right: 10px; }"
                    + "        .highlight { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); -webkit-background-clip: text; -webkit-text-fill-color: transparent; font-weight: bold; font-size: 18px; }"
                    + "    </style>"
                    + "</head>"
                    + "<body>"
                    + "    <div class='container'>"
                    + "        <div class='header'>"
                    + "            <h1>🔐 Recuperación de Credenciales</h1>"
                    + "            <p>Sistema TodoTech - Acceso Seguro</p>"
                    + "        </div>"
                    + "        <div class='content'>"
                    + "            <div class='welcome'>"
                    + "                <h2>Hola, <span class='highlight'>" + nombre + "</span>! 👋</h2>"
                    + "                <p>Has solicitado recordar tus credenciales de acceso al sistema</p>"
                    + "            </div>"
                    + "            "
                    + "            <div class='credentials-box'>"
                    + "                <h3 style='text-align: center; margin-bottom: 20px;'>🎯 Tus Credenciales de Acceso</h3>"
                    + "                "
                    + "                <div class='credential-item'>"
                    + "                    <span class='icon'>📧</span>"
                    + "                    <strong>Correo Electrónico:</strong><br>"
                    + "                    <span style='font-size: 16px;'>" + destinatario + "</span>"
                    + "                </div>"
                    + "                "
                    + "                <div class='credential-item'>"
                    + "                    <span class='icon'>👤</span>"
                    + "                    <strong>Nombre de Usuario:</strong><br>"
                    + "                    <span style='font-size: 16px;'>" + nombre + "</span>"
                    + "                </div>"
                    + "                "
                    + "                <div class='credential-item'>"
                    + "                    <span class='icon'>🔑</span>"
                    + "                    <strong>Contraseña:</strong><br>"
                    + "                    <span style='font-size: 18px; font-weight: bold; letter-spacing: 2px;'>" + contrasena + "</span>"
                    + "                </div>"
                    + "            </div>"
                    + "            "
                    + "            <div class='security-note'>"
                    + "                <h3>⚠️ Importante: Seguridad de la Información</h3>"
                    + "                <p>• Mantén tus credenciales en un lugar seguro<br>"
                    + "                   • No compartas esta información con nadie<br>"
                    + "                   • Cambia tu contraseña periódicamente<br>"
                    + "                   • Contacta a soporte si no solicitaste este recordatorio</p>"
                    + "                <a href='mailto:soporte@todotech.com' class='button'>🛡️ Contactar Seguridad</a>"
                    + "            </div>"
                    + "        </div>"
                    + "        <div class='footer'>"
                    + "            <p>© 2024 TodoTech - Todos los derechos reservados</p>"
                    + "            <p>🔒 Este es un mensaje automático de seguridad</p>"
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

    private String convertirTextoAHtml(String texto) {
        // Conversión básica de texto plano a HTML
        return "<!DOCTYPE html><html><head><meta charset='UTF-8'><style>"
                + "body { font-family: Arial, sans-serif; line-height: 1.6; padding: 20px; }"
                + ".container { max-width: 600px; margin: 0 auto; }"
                + "</style></head><body><div class='container'>"
                + texto.replace("\n", "<br>")
                + "</div></body></html>";
    }
}