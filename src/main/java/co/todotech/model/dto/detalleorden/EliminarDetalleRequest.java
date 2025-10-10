package co.todotech.model.dto.detalleorden;

import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

public record EliminarDetalleRequest(
        @NotNull(message = "El ID del producto es obligatorio")
        Long productoId,

        @NotNull(message = "El ID de la orden es obligatorio")
        Long ordenVentaId
) implements Serializable {}