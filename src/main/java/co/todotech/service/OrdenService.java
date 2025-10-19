package co.todotech.service;

import co.todotech.model.dto.ordenventa.CreateOrdenDto;
import co.todotech.model.dto.ordenventa.OrdenConDetallesDto;
import co.todotech.model.dto.ordenventa.OrdenDto;
import co.todotech.model.enums.EstadoOrden;

import java.util.List;

public interface OrdenService {

    OrdenDto crearOrden(CreateOrdenDto createOrdenDto);

    OrdenDto quitarDescuento(Long ordenId);

    OrdenConDetallesDto obtenerOrdenConDetalles(Long id);

    OrdenDto obtenerOrden(Long id);

    List<OrdenDto> obtenerTodasLasOrdenes();

    List<OrdenDto> obtenerOrdenesPorCliente(Long clienteId);

    List<OrdenDto> obtenerOrdenesPorEstado(EstadoOrden estado);

    OrdenDto actualizarOrden(Long id, OrdenDto ordenDto);

    OrdenDto actualizarEstadoOrden(Long id, EstadoOrden nuevoEstado);

    OrdenDto marcarComoPagada(Long id);

    OrdenDto marcarComoEntregada(Long id);

    OrdenDto marcarComoCerrada(Long id);

    void eliminarOrden(Long id);

    OrdenDto aplicarDescuento(Long ordenId, Double porcentajeDescuento);

    // En OrdenService.java - añade este método:
    List<OrdenDto> obtenerOrdenesPorVendedor(Long vendedorId);

    OrdenDto marcarComoAgregandoProductos(Long id);
    OrdenDto marcarComoDisponibleParaPago(Long id);

    OrdenDto actualizarTotalOrden(Long ordenId, Double nuevoTotal);
}