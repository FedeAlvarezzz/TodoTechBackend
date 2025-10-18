package co.todotech.model.dto.metodopago;

import co.todotech.model.entities.MetodoPago;
import co.todotech.model.enums.TipoMetodo;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.io.Serializable;

/**
 * DTO for {@link MetodoPago}
 */
public record MetodoPagoDto(
        Long id,

        @NotNull(message = "El método de pago no puede ser nulo")
        TipoMetodo metodo,

        @Size(max = 255, message = "La descripción no puede exceder 255 caracteres")
        String descripcion,

        @NotNull(message = "La aprobación no puede ser nula")
        Boolean aprobacion,

        @NotNull(message = "La comisión no puede ser nula")
        Double comision
) implements Serializable {

    // Constructor para creación sin ID
    public MetodoPagoDto(TipoMetodo metodo, String descripcion, Boolean aprobacion, Double comision) {
        this(null, metodo, descripcion, aprobacion, comision);
    }
}