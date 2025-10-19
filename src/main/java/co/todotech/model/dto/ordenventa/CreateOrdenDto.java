package co.todotech.model.dto.ordenventa;

import co.todotech.model.enums.EstadoOrden;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;

import java.io.Serializable;

public record CreateOrdenDto(
        @NotNull(message = "El cliente es obligatorio")
        Long clienteId,

        @NotNull(message = "El vendedor es obligatorio")
        Long vendedorId,

        @DecimalMin(value = "0.0", message = "El descuento no puede ser negativo")
        Double descuento // ✅ NUEVO: Campo descuento agregado
) implements Serializable {}