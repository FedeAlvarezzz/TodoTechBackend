// ProductoNoDisponibleException.java
package co.todotech.exception.detalleorden;

public class ProductoNoDisponibleException extends DetalleOrdenException {
    public ProductoNoDisponibleException(String message) {
        super(message);
    }

    public ProductoNoDisponibleException(String productoNombre, String estado) {
        super(String.format("El producto '%s' no está disponible para la venta. Estado actual: %s",
                productoNombre, estado));
    }
}