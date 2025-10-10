package co.todotech.mapper;

import co.todotech.model.dto.producto.ProductoDto;
import co.todotech.model.entities.Producto;
import org.mapstruct.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface ProductoMapper {

    // Método para obtener el logger
    default Logger getLog() {
        return LoggerFactory.getLogger(ProductoMapper.class);
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "estado", ignore = true) // El estado se maneja en el servicio
    Producto toEntity(ProductoDto productoDto);

    ProductoDto toDto(Producto producto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "estado", ignore = true) // El estado se maneja en el servicio
    void updateProductoFromDto(ProductoDto productoDto, @MappingTarget Producto producto);

    // Método seguro con logs para debug
    default ProductoDto toDtoWithLogs(Producto producto) {
        Logger log = getLog();
        log.debug("=== MAPEANDO PRODUCTO ===");
        log.debug("Producto: {} (ID: {})", producto.getNombre(), producto.getId());

        ProductoDto dto = toDto(producto);

        log.debug("DTO mapeado - Nombre: {}, Precio: {}", dto.getNombre(), dto.getPrecio());
        log.debug("=== FIN MAPEO PRODUCTO ===");

        return dto;
    }

    // Método para creación con validaciones adicionales
    default Producto toEntityWithValidation(ProductoDto productoDto) {
        Logger log = getLog();
        log.debug("Creando entidad Producto desde DTO: {}", productoDto.getNombre());

        if (productoDto.getNombre() == null || productoDto.getNombre().trim().isEmpty()) {
            log.warn("Intento de crear producto sin nombre");
            throw new IllegalArgumentException("El nombre del producto es requerido");
        }

        Producto producto = toEntity(productoDto);
        log.debug("Entidad Producto creada para: {}", producto.getNombre());

        return producto;
    }
}