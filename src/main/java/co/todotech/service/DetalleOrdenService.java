package co.todotech.service;

import co.todotech.model.dto.detalleorden.CreateDetalleOrdenDto;
import co.todotech.model.dto.detalleorden.DetalleOrdenDto;
import co.todotech.model.dto.detalleorden.EliminarDetalleRequest;

import java.util.List;

public interface DetalleOrdenService {

    DetalleOrdenDto crearDetalleOrden(CreateDetalleOrdenDto createDetalleOrdenDto, Long ordenId);

    DetalleOrdenDto obtenerDetalleOrden(Long id);

    List<DetalleOrdenDto> obtenerDetallesPorOrden(Long ordenId);

    DetalleOrdenDto actualizarCantidad(Long detalleId, Integer nuevaCantidad);

    DetalleOrdenDto actualizarDetalleOrden(Long id, DetalleOrdenDto detalleOrdenDto);

    void eliminarDetalleOrden(Long id);

    void eliminarDetallePorProductoYOrden(EliminarDetalleRequest request);

    void validarStockDisponible(Long productoId, Integer cantidadRequerida);
}