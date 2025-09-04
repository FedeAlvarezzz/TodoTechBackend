package co.todotech.model.dto.usuario;

import co.todotech.model.enums.TipoUsuario;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * DTO for {@link co.todotech.model.entities.Usuario}
 */
public record UsuarioDto(
        Long id,
        String nombre,
        String cedula,
        String correo,
        String telefono,
        String nombreUsuario,  // Cambiado de 'usuario' a 'nombreUsuario'
        String contrasena,
        TipoUsuario tipoUsuario,
        LocalDateTime fechaCreacion,
        Boolean estado
) implements Serializable {
}