package co.todotech.mapper;

import co.todotech.model.dto.metodopago.MetodoPagoDto;
import co.todotech.model.entities.MetodoPago;
import org.mapstruct.*;

/**
 * Mapper de MapStruct para convertir entre la entidad {@link MetodoPago}
 * y su correspondiente DTO {@link MetodoPagoDto}.
 *
 * Este mapper automatiza la transformación de datos entre las capas
 * de persistencia (Entity) y de transferencia (DTO),
 * evitando código repetitivo de conversión manual.
 */
@Mapper(
        componentModel = "spring", // Permite que Spring inyecte este mapper como un componente (@Component)
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, // Ignora los campos nulos al mapear (no los sobrescribe)
        unmappedTargetPolicy = ReportingPolicy.IGNORE // Evita advertencias si hay propiedades no mapeadas
)
public interface MetodoPagoMapper {

    /**
     * Convierte un {@link MetodoPagoDto} a una entidad {@link MetodoPago}.
     *
     * Se ignora el campo "id" ya que normalmente lo genera la base de datos
     * al momento de persistir el registro.
     */
    @Mapping(target = "id", ignore = true)
    MetodoPago toEntity(MetodoPagoDto metodoPagoDto);

    /**
     * Convierte una entidad {@link MetodoPago} a su DTO equivalente {@link MetodoPagoDto}.
     *
     * Este método se utiliza cuando se desea enviar datos al cliente (por ejemplo,
     * como respuesta a una petición REST).
     */
    MetodoPagoDto toDto(MetodoPago metodoPago);

    /**
     * Actualiza una entidad existente de {@link MetodoPago} con los valores
     * de un {@link MetodoPagoDto}, ignorando los campos nulos.
     *
     * Esto es útil para operaciones de actualización parcial (PUT o PATCH),
     * ya que solo se sobrescriben los campos no nulos del DTO.
     *
     * @param metodoPagoDto DTO con los nuevos valores.
     * @param metodoPago Entidad que se actualizará.
     */
    @Mapping(target = "id", ignore = true)
    void updateMetodoPagoFromDto(MetodoPagoDto metodoPagoDto, @MappingTarget MetodoPago metodoPago);
}