package co.todotech.model.dto.pago;

import co.todotech.model.entities.Pago;
import co.todotech.model.enums.EstadoPago;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * DTO for {@link Pago}
 */
public record PagoDto(
        Long id,

        @NotNull(message = "La orden de venta no puede ser nula")
        Long ordenVentaId,

        @NotNull(message = "El monto no puede ser nulo")
        @Positive(message = "El monto debe ser mayor a cero")
        Double monto,

        @NotNull(message = "El método de pago no puede ser nulo")
        Long metodoPagoId,

        @Size(max = 100, message = "El número de transacción no puede exceder 100 caracteres")
        String numeroTransaccion,

        LocalDateTime fechaPago,

        @NotNull(message = "El usuario no puede ser nulo")
        Long usuarioId,

        @Size(max = 255, message = "El comprobante no puede exceder 255 caracteres")
        String comprobante,

        EstadoPago estadoPago
) implements Serializable {

    // Constructor para creación sin ID
    public PagoDto(Long ordenVentaId, Double monto, Long metodoPagoId, String numeroTransaccion,
                   Long usuarioId, String comprobante, EstadoPago estadoPago) {
        this(null, ordenVentaId, monto, metodoPagoId, numeroTransaccion, null, usuarioId, comprobante, estadoPago);
    }
}