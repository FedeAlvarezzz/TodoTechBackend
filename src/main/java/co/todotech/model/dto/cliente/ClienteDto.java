package co.todotech.model.dto.cliente;

import co.todotech.model.entities.Cliente;
import co.todotech.model.enums.TipoCliente;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * DTO for {@link Cliente}
 */
public record ClienteDto(
        Long id,

        @NotNull(message = "El nombre no puede ser nulo")
        @Size(min = 2, max = 150, message = "El nombre debe tener entre 2 y 150 caracteres")
        String nombre,

        @NotNull(message = "La cédula no puede ser nula")
        @Size(min = 5, max = 20, message = "La cédula debe tener entre 5 y 20 caracteres")
        String cedula,

        @Email(message = "El correo debe tener un formato válido")
        @Size(max = 100, message = "El correo no puede exceder 100 caracteres")
        String correo,

        @Pattern(regexp = "^[+]?[0-9]{10,20}$", message = "El teléfono debe tener un formato válido")
        String telefono,

        @Size(max = 255, message = "La dirección no puede exceder 255 caracteres")
        String direccion,

        LocalDateTime fechaRegistro,

        @NotNull(message = "El tipo de cliente no puede ser nulo")
        TipoCliente tipoCliente,

        Double descuentoAplicable
) implements Serializable {

    // Constructor para creación sin ID
    public ClienteDto(String nombre, String cedula, String correo, String telefono,
                      String direccion, TipoCliente tipoCliente, Double descuentoAplicable) {
        this(null, nombre, cedula, correo, telefono, direccion, null, tipoCliente, descuentoAplicable);
    }
}