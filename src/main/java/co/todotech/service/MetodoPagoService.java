package co.todotech.service;

import co.todotech.model.dto.metodopago.MetodoPagoDto;
import co.todotech.model.enums.TipoMetodo;

import java.util.List;

public interface MetodoPagoService {

    MetodoPagoDto crearMetodoPago(MetodoPagoDto dto) throws Exception;
    MetodoPagoDto actualizarMetodoPago(Long id, MetodoPagoDto dto) throws Exception;
    void eliminarMetodoPago(Long id) throws Exception;
    MetodoPagoDto obtenerMetodoPagoPorId(Long id) throws Exception;
    List<MetodoPagoDto> obtenerMetodosPagoPorTipo(TipoMetodo tipo);
    List<MetodoPagoDto> obtenerMetodosPagoPorAprobacion(Boolean aprobacion);
    List<MetodoPagoDto> obtenerTodosLosMetodosPago();
    List<MetodoPagoDto> obtenerMetodosPagoConComisionMenorIgual(Double comisionMaxima);
    List<MetodoPagoDto> obtenerMetodosAprobadosOrdenadosPorComision();
}