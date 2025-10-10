package co.todotech.mapper;


import co.todotech.model.dto.detalleorden.CreateDetalleOrdenDto;
import co.todotech.model.dto.detalleorden.DetalleOrdenDto;
import co.todotech.model.entities.DetalleOrden;
import org.mapstruct.*;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface DetalleOrdenMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "subtotal", ignore = true) // Se calcula automáticamente
    DetalleOrden toEntity(DetalleOrdenDto detalleOrdenDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "orden", ignore = true)
    @Mapping(target = "producto", ignore = true) // Se setea después con el repository
    @Mapping(target = "subtotal", ignore = true) // Se calcula automáticamente
    DetalleOrden toEntityFromCreateDto(CreateDetalleOrdenDto createDetalleOrdenDto);

    DetalleOrdenDto toDto(DetalleOrden detalleOrden);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "subtotal", ignore = true) // Se recalcula automáticamente
    void updateDetalleOrdenFromDto(DetalleOrdenDto detalleOrdenDto, @MappingTarget DetalleOrden detalleOrden);
}