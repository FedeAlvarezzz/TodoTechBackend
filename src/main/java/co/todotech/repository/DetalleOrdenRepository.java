package co.todotech.repository;

import co.todotech.model.entities.DetalleOrden;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DetalleOrdenRepository extends JpaRepository<DetalleOrden, Long> {

    List<DetalleOrden> findByOrdenId(Long ordenId);

    Optional<DetalleOrden> findByOrdenIdAndProductoId(Long ordenId, Long productoId);

    boolean existsByOrdenIdAndProductoId(Long ordenId, Long productoId);

    void deleteByOrdenIdAndProductoId(Long ordenId, Long productoId);

    long countByOrdenId(Long ordenId);
}