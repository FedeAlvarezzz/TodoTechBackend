package co.todotech.exception.detalleorden;

public class DetallesNoEncontradosException extends DetalleOrdenException {
    public DetallesNoEncontradosException(String message) {
        super(message);
    }

    public DetallesNoEncontradosException(Long ordenId) {
        super("No se encontraron detalles para la orden con ID: " + ordenId + ". La orden aún no tiene detalles agregados.");
    }
}