package co.todotech.service;

import co.todotech.model.dto.categoria.CategoriaDto;

import java.util.List;

public interface CategoriaService {

    CategoriaDto crearCategoria(CategoriaDto dto) throws Exception;

    CategoriaDto actualizarCategoria(Long id, CategoriaDto dto) throws Exception;

    void eliminarCategoria(Long id) throws Exception;

    CategoriaDto obtenerCategoriaPorId(Long id) throws Exception;

    CategoriaDto obtenerCategoriaPorNombre(String nombre) throws Exception;

    List<CategoriaDto> obtenerTodasLasCategorias();

    List<CategoriaDto> obtenerCategoriasConProductos();
}