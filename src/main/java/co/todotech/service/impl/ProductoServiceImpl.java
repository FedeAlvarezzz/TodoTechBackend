package co.todotech.service.impl;

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
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductoServiceImpl implements ProductoService {

    private final ProductoMapper productoMapper;
    private final ProductoRepository productoRepository;

    @Override
    @Transactional
    public void crearProducto(ProductoDto dto) throws Exception {
        log.info("Creando producto {}", dto.nombre());


        if (productoRepository.existsByCodigo(dto.codigo())) {
            throw new Exception("Ya existe un producto con el código: " + dto.codigo());
        }
        if (productoRepository.existsByNombre(dto.nombre())) {
            throw new Exception("Ya existe un producto con el nombre: " + dto.nombre());
        }

        Producto producto = productoMapper.toEntity(dto);

        // Estado por defecto: ACTIVO (si no viene) y AGOTADO si stock <= 0
        if (producto.getEstado() == null) {
            producto.setEstado(EstadoProducto.ACTIVO);
        }
        if (producto.getStock() != null && producto.getStock() <= 0) {
            producto.setEstado(EstadoProducto.AGOTADO);
        }

        productoRepository.save(producto);
        log.info("Producto creado exitosamente: id={}, codigo={}", producto.getId(), producto.getCodigo());
    }

    @Transactional
    public void actualizarProducto(Long id, ProductoDto dto) throws Exception {
        log.info("Actualizando producto id={}", id);


        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new Exception("Producto no encontrado con ID: " + id));

        // Validar unicidad contra otros registros
        if (!producto.getCodigo().equals(dto.codigo())
                && productoRepository.existsByCodigoAndIdNot(dto.codigo(), id)) {
            throw new Exception("Ya existe otro producto con el código: " + dto.codigo());
        }
        if (!producto.getNombre().equals(dto.nombre())
                && productoRepository.existsByNombreAndIdNot(dto.nombre(), id)) {
            throw new Exception("Ya existe otro producto con el nombre: " + dto.nombre());
        }

        // Actualiza campos editables desde el DTO (ignora nulls)
        productoMapper.updateProductoFromDto(dto, producto);

        // Si no vino estado en el DTO, ajusta automáticamente según stock
        if (dto.estado() == null && producto.getStock() != null && producto.getStock() <= 0) {
            producto.setEstado(EstadoProducto.AGOTADO);
        }

        productoRepository.save(producto);
        log.info("Producto actualizado: id={}, codigo={}", producto.getId(), producto.getCodigo());
    }

    @Override
    @Transactional
    public void eliminarProducto(Long id) throws Exception {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new Exception("Producto no encontrado con ID: " + id));

        productoRepository.delete(producto);
        log.info("Producto eliminado físicamente: {}", id);
    }

    @Override
    @Transactional
    public void cambiarEstadoProducto(Long id) throws Exception {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new Exception("Producto no encontrado con ID: " + id));

        EstadoProducto anterior = producto.getEstado();
        // Regla simple: si está ACTIVO -> INACTIVO, en otro caso -> ACTIVO
        if (anterior == EstadoProducto.ACTIVO) {
            producto.setEstado(EstadoProducto.INACTIVO);
        } else {
            producto.setEstado(EstadoProducto.ACTIVO);
        }

        productoRepository.save(producto);
        log.info("Estado del producto {} cambiado de {} a {}", id, anterior, producto.getEstado());
    }

    @Override
    public ProductoDto obtenerProductoPorId(Long id) throws Exception {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new Exception("Producto no encontrado con ID: " + id));
        return productoMapper.toDto(producto);
    }

    @Override
    public ProductoDto obtenerProductoPorCodigo(String codigo) throws Exception {
        Producto producto = productoRepository.findByCodigo(codigo)
                .orElseThrow(() -> new Exception("Producto no encontrado con código: " + codigo));
        return productoMapper.toDto(producto);
    }

    @Override
    public ProductoDto obtenerProductoPorNombre(String nombre) throws Exception {
        Producto producto = productoRepository.findFirstByNombreIgnoreCase(nombre)
                .orElseThrow(() -> new Exception("Producto no encontrado con nombre: " + nombre));
        return productoMapper.toDto(producto);
    }

    @Override
    public List<ProductoDto> obtenerProductoPorEstado(EstadoProducto estado) {
        return productoRepository.findAllByEstado(estado).stream()
                .map(productoMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductoDto> obtenerProductoPorCategoriaId(Long categoriaId) {
        return productoRepository.findAllByCategoriaId(categoriaId).stream()
                .map(productoMapper::toDto)
                .collect(Collectors.toList());
    }


}