package co.todotech.model.dto.detalleorden.validacion;// BulkStockValidationRequest.java


import co.todotech.model.dto.detalleorden.validacion.StockValidationRequest;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.util.List;

public record BulkStockValidationRequest(
        @NotNull(message = "La lista de validaciones no puede ser nula")
        @Size(min = 1, message = "Debe incluir al menos una validación")
        List<StockValidationRequest> validaciones
) implements Serializable {}