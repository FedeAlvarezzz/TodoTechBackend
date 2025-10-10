package co.todotech.model.dto.ordenventa;

import co.todotech.model.dto.cliente.ClienteDto;
import co.todotech.model.dto.detalleorden.DetalleOrdenDto;
import co.todotech.model.dto.usuario.UsuarioDto;
import co.todotech.model.enums.EstadoOrden;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

public record OrdenConDetallesDto(
        Long id,

        @NotBlank(message = "El número de orden es obligatorio")
        String numeroOrden,

        @NotNull(message = "La fecha es obligatoria")
        LocalDateTime fecha,

        @NotNull(message = "El cliente es obligatorio")
        ClienteDto cliente,

        @NotNull(message = "El vendedor es obligatorio")
        UsuarioDto vendedor,

        @NotNull(message = "Los productos son obligatorios")
        List<DetalleOrdenDto> productos,

        @NotNull(message = "El estado es obligatorio")
        EstadoOrden estado,

        @PositiveOrZero(message = "El subtotal debe ser mayor o igual a 0")
        Double subtotal,

        @PositiveOrZero(message = "El descuento debe ser mayor o igual a 0")
        Double descuento,

        @PositiveOrZero(message = "Los impuestos deben ser mayor o igual a 0")
        Double impuestos,

        @PositiveOrZero(message = "El total debe ser mayor o igual a 0")
        Double total,

        String observaciones
) implements Serializable {}