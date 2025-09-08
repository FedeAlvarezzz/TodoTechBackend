package co.todotech.utils;

public interface EmailService {
    void enviarEmail(String to, String subject, String text);
    void enviarNotificacionAdminLogin(String adminEmail, String adminName, String fechaHora);
    void enviarRecordatorioContrasena(String destinatario, String nombre, String contrasena, String codigoVerificacion);
}