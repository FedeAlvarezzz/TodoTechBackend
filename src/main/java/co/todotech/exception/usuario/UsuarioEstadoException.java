package co.todotech.exception.usuario;

public class UsuarioEstadoException extends RuntimeException {
    public UsuarioEstadoException(String message) {
        super(message);
    }

    public UsuarioEstadoException(String message, Throwable cause) {
        super(message, cause);
    }
}