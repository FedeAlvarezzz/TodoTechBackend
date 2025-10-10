package co.todotech.service.impl;

import co.todotech.mapper.CategoriaMapper;
import co.todotech.model.dto.categoria.CategoriaDto;
import co.todotech.model.entities.Categoria;
import co.todotech.repository.CategoriaRepository;
import co.todotech.service.CategoriaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoriaServiceImpl implements CategoriaService {

    private final CategoriaMapper categoriaMapper;
    private final CategoriaRepository categoriaRepository;

    @Override
    @Transactional
    public CategoriaDto crearCategoria(CategoriaDto dto) throws Exception {
        log.info("Creando categoría: {}", dto.getNombre());

        // Validar que el nombre no esté vacío
        if (dto.getNombre() == null || dto.getNombre().trim().isEmpty()) {
            throw new Exception("El nombre de la categoría es obligatorio");
        }

        // Validar unicidad del nombre
        if (categoriaRepository.existsByNombre(dto.getNombre().trim())) {
            throw new Exception("Ya existe una categoría con el nombre: " + dto.getNombre());
        }

        Categoria categoria = categoriaMapper.toEntity(dto);
        categoria = categoriaRepository.save(categoria);

        log.info("Categoría creada exitosamente: id={}, nombre={}", categoria.getId(), categoria.getNombre());
        return categoriaMapper.toDto(categoria);
    }

    @Override
    @Transactional
    public CategoriaDto actualizarCategoria(Long id, CategoriaDto dto) throws Exception {
        log.info("Actualizando categoría id={}", id);

        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new Exception("Categoría no encontrada con ID: " + id));

        // Validar que el nombre no esté vacío
        if (dto.getNombre() != null && dto.getNombre().trim().isEmpty()) {
            throw new Exception("El nombre de la categoría no puede estar vacío");
        }

        // Validar unicidad del nombre contra otros registros (solo si cambió el nombre)
        if (dto.getNombre() != null && !dto.getNombre().trim().equals(categoria.getNombre())) {
            String nuevoNombre = dto.getNombre().trim();
            if (categoriaRepository.existsByNombreAndIdNot(nuevoNombre, id)) {
                throw new Exception("Ya existe otra categoría con el nombre: " + nuevoNombre);
            }
        }

        categoriaMapper.updateCategoriaFromDto(dto, categoria);
        categoria = categoriaRepository.save(categoria);

        log.info("Categoría actualizada: id={}, nombre={}", categoria.getId(), categoria.getNombre());
        return categoriaMapper.toDto(categoria);
    }

    @Override
    @Transactional
    public void eliminarCategoria(Long id) throws Exception {
        log.info("Eliminando categoría id={}", id);

        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new Exception("Categoría no encontrada con ID: " + id));

        // Verificar si la categoría tiene productos asociados
        Long countProductos = categoriaRepository.countProductosByCategoriaId(id);
        if (countProductos > 0) {
            throw new Exception("No se puede eliminar la categoría porque tiene " + countProductos + " producto(s) asociado(s)");
        }

        categoriaRepository.delete(categoria);
        log.info("Categoría eliminada: id={}, nombre={}", id, categoria.getNombre());
    }

    @Override
    @Transactional(readOnly = true)
    public CategoriaDto obtenerCategoriaPorId(Long id) throws Exception {
        log.debug("Buscando categoría por ID: {}", id);

        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new Exception("Categoría no encontrada con ID: " + id));

        return categoriaMapper.toDto(categoria);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoriaDto obtenerCategoriaPorNombre(String nombre) throws Exception {
        log.debug("Buscando categoría por nombre: {}", nombre);

        if (nombre == null || nombre.trim().isEmpty()) {
            throw new Exception("El nombre de búsqueda no puede estar vacío");
        }

        Categoria categoria = categoriaRepository.findByNombreIgnoreCase(nombre.trim())
                .orElseThrow(() -> new Exception("Categoría no encontrada con nombre: " + nombre));

        return categoriaMapper.toDto(categoria);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoriaDto> obtenerTodasLasCategorias() {
        log.debug("Obteniendo todas las categorías");

        return categoriaRepository.findAll().stream()
                .map(categoriaMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoriaDto> obtenerCategoriasConProductos() {
        log.debug("Obteniendo categorías con productos");

        // Obtener categorías que tienen al menos un producto
        return categoriaRepository.findAll().stream()
                .filter(categoria -> {
                    Long count = categoriaRepository.countProductosByCategoriaId(categoria.getId());
                    return count != null && count > 0;
                })
                .map(categoriaMapper::toDto)
                .collect(Collectors.toList());
    }
}