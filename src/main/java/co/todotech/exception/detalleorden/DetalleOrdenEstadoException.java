package co.todotech.exception.detalleorden;

public class DetalleOrdenEstadoException extends DetalleOrdenException {
    public DetalleOrdenEstadoException(String message) {
        super(message);
    }

    public DetalleOrdenEstadoException(String estadoActual, String estadoRequerido) {
        super("Operación no permitida. Estado actual: " + estadoActual + ". Estado requerido: " + estadoRequerido);
    }
}