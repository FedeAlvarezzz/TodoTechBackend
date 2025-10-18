package co.todotech.mapper;

import co.todotech.model.dto.pago.PagoDto;

import co.todotech.model.entities.Orden;
import co.todotech.model.entities.Pago;
import co.todotech.model.entities.Usuario;
import co.todotech.model.entities.MetodoPago;
import org.mapstruct.*;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface PagoMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fechaPago", ignore = true)
    @Mapping(target = "ordenVenta", source = "ordenVentaId")
    @Mapping(target = "metodoPago", source = "metodoPagoId")
    @Mapping(target = "usuario", source = "usuarioId")
    Pago toEntity(PagoDto pagoDto);

    @Mapping(target = "ordenVentaId", source = "ordenVenta.id")
    @Mapping(target = "metodoPagoId", source = "metodoPago.id")
    @Mapping(target = "usuarioId", source = "usuario.id")
    PagoDto toDto(Pago pago);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fechaPago", ignore = true)
    @Mapping(target = "ordenVenta", source = "ordenVentaId")
    @Mapping(target = "metodoPago", source = "metodoPagoId")
    @Mapping(target = "usuario", source = "usuarioId")
    void updatePagoFromDto(PagoDto pagoDto, @MappingTarget Pago pago);

    // Métodos auxiliares para las conversiones de ID a entidad
    default Orden mapOrdenVenta(Long ordenVentaId) {
        if (ordenVentaId == null) return null;
        Orden ordenVenta = new Orden();
        ordenVenta.setId(ordenVentaId);
        return ordenVenta;
    }

    default MetodoPago mapMetodoPago(Long metodoPagoId) {
        if (metodoPagoId == null) return null;
        MetodoPago metodoPago = new MetodoPago();
        metodoPago.setId(metodoPagoId);
        return metodoPago;
    }

    default Usuario mapUsuario(Long usuarioId) {
        if (usuarioId == null) return null;
        Usuario usuario = new Usuario();
        usuario.setId(usuarioId);
        return usuario;
    }
}