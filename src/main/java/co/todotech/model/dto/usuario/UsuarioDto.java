package co.todotech.model.dto.usuario;

import java.io.Serializable;

/**
 * DTO for {@link co.todotech.model.entities.Usuario}
 */
public record UsuarioDto(String nombre, String cedula, String correo, String telefono, String usuario,
                         String contrasena) implements Serializable {
}