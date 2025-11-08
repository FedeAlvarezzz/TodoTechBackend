// StockCriticalException.java
package co.todotech.exception.detalleorden;

public class StockCriticalException extends DetalleOrdenException {
    public StockCriticalException(String message) {
        super(message);
    }

    public StockCriticalException(String productoNombre, Integer stockActual) {
        super(String.format("Stock crítico para el producto '%s'. Stock actual: %d. No se pueden agregar más unidades.",
                productoNombre, stockActual));
    }

    public StockCriticalException(String productoNombre, Integer stockActual, Integer cantidadActual) {
        super(String.format("Stock crítico para el producto '%s'. Stock actual: %d. Cantidad actual en carrito: %d. No se puede aumentar la cantidad.",
                productoNombre, stockActual, cantidadActual));
    }
}
