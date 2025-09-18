package co.todotech.utils;

public interface EmailService {

    void sendAdminLoginNotification(String email, String nombre, String fechaHora) throws Exception;

    void sendPasswordReminder(String email, String nombre, String nombreUsuario, String contrasena) throws Exception;
}