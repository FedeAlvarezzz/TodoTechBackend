package co.todotech.model.dto.detalleorden;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.io.Serializable;

public record AplicarDescuentoRequest(
        @NotNull(message = "El ID de la orden es obligatorio")
        Long ordenVentaId,

        @NotNull(message = "El porcentaje de descuento es obligatorio")
        @Positive(message = "El porcentaje debe ser mayor a 0")
        Double porcentajeDescuento
) implements Serializable {}