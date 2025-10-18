package co.todotech.exception.detalleorden;

public class DetalleOrdenNotFoundException extends DetalleOrdenException {
    public DetalleOrdenNotFoundException(String message) {
        super(message);
    }

    public DetalleOrdenNotFoundException(Long id) {
        super("Detalle de orden no encontrado con ID: " + id);
    }

    public DetalleOrdenNotFoundException(Long ordenId, Long productoId) {
        super("Detalle de orden no encontrado para orden ID: " + ordenId + " y producto ID: " + productoId);
    }
}