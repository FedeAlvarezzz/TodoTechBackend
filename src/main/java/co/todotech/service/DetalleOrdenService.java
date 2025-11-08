package co.todotech.service;

import co.todotech.model.dto.detalleorden.CreateDetalleOrdenDto;
import co.todotech.model.dto.detalleorden.DetalleOrdenDto;
import co.todotech.model.dto.detalleorden.EliminarDetalleRequest;
import co.todotech.model.dto.detalleorden.validacion.BulkStockValidationRequest;
import co.todotech.model.dto.detalleorden.validacion.BulkValidationResultDto;
import co.todotech.model.dto.detalleorden.validacion.StockValidationRequest;
import co.todotech.model.dto.detalleorden.validacion.ValidationResultDto;

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

    ValidationResultDto validarStockParaDetalle(StockValidationRequest request);
    BulkValidationResultDto validarStockMultiple(BulkStockValidationRequest request);
    List<ValidationResultDto> obtenerProductosStockCritico();
    ValidationResultDto obtenerStockDisponible(Long productoId);

}