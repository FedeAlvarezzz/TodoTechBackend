package co.todotech.model.dto.usuario;

import co.todotech.model.enums.TipoUsuario;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioDto implements Serializable {
    private Long id;
    private String nombre;
    private String cedula;
    private String correo;
    private String telefono;
    private String nombreUsuario;

    // REMOVER @JsonIgnore y usar solo @JsonProperty
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) // Solo se puede escribir, no leer
    private String contrasena;

    private Boolean cambiarContrasena;
    private TipoUsuario tipoUsuario;
    private LocalDateTime fechaCreacion;
    private Boolean estado;

    // Método para mostrar información de la contraseña (opcional)
    public String getInfoContrasena() {
        return contrasena != null ? "••••••••" : "No definida";
    }
}