package co.todotech.exception.usuario;

public class UsuarioDuplicateException extends RuntimeException {
    public UsuarioDuplicateException(String message) {
        super(message);
    }

    public UsuarioDuplicateException(String message, Throwable cause) {
        super(message, cause);
    }
}