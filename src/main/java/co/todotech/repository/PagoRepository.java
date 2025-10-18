package co.todotech.repository;

import co.todotech.model.entities.Pago;
import co.todotech.model.enums.EstadoPago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PagoRepository extends JpaRepository<Pago, Long> {

    List<Pago> findByOrdenVentaId(Long ordenVentaId);

    List<Pago> findByEstadoPago(EstadoPago estadoPago);

    List<Pago> findByMetodoPagoId(Long metodoPagoId);

    List<Pago> findByUsuarioId(Long usuarioId);

    List<Pago> findByFechaPagoBetween(LocalDateTime fechaInicio, LocalDateTime fechaFin);

    List<Pago> findByFechaPagoAfter(LocalDateTime fecha);

    Optional<Pago> findByNumeroTransaccion(String numeroTransaccion);

    @Query("SELECT p FROM Pago p WHERE p.monto >= :montoMinimo")
    List<Pago> findByMontoGreaterThanEqual(@Param("montoMinimo") Double montoMinimo);

    @Query("SELECT p FROM Pago p WHERE p.ordenVenta.id = :ordenVentaId AND p.estadoPago = 'APROBADO'")
    List<Pago> findPagosAprobadosByOrdenVenta(@Param("ordenVentaId") Long ordenVentaId);

    @Query("SELECT SUM(p.monto) FROM Pago p WHERE p.ordenVenta.id = :ordenVentaId AND p.estadoPago = 'APROBADO'")
    Double sumMontoAprobadoByOrdenVenta(@Param("ordenVentaId") Long ordenVentaId);

    @Query("SELECT p FROM Pago p ORDER BY p.fechaPago DESC")
    List<Pago> findAllOrderByFechaPagoDesc();
}