package co.todotech.repository;

import co.todotech.model.entities.Producto;
import co.todotech.model.enums.EstadoProducto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

    Optional<Producto> findByCodigo(String codigo);
    Optional<Producto> findFirstByNombreIgnoreCase(String nombre);
    boolean existsByCodigo(String codigo);
    boolean existsByNombre(String nombre);
    boolean existsByCodigoAndIdNot(String codigo, Long id);
    boolean existsByNombreAndIdNot(String nombre, Long id);
    List<Producto> findAllByEstado(EstadoProducto estado);
    List<Producto> findAllByCategoriaId(Long categoriaId);
}