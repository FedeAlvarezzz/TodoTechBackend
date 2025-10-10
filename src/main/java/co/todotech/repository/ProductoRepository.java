package co.todotech.repository;

import co.todotech.model.entities.Producto;
import co.todotech.model.enums.EstadoProducto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    // Métodos adicionales para búsqueda
    @Query("SELECT p FROM Producto p WHERE LOWER(p.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))")
    List<Producto> findByNombreContainingIgnoreCase(@Param("nombre") String nombre);

    @Query("SELECT p FROM Producto p WHERE p.estado = 'ACTIVO' AND p.stock > 0")
    List<Producto> findProductosDisponibles();

    @Query("SELECT COUNT(p) FROM Producto p WHERE p.categoria.id = :categoriaId AND p.estado = 'ACTIVO'")
    Long countProductosActivosPorCategoria(@Param("categoriaId") Long categoriaId);

    // Método para búsqueda por nombre exacto (case insensitive)
    Optional<Producto> findByNombreIgnoreCase(String nombre);
}