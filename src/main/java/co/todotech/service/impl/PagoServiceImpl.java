package co.todotech.service.impl;

import co.todotech.mapper.PagoMapper;
import co.todotech.model.dto.pago.PagoDto;
import co.todotech.model.entities.MetodoPago;
import co.todotech.model.entities.Orden;
import co.todotech.model.entities.Pago;
import co.todotech.model.entities.Usuario;
import co.todotech.model.enums.EstadoPago;
import co.todotech.repository.MetodoPagoRepository;
import co.todotech.repository.OrdenRepository;
import co.todotech.repository.PagoRepository;
import co.todotech.repository.UsuarioRepository;
import co.todotech.service.PagoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementación del servicio de gestión de pagos.
 * Proporciona operaciones CRUD, validaciones y consultas avanzadas
 * relacionadas con los pagos asociados a órdenes de venta.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PagoServiceImpl implements PagoService {

    private final PagoMapper pagoMapper;
    private final PagoRepository pagoRepository;
    private final OrdenRepository ordenVentaRepository;
    private final MetodoPagoRepository metodoPagoRepository;
    private final UsuarioRepository usuarioRepository;

    /**
     * Crea un nuevo registro de pago asociado a una orden, usuario y método de pago.
     * @param dto DTO con la información del pago.
     * @return PagoDto con los datos del pago creado.
     * @throws Exception si alguna entidad relacionada no existe o hay conflicto en el número de transacción.
     */
    @Override
    @Transactional
    public PagoDto crearPago(PagoDto dto) throws Exception {
        log.info("Creando pago para orden de venta: {}", dto.ordenVentaId());

        Orden ordenVenta = ordenVentaRepository.findById(dto.ordenVentaId())
                .orElseThrow(() -> new Exception("Orden de venta no encontrada con ID: " + dto.ordenVentaId()));

        MetodoPago metodoPago = metodoPagoRepository.findById(dto.metodoPagoId())
                .orElseThrow(() -> new Exception("Método de pago no encontrado con ID: " + dto.metodoPagoId()));

        Usuario usuario = usuarioRepository.findById(dto.usuarioId())
                .orElseThrow(() -> new Exception("Usuario no encontrado con ID: " + dto.usuarioId()));

        // Validar número de transacción único
        if (dto.numeroTransaccion() != null && !dto.numeroTransaccion().isBlank()) {
            pagoRepository.findByNumeroTransaccion(dto.numeroTransaccion())
                    .ifPresent(p -> {
                        try {
                            throw new Exception("Ya existe un pago con el número de transacción: " + dto.numeroTransaccion());
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    });
        }

        Pago pago = pagoMapper.toEntity(dto);
        pagoRepository.save(pago);

        log.info("Pago creado exitosamente: id={}, ordenVenta={}", pago.getId(), pago.getOrdenVenta().getId());
        return pagoMapper.toDto(pago);
    }

    /**
     * Actualiza los datos de un pago existente.
     * @param id ID del pago a actualizar.
     * @param dto DTO con los nuevos datos.
     * @return PagoDto actualizado.
     * @throws Exception si el pago no existe o hay conflictos de datos.
     */
    @Override
    @Transactional
    public PagoDto actualizarPago(Long id, PagoDto dto) throws Exception {
        log.info("Actualizando pago id={}", id);

        Pago pago = pagoRepository.findById(id)
                .orElseThrow(() -> new Exception("Pago no encontrado con ID: " + id));

        // Validar cambios de relaciones
        if (!pago.getOrdenVenta().getId().equals(dto.ordenVentaId())) {
            ordenVentaRepository.findById(dto.ordenVentaId())
                    .orElseThrow(() -> new Exception("Orden de venta no encontrada con ID: " + dto.ordenVentaId()));
        }

        if (!pago.getMetodoPago().getId().equals(dto.metodoPagoId())) {
            metodoPagoRepository.findById(dto.metodoPagoId())
                    .orElseThrow(() -> new Exception("Método de pago no encontrado con ID: " + dto.metodoPagoId()));
        }

        if (!pago.getUsuario().getId().equals(dto.usuarioId())) {
            usuarioRepository.findById(dto.usuarioId())
                    .orElseThrow(() -> new Exception("Usuario no encontrado con ID: " + dto.usuarioId()));
        }

        // Validar número de transacción único si se cambia
        if (dto.numeroTransaccion() != null && !dto.numeroTransaccion().isBlank() &&
                !dto.numeroTransaccion().equals(pago.getNumeroTransaccion())) {
            pagoRepository.findByNumeroTransaccion(dto.numeroTransaccion())
                    .ifPresent(p -> {
                        try {
                            throw new Exception("Ya existe otro pago con el número de transacción: " + dto.numeroTransaccion());
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    });
        }

        pagoMapper.updatePagoFromDto(dto, pago);
        pagoRepository.save(pago);

        log.info("Pago actualizado: id={}", pago.getId());
        return pagoMapper.toDto(pago);
    }

    /**
     * Elimina un pago existente por su ID.
     * @param id ID del pago.
     * @throws Exception si el pago no existe.
     */
    @Override
    @Transactional
    public void eliminarPago(Long id) throws Exception {
        Pago pago = pagoRepository.findById(id)
                .orElseThrow(() -> new Exception("Pago no encontrado con ID: " + id));

        pagoRepository.delete(pago);
        log.info("Pago eliminado: {}", id);
    }

    /**
     * Obtiene un pago por su identificador.
     * @param id ID del pago.
     * @return PagoDto correspondiente.
     * @throws Exception si no se encuentra el pago.
     */
    @Override
    public PagoDto obtenerPagoPorId(Long id) throws Exception {
        Pago pago = pagoRepository.findById(id)
                .orElseThrow(() -> new Exception("Pago no encontrado con ID: " + id));
        return pagoMapper.toDto(pago);
    }

    /**
     * Lista todos los pagos asociados a una orden de venta.
     * @param ordenVentaId ID de la orden.
     * @return Lista de pagos asociados.
     */
    @Override
    public List<PagoDto> obtenerPagosPorOrdenVenta(Long ordenVentaId) {
        return pagoRepository.findByOrdenVentaId(ordenVentaId).stream()
                .map(pagoMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene todos los pagos filtrados por estado (APROBADO, RECHAZADO, etc.).
     * @param estadoPago Estado del pago.
     * @return Lista de pagos con dicho estado.
     */
    @Override
    public List<PagoDto> obtenerPagosPorEstado(EstadoPago estadoPago) {
        return pagoRepository.findByEstadoPago(estadoPago).stream()
                .map(pagoMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene los pagos realizados por un usuario específico.
     * @param usuarioId ID del usuario.
     * @return Lista de pagos.
     */
    @Override
    public List<PagoDto> obtenerPagosPorUsuario(Long usuarioId) {
        return pagoRepository.findByUsuarioId(usuarioId).stream()
                .map(pagoMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene los pagos realizados dentro de un rango de fechas.
     * @param fechaInicio Fecha inicial del rango.
     * @param fechaFin Fecha final del rango.
     * @return Lista de pagos en el rango indicado.
     */
    @Override
    public List<PagoDto> obtenerPagosPorFecha(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        return pagoRepository.findByFechaPagoBetween(fechaInicio, fechaFin).stream()
                .map(pagoMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene los pagos cuyo monto es mayor o igual a un valor mínimo.
     * @param montoMinimo Valor mínimo a filtrar.
     * @return Lista de pagos que cumplen el criterio.
     */
    @Override
    public List<PagoDto> obtenerPagosPorMontoMinimo(Double montoMinimo) {
        return pagoRepository.findByMontoGreaterThanEqual(montoMinimo).stream()
                .map(pagoMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene los pagos aprobados para una orden específica.
     * @param ordenVentaId ID de la orden.
     * @return Lista de pagos aprobados.
     */
    @Override
    public List<PagoDto> obtenerPagosAprobadosPorOrdenVenta(Long ordenVentaId) {
        return pagoRepository.findPagosAprobadosByOrdenVenta(ordenVentaId).stream()
                .map(pagoMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Calcula el total de pagos aprobados asociados a una orden.
     * @param ordenVentaId ID de la orden.
     * @return Suma total de los montos aprobados.
     */
    @Override
    public Double obtenerTotalPagosAprobadosPorOrdenVenta(Long ordenVentaId) {
        Double total = pagoRepository.sumMontoAprobadoByOrdenVenta(ordenVentaId);
        return total != null ? total : 0.0;
    }

    /**
     * Obtiene todos los pagos ordenados por fecha descendente.
     * @return Lista completa de pagos.
     */
    @Override
    public List<PagoDto> obtenerTodosLosPagos() {
        log.info("Obteniendo todos los pagos");
        return pagoRepository.findAllOrderByFechaPagoDesc().stream()
                .map(pagoMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Busca un pago por su número de transacción único.
     * @param numeroTransaccion Número de transacción del pago.
     * @return PagoDto correspondiente.
     * @throws Exception si no se encuentra el pago.
     */
    @Override
    public PagoDto obtenerPagoPorNumeroTransaccion(String numeroTransaccion) throws Exception {
        Pago pago = pagoRepository.findByNumeroTransaccion(numeroTransaccion)
                .orElseThrow(() -> new Exception("Pago no encontrado con número de transacción: " + numeroTransaccion));
        return pagoMapper.toDto(pago);
    }
}