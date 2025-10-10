package co.todotech.exception.producto;


public class ProductoNotFoundException extends ProductoException {
    public ProductoNotFoundException(String message) {
        super(message);
    }

    public ProductoNotFoundException(Long id) {
        super("Producto no encontrado con ID: " + id);
    }

    public ProductoNotFoundException(String campo, String valor) {
        super("Producto no encontrado con " + campo + ": " + valor);
    }
}