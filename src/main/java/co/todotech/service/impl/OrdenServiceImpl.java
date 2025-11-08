package co.todotech.service.impl;

import co.todotech.mapper.ClienteMapper;
import co.todotech.mapper.DetalleOrdenMapper;
import co.todotech.mapper.OrdenMapper;
import co.todotech.mapper.UsuarioMapper;
import co.todotech.model.dto.cliente.ClienteDto;
import co.todotech.model.dto.detalleorden.DetalleOrdenDto;
import co.todotech.model.dto.ordenventa.CreateOrdenDto;
import co.todotech.model.dto.ordenventa.OrdenConDetallesDto;
import co.todotech.model.dto.ordenventa.OrdenDto;
import co.todotech.model.dto.usuario.UsuarioDto;
import co.todotech.model.entities.Cliente;
import co.todotech.model.entities.Orden;
import co.todotech.model.entities.Usuario;
import co.todotech.model.enums.EstadoOrden;
import co.todotech.repository.ClienteRepository;
import co.todotech.repository.OrdenRepository;
import co.todotech.repository.UsuarioRepository;
import co.todotech.service.OrdenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrdenServiceImpl implements OrdenService {

    private final OrdenRepository ordenRepository;
    private final ClienteRepository clienteRepository;
    private final UsuarioRepository usuarioRepository;
    private final OrdenMapper ordenMapper;
    private final DetalleOrdenMapper detalleOrdenMapper;
    private final ClienteMapper clienteMapper;
    private final UsuarioMapper usuarioMapper;


    @Override
    @Transactional
    public OrdenDto crearOrden(CreateOrdenDto createOrdenDto) {
        log.info("Creando nueva orden para cliente: {}", createOrdenDto.clienteId());
        log.info("Descuento recibido en DTO: {}", createOrdenDto.descuento());

        Cliente cliente = clienteRepository.findById(createOrdenDto.clienteId())
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado con ID: " + createOrdenDto.clienteId()));

        Usuario vendedor = usuarioRepository.findById(createOrdenDto.vendedorId())
                .orElseThrow(() -> new RuntimeException("Vendedor no encontrado con ID: " + createOrdenDto.vendedorId()));

        // ✅ CONVERTIR: Si el descuento es porcentaje (0-100), convertirlo a monto
        Double descuentoAplicar = createOrdenDto.descuento();

        // Si el descuento es <= 100, asumimos que es porcentaje
        if (descuentoAplicar != null && descuentoAplicar <= 100) {
            log.info("Descuento interpretado como porcentaje: {}%", descuentoAplicar);
            // El monto real se calculará cuando se agreguen productos
            // Por ahora guardamos el porcentaje
        }

        Orden orden = Orden.builder()
                .numeroOrden(generarNumeroOrden())
                .fecha(LocalDateTime.now())
                .cliente(cliente)
                .vendedor(vendedor)
                .productos(new ArrayList<>())
                .estado(EstadoOrden.PENDIENTE)
                .subtotal(0.0)
                .descuento(descuentoAplicar) // ✅ Puede ser porcentaje o monto
                .impuestos(0.0)
                .total(0.0)
                .observaciones(null)
                .build();

        log.info("Orden antes de guardar - Descuento: {}", orden.getDescuento());

        Orden ordenGuardada = ordenRepository.save(orden);

        log.info("Orden guardada en BD - ID: {}, Descuento: {}",
                ordenGuardada.getId(), ordenGuardada.getDescuento());

        return ordenMapper.toDto(ordenGuardada);
    }

    @Override
    @Transactional(readOnly = true)
    public OrdenConDetallesDto obtenerOrdenConDetalles(Long id) {
        log.info("Obteniendo orden con detalles para ID: {}", id);

        Orden orden = ordenRepository.findByIdWithDetalles(id)
                .orElseThrow(() -> new RuntimeException("Orden no encontrada con ID: " + id));

        return mapToOrdenConDetallesDto(orden);
    }

    @Override
    @Transactional(readOnly = true)
    public OrdenDto obtenerOrden(Long id) {
        log.info("Obteniendo orden con ID: {}", id);

        Orden orden = ordenRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Orden no encontrada con ID: " + id));

        return ordenMapper.toDto(orden);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrdenDto> obtenerTodasLasOrdenes() {
        log.info("Obteniendo todas las órdenes");

        return ordenRepository.findAll()
                .stream()
                .map(ordenMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrdenDto> obtenerOrdenesPorCliente(Long clienteId) {
        log.info("Obteniendo órdenes para cliente ID: {}", clienteId);

        return ordenRepository.findByClienteId(clienteId)
                .stream()
                .map(ordenMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public OrdenDto actualizarOrden(Long id, OrdenDto ordenDto) {
        log.info("Actualizando orden con ID: {}", id);

        // ✅ CORREGIDO: Cargar con detalles para cálculos precisos
        Orden ordenExistente = ordenRepository.findByIdWithDetalles(id)
                .orElseThrow(() -> new RuntimeException("Orden no encontrada con ID: " + id));

        // ✅ CORREGIDO: Usar el nuevo método de validación de la entidad
        if (!ordenExistente.puedeSerModificada()) {
            throw new RuntimeException("No se puede modificar una orden en estado: " + ordenExistente.getEstado());
        }

        // ✅ CORREGIDO: Validar descuento antes de actualizar
        if (ordenDto.descuento() != null && ordenDto.descuento() < 0) {
            throw new RuntimeException("El descuento no puede ser negativo");
        }

        // ✅ CORREGIDO: Usar mapper que NO ignora el descuento
        ordenMapper.updateOrdenFromDto(ordenDto, ordenExistente);

        // Recalcular totales automáticamente
        ordenExistente.calcularTotales();

        Orden ordenActualizada = ordenRepository.save(ordenExistente);
        log.info("Orden actualizada exitosamente con ID: {}, Descuento: {}, Total: {}",
                ordenActualizada.getId(), ordenActualizada.getDescuento(), ordenActualizada.getTotal());

        return ordenMapper.toDto(ordenActualizada);
    }

    @Override
    @Transactional
    public OrdenDto actualizarEstadoOrden(Long id, EstadoOrden nuevoEstado) {
        log.info("Actualizando estado de orden ID: {} a {}", id, nuevoEstado);

        Orden orden = ordenRepository.findByIdWithDetalles(id)
                .orElseThrow(() -> new RuntimeException("Orden no encontrada con ID: " + id));

        validarTransicionEstado(orden.getEstado(), nuevoEstado);

        orden.setEstado(nuevoEstado);

        if (nuevoEstado == EstadoOrden.PAGADA) {
            log.info("Orden ID: {} marcada como pagada", id);
        } else if (nuevoEstado == EstadoOrden.ENTREGADA) {
            log.info("Orden ID: {} marcada como entregada", id);
        } else if (nuevoEstado == EstadoOrden.CERRADA) {
            log.info("Orden ID: {} marcada como cerrada", id);
        }

        Orden ordenActualizada = ordenRepository.save(orden);

        log.info("Estado de orden actualizado exitosamente. Orden ID: {}, Nuevo estado: {}", id, nuevoEstado);
        return ordenMapper.toDto(ordenActualizada);
    }

    @Override
    @Transactional
    public void eliminarOrden(Long id) {
        log.info("Eliminando orden con ID: {}", id);

        Orden orden = ordenRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Orden no encontrada con ID: " + id));

        if (orden.getEstado() != EstadoOrden.PENDIENTE) {
            throw new RuntimeException("Solo se pueden eliminar órdenes en estado PENDIENTE. Estado actual: " + orden.getEstado());
        }

        ordenRepository.delete(orden);
        log.info("Orden eliminada exitosamente con ID: {}", id);
    }

    @Override
    @Transactional
    public OrdenDto aplicarDescuento(Long ordenId, Double porcentajeDescuento) {
        log.info("Aplicando descuento del {}% a orden ID: {}", porcentajeDescuento, ordenId);

        // ✅ CORREGIDO: Cargar con detalles para cálculo preciso
        Orden orden = ordenRepository.findByIdWithDetalles(ordenId)
                .orElseThrow(() -> new RuntimeException("Orden no encontrada con ID: " + ordenId));

        // ✅ CORREGIDO: Usar el nuevo método de validación de la entidad
        if (!orden.puedeAplicarDescuento()) {
            throw new RuntimeException("No se pueden aplicar descuentos a órdenes en estado: " + orden.getEstado());
        }

        // ✅ CORREGIDO: Validar porcentaje correctamente
        if (porcentajeDescuento < 0 || porcentajeDescuento > 100) {
            throw new RuntimeException("El porcentaje de descuento debe estar entre 0 y 100");
        }

        // ✅ CORREGIDO: Usar el nuevo método de la entidad para aplicar descuento
        try {
            orden.aplicarDescuentoPorcentaje(porcentajeDescuento);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e.getMessage());
        }

        Orden ordenActualizada = ordenRepository.save(orden);

        // ✅ NUEVO: Log detallado con información completa
        log.info("Descuento aplicado exitosamente. Orden ID: {}, Porcentaje: {}%, " +
                        "Subtotal: {}, Descuento: {}, Impuestos: {}, Total: {}",
                ordenId, porcentajeDescuento,
                ordenActualizada.getSubtotal(),
                ordenActualizada.getDescuento(),
                ordenActualizada.getImpuestos(),
                ordenActualizada.getTotal());

        return ordenMapper.toDto(ordenActualizada);
    }

    @Override
    @Transactional
    public OrdenDto quitarDescuento(Long ordenId) {
        log.info("Quitando descuento a orden ID: {}", ordenId);

        Orden orden = ordenRepository.findByIdWithDetalles(ordenId)
                .orElseThrow(() -> new RuntimeException("Orden no encontrada con ID: " + ordenId));

        // ✅ CORREGIDO: Usar el nuevo método de validación de la entidad
        if (!orden.puedeAplicarDescuento()) {
            throw new RuntimeException("No se pueden quitar descuentos a órdenes en estado: " + orden.getEstado());
        }

        // ✅ CORREGIDO: Usar el nuevo método de la entidad para quitar descuento
        orden.quitarDescuento();

        Orden ordenActualizada = ordenRepository.save(orden);

        // ✅ NUEVO: Log detallado
        log.info("Descuento quitado exitosamente. Orden ID: {}, " +
                        "Subtotal: {}, Descuento: {}, Impuestos: {}, Total: {}",
                ordenId,
                ordenActualizada.getSubtotal(),
                ordenActualizada.getDescuento(),
                ordenActualizada.getImpuestos(),
                ordenActualizada.getTotal());

        return ordenMapper.toDto(ordenActualizada);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrdenDto> obtenerOrdenesPorEstado(EstadoOrden estado) {
        log.info("Obteniendo órdenes por estado: {}", estado);

        return ordenRepository.findByEstado(estado)
                .stream()
                .map(ordenMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public OrdenDto marcarComoPagada(Long id) {
        return actualizarEstadoOrden(id, EstadoOrden.PAGADA);
    }

    @Override
    @Transactional
    public OrdenDto marcarComoEntregada(Long id) {
        return actualizarEstadoOrden(id, EstadoOrden.ENTREGADA);
    }

    @Override
    @Transactional
    public OrdenDto marcarComoCerrada(Long id) {
        return actualizarEstadoOrden(id, EstadoOrden.CERRADA);
    }

    private String generarNumeroOrden() {
        String fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String uuid = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "ORD-" + fecha + "-" + uuid;
    }

    private void validarTransicionEstado(EstadoOrden estadoActual, EstadoOrden nuevoEstado) {
        if (estadoActual == EstadoOrden.CERRADA) {
            throw new RuntimeException("No se puede modificar el estado de una orden CERRADA");
        }

        if (estadoActual == EstadoOrden.PENDIENTE &&
                (nuevoEstado == EstadoOrden.ENTREGADA || nuevoEstado == EstadoOrden.CERRADA)) {
            throw new RuntimeException("No se puede saltar de PENDIENTE a " + nuevoEstado + ". Primero debe marcarse como PAGADA");
        }

        if (estadoActual == EstadoOrden.PAGADA && nuevoEstado == EstadoOrden.CERRADA) {
            throw new RuntimeException("No se puede saltar de PAGADA a CERRADA. Primero debe marcarse como ENTREGADA");
        }

        if ((estadoActual == EstadoOrden.PAGADA && nuevoEstado == EstadoOrden.PENDIENTE) ||
                (estadoActual == EstadoOrden.ENTREGADA &&
                        (nuevoEstado == EstadoOrden.PENDIENTE || nuevoEstado == EstadoOrden.PAGADA)) ||
                (estadoActual == EstadoOrden.CERRADA && nuevoEstado != EstadoOrden.CERRADA)) {
            throw new RuntimeException("No se puede retroceder el estado de la orden");
        }
    }

    private OrdenConDetallesDto mapToOrdenConDetallesDto(Orden orden) {
        List<DetalleOrdenDto> detallesDto = orden.getProductos().stream()
                .map(detalleOrdenMapper::toDto)
                .collect(Collectors.toList());

        ClienteDto clienteDto = clienteMapper.toDto(orden.getCliente());
        UsuarioDto vendedorDto = usuarioMapper.toDtoSafe(orden.getVendedor());

        return new OrdenConDetallesDto(
                orden.getId(),
                orden.getNumeroOrden(),
                orden.getFecha(),
                clienteDto,
                vendedorDto,
                detallesDto,
                orden.getEstado(),
                orden.getSubtotal(),
                orden.getDescuento(),
                orden.getImpuestos(),
                orden.getTotal(),
                orden.getObservaciones()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrdenDto> obtenerOrdenesPorVendedor(Long vendedorId) {
        log.info("Obteniendo órdenes para vendedor ID: {}", vendedorId);

        if (!usuarioRepository.existsById(vendedorId)) {
            throw new RuntimeException("Vendedor no encontrado con ID: " + vendedorId);
        }

        return ordenRepository.findByVendedorId(vendedorId)
                .stream()
                .map(ordenMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public OrdenDto marcarComoAgregandoProductos(Long id) {
        return actualizarEstadoOrden(id, EstadoOrden.AGREGANDOPRODUCTOS);
    }

    @Override
    @Transactional
    public OrdenDto marcarComoDisponibleParaPago(Long id) {
        return actualizarEstadoOrden(id, EstadoOrden.DISPONIBLEPARAPAGO);
    }

    // ✅ NUEVO: Método para obtener información detallada del descuento
    public String obtenerInformacionDescuento(Long ordenId) {
        Orden orden = ordenRepository.findByIdWithDetalles(ordenId)
                .orElseThrow(() -> new RuntimeException("Orden no encontrada con ID: " + ordenId));

        return String.format(
                "Orden #%s - Subtotal: $%,.2f - Descuento: $%,.2f (%.1f%%) - Impuestos: $%,.2f - Total: $%,.2f",
                orden.getNumeroOrden(),
                orden.getSubtotal(),
                orden.getDescuento(),
                orden.getPorcentajeDescuento(),
                orden.getImpuestos(),
                orden.getTotal()
        );
    }

    @Override
    @Transactional
    public OrdenDto actualizarTotalOrden(Long ordenId, Double nuevoTotal) {
        log.info("Actualizando total de orden ID: {} a {}", ordenId, nuevoTotal);

        Orden orden = ordenRepository.findByIdWithDetalles(ordenId)
                .orElseThrow(() -> new RuntimeException("Orden no encontrada con ID: " + ordenId));

        // Validar que la orden esté en un estado que permita modificar el total
        if (!orden.puedeSerModificada()) {
            throw new RuntimeException("No se puede modificar el total de una orden en estado: " + orden.getEstado());
        }

        // Validar que el nuevo total sea válido
        if (nuevoTotal == null || nuevoTotal < 0) {
            throw new RuntimeException("El total debe ser un valor mayor o igual a 0");
        }

        // Actualizar el total y recalcular los otros campos si es necesario
        // En este caso, mantenemos el subtotal y descuento existentes, solo actualizamos el total
        // Esto podría variar según tu lógica de negocio
        orden.setTotal(nuevoTotal);

        // Si quieres mantener la coherencia, podrías recalcular el descuento basado en la diferencia
        // o simplemente actualizar el total directamente según tus necesidades

        Orden ordenActualizada = ordenRepository.save(orden);

        log.info("Total de orden actualizado exitosamente. Orden ID: {}, Nuevo total: {}",
                ordenId, nuevoTotal);

        return ordenMapper.toDto(ordenActualizada);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrdenDto> obtenerOrdenesDisponiblesParaPago() {
        log.info("Obteniendo órdenes disponibles para pago");

        return ordenRepository.findByEstado(EstadoOrden.DISPONIBLEPARAPAGO)
                .stream()
                .map(ordenMapper::toDto)
                .collect(Collectors.toList());
    }
}