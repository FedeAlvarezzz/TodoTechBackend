package co.todotech.handler;

import co.todotech.exception.producto.ProductoBusinessException;
import co.todotech.exception.producto.ProductoDuplicateException;
import co.todotech.exception.producto.ProductoNotFoundException;
import co.todotech.model.dto.MensajeDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ProductoNotFoundException.class)
    public ResponseEntity<MensajeDto<?>> handleProductoNotFoundException(ProductoNotFoundException ex) {
        log.warn("Producto no encontrado: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new MensajeDto<>(true, ex.getMessage()));
    }

    @ExceptionHandler(ProductoDuplicateException.class)
    public ResponseEntity<MensajeDto<?>> handleProductoDuplicateException(ProductoDuplicateException ex) {
        log.warn("Intento de duplicado: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new MensajeDto<>(true, ex.getMessage()));
    }

    @ExceptionHandler(ProductoBusinessException.class)
    public ResponseEntity<MensajeDto<?>> handleProductoBusinessException(ProductoBusinessException ex) {
        log.warn("Error de negocio: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new MensajeDto<>(true, ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<MensajeDto<?>> handleGenericException(Exception ex) {
        log.error("Error inesperado: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new MensajeDto<>(true, "Error interno del servidor"));
    }
}