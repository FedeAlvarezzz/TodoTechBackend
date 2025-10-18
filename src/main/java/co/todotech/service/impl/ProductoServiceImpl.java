package co.todotech.service.impl;

import co.todotech.exception.producto.ProductoBusinessException;
import co.todotech.exception.producto.ProductoDuplicateException;
import co.todotech.exception.producto.ProductoNotFoundException;
import co.todotech.mapper.ProductoMapper;
import co.todotech.model.dto.producto.ProductoDto;
import co.todotech.model.entities.Producto;
import co.todotech.model.enums.EstadoProducto;
import co.todotech.repository.ProductoRepository;
import co.todotech.service.ProductoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductoServiceImpl implements ProductoService {

    private final ProductoMapper productoMapper;
    private final ProductoRepository productoRepository;

    @Override
    @Transactional
    public void crearProducto(ProductoDto dto) {
        log.info("Creando producto: {}", dto.getNombre());

        validarDatosCreacion(dto);

        Producto producto = productoMapper.toEntity(dto);
        establecerEstadoAutomatico(producto);

        productoRepository.save(producto);
        log.info("Producto creado exitosamente: id={}, codigo={}", producto.getId(), producto.getCodigo());
    }

    @Override
    @Transactional
    public void actualizarProducto(Long id, ProductoDto dto) {
        log.info("Actualizando producto id={}", id);

        Producto producto = obtenerProductoPorIdSeguro(id);
        validarDatosActualizacion(id, dto, producto);

        productoMapper.updateProductoFromDto(dto, producto);
        ajustarEstadoSegunStock(producto, dto.getEstado());

        productoRepository.save(producto);
        log.info("Producto actualizado: id={}, codigo={}", producto.getId(), producto.getCodigo());
    }

    @Override
    @Transactional
    public void eliminarProducto(Long id) {
        log.info("Eliminando producto id={}", id);

        Producto producto = obtenerProductoPorIdSeguro(id);

        // Validar que no tenga órdenes asociadas (opcional)
        // if (ordenRepository.existsByProductoId(id)) {
        //     throw new ProductoBusinessException("No se puede eliminar el producto porque tiene órdenes asociadas");
        // }

        productoRepository.delete(producto);
        log.info("Producto eliminado físicamente: id={}", id);
    }

    @Override
    @Transactional
    public void cambiarEstadoProducto(Long id) {
        log.info("Cambiando estado del producto id={}", id);

        Producto producto = obtenerProductoPorIdSeguro(id);
        EstadoProducto nuevoEstado = calcularNuevoEstado(producto.getEstado());

        producto.setEstado(nuevoEstado);
        productoRepository.save(producto);

        log.info("Estado del producto {} cambiado de {} a {}", id, producto.getEstado(), nuevoEstado);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductoDto obtenerProductoPorId(Long id) {
        log.debug("Buscando producto por ID: {}", id);

        Producto producto = obtenerProductoPorIdSeguro(id);
        return productoMapper.toDto(producto);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductoDto obtenerProductoPorCodigo(String codigo) {
        log.debug("Buscando producto por código: {}", codigo);

        if (codigo == null || codigo.trim().isEmpty()) {
            throw new ProductoBusinessException("El código de búsqueda no puede estar vacío");
        }

        Producto producto = productoRepository.findByCodigo(codigo.trim())
                .orElseThrow(() -> new ProductoNotFoundException("código", codigo));

        return productoMapper.toDto(producto);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductoDto obtenerProductoPorNombre(String nombre) {
        log.debug("Buscando producto por nombre: {}", nombre);

        if (nombre == null || nombre.trim().isEmpty()) {
            throw new ProductoBusinessException("El nombre de búsqueda no puede estar vacío");
        }

        Producto producto = productoRepository.findFirstByNombreIgnoreCase(nombre.trim())
                .orElseThrow(() -> new ProductoNotFoundException("nombre", nombre));

        return productoMapper.toDto(producto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductoDto> obtenerProductoPorEstado(EstadoProducto estado) {
        log.debug("Buscando productos por estado: {}", estado);

        if (estado == null) {
            throw new ProductoBusinessException("El estado no puede ser nulo");
        }

        return productoRepository.findAllByEstado(estado).stream()
                .map(productoMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductoDto> obtenerProductoPorCategoriaId(Long categoriaId) {
        log.debug("Buscando productos por categoría ID: {}", categoriaId);

        if (categoriaId == null) {
            throw new ProductoBusinessException("El ID de categoría no puede ser nulo");
        }

        return productoRepository.findAllByCategoriaId(categoriaId).stream()
                .map(productoMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductoDto> obtenerProductosActivos() {
        log.debug("Obteniendo productos activos");
        return obtenerProductoPorEstado(EstadoProducto.ACTIVO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductoDto> buscarProductosPorNombre(String nombre) {
        log.debug("Buscando productos por nombre: {}", nombre);

        if (nombre == null || nombre.trim().isEmpty()) {
            throw new ProductoBusinessException("El término de búsqueda no puede estar vacío");
        }

        return productoRepository.findByNombreContainingIgnoreCase(nombre.trim()).stream()
                .map(productoMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductoDto> obtenerProductosDisponibles() {
        log.debug("Obteniendo productos disponibles (activos y con stock)");

        return productoRepository.findProductosDisponibles().stream()
                .map(productoMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductoDto> obtenerTodosLosProductos() {
        log.debug("Obteniendo todos los productos");

        return productoRepository.findAll().stream()
                .map(productoMapper::toDto)
                .toList();
    }

    // ========== NUEVOS MÉTODOS PARA AJUSTAR STOCK ==========

    @Override
    @Transactional
    public void ajustarStockProducto(Long id, Integer cantidad, String operacion) {
        log.info("Ajustando stock del producto id={}, operación: {}, cantidad: {}", id, operacion, cantidad);

        // Validaciones básicas
        validarParametrosStock(id, cantidad, operacion);

        // Obtener el producto
        Producto producto = obtenerProductoPorIdSeguro(id);

        // Validar que el producto esté activo para operaciones de stock
        if (producto.getEstado() == EstadoProducto.INACTIVO) {
            throw new ProductoBusinessException("No se puede ajustar el stock de un producto inactivo");
        }

        // Realizar la operación correspondiente
        switch (operacion.toUpperCase()) {
            case "INCREMENTAR":
                incrementarStock(producto, cantidad);
                break;
            case "DECREMENTAR":
                decrementarStock(producto, cantidad);
                break;
            case "AJUSTAR":
                establecerStockDirecto(producto, cantidad);
                break;
            default:
                throw new ProductoBusinessException("Operación no válida. Use: INCREMENTAR, DECREMENTAR o AJUSTAR");
        }

        // Actualizar estado automáticamente según el stock
        actualizarEstadoSegunStock(producto);

        productoRepository.save(producto);
        log.info("Stock ajustado exitosamente - Producto: {}, Stock final: {}, Operación: {}",
                producto.getNombre(), producto.getStock(), operacion);
    }

    @Override
    @Transactional
    public void incrementarStock(Long id, Integer cantidad) {
        log.info("Incrementando stock del producto id={}, cantidad: {}", id, cantidad);
        ajustarStockProducto(id, cantidad, "INCREMENTAR");
    }

    @Override
    @Transactional
    public void decrementarStock(Long id, Integer cantidad) {
        log.info("Decrementando stock del producto id={}, cantidad: {}", id, cantidad);
        ajustarStockProducto(id, cantidad, "DECREMENTAR");
    }

    @Override
    @Transactional
    public void establecerStock(Long id, Integer nuevoStock) {
        log.info("Estableciendo stock del producto id={} a: {}", id, nuevoStock);
        ajustarStockProducto(id, nuevoStock, "AJUSTAR");
    }

    @Override
    @Transactional(readOnly = true)
    public Integer consultarStock(Long id) {
        log.debug("Consultando stock del producto id={}", id);

        if (id == null) {
            throw new ProductoBusinessException("El ID del producto no puede ser nulo");
        }

        Producto producto = obtenerProductoPorIdSeguro(id);
        return producto.getStock();
    }

    // ========== MÉTODOS PRIVADOS DE APOYO PARA STOCK ==========

    private void validarParametrosStock(Long id, Integer cantidad, String operacion) {
        if (id == null) {
            throw new ProductoBusinessException("El ID del producto no puede ser nulo");
        }

        if (cantidad == null) {
            throw new ProductoBusinessException("La cantidad no puede ser nula");
        }

        if (operacion == null || operacion.trim().isEmpty()) {
            throw new ProductoBusinessException("La operación no puede estar vacía");
        }

        if (cantidad < 0) {
            throw new ProductoBusinessException("La cantidad no puede ser negativa");
        }
    }

    private void incrementarStock(Producto producto, Integer cantidad) {
        int nuevoStock = producto.getStock() + cantidad;
        producto.setStock(nuevoStock);
        log.debug("Stock incrementado: {} + {} = {}", producto.getStock() - cantidad, cantidad, nuevoStock);
    }

    private void decrementarStock(Producto producto, Integer cantidad) {
        int stockActual = producto.getStock();

        if (stockActual < cantidad) {
            throw new ProductoBusinessException(
                    String.format("Stock insuficiente. Stock actual: %d, cantidad a decrementar: %d",
                            stockActual, cantidad)
            );
        }

        int nuevoStock = stockActual - cantidad;
        producto.setStock(nuevoStock);
        log.debug("Stock decrementado: {} - {} = {}", stockActual, cantidad, nuevoStock);
    }

    private void establecerStockDirecto(Producto producto, Integer nuevoStock) {
        if (nuevoStock < 0) {
            throw new ProductoBusinessException("El stock no puede ser negativo");
        }

        producto.setStock(nuevoStock);
        log.debug("Stock establecido directamente a: {}", nuevoStock);
    }

    private void actualizarEstadoSegunStock(Producto producto) {
        // Solo actualizar estado si el producto está ACTIVO o AGOTADO
        if (producto.getEstado() == EstadoProducto.ACTIVO || producto.getEstado() == EstadoProducto.AGOTADO) {
            if (producto.getStock() <= 0) {
                producto.setEstado(EstadoProducto.AGOTADO);
                log.debug("Producto marcado como AGOTADO por stock cero");
            } else if (producto.getEstado() == EstadoProducto.AGOTADO && producto.getStock() > 0) {
                producto.setEstado(EstadoProducto.ACTIVO);
                log.debug("Producto reactivado - Stock disponible: {}", producto.getStock());
            }
        }
    }

    // ========== MÉTODOS PRIVADOS DE APOYO EXISTENTES ==========

    private Producto obtenerProductoPorIdSeguro(Long id) {
        if (id == null) {
            throw new ProductoBusinessException("El ID del producto no puede ser nulo");
        }

        return productoRepository.findById(id)
                .orElseThrow(() -> new ProductoNotFoundException(id));
    }

    private void validarDatosCreacion(ProductoDto dto) {
        if (dto.getCodigo() != null && productoRepository.existsByCodigo(dto.getCodigo())) {
            throw new ProductoDuplicateException("código", dto.getCodigo());
        }

        if (dto.getNombre() != null && productoRepository.existsByNombre(dto.getNombre())) {
            throw new ProductoDuplicateException("nombre", dto.getNombre());
        }

        validarDatosBasicos(dto);
    }

    private void validarDatosActualizacion(Long id, ProductoDto dto, Producto productoExistente) {
        if (dto.getCodigo() != null && !dto.getCodigo().equals(productoExistente.getCodigo())
                && productoRepository.existsByCodigoAndIdNot(dto.getCodigo(), id)) {
            throw new ProductoDuplicateException("código", dto.getCodigo());
        }

        if (dto.getNombre() != null && !dto.getNombre().equals(productoExistente.getNombre())
                && productoRepository.existsByNombreAndIdNot(dto.getNombre(), id)) {
            throw new ProductoDuplicateException("nombre", dto.getNombre());
        }

        validarDatosBasicos(dto);
    }

    private void validarDatosBasicos(ProductoDto dto) {
        if (dto.getPrecio() != null && dto.getPrecio() <= 0) {
            throw new ProductoBusinessException("El precio debe ser mayor a 0");
        }

        if (dto.getStock() != null && dto.getStock() < 0) {
            throw new ProductoBusinessException("El stock no puede ser negativo");
        }

        // Validar categoría
        if (dto.getCategoria() == null || dto.getCategoria().getId() == null) {
            throw new ProductoBusinessException("La categoría es obligatoria");
        }
    }

    private void establecerEstadoAutomatico(Producto producto) {
        if (producto.getEstado() == null) {
            producto.setEstado(EstadoProducto.ACTIVO);
        }

        if (producto.getStock() != null && producto.getStock() <= 0) {
            producto.setEstado(EstadoProducto.AGOTADO);
        }
    }

    private void ajustarEstadoSegunStock(Producto producto, EstadoProducto estadoDto) {
        // Solo ajustar automáticamente si no se envió estado en el DTO
        if (estadoDto == null && producto.getStock() != null && producto.getStock() <= 0) {
            producto.setEstado(EstadoProducto.AGOTADO);
        }
    }

    private EstadoProducto calcularNuevoEstado(EstadoProducto estadoActual) {
        return (estadoActual == EstadoProducto.ACTIVO)
                ? EstadoProducto.INACTIVO
                : EstadoProducto.ACTIVO;
    }
}