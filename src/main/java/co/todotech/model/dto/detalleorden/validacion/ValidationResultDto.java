package co.todotech.model.dto.detalleorden.validacion;// ValidationResultDto.java

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ValidationResultDto implements Serializable {
    private final boolean valido;
    private final String mensaje;
    private final Integer stockActual;
    private final Integer stockDisponible;
    private final Boolean stockCritico;
    private final String accionRecomendada;

    // Factory methods
    public static ValidationResultDto valido(String mensaje, Integer stockActual) {
        return ValidationResultDto.builder()
                .valido(true)
                .mensaje(mensaje)
                .stockActual(stockActual)
                .stockDisponible(stockActual)
                .stockCritico(stockActual <= 3)
                .accionRecomendada(stockActual <= 3 ? "Comprar pronto" : "Disponible")
                .build();
    }

    public static ValidationResultDto invalido(String mensaje, Integer stockActual, Integer stockRequerido) {
        return ValidationResultDto.builder()
                .valido(false)
                .mensaje(mensaje)
                .stockActual(stockActual)
                .stockDisponible(stockActual)
                .stockCritico(true)
                .accionRecomendada("Reducir cantidad o elegir otro producto")
                .build();
    }

    public static ValidationResultDto stockInsuficiente(Integer stockActual, Integer stockRequerido) {
        return ValidationResultDto.builder()
                .valido(false)
                .mensaje(String.format("Stock insuficiente. Disponible: %d, Requerido: %d",
                        stockActual, stockRequerido))
                .stockActual(stockActual)
                .stockDisponible(stockActual)
                .stockCritico(true)
                .accionRecomendada("Reducir cantidad a " + stockActual)
                .build();
    }

    public static ValidationResultDto productoNoDisponible() {
        return ValidationResultDto.builder()
                .valido(false)
                .mensaje("Producto no disponible para la venta")
                .stockActual(0)
                .stockDisponible(0)
                .stockCritico(true)
                .accionRecomendada("Elegir otro producto")
                .build();
    }
}