package co.todotech.model.dto.producto;

import co.todotech.model.entities.Categoria;
import co.todotech.model.entities.Producto;
import co.todotech.model.enums.EstadoProducto;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

/**
 * DTO for {@link Producto}
 */
public record ProductoDto(Long id, String nombre, String codigo, String descripcion,
                          @NotNull(message = "It cannot be null") Categoria categoria, Double precio, Integer stock,
                          String imagenUrl, String marca, Integer garantia,
                          EstadoProducto estado) implements Serializable {
}