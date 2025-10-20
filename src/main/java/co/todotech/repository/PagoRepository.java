package co.todotech.repository;

import co.todotech.model.entities.Pago;
import co.todotech.model.enums.EstadoPago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio de acceso a datos para la entidad {@link Pago}.
 *
 * Esta interfaz extiende {@link JpaRepository}, proporcionando las operaciones CRUD básicas
 * y consultas personalizadas específicas para la gestión de pagos dentro del sistema.
 *
 * El objetivo de este repositorio es abstraer la capa de persistencia,
 * permitiendo que los servicios interactúen con la base de datos de manera declarativa.
 */
public interface PagoRepository extends JpaRepository<Pago, Long> {

    // =====================================================
    // MÉTODOS DERIVADOS DE NOMBRES (Query Methods)
    // =====================================================

    /**
     * Obtiene todos los pagos asociados a una orden de venta específica.
     *
     * @param ordenVentaId ID de la orden de venta.
     * @return Lista de pagos asociados a esa orden.
     */
    List<Pago> findByOrdenVentaId(Long ordenVentaId);

    /**
     * Obtiene todos los pagos que tienen un estado de pago específico.
     *
     * @param estadoPago Estado del pago (por ejemplo: APROBADO, PENDIENTE, RECHAZADO).
     * @return Lista de pagos con el estado indicado.
     */
    List<Pago> findByEstadoPago(EstadoPago estadoPago);

    /**
     * Obtiene todos los pagos realizados con un método de pago específico.
     *
     * @param metodoPagoId ID del método de pago.
     * @return Lista de pagos con ese método.
     */
    List<Pago> findByMetodoPagoId(Long metodoPagoId);

    /**
     * Obtiene todos los pagos realizados por un usuario específico.
     *
     * @param usuarioId ID del usuario.
     * @return Lista de pagos asociados al usuario.
     */
    List<Pago> findByUsuarioId(Long usuarioId);

    /**
     * Obtiene los pagos realizados entre dos fechas.
     *
     * @param fechaInicio Fecha inicial (inclusive).
     * @param fechaFin Fecha final (inclusive).
     * @return Lista de pagos realizados dentro del rango.
     */
    List<Pago> findByFechaPagoBetween(LocalDateTime fechaInicio, LocalDateTime fechaFin);

    /**
     * Obtiene los pagos realizados después de una fecha específica.
     *
     * @param fecha Fecha mínima (exclusiva).
     * @return Lista de pagos posteriores a esa fecha.
     */
    List<Pago> findByFechaPagoAfter(LocalDateTime fecha);

    /**
     * Busca un pago por su número de transacción.
     *
     * @param numeroTransaccion Número único de la transacción.
     * @return Pago correspondiente si existe.
     */
    Optional<Pago> findByNumeroTransaccion(String numeroTransaccion);

    // =====================================================
    // MÉTODOS PERSONALIZADOS CON JPQL
    // =====================================================

    /**
     * Obtiene todos los pagos cuyo monto sea mayor o igual a un valor mínimo.
     *
     * @param montoMinimo Valor mínimo del monto.
     * @return Lista de pagos con monto mayor o igual al indicado.
     */
    @Query("SELECT p FROM Pago p WHERE p.monto >= :montoMinimo")
    List<Pago> findByMontoGreaterThanEqual(@Param("montoMinimo") Double montoMinimo);

    /**
     * Obtiene los pagos aprobados asociados a una orden de venta específica.
     *
     * @param ordenVentaId ID de la orden de venta.
     * @return Lista de pagos aprobados.
     */
    @Query("SELECT p FROM Pago p WHERE p.ordenVenta.id = :ordenVentaId AND p.estadoPago = 'APROBADO'")
    List<Pago> findPagosAprobadosByOrdenVenta(@Param("ordenVentaId") Long ordenVentaId);

    /**
     * Calcula el total del monto aprobado de los pagos asociados a una orden de venta.
     *
     * @param ordenVentaId ID de la orden de venta.
     * @return Suma total del monto de pagos aprobados, o {@code null} si no hay registros.
     */
    @Query("SELECT SUM(p.monto) FROM Pago p WHERE p.ordenVenta.id = :ordenVentaId AND p.estadoPago = 'APROBADO'")
    Double sumMontoAprobadoByOrdenVenta(@Param("ordenVentaId") Long ordenVentaId);

    /**
     * Obtiene todos los pagos ordenados por fecha de pago en orden descendente
     * (los más recientes primero).
     *
     * @return Lista de pagos ordenada por fecha de pago.
     */
    @Query("SELECT p FROM Pago p ORDER BY p.fechaPago DESC")
    List<Pago> findAllOrderByFechaPagoDesc();
}