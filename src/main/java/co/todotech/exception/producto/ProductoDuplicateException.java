package co.todotech.exception.producto;


public class ProductoDuplicateException extends ProductoException {
    public ProductoDuplicateException(String message) {
        super(message);
    }

    public ProductoDuplicateException(String campo, String valor) {
        super("Ya existe un producto con " + campo + ": " + valor);
    }
}