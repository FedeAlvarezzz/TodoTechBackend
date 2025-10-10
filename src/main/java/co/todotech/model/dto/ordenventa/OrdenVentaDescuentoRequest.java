package co.todotech.model.dto.ordenventa;

import co.todotech.model.dto.cliente.ClienteDto;
import co.todotech.model.dto.detalleorden.DetalleOrdenDto;
import co.todotech.model.dto.usuario.UsuarioDto;
import co.todotech.model.enums.EstadoOrden;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

public record OrdenVentaDescuentoRequest(
        Long id,
        LocalDateTime fecha,
        ClienteDto cliente,
        UsuarioDto vendedor,
        List<DetalleOrdenDto> productos,
        EstadoOrden estado,
        Double total,
        Double porcentajeDescuento
) implements Serializable {}