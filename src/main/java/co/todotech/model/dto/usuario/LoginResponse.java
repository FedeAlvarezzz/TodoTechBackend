package co.todotech.model.dto.usuario;

import co.todotech.model.enums.TipoUsuario;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private String mensaje;
    private TipoUsuario tipoUsuario;
    private String nombre;
    private String nombreUsuario;
}