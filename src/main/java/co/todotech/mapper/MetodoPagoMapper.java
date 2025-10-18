package co.todotech.mapper;

import co.todotech.model.dto.metodopago.MetodoPagoDto;
import co.todotech.model.entities.MetodoPago;
import org.mapstruct.*;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface MetodoPagoMapper {

    @Mapping(target = "id", ignore = true)
    MetodoPago toEntity(MetodoPagoDto metodoPagoDto);

    MetodoPagoDto toDto(MetodoPago metodoPago);

    @Mapping(target = "id", ignore = true)
    void updateMetodoPagoFromDto(MetodoPagoDto metodoPagoDto, @MappingTarget MetodoPago metodoPago);
}