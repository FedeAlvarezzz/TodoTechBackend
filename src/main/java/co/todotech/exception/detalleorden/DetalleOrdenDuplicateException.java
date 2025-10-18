package co.todotech.exception.detalleorden;

public class DetalleOrdenDuplicateException extends DetalleOrdenException {
    public DetalleOrdenDuplicateException(String message) {
        super(message);
    }

    public DetalleOrdenDuplicateException(Long ordenId, Long productoId) {
        super("Ya existe un detalle para el producto ID: " + productoId + " en la orden ID: " + ordenId + ". Use actualizar cantidad en su lugar.");
    }
}