package co.todotech.model.dto.usuario;

import co.todotech.model.enums.TipoUsuario;
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
    private String contrasena;
    private TipoUsuario tipoUsuario;
    private LocalDateTime fechaCreacion;
    private Boolean estado;
}