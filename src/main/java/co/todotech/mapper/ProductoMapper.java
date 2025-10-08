package co.todotech.mapper;

import co.todotech.model.dto.producto.ProductoDto;
import co.todotech.model.entities.Producto;
import org.mapstruct.*;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface ProductoMapper {

    @Mapping(target = "id", ignore = true) // Ignorar ID al crear desde DTO
    @Mapping(target = "estado", ignore = true) // El estado se maneja en el servicio
    Producto toEntity(ProductoDto productoDto);

    ProductoDto toDto(Producto producto);

    @Mapping(target = "id", ignore = true)
    void updateProductoFromDto(ProductoDto productoDto, @MappingTarget Producto producto);
}