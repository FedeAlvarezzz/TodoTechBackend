package co.todotech.service.impl;

import co.todotech.mapper.DetalleOrdenMapper;
import co.todotech.model.dto.detalleorden.CreateDetalleOrdenDto;
import co.todotech.model.dto.detalleorden.DetalleOrdenDto;
import co.todotech.model.dto.detalleorden.EliminarDetalleRequest;
import co.todotech.model.entities.DetalleOrden;
import co.todotech.model.entities.Orden;
import co.todotech.model.entities.Producto;
import co.todotech.model.enums.EstadoOrden;
import co.todotech.model.enums.EstadoProducto;
import co.todotech.repository.DetalleOrdenRepository;
import co.todotech.repository.OrdenRepository;
import co.todotech.repository.ProductoRepository;
import co.todotech.service.DetalleOrdenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DetalleOrdenServiceImpl implements DetalleOrdenService {

    private final DetalleOrdenRepository detalleOrdenRepository;
    private final OrdenRepository ordenRepository;
    private final ProductoRepository productoRepository;
    private final DetalleOrdenMapper detalleOrdenMapper;

    @Override
    @Transactional
    public DetalleOrdenDto crearDetalleOrden(CreateDetalleOrdenDto createDetalleOrdenDto, Long ordenId) {
        log.info("Creando detalle de orden para orden ID: {} y producto ID: {}",
                ordenId, createDetalleOrdenDto.productoId());

        // Validar que la orden existe y está en estado PENDIENTE
        Orden orden = ordenRepository.findById(ordenId)
                .orElseThrow(() -> new RuntimeException("Orden no encontrada con ID: " + ordenId));

        if (orden.getEstado() != EstadoOrden.PENDIENTE) {
            throw new RuntimeException("Solo se pueden agregar detalles a órdenes en estado PENDIENTE. Estado actual: " + orden.getEstado());
        }

        // Validar que el producto existe
        Producto producto = productoRepository.findById(createDetalleOrdenDto.productoId())
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + createDetalleOrdenDto.productoId()));

        // Validar stock disponible y estado del producto
        validarStockDisponible(createDetalleOrdenDto.productoId(), createDetalleOrdenDto.cantidad());

        // Verificar si ya existe un detalle para este producto en la orden
        detalleOrdenRepository.findByOrdenIdAndProductoId(ordenId, createDetalleOrdenDto.productoId())
                .ifPresent(existingDetail -> {
                    throw new RuntimeException("Ya existe un detalle para este producto en la orden. Use actualizar cantidad en su lugar.");
                });

        // Crear el detalle de orden
        DetalleOrden detalleOrden = DetalleOrden.builder()
                .orden(orden)
                .producto(producto)
                .cantidad(createDetalleOrdenDto.cantidad())
                .precioUnitario(producto.getPrecio()) // Usar el precio actual del producto
                .subtotal(0.0) // Se calculará automáticamente con @PrePersist
                .build();

        // El subtotal se calculará automáticamente con @PrePersist
        DetalleOrden detalleGuardado = detalleOrdenRepository.save(detalleOrden);

        // Agregar el detalle a la orden para mantener la relación bidireccional
        orden.agregarDetalle(detalleGuardado);

        // Recalcular totales de la orden
        orden.calcularTotales();
        ordenRepository.save(orden);

        log.info("Detalle de orden creado exitosamente con ID: {}", detalleGuardado.getId());
        return detalleOrdenMapper.toDto(detalleGuardado);
    }

    @Override
    @Transactional(readOnly = true)
    public DetalleOrdenDto obtenerDetalleOrden(Long id) {
        log.info("Obteniendo detalle de orden con ID: {}", id);

        DetalleOrden detalleOrden = detalleOrdenRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Detalle de orden no encontrado con ID: " + id));

        return detalleOrdenMapper.toDto(detalleOrden);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DetalleOrdenDto> obtenerDetallesPorOrden(Long ordenId) {
        log.info("Obteniendo detalles para orden ID: {}", ordenId);

        // Validar que la orden existe
        if (!ordenRepository.existsById(ordenId)) {
            throw new RuntimeException("Orden no encontrada con ID: " + ordenId);
        }

        return detalleOrdenRepository.findByOrdenId(ordenId)
                .stream()
                .map(detalleOrdenMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public DetalleOrdenDto actualizarCantidad(Long detalleId, Integer nuevaCantidad) {
        log.info("Actualizando cantidad del detalle ID: {} a {}", detalleId, nuevaCantidad);

        if (nuevaCantidad <= 0) {
            throw new RuntimeException("La cantidad debe ser mayor a 0");
        }

        DetalleOrden detalleOrden = detalleOrdenRepository.findById(detalleId)
                .orElseThrow(() -> new RuntimeException("Detalle de orden no encontrado con ID: " + detalleId));

        // Validar que la orden esté en estado PENDIENTE
        if (detalleOrden.getOrden().getEstado() != EstadoOrden.PENDIENTE) {
            throw new RuntimeException("Solo se pueden modificar detalles de órdenes en estado PENDIENTE");
        }

        // Validar stock disponible para la nueva cantidad
        validarStockDisponible(detalleOrden.getProducto().getId(), nuevaCantidad);

        // Actualizar cantidad
        detalleOrden.setCantidad(nuevaCantidad);
        // El subtotal se recalculará automáticamente con @PreUpdate

        DetalleOrden detalleActualizado = detalleOrdenRepository.save(detalleOrden);

        // Recalcular totales de la orden
        Orden orden = detalleOrden.getOrden();
        orden.calcularTotales();
        ordenRepository.save(orden);

        log.info("Cantidad actualizada exitosamente para detalle ID: {}", detalleId);
        return detalleOrdenMapper.toDto(detalleActualizado);
    }

    @Override
    @Transactional
    public DetalleOrdenDto actualizarDetalleOrden(Long id, DetalleOrdenDto detalleOrdenDto) {
        log.info("Actualizando detalle de orden con ID: {}", id);

        DetalleOrden detalleExistente = detalleOrdenRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Detalle de orden no encontrado con ID: " + id));

        // Validar que la orden esté en estado PENDIENTE
        if (detalleExistente.getOrden().getEstado() != EstadoOrden.PENDIENTE) {
            throw new RuntimeException("Solo se pueden modificar detalles de órdenes en estado PENDIENTE");
        }

        // Validar stock si se está actualizando la cantidad
        if (detalleOrdenDto.cantidad() != null && !detalleOrdenDto.cantidad().equals(detalleExistente.getCantidad())) {
            validarStockDisponible(detalleExistente.getProducto().getId(), detalleOrdenDto.cantidad());
        }

        // Actualizar campos permitidos
        detalleOrdenMapper.updateDetalleOrdenFromDto(detalleOrdenDto, detalleExistente);

        // El subtotal se recalculará automáticamente con @PreUpdate
        DetalleOrden detalleActualizado = detalleOrdenRepository.save(detalleExistente);

        // Recalcular totales de la orden
        Orden orden = detalleExistente.getOrden();
        orden.calcularTotales();
        ordenRepository.save(orden);

        log.info("Detalle de orden actualizado exitosamente con ID: {}", id);
        return detalleOrdenMapper.toDto(detalleActualizado);
    }

    @Override
    @Transactional
    public void eliminarDetalleOrden(Long id) {
        log.info("Eliminando detalle de orden con ID: {}", id);

        DetalleOrden detalleOrden = detalleOrdenRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Detalle de orden no encontrado con ID: " + id));

        // Validar que la orden esté en estado PENDIENTE
        if (detalleOrden.getOrden().getEstado() != EstadoOrden.PENDIENTE) {
            throw new RuntimeException("Solo se pueden eliminar detalles de órdenes en estado PENDIENTE");
        }

        Orden orden = detalleOrden.getOrden();

        // Remover el detalle de la orden
        orden.removerDetalle(detalleOrden);

        // Eliminar el detalle
        detalleOrdenRepository.delete(detalleOrden);

        // Recalcular totales de la orden
        orden.calcularTotales();
        ordenRepository.save(orden);

        log.info("Detalle de orden eliminado exitosamente con ID: {}", id);
    }

    @Override
    @Transactional
    public void eliminarDetallePorProductoYOrden(EliminarDetalleRequest request) {
        log.info("Eliminando detalle para producto ID: {} y orden ID: {}",
                request.productoId(), request.ordenVentaId());

        DetalleOrden detalleOrden = detalleOrdenRepository.findByOrdenIdAndProductoId(
                        request.ordenVentaId(), request.productoId())
                .orElseThrow(() -> new RuntimeException("Detalle de orden no encontrado para el producto y orden especificados"));

        eliminarDetalleOrden(detalleOrden.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public void validarStockDisponible(Long productoId, Integer cantidadRequerida) {
        log.debug("Validando stock para producto ID: {}, cantidad requerida: {}", productoId, cantidadRequerida);

        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + productoId));

        // Validar estado del producto
        if (producto.getEstado() != EstadoProducto.ACTIVO) {
            throw new RuntimeException(
                    String.format("El producto '%s' no está disponible para la venta. Estado actual: %s",
                            producto.getNombre(), producto.getEstado())
            );
        }

        // Validar stock disponible
        if (producto.getStock() < cantidadRequerida) {
            throw new RuntimeException(
                    String.format("Stock insuficiente para el producto '%s'. Stock disponible: %d, Cantidad requerida: %d",
                            producto.getNombre(), producto.getStock(), cantidadRequerida)
            );
        }
    }

    // Método adicional para obtener productos disponibles
    @Transactional(readOnly = true)
    public List<Producto> obtenerProductosDisponibles() {
        return productoRepository.findProductosDisponibles();
    }

    // Método para obtener productos por estado
    @Transactional(readOnly = true)
    public List<Producto> obtenerProductosPorEstado(EstadoProducto estado) {
        return productoRepository.findAllByEstado(estado);
    }

    // Método para verificar disponibilidad de un producto específico
    @Transactional(readOnly = true)
    public boolean esProductoDisponible(Long productoId) {
        return productoRepository.findById(productoId)
                .map(producto -> producto.getEstado() == EstadoProducto.ACTIVO && producto.getStock() > 0)
                .orElse(false);
    }

    // Método para obtener el stock disponible de un producto
    @Transactional(readOnly = true)
    public Integer obtenerStockDisponible(Long productoId) {
        return productoRepository.findById(productoId)
                .map(Producto::getStock)
                .orElse(0);
    }
}