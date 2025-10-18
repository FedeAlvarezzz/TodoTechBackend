package co.todotech.model.dto.producto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AjusteStockRequest implements Serializable {

    @NotNull(message = "La cantidad no puede ser nula")
    private Integer cantidad;

    @NotNull(message = "La operación no puede ser nula")
    @Pattern(regexp = "INCREMENTAR|DECREMENTAR|AJUSTAR",
            message = "La operación debe ser: INCREMENTAR, DECREMENTAR o AJUSTAR")
    private String operacion;
}