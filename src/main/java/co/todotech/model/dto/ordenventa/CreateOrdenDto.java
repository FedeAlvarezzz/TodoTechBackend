package co.todotech.model.dto.ordenventa;

import co.todotech.model.enums.EstadoOrden;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

public record CreateOrdenDto(
        @NotNull(message = "El cliente es obligatorio")
        Long clienteId,

        @NotNull(message = "El vendedor es obligatorio")
        Long vendedorId
) implements Serializable {}