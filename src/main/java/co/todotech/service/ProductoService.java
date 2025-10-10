package co.todotech.service;

import co.todotech.model.dto.producto.ProductoDto;
import co.todotech.model.enums.EstadoProducto;

import java.util.List;

public interface ProductoService {

    void crearProducto(ProductoDto productoDto);
    void actualizarProducto(Long id, ProductoDto dto);
    void eliminarProducto(Long id);
    void cambiarEstadoProducto(Long id);

    ProductoDto obtenerProductoPorId(Long id);
    ProductoDto obtenerProductoPorCodigo(String codigo);
    ProductoDto obtenerProductoPorNombre(String nombre);

    List<ProductoDto> obtenerProductoPorEstado(EstadoProducto estado);
    List<ProductoDto> obtenerProductoPorCategoriaId(Long categoriaId);

    // Nuevos métodos para los endpoints adicionales
    List<ProductoDto> obtenerProductosActivos();
    List<ProductoDto> buscarProductosPorNombre(String nombre);
    List<ProductoDto> obtenerProductosDisponibles();

    // Método para obtener todos los productos (NUEVO)
    List<ProductoDto> obtenerTodosLosProductos();
}