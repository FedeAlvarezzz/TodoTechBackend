package co.todotech.model.dto.detalleorden.validacion;// BulkValidationResultDto.java


import co.todotech.model.dto.detalleorden.validacion.ValidationResultDto;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class BulkValidationResultDto implements Serializable {
    private final boolean todoValido;
    private final String mensajeGeneral;
    private final Map<Long, ValidationResultDto> resultados;
    private final List<Long> productosConProblemas;
    private final Integer totalProductos;
    private final Integer productosValidos;
    private final Integer productosInvalidos;
}