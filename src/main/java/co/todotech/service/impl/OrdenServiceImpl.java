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

        // Validar que el cliente existe
        Cliente cliente = clienteRepository.findById(createOrdenDto.clienteId())
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado con ID: " + createOrdenDto.clienteId()));

        // Validar que el vendedor existe
        Usuario vendedor = usuarioRepository.findById(createOrdenDto.vendedorId())
                .orElseThrow(() -> new RuntimeException("Vendedor no encontrado con ID: " + createOrdenDto.vendedorId()));

        // Crear la orden con valores iniciales en cero
        Orden orden = Orden.builder()
                .numeroOrden(generarNumeroOrden())
                .fecha(LocalDateTime.now())
                .cliente(cliente)
                .vendedor(vendedor)
                .productos(new ArrayList<>()) // Lista vacía de detalles
                .estado(EstadoOrden.PENDIENTE)
                .subtotal(0.0)
                .descuento(0.0)
                .impuestos(0.0)
                .total(0.0)
                .observaciones(null)
                .build();

        Orden ordenGuardada = ordenRepository.save(orden);
        log.info("Orden creada exitosamente con ID: {}", ordenGuardada.getId());

        return ordenMapper.toDto(ordenGuardada);
    }

    @Override
    @Transactional(readOnly = true)
    public OrdenConDetallesDto obtenerOrdenConDetalles(Long id) {
        log.info("Obteniendo orden con detalles para ID: {}", id);

        Orden orden = ordenRepository.findByIdWithDetalles(id)
                .orElseThrow(() -> new RuntimeException("Orden no encontrada con ID: " + id));

        // Mapear a DTO con detalles usando los mappers
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

        Orden ordenExistente = ordenRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Orden no encontrada con ID: " + id));

        // Validar que no se esté intentando modificar una orden ya CERRADA
        if (ordenExistente.getEstado() == EstadoOrden.CERRADA) {
            throw new RuntimeException("No se puede modificar una orden en estado CERRADA");
        }

        // Actualizar campos permitidos
        ordenMapper.updateOrdenFromDto(ordenDto, ordenExistente);

        // Recalcular totales si hay cambios que los afecten
        ordenExistente.calcularTotales();

        Orden ordenActualizada = ordenRepository.save(ordenExistente);
        log.info("Orden actualizada exitosamente con ID: {}", ordenActualizada.getId());

        return ordenMapper.toDto(ordenActualizada);
    }

    @Override
    @Transactional
    public OrdenDto actualizarEstadoOrden(Long id, EstadoOrden nuevoEstado) {
        log.info("Actualizando estado de orden ID: {} a {}", id, nuevoEstado);

        Orden orden = ordenRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Orden no encontrada con ID: " + id));

        // Validar transiciones de estado
        validarTransicionEstado(orden.getEstado(), nuevoEstado);

        orden.setEstado(nuevoEstado);

        // Si la orden se marca como PAGADA, ENTREGADA o CERRADA, podemos realizar acciones adicionales
        if (nuevoEstado == EstadoOrden.PAGADA) {
            log.info("Orden ID: {} marcada como pagada", id);
            // Aquí podrías integrar con sistema de pagos, enviar notificaciones, etc.
        } else if (nuevoEstado == EstadoOrden.ENTREGADA) {
            log.info("Orden ID: {} marcada como entregada", id);
            // Aquí podrías actualizar inventario, enviar notificaciones de entrega, etc.
        } else if (nuevoEstado == EstadoOrden.CERRADA) {
            log.info("Orden ID: {} marcada como cerrada", id);
            // Orden completada completamente, posiblemente generar factura
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

        // Validar que solo se puedan eliminar órdenes en estado PENDIENTE
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

        Orden orden = ordenRepository.findById(ordenId)
                .orElseThrow(() -> new RuntimeException("Orden no encontrada con ID: " + ordenId));

        // Validar que la orden esté en estado PENDIENTE para aplicar descuentos
        if (orden.getEstado() != EstadoOrden.PENDIENTE) {
            throw new RuntimeException("Solo se pueden aplicar descuentos a órdenes en estado PENDIENTE");
        }

        // Validar que el porcentaje sea válido
        if (porcentajeDescuento <= 0 || porcentajeDescuento > 100) {
            throw new RuntimeException("El porcentaje de descuento debe estar entre 0 y 100");
        }

        // Calcular el monto del descuento basado en el subtotal
        Double montoDescuento = orden.getSubtotal() * (porcentajeDescuento / 100);
        orden.setDescuento(montoDescuento);

        // Recalcular totales
        orden.calcularTotales();

        Orden ordenActualizada = ordenRepository.save(orden);
        log.info("Descuento aplicado exitosamente. Orden ID: {}, Monto descuento: {}", ordenId, montoDescuento);

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
        // Generar número de orden único con formato: ORD-YYYYMMDD-UUID
        String fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String uuid = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "ORD-" + fecha + "-" + uuid;
    }

    private void validarTransicionEstado(EstadoOrden estadoActual, EstadoOrden nuevoEstado) {
        // Validar transiciones de estado lógicas
        if (estadoActual == EstadoOrden.CERRADA) {
            throw new RuntimeException("No se puede modificar el estado de una orden CERRADA");
        }

        // Validar flujo lógico: PENDIENTE -> PAGADA -> ENTREGADA -> CERRADA
        if (estadoActual == EstadoOrden.PENDIENTE &&
                (nuevoEstado == EstadoOrden.ENTREGADA || nuevoEstado == EstadoOrden.CERRADA)) {
            throw new RuntimeException("No se puede saltar de PENDIENTE a " + nuevoEstado + ". Primero debe marcarse como PAGADA");
        }

        if (estadoActual == EstadoOrden.PAGADA && nuevoEstado == EstadoOrden.CERRADA) {
            throw new RuntimeException("No se puede saltar de PAGADA a CERRADA. Primero debe marcarse como ENTREGADA");
        }

        // No permitir retrocesos en el flujo
        if ((estadoActual == EstadoOrden.PAGADA && nuevoEstado == EstadoOrden.PENDIENTE) ||
                (estadoActual == EstadoOrden.ENTREGADA &&
                        (nuevoEstado == EstadoOrden.PENDIENTE || nuevoEstado == EstadoOrden.PAGADA)) ||
                (estadoActual == EstadoOrden.CERRADA && nuevoEstado != EstadoOrden.CERRADA)) {
            throw new RuntimeException("No se puede retroceder el estado de la orden");
        }
    }

    private OrdenConDetallesDto mapToOrdenConDetallesDto(Orden orden) {
        // Mapear los detalles usando el DetalleOrdenMapper
        List<DetalleOrdenDto> detallesDto = orden.getProductos().stream()
                .map(detalleOrdenMapper::toDto)
                .collect(Collectors.toList());

        // Mapear cliente y vendedor usando sus respectivos mappers
        ClienteDto clienteDto = clienteMapper.toDto(orden.getCliente());
        UsuarioDto vendedorDto = usuarioMapper.toDtoSafe(orden.getVendedor()); // Usamos el método seguro

        // Crear el DTO con los detalles mapeados correctamente
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
}