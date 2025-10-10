package co.todotech.model.dto.producto;

import co.todotech.model.entities.Categoria;
import co.todotech.model.enums.EstadoProducto;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductoDto implements Serializable {
    private Long id;

    @NotNull(message = "El nombre no puede ser nulo")
    private String nombre;

    @NotNull(message = "El código no puede ser nulo")
    private String codigo;

    private String descripcion;

    @NotNull(message = "La categoría no puede ser nula")
    private Categoria categoria;

    @NotNull(message = "El precio no puede ser nulo")
    @Positive(message = "El precio debe ser mayor a 0")
    private Double precio;

    @NotNull(message = "El stock no puede ser nulo")
    private Integer stock;

    private String imagenUrl;
    private String marca;
    private Integer garantia;
    private EstadoProducto estado;
}