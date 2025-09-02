package co.todotech.model.dto;

public record MensajeDto<T>(
    boolean error, T respuesta
) {
}
