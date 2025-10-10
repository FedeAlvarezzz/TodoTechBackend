package co.todotech.model.dto.detalleorden;

import co.todotech.model.dto.producto.ProductoDto;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.io.Serializable;

public record DetalleOrdenDto(
        Long id,

        @NotNull(message = "El producto no puede ser nulo")
        ProductoDto producto,

        @NotNull(message = "La cantidad no puede ser nula")
        @Positive(message = "La cantidad debe ser mayor a 0")
        Integer cantidad,

        @NotNull(message = "El precio unitario no puede ser nulo")
        @Positive(message = "El precio unitario debe ser mayor a 0")
        Double precioUnitario,

        @NotNull(message = "El subtotal no puede ser nulo")
        @Positive(message = "El subtotal debe ser mayor a 0")
        Double subtotal
) implements Serializable {}