package co.todotech.exception.ordenventa;

public class OrdenNotFoundException extends RuntimeException {
    public OrdenNotFoundException(String message) {
        super(message);
    }

    public OrdenNotFoundException(Long id) {
        super("Orden no encontrada con ID: " + id);
    }

    public OrdenNotFoundException(String campo, String valor) {
        super("Orden no encontrada con " + campo + ": " + valor);
    }
}