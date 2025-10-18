package co.todotech.model.dto.producto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockResponse implements Serializable {
    private Long productoId;
    private Integer stock;
    private String nombreProducto;
    private String estado;
}