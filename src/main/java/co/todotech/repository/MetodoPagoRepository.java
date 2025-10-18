package co.todotech.repository;

import co.todotech.model.entities.MetodoPago;
import co.todotech.model.enums.TipoMetodo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MetodoPagoRepository extends JpaRepository<MetodoPago, Long> {

    List<MetodoPago> findByMetodo(TipoMetodo metodo);

    List<MetodoPago> findByAprobacion(Boolean aprobacion);

    Optional<MetodoPago> findByDescripcion(String descripcion);

    @Query("SELECT mp FROM MetodoPago mp WHERE mp.comision <= :comisionMaxima")
    List<MetodoPago> findByComisionLessThanEqual(@Param("comisionMaxima") Double comisionMaxima);

    boolean existsByMetodo(TipoMetodo metodo);

    @Query("SELECT mp FROM MetodoPago mp WHERE mp.aprobacion = true ORDER BY mp.comision ASC")
    List<MetodoPago> findMetodosAprobadosOrderByComision();
}