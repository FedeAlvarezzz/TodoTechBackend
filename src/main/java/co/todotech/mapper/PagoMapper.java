package co.todotech.mapper;

import co.todotech.model.dto.pago.PagoDto;
import co.todotech.model.entities.Orden;
import co.todotech.model.entities.Pago;
import co.todotech.model.entities.Usuario;
import co.todotech.model.entities.MetodoPago;
import org.mapstruct.*;

/**
 * Mapper de MapStruct encargado de convertir entre la entidad {@link Pago}
 * y su correspondiente DTO {@link PagoDto}.
 *
 * Este mapper simplifica la conversión entre objetos de la capa de persistencia
 * (Entity) y los objetos de transferencia de datos (DTO),
 * especialmente en relaciones con otras entidades como Orden, Usuario y Método de Pago.
 */
@Mapper(
        componentModel = "spring", // Permite que Spring inyecte automáticamente el mapper como un bean
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, // Ignora los valores nulos en actualizaciones
        unmappedTargetPolicy = ReportingPolicy.IGNORE // Evita advertencias por campos no mapeados explícitamente
)
public interface PagoMapper {

    /**
     * Convierte un {@link PagoDto} en una entidad {@link Pago}.
     *
     * - Ignora el campo "id" (lo genera la base de datos).
     * - Ignora el campo "fechaPago" (probablemente se asigna al momento del pago).
     * - Convierte los IDs de orden, método de pago y usuario en entidades correspondientes.
     *
     * @param pagoDto DTO con la información del pago.
     * @return Entidad {@link Pago} lista para persistirse.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fechaPago", ignore = true)
    @Mapping(target = "ordenVenta", source = "ordenVentaId")
    @Mapping(target = "metodoPago", source = "metodoPagoId")
    @Mapping(target = "usuario", source = "usuarioId")
    Pago toEntity(PagoDto pagoDto);

    /**
     * Convierte una entidad {@link Pago} a un {@link PagoDto}.
     *
     * - Extrae los IDs de las entidades relacionadas (ordenVenta, metodoPago, usuario)
     *   para que el DTO contenga solo valores simples (primitivos).
     *
     * @param pago Entidad {@link Pago}.
     * @return DTO {@link PagoDto} con los datos convertidos.
     */
    @Mapping(target = "ordenVentaId", source = "ordenVenta.id")
    @Mapping(target = "metodoPagoId", source = "metodoPago.id")
    @Mapping(target = "usuarioId", source = "usuario.id")
    PagoDto toDto(Pago pago);

    /**
     * Actualiza una entidad {@link Pago} existente a partir de un {@link PagoDto}.
     *
     * - Ignora el campo "id" y "fechaPago" para no sobrescribir valores del sistema.
     * - Solo actualiza los campos no nulos del DTO.
     * - Reasigna las relaciones (orden, método de pago, usuario) a partir de sus IDs.
     *
     * @param pagoDto DTO con los nuevos valores.
     * @param pago Entidad que será actualizada.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fechaPago", ignore = true)
    @Mapping(target = "ordenVenta", source = "ordenVentaId")
    @Mapping(target = "metodoPago", source = "metodoPagoId")
    @Mapping(target = "usuario", source = "usuarioId")
    void updatePagoFromDto(PagoDto pagoDto, @MappingTarget Pago pago);

    // =====================================================
    // MÉTODOS AUXILIARES
    // =====================================================

    /**
     * Convierte un ID de orden en una entidad {@link Orden} con solo su ID asignado.
     * Esto permite establecer relaciones sin tener que consultar la base de datos.
     */
    default Orden mapOrdenVenta(Long ordenVentaId) {
        if (ordenVentaId == null) return null;
        Orden ordenVenta = new Orden();
        ordenVenta.setId(ordenVentaId);
        return ordenVenta;
    }

    /**
     * Convierte un ID de método de pago en una entidad {@link MetodoPago}.
     */
    default MetodoPago mapMetodoPago(Long metodoPagoId) {
        if (metodoPagoId == null) return null;
        MetodoPago metodoPago = new MetodoPago();
        metodoPago.setId(metodoPagoId);
        return metodoPago;
    }

    /**
     * Convierte un ID de usuario en una entidad {@link Usuario}.
     */
    default Usuario mapUsuario(Long usuarioId) {
        if (usuarioId == null) return null;
        Usuario usuario = new Usuario();
        usuario.setId(usuarioId);
        return usuario;
    }
}