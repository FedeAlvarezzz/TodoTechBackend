package co.todotech.exception.detalleorden;

public abstract class DetalleOrdenException extends RuntimeException {
    public DetalleOrdenException(String message) {
        super(message);
    }

    public DetalleOrdenException(String message, Throwable cause) {
        super(message, cause);
    }
}