package co.todotech.repository;

import co.todotech.model.entities.Cliente;
import co.todotech.model.enums.TipoCliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    Optional<Cliente> findByCedula(String cedula);
    Optional<Cliente> findByCorreo(String correo);

    boolean existsByCedula(String cedula);
    boolean existsByCorreo(String correo);
    boolean existsByCedulaAndIdNot(String cedula, Long id);
    boolean existsByCorreoAndIdNot(String correo, Long id);

    List<Cliente> findByTipoCliente(TipoCliente tipoCliente);
    List<Cliente> findByFechaRegistroAfter(LocalDateTime fecha);
    List<Cliente> findByFechaRegistroBetween(LocalDateTime fechaInicio, LocalDateTime fechaFin);

    @Query("SELECT c FROM Cliente c WHERE c.nombre LIKE %:nombre%")
    List<Cliente> findByNombreContaining(@Param("nombre") String nombre);

    long countByTipoCliente(TipoCliente tipoCliente);
}