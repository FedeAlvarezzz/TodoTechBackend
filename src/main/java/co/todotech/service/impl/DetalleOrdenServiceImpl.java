package co.todotech.service.impl;

import co.todotech.exception.detalleorden.*;
import co.todotech.exception.ordenventa.OrdenNotFoundException;
import co.todotech.exception.producto.ProductoNotFoundException;
import co.todotech.mapper.DetalleOrdenMapper;
import co.todotech.model.dto.detalleorden.CreateDetalleOrdenDto;
import co.todotech.model.dto.detalleorden.DetalleOrdenDto;
import co.todotech.model.dto.detalleorden.EliminarDetalleRequest;
import co.todotech.model.dto.detalleorden.validacion.BulkStockValidationRequest;
import co.todotech.model.dto.detalleorden.validacion.BulkValidationResultDto;
import co.todotech.model.dto.detalleorden.validacion.StockValidationRequest;
import co.todotech.model.dto.detalleorden.validacion.ValidationResultDto;
import co.todotech.model.entities.DetalleOrden;
import co.todotech.model.entities.Orden;
import co.todotech.model.entities.Producto;
import co.todotech.model.enums.EstadoOrden;
import co.todotech.model.enums.EstadoProducto;
import co.todotech.repository.DetalleOrdenRepository;
import co.todotech.repository.OrdenRepository;
import co.todotech.repository.ProductoRepository;
import co.todotech.service.DetalleOrdenService;
import co.todotech.service.ProductoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DetalleOrdenServiceImpl implements DetalleOrdenService {

    private final DetalleOrdenRepository detalleOrdenRepository;
    private final OrdenRepository ordenRepository;
    private final ProductoRepository productoRepository;
    private final DetalleOrdenMapper detalleOrdenMapper;
    private final ProductoService productoService;

    // Constantes para stock crítico
    private static final int STOCK_CRITICO = 3;
    private static final int STOCK_MINIMO_CREACION = 1;
    private static final int STOCK_MINIMO_ACTUALIZACION = 1;

    @Override
    @Transactional
    public DetalleOrdenDto crearDetalleOrden(CreateDetalleOrdenDto createDetalleOrdenDto, Long ordenId) {
        log.info("Creando detalle de orden para orden ID: {} y producto ID: {}",
                ordenId, createDetalleOrdenDto.productoId());

        // Validar que la orden existe y está en estado permitido
        Orden orden = ordenRepository.findById(ordenId)
                .orElseThrow(() -> new OrdenNotFoundException(ordenId));

        validarEstadoOrdenParaModificacion(orden);

        // Validar que el producto existe
        Producto producto = productoRepository.findById(createDetalleOrdenDto.productoId())
                .orElseThrow(() -> new ProductoNotFoundException(createDetalleOrdenDto.productoId()));

        // 🔥 CORREGIDO: Usar validación que considera contexto
        validarStockMinimoParaCreacion(producto, createDetalleOrdenDto.cantidad());
        validarStockDisponible(producto, createDetalleOrdenDto.cantidad(), null);

        // Verificar si ya existe un detalle para este producto en la orden
        detalleOrdenRepository.findByOrdenIdAndProductoId(ordenId, createDetalleOrdenDto.productoId())
                .ifPresent(existingDetail -> {
                    throw new DetalleOrdenDuplicateException(ordenId, createDetalleOrdenDto.productoId());
                });

        // 🔥 ACTUALIZAR STOCK: Decrementar el stock del producto
        productoService.decrementarStock(producto.getId(), createDetalleOrdenDto.cantidad());

        // Crear el detalle de orden
        DetalleOrden detalleOrden = DetalleOrden.builder()
                .orden(orden)
                .producto(producto)
                .cantidad(createDetalleOrdenDto.cantidad())
                .precioUnitario(producto.getPrecio())
                .subtotal(0.0)
                .build();

        DetalleOrden detalleGuardado = detalleOrdenRepository.save(detalleOrden);

        // Agregar el detalle a la orden para mantener la relación bidireccional
        orden.agregarDetalle(detalleGuardado);

        // Recalcular totales de la orden
        orden.calcularTotales();
        ordenRepository.save(orden);

        log.info("Detalle de orden creado exitosamente con ID: {}. Stock del producto actualizado.", detalleGuardado.getId());
        return detalleOrdenMapper.toDto(detalleGuardado);
    }

    @Override
    @Transactional(readOnly = true)
    public DetalleOrdenDto obtenerDetalleOrden(Long id) {
        log.info("Obteniendo detalle de orden con ID: {}", id);

        DetalleOrden detalleOrden = detalleOrdenRepository.findById(id)
                .orElseThrow(() -> new DetalleOrdenNotFoundException(id));

        return detalleOrdenMapper.toDto(detalleOrden);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DetalleOrdenDto> obtenerDetallesPorOrden(Long ordenId) {
        log.info("Obteniendo detalles para orden ID: {}", ordenId);

        // Validar que la orden existe
        if (!ordenRepository.existsById(ordenId)) {
            throw new OrdenNotFoundException(ordenId);
        }

        List<DetalleOrden> detalles = detalleOrdenRepository.findByOrdenId(ordenId);

        // Validar que hay detalles para esta orden
        if (detalles.isEmpty()) {
            throw new DetallesNoEncontradosException(ordenId);
        }

        return detalles.stream()
                .map(detalleOrdenMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public DetalleOrdenDto actualizarCantidad(Long detalleId, Integer nuevaCantidad) {
        log.info("Actualizando cantidad del detalle ID: {} a {}", detalleId, nuevaCantidad);

        if (nuevaCantidad <= 0) {
            throw new DetalleOrdenBusinessException("La cantidad debe ser mayor a 0");
        }

        DetalleOrden detalleOrden = detalleOrdenRepository.findById(detalleId)
                .orElseThrow(() -> new DetalleOrdenNotFoundException(detalleId));

        // Validar que la orden esté en estado permitido
        validarEstadoOrdenParaModificacion(detalleOrden.getOrden());

        Producto producto = detalleOrden.getProducto();
        Integer cantidadActual = detalleOrden.getCantidad();

        // 🔥 CORREGIDO: Validar considerando unidades ya reservadas
        validarStockMinimoParaActualizacion(producto, nuevaCantidad, detalleId);

        // Calcular la diferencia de cantidad
        int diferencia = nuevaCantidad - cantidadActual;

        if (diferencia != 0) {
            // 🔥 CORREGIDO: Validar stock disponible considerando reservas existentes
            if (diferencia > 0) {
                validarStockDisponible(producto, diferencia, detalleId);
            }

            // 🔥 ACTUALIZAR STOCK: Ajustar según la diferencia
            if (diferencia > 0) {
                // Si se aumenta la cantidad, decrementar stock
                productoService.decrementarStock(producto.getId(), diferencia);
            } else {
                // Si se disminuye la cantidad, incrementar stock
                productoService.incrementarStock(producto.getId(), Math.abs(diferencia));
            }
        }

        // Actualizar cantidad
        detalleOrden.setCantidad(nuevaCantidad);

        DetalleOrden detalleActualizado = detalleOrdenRepository.save(detalleOrden);

        // Recalcular totales de la orden
        Orden orden = detalleOrden.getOrden();
        orden.calcularTotales();
        ordenRepository.save(orden);

        log.info("Cantidad actualizada exitosamente para detalle ID: {}. Stock del producto ajustado.", detalleId);
        return detalleOrdenMapper.toDto(detalleActualizado);
    }

    @Override
    @Transactional
    public DetalleOrdenDto actualizarDetalleOrden(Long id, DetalleOrdenDto detalleOrdenDto) {
        log.info("Actualizando detalle de orden con ID: {}", id);

        DetalleOrden detalleExistente = detalleOrdenRepository.findById(id)
                .orElseThrow(() -> new DetalleOrdenNotFoundException(id));

        // Validar que la orden esté en estado permitido
        validarEstadoOrdenParaModificacion(detalleExistente.getOrden());

        Producto producto = detalleExistente.getProducto();
        Integer cantidadActual = detalleExistente.getCantidad();

        // Validar stock si se está actualizando la cantidad
        if (detalleOrdenDto.cantidad() != null && !detalleOrdenDto.cantidad().equals(cantidadActual)) {
            Integer nuevaCantidad = detalleOrdenDto.cantidad();

            // 🔥 CORREGIDO: Validar considerando unidades ya reservadas
            validarStockMinimoParaActualizacion(producto, nuevaCantidad, id);

            // Calcular la diferencia de cantidad
            int diferencia = nuevaCantidad - cantidadActual;

            if (diferencia != 0) {
                // 🔥 CORREGIDO: Validar stock disponible considerando reservas existentes
                if (diferencia > 0) {
                    validarStockDisponible(producto, diferencia, id);
                }

                // 🔥 ACTUALIZAR STOCK: Ajustar según la diferencia
                if (diferencia > 0) {
                    productoService.decrementarStock(producto.getId(), diferencia);
                } else {
                    productoService.incrementarStock(producto.getId(), Math.abs(diferencia));
                }
            }
        }

        // Actualizar campos permitidos
        detalleOrdenMapper.updateDetalleOrdenFromDto(detalleOrdenDto, detalleExistente);

        DetalleOrden detalleActualizado = detalleOrdenRepository.save(detalleExistente);

        // Recalcular totales de la orden
        Orden orden = detalleExistente.getOrden();
        orden.calcularTotales();
        ordenRepository.save(orden);

        log.info("Detalle de orden actualizado exitosamente con ID: {}. Stock del producto ajustado si fue necesario.", id);
        return detalleOrdenMapper.toDto(detalleActualizado);
    }

    @Override
    @Transactional
    public void eliminarDetalleOrden(Long id) {
        log.info("Eliminando detalle de orden con ID: {}", id);

        DetalleOrden detalleOrden = detalleOrdenRepository.findById(id)
                .orElseThrow(() -> new DetalleOrdenNotFoundException(id));

        // Validar que la orden esté en estado permitido
        validarEstadoOrdenParaModificacion(detalleOrden.getOrden());

        Producto producto = detalleOrden.getProducto();
        Integer cantidad = detalleOrden.getCantidad();

        Orden orden = detalleOrden.getOrden();

        // 🔥 ACTUALIZAR STOCK: Incrementar el stock al eliminar el detalle
        productoService.incrementarStock(producto.getId(), cantidad);

        // Remover el detalle de la orden
        orden.removerDetalle(detalleOrden);

        // Eliminar el detalle
        detalleOrdenRepository.delete(detalleOrden);

        // Recalcular totales de la orden
        orden.calcularTotales();
        ordenRepository.save(orden);

        log.info("Detalle de orden eliminado exitosamente con ID: {}. Stock del producto restaurado.", id);
    }

    @Override
    @Transactional
    public void eliminarDetallePorProductoYOrden(EliminarDetalleRequest request) {
        log.info("Eliminando detalle para producto ID: {} y orden ID: {}",
                request.productoId(), request.ordenVentaId());

        DetalleOrden detalleOrden = detalleOrdenRepository.findByOrdenIdAndProductoId(
                        request.ordenVentaId(), request.productoId())
                .orElseThrow(() -> new DetalleOrdenNotFoundException(
                        request.ordenVentaId(), request.productoId()));

        eliminarDetalleOrden(detalleOrden.getId());
    }

    // 🔥 CORREGIDO: Método mejorado con nuevas excepciones
    @Override
    @Transactional(readOnly = true)
    public void validarStockDisponible(Long productoId, Integer cantidadRequerida) {
        log.debug("Validando stock para producto ID: {}, cantidad requerida: {}", productoId, cantidadRequerida);

        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new ProductoNotFoundException(productoId));

        validarStockDisponible(producto, cantidadRequerida, null);
    }

    // 🔥 CORREGIDO COMPLETAMENTE: Método sobrecargado que considera unidades reservadas
    private void validarStockDisponible(Producto producto, Integer cantidadRequerida, Long detalleOrdenId) {
        // Validar estado del producto
        if (producto.getEstado() != EstadoProducto.ACTIVO) {
            throw new ProductoNoDisponibleException(producto.getNombre(), producto.getEstado().toString());
        }

        // 🔥 NUEVA LÓGICA: Calcular stock disponible real considerando reservas
        Integer stockDisponibleReal = calcularStockDisponibleReal(producto, detalleOrdenId);

        // Validar stock disponible
        if (stockDisponibleReal < cantidadRequerida) {
            throw new StockInsufficientException(producto.getNombre(), stockDisponibleReal, cantidadRequerida);
        }
    }

    // 🔥 NUEVO: Calcular stock disponible real considerando reservas existentes
    private Integer calcularStockDisponibleReal(Producto producto, Long detalleOrdenIdExcluir) {
        Integer stockTotal = producto.getStock();

        // Si no hay detalleOrdenId, es una creación nueva - usar stock total
        if (detalleOrdenIdExcluir == null) {
            return stockTotal;
        }

        // 🔥 LÓGICA CORREGIDA: Para actualización, considerar que ya tenemos unidades reservadas
        // Stock disponible real = Stock total + Cantidad ya reservada en este detalle
        // Porque al modificar, primero liberamos lo reservado y luego reservamos lo nuevo

        // Obtener la cantidad actual del detalle que se está modificando
        Integer cantidadReservadaActual = detalleOrdenRepository.findById(detalleOrdenIdExcluir)
                .map(DetalleOrden::getCantidad)
                .orElse(0);

        // Stock disponible = Stock actual + Lo que ya tenemos reservado
        return stockTotal + cantidadReservadaActual;
    }

    // 🔥 CORREGIDO COMPLETAMENTE: Validación de stock para detalle
    @Override
    @Transactional(readOnly = true)
    public ValidationResultDto validarStockParaDetalle(StockValidationRequest request) {
        log.info("Validando stock para producto ID: {}, cantidad: {}, detalle: {}",
                request.productoId(), request.cantidad(), request.detalleOrdenId());

        try {
            Producto producto = productoRepository.findById(request.productoId())
                    .orElseThrow(() -> new ProductoNotFoundException(request.productoId()));

            // Validar estado del producto
            if (producto.getEstado() != EstadoProducto.ACTIVO) {
                return ValidationResultDto.productoNoDisponible();
            }

            // 🔥 CORREGIDO: Calcular stock disponible real
            Integer stockDisponibleReal = calcularStockDisponibleReal(producto, request.detalleOrdenId());

            // Validar stock mínimo según el contexto
            if (request.detalleOrdenId() == null) {
                // Validación para creación
                validarStockMinimoParaCreacion(producto, request.cantidad());
            } else {
                // Validación para actualización
                validarStockMinimoParaActualizacion(producto, request.cantidad(), request.detalleOrdenId());
            }

            // 🔥 CORREGIDO: Validar stock disponible considerando reservas
            if (stockDisponibleReal < request.cantidad()) {
                throw new StockInsufficientException(producto.getNombre(), stockDisponibleReal, request.cantidad());
            }

            // Si pasa todas las validaciones
            return ValidationResultDto.valido(
                    "Stock suficiente para la operación",
                    stockDisponibleReal
            );

        } catch (StockInsufficientException | StockCriticalException | ProductoNoDisponibleException e) {
            // Capturar excepciones específicas de stock y convertirlas en resultado de validación
            Producto producto = productoRepository.findById(request.productoId()).orElse(null);
            Integer stockActual = producto != null ? producto.getStock() : 0;

            return ValidationResultDto.invalido(
                    e.getMessage(),
                    stockActual,
                    request.cantidad()
            );
        } catch (Exception e) {
            return ValidationResultDto.invalido(
                    e.getMessage(),
                    0,
                    request.cantidad()
            );
        }
    }

    // 🔥 NUEVO: Validación múltiple de stock
    @Override
    @Transactional(readOnly = true)
    public BulkValidationResultDto validarStockMultiple(BulkStockValidationRequest request) {
        log.info("Validando stock para {} productos", request.validaciones().size());

        Map<Long, ValidationResultDto> resultados = request.validaciones().stream()
                .collect(Collectors.toMap(
                        StockValidationRequest::productoId,
                        this::validarStockParaDetalle
                ));

        List<Long> productosConProblemas = resultados.entrySet().stream()
                .filter(entry -> !entry.getValue().isValido())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        int totalProductos = request.validaciones().size();
        int productosValidos = totalProductos - productosConProblemas.size();

        return BulkValidationResultDto.builder()
                .todoValido(productosConProblemas.isEmpty())
                .mensajeGeneral(productosConProblemas.isEmpty() ?
                        "Todos los productos tienen stock suficiente" :
                        String.format("%d productos tienen problemas de stock", productosConProblemas.size()))
                .resultados(resultados)
                .productosConProblemas(productosConProblemas)
                .totalProductos(totalProductos)
                .productosValidos(productosValidos)
                .productosInvalidos(productosConProblemas.size())
                .build();
    }

    // 🔥 NUEVO: Obtener productos con stock crítico
    @Override
    @Transactional(readOnly = true)
    public List<ValidationResultDto> obtenerProductosStockCritico() {
        log.info("Obteniendo productos con stock crítico");

        List<Producto> productosCriticos = productoRepository.findByStockLessThanEqualAndEstado(
                STOCK_CRITICO, EstadoProducto.ACTIVO);

        return productosCriticos.stream()
                .map(producto -> ValidationResultDto.builder()
                        .valido(false)
                        .mensaje(String.format("Stock crítico: Solo quedan %d unidades", producto.getStock()))
                        .stockActual(producto.getStock())
                        .stockDisponible(producto.getStock())
                        .stockCritico(true)
                        .accionRecomendada("Reabastecer inventario")
                        .build())
                .collect(Collectors.toList());
    }

    // 🔥 NUEVO: Obtener stock disponible de un producto
    @Override
    @Transactional(readOnly = true)
    public ValidationResultDto obtenerStockDisponible(Long productoId) {
        log.info("Obteniendo stock disponible para producto ID: {}", productoId);

        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new ProductoNotFoundException(productoId));

        if (producto.getEstado() != EstadoProducto.ACTIVO) {
            return ValidationResultDto.productoNoDisponible();
        }

        return ValidationResultDto.valido(
                "Stock disponible obtenido",
                producto.getStock()
        );
    }

    // 🔥 CORREGIDO: Validar stock mínimo para creación con nuevas excepciones
    private void validarStockMinimoParaCreacion(Producto producto, Integer cantidad) {
        if (producto.getStock() < STOCK_MINIMO_CREACION) {
            throw new StockInsufficientException(
                    producto.getNombre(), producto.getStock(), STOCK_MINIMO_CREACION);
        }

        if (producto.getStock() == 1 && cantidad > 1) {
            throw new StockCriticalException(
                    producto.getNombre(), producto.getStock());
        }
    }

    // 🔥 CORREGIDO: Validar stock mínimo para actualización con nuevas excepciones
    private void validarStockMinimoParaActualizacion(Producto producto, Integer nuevaCantidad, Long detalleOrdenId) {
        // 🔥 CORREGIDO: Calcular stock disponible real
        Integer stockDisponibleReal = calcularStockDisponibleReal(producto, detalleOrdenId);

        if (stockDisponibleReal < STOCK_MINIMO_ACTUALIZACION) {
            throw new StockInsufficientException(
                    producto.getNombre(), stockDisponibleReal, STOCK_MINIMO_ACTUALIZACION);
        }

        // Obtener la cantidad actual del detalle
        Integer cantidadActual = detalleOrdenRepository.findById(detalleOrdenId)
                .map(DetalleOrden::getCantidad)
                .orElse(0);

        // Si el stock real es igual a la cantidad actual, solo permitir mantener o reducir
        if (stockDisponibleReal == cantidadActual && nuevaCantidad > cantidadActual) {
            throw new StockCriticalException(
                    producto.getNombre(), stockDisponibleReal, cantidadActual);
        }
    }

    // 🔥 NUEVO: Validar estado de orden para modificación
    private void validarEstadoOrdenParaModificacion(Orden orden) {
        if (orden.getEstado() != EstadoOrden.PENDIENTE && orden.getEstado() != EstadoOrden.AGREGANDOPRODUCTOS) {
            throw new DetalleOrdenEstadoException(
                    "Solo se pueden modificar detalles de órdenes en estado PENDIENTE o AGREGANDOPRODUCTOS. Estado actual: " + orden.getEstado(),
                    "PENDIENTE o AGREGANDOPRODUCTOS"
            );
        }
    }

    // 🔥 CORREGIDO: Métodos adicionales
    @Transactional(readOnly = true)
    public List<Producto> obtenerProductosDisponibles() {
        return productoRepository.findProductosDisponibles();
    }

    @Transactional(readOnly = true)
    public List<Producto> obtenerProductosPorEstado(EstadoProducto estado) {
        return productoRepository.findAllByEstado(estado);
    }

    @Transactional(readOnly = true)
    public boolean esProductoDisponible(Long productoId) {
        return productoRepository.findById(productoId)
                .map(producto -> producto.getEstado() == EstadoProducto.ACTIVO && producto.getStock() > 0)
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public Integer obtenerCantidadStockDisponible(Long productoId) {
        return productoRepository.findById(productoId)
                .map(Producto::getStock)
                .orElse(0);
    }
}