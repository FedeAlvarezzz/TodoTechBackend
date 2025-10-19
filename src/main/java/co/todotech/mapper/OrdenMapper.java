package co.todotech.mapper;

import co.todotech.model.dto.ordenventa.CreateOrdenDto;
import co.todotech.model.dto.ordenventa.OrdenDto;
import co.todotech.model.entities.Orden;
import org.mapstruct.*;

@Mapper(
        componentModel = "spring",
        uses = {ClienteMapper.class, UsuarioMapper.class},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface OrdenMapper {

    // ✅ CORREGIDO: Mapear el descuento desde CreateOrdenDto
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "numeroOrden", ignore = true)
    @Mapping(target = "fecha", ignore = true)
    @Mapping(target = "cliente", ignore = true)
    @Mapping(target = "vendedor", ignore = true)
    @Mapping(target = "productos", ignore = true)
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "subtotal", ignore = true)
    @Mapping(target = "impuestos", ignore = true)
    @Mapping(target = "total", ignore = true)
    @Mapping(target = "observaciones", ignore = true)
    // ✅ IMPORTANTE: NO ignorar el descuento - mapearlo desde el DTO
    @Mapping(source = "descuento", target = "descuento")
    Orden toEntityFromCreateDto(CreateOrdenDto createOrdenDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "productos", ignore = true)
    @Mapping(target = "subtotal", ignore = true)
    @Mapping(target = "impuestos", ignore = true)
    @Mapping(target = "total", ignore = true)
    Orden toEntity(OrdenDto ordenDto);

    // ✅ CORREGIDO: Especificar el método exacto del UsuarioMapper
    @Mapping(target = "vendedor", expression = "java(usuarioMapper.toDtoSafe(orden.getVendedor()))")
    OrdenDto toDto(Orden orden);

    // ✅ CORREGIDO: Update method - NO ignorar descuento
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "cliente", ignore = true)
    @Mapping(target = "vendedor", ignore = true)
    @Mapping(target = "productos", ignore = true)
    @Mapping(target = "subtotal", ignore = true)
    @Mapping(target = "impuestos", ignore = true)
    @Mapping(target = "total", ignore = true)
    // ✅ IMPORTANTE: NO ignorar descuento en update
    void updateOrdenFromDto(OrdenDto ordenDto, @MappingTarget Orden orden);

    // Necesitas inyectar el UsuarioMapper manualmente
    @ObjectFactory
    default void injectMappers(ClienteMapper clienteMapper, UsuarioMapper usuarioMapper) {
        // Este método permite inyectar los mappers para usar en expressions
    }
}