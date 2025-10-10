package co.todotech.mapper;

import co.todotech.model.dto.ordenventa.OrdenDto;
import co.todotech.model.entities.Orden;
import org.mapstruct.*;

@Mapper(
        componentModel = "spring",
        uses = {DetalleOrdenMapper.class},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface OrdenMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "subtotal", ignore = true)
    @Mapping(target = "impuestos", ignore = true)
    @Mapping(target = "total", ignore = true)
    @Mapping(target = "productos", ignore = true) // productos no viene del DTO
    Orden toEntity(OrdenDto ordenDto);

    // Elimina el mapeo de productos aquí ya que OrdenDto no tiene esa propiedad
    OrdenDto toDto(Orden orden);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "cliente", ignore = true)
    @Mapping(target = "vendedor", ignore = true)
    @Mapping(target = "subtotal", ignore = true)
    @Mapping(target = "impuestos", ignore = true)
    @Mapping(target = "total", ignore = true)
    @Mapping(target = "productos", ignore = true) // productos no viene del DTO
    void updateOrdenFromDto(OrdenDto ordenDto, @MappingTarget Orden orden);
}