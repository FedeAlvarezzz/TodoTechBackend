package co.todotech.mapper;

import co.todotech.model.dto.categoria.CategoriaDto;
import co.todotech.model.entities.Categoria;
import org.mapstruct.*;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface CategoriaMapper {

    @Mapping(target = "id", ignore = true)
    Categoria toEntity(CategoriaDto categoriaDto);

    CategoriaDto toDto(Categoria categoria);

    @Mapping(target = "id", ignore = true)
    void updateCategoriaFromDto(CategoriaDto categoriaDto, @MappingTarget Categoria categoria);
}