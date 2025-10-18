package co.todotech.service;

import co.todotech.model.dto.pago.PagoDto;
import co.todotech.model.enums.EstadoPago;

import java.time.LocalDateTime;
import java.util.List;

public interface PagoService {

    PagoDto crearPago(PagoDto dto) throws Exception;
    PagoDto actualizarPago(Long id, PagoDto dto) throws Exception;
    void eliminarPago(Long id) throws Exception;
    PagoDto obtenerPagoPorId(Long id) throws Exception;
    List<PagoDto> obtenerPagosPorOrdenVenta(Long ordenVentaId);
    List<PagoDto> obtenerPagosPorEstado(EstadoPago estadoPago);
    List<PagoDto> obtenerPagosPorUsuario(Long usuarioId);
    List<PagoDto> obtenerPagosPorFecha(LocalDateTime fechaInicio, LocalDateTime fechaFin);
    List<PagoDto> obtenerPagosPorMontoMinimo(Double montoMinimo);
    List<PagoDto> obtenerPagosAprobadosPorOrdenVenta(Long ordenVentaId);
    Double obtenerTotalPagosAprobadosPorOrdenVenta(Long ordenVentaId);
    List<PagoDto> obtenerTodosLosPagos();
    PagoDto obtenerPagoPorNumeroTransaccion(String numeroTransaccion) throws Exception;
}