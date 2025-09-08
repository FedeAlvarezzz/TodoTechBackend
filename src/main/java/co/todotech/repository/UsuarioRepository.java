package co.todotech.repository;

import co.todotech.model.entities.Usuario;
import co.todotech.model.enums.TipoUsuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByCedula(String cedula);
    Optional<Usuario> findByCorreo(String correo);
    Optional<Usuario> findByNombreUsuario(String nombreUsuario);
    List<Usuario> findByEstado(Boolean estado);

    boolean existsByCedula(String cedula);
    boolean existsByCorreo(String correo);
    boolean existsByNombreUsuario(String nombreUsuario);

    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END " +
            "FROM Usuario u WHERE u.cedula = :cedula AND u.id != :id")
    boolean existsByCedulaAndIdNot(@Param("cedula") String cedula, @Param("id") Long id);

    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END " +
            "FROM Usuario u WHERE u.correo = :correo AND u.id != :id")
    boolean existsByCorreoAndIdNot(@Param("correo") String correo, @Param("id") Long id);

    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END " +
            "FROM Usuario u WHERE u.nombreUsuario = :nombreUsuario AND u.id != :id")
    boolean existsByNombreUsuarioAndIdNot(@Param("nombreUsuario") String nombreUsuario, @Param("id") Long id);


    // Nuevos métodos para los endpoints
    List<Usuario> findByTipoUsuario(TipoUsuario tipoUsuario);

    List<Usuario> findByNombreContainingIgnoreCase(String nombre);

    List<Usuario> findByCedulaContaining(String cedula);

    List<Usuario> findByFechaCreacionBetween(LocalDateTime fechaInicio, LocalDateTime fechaFin);

    List<Usuario> findByFechaCreacionAfter(LocalDateTime fecha);

    List<Usuario> findByFechaCreacionBefore(LocalDateTime fecha);

    // Agrega este método para buscar por correo y tipo de usuario
    @Query("SELECT u FROM Usuario u WHERE u.correo = :correo AND u.tipoUsuario IN :tiposUsuario")
    Optional<Usuario> findByCorreoAndTipoUsuarioIn(@Param("correo") String correo,
                                                   @Param("tiposUsuario") List<TipoUsuario> tiposUsuario);
}