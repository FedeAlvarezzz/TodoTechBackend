package co.todotech.repository;

import co.todotech.model.entities.Orden;
import co.todotech.model.enums.EstadoOrden;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrdenRepository extends JpaRepository<Orden, Long> {

    Optional<Orden> findByNumeroOrden(String numeroOrden);

    boolean existsByNumeroOrden(String numeroOrden);

    List<Orden> findByClienteId(Long clienteId);

    List<Orden> findByVendedorId(Long vendedorId);

    List<Orden> findByEstado(EstadoOrden estado);

    // Método para cargar la orden con sus detalles (productos) usando JOIN FETCH
    @Query("SELECT o FROM Orden o LEFT JOIN FETCH o.productos WHERE o.id = :id")
    Optional<Orden> findByIdWithDetalles(@Param("id") Long id);

    // Método para cargar orden con detalles y relaciones completas
    @Query("SELECT o FROM Orden o " +
            "LEFT JOIN FETCH o.productos " +
            "LEFT JOIN FETCH o.cliente " +
            "LEFT JOIN FETCH o.vendedor " +
            "WHERE o.id = :id")
    Optional<Orden> findByIdWithDetallesCompletos(@Param("id") Long id);

    // Método para verificar si existe una orden con estado específico para un cliente
    boolean existsByClienteIdAndEstado(Long clienteId, EstadoOrden estado);

    // Método para contar órdenes por estado
    long countByEstado(EstadoOrden estado);

    // Método para buscar órdenes por rango de fechas
    @Query("SELECT o FROM Orden o WHERE o.fecha BETWEEN :fechaInicio AND :fechaFin")
    List<Orden> findByFechaBetween(@Param("fechaInicio") LocalDateTime fechaInicio,
                                   @Param("fechaFin") LocalDateTime fechaFin);
}