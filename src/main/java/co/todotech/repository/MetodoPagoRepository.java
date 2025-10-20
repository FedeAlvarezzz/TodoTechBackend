package co.todotech.repository;

import co.todotech.model.entities.MetodoPago;
import co.todotech.model.enums.TipoMetodo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio de acceso a datos para la entidad {@link MetodoPago}.
 *
 * Esta interfaz define las operaciones CRUD y consultas específicas
 * relacionadas con los métodos de pago registrados en el sistema.
 * Extiende {@link JpaRepository}, lo que permite interactuar con la base de datos
 * sin necesidad de escribir implementaciones manuales.
 */
public interface MetodoPagoRepository extends JpaRepository<MetodoPago, Long> {

    // =====================================================
    // MÉTODOS DERIVADOS DE NOMBRES (Query Methods)
    // =====================================================

    /**
     * Obtiene una lista de métodos de pago que coinciden con un tipo específico.
     *
     * @param metodo Tipo de método de pago (por ejemplo: EFECTIVO, TARJETA, CREDITO, etc.).
     * @return Lista de métodos de pago del tipo indicado.
     */
    List<MetodoPago> findByMetodo(TipoMetodo metodo);

    /**
     * Obtiene una lista de métodos de pago filtrados por su estado de aprobación.
     *
     * @param aprobacion Valor booleano que indica si el método está aprobado o no.
     * @return Lista de métodos de pago aprobados o no aprobados según el parámetro.
     */
    List<MetodoPago> findByAprobacion(Boolean aprobacion);

    /**
     * Busca un método de pago por su descripción exacta.
     *
     * @param descripcion Descripción del método de pago.
     * @return Un {@link Optional} que contiene el método encontrado, si existe.
     */
    Optional<MetodoPago> findByDescripcion(String descripcion);

    /**
     * Verifica si ya existe un método de pago registrado con un tipo específico.
     *
     * @param metodo Tipo de método de pago a verificar.
     * @return {@code true} si el método ya existe, {@code false} en caso contrario.
     */
    boolean existsByMetodo(TipoMetodo metodo);

    // =====================================================
    // CONSULTAS PERSONALIZADAS CON JPQL
    // =====================================================

    /**
     * Obtiene todos los métodos de pago cuya comisión sea menor o igual a un valor máximo.
     *
     * @param comisionMaxima Valor máximo permitido para la comisión.
     * @return Lista de métodos de pago con comisión menor o igual al valor indicado.
     */
    @Query("SELECT mp FROM MetodoPago mp WHERE mp.comision <= :comisionMaxima")
    List<MetodoPago> findByComisionLessThanEqual(@Param("comisionMaxima") Double comisionMaxima);

    /**
     * Obtiene todos los métodos de pago aprobados, ordenados ascendentemente por su comisión.
     *
     * @return Lista de métodos aprobados, ordenada por comisión (de menor a mayor).
     */
    @Query("SELECT mp FROM MetodoPago mp WHERE mp.aprobacion = true ORDER BY mp.comision ASC")
    List<MetodoPago> findMetodosAprobadosOrderByComision();
}