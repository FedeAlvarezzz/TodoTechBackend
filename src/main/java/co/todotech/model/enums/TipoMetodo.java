package co.todotech.model.enums;

public enum TipoMetodo {
    EFECTIVO,
    TARJETA_CREDITO,
    TARJETA_DEBITO,
    TRANSFERENCIA,
    REDCOMPRA,
    // Nuevos métodos para pasarelas
    STRIPE,
    PAYPAL,
    MERCADOPAGO,
    WOMPI
}