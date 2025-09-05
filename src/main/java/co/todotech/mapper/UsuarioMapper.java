package co.todotech.mapper;

import co.todotech.model.dto.usuario.UsuarioDto;
import co.todotech.model.entities.Usuario;
import org.mapstruct.*;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface UsuarioMapper {

    @Mapping(target = "id", ignore = true) // Ignorar ID al crear desde DTO
    @Mapping(target = "fechaCreacion", ignore = true) // La fecha se maneja automáticamente
    @Mapping(target = "estado", ignore = true) // El estado se maneja en el servicio
    Usuario toEntity(UsuarioDto usuarioDto);

    UsuarioDto toDto(Usuario usuario);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "estado", ignore = true)
    void updateUsuarioFromDto(UsuarioDto usuarioDto, @MappingTarget Usuario usuario);
}