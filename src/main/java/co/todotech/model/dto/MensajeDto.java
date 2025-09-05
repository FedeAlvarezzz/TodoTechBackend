package co.todotech.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MensajeDto<T> {
    private boolean error;
    private String mensaje;
    private T data;

    public MensajeDto(boolean error, String mensaje) {
        this.error = error;
        this.mensaje = mensaje;
        this.data = null;
    }
}