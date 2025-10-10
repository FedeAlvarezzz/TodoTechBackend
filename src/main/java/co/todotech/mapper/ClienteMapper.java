package co.todotech.mapper;

import co.todotech.model.dto.cliente.ClienteDto;
import co.todotech.model.entities.Cliente;
import org.mapstruct.*;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface ClienteMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fechaRegistro", ignore = true) // Se genera automáticamente
    Cliente toEntity(ClienteDto clienteDto);

    ClienteDto toDto(Cliente cliente);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fechaRegistro", ignore = true) // No se actualiza
    void updateClienteFromDto(ClienteDto clienteDto, @MappingTarget Cliente cliente);
}