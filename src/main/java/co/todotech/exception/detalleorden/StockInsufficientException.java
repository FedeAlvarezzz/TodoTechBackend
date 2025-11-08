// StockInsufficientException.java
package co.todotech.exception.detalleorden;

public class StockInsufficientException extends DetalleOrdenException {
    public StockInsufficientException(String message) {
        super(message);
    }

    public StockInsufficientException(String productoNombre, Integer stockDisponible, Integer cantidadRequerida) {
        super(String.format("Stock insuficiente para el producto '%s'. Stock disponible: %d, Cantidad requerida: %d",
                productoNombre, stockDisponible, cantidadRequerida));
    }

    public StockInsufficientException(Long productoId, Integer stockDisponible, Integer cantidadRequerida) {
        super(String.format("Stock insuficiente para el producto ID: %d. Stock disponible: %d, Cantidad requerida: %d",
                productoId, stockDisponible, cantidadRequerida));
    }
}