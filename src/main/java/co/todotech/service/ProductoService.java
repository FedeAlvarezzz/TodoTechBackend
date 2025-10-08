package co.todotech.service;

import co.todotech.model.dto.producto.ProductoDto;
import co.todotech.model.enums.EstadoProducto;

import java.util.List;

public interface ProductoService {

    void crearProducto(ProductoDto productoDto) throws  Exception;
    void actualizarProducto(Long id, ProductoDto dto) throws  Exception;
    void eliminarProducto(Long id) throws  Exception;
    void cambiarEstadoProducto(Long id) throws  Exception;

    ProductoDto obtenerProductoPorId(Long id) throws  Exception;
    ProductoDto obtenerProductoPorCodigo(String codigo) throws  Exception;
    ProductoDto obtenerProductoPorNombre(String nombre) throws Exception;

    List<ProductoDto> obtenerProductoPorEstado(EstadoProducto estado);
    List<ProductoDto> obtenerProductoPorCategoriaId(Long categoriaId);

}