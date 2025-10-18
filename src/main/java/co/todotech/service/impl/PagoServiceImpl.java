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

@Slf4j
@Service
@RequiredArgsConstructor
public class PagoServiceImpl implements PagoService {

    private final PagoMapper pagoMapper;
    private final PagoRepository pagoRepository;
    private final OrdenRepository ordenVentaRepository;
    private final MetodoPagoRepository metodoPagoRepository;
    private final UsuarioRepository usuarioRepository;

    @Override
    @Transactional
    public PagoDto crearPago(PagoDto dto) throws Exception {
        log.info("Creando pago para orden de venta: {}", dto.ordenVentaId());

        // Validar que exista la orden de venta
        Orden ordenVenta = ordenVentaRepository.findById(dto.ordenVentaId())
                .orElseThrow(() -> new Exception("Orden de venta no encontrada con ID: " + dto.ordenVentaId()));

        // Validar que exista el método de pago
        MetodoPago metodoPago = metodoPagoRepository.findById(dto.metodoPagoId())
                .orElseThrow(() -> new Exception("Método de pago no encontrado con ID: " + dto.metodoPagoId()));

        // Validar que exista el usuario
        Usuario usuario = usuarioRepository.findById(dto.usuarioId())
                .orElseThrow(() -> new Exception("Usuario no encontrado con ID: " + dto.usuarioId()));

        // Validar número de transacción único si se proporciona
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

    @Override
    @Transactional
    public PagoDto actualizarPago(Long id, PagoDto dto) throws Exception {
        log.info("Actualizando pago id={}", id);

        Pago pago = pagoRepository.findById(id)
                .orElseThrow(() -> new Exception("Pago no encontrado con ID: " + id));

        // Validar que exista la orden de venta si se cambia
        if (!pago.getOrdenVenta().getId().equals(dto.ordenVentaId())) {
            Orden ordenVenta = ordenVentaRepository.findById(dto.ordenVentaId())
                    .orElseThrow(() -> new Exception("Orden de venta no encontrada con ID: " + dto.ordenVentaId()));
        }

        // Validar que exista el método de pago si se cambia
        if (!pago.getMetodoPago().getId().equals(dto.metodoPagoId())) {
            MetodoPago metodoPago = metodoPagoRepository.findById(dto.metodoPagoId())
                    .orElseThrow(() -> new Exception("Método de pago no encontrado con ID: " + dto.metodoPagoId()));
        }

        // Validar que exista el usuario si se cambia
        if (!pago.getUsuario().getId().equals(dto.usuarioId())) {
            Usuario usuario = usuarioRepository.findById(dto.usuarioId())
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

    @Override
    @Transactional
    public void eliminarPago(Long id) throws Exception {
        Pago pago = pagoRepository.findById(id)
                .orElseThrow(() -> new Exception("Pago no encontrado con ID: " + id));

        pagoRepository.delete(pago);
        log.info("Pago eliminado: {}", id);
    }

    @Override
    public PagoDto obtenerPagoPorId(Long id) throws Exception {
        Pago pago = pagoRepository.findById(id)
                .orElseThrow(() -> new Exception("Pago no encontrado con ID: " + id));
        return pagoMapper.toDto(pago);
    }

    @Override
    public List<PagoDto> obtenerPagosPorOrdenVenta(Long ordenVentaId) {
        return pagoRepository.findByOrdenVentaId(ordenVentaId).stream()
                .map(pagoMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<PagoDto> obtenerPagosPorEstado(EstadoPago estadoPago) {
        return pagoRepository.findByEstadoPago(estadoPago).stream()
                .map(pagoMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<PagoDto> obtenerPagosPorUsuario(Long usuarioId) {
        return pagoRepository.findByUsuarioId(usuarioId).stream()
                .map(pagoMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<PagoDto> obtenerPagosPorFecha(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        return pagoRepository.findByFechaPagoBetween(fechaInicio, fechaFin).stream()
                .map(pagoMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<PagoDto> obtenerPagosPorMontoMinimo(Double montoMinimo) {
        return pagoRepository.findByMontoGreaterThanEqual(montoMinimo).stream()
                .map(pagoMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<PagoDto> obtenerPagosAprobadosPorOrdenVenta(Long ordenVentaId) {
        return pagoRepository.findPagosAprobadosByOrdenVenta(ordenVentaId).stream()
                .map(pagoMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public Double obtenerTotalPagosAprobadosPorOrdenVenta(Long ordenVentaId) {
        Double total = pagoRepository.sumMontoAprobadoByOrdenVenta(ordenVentaId);
        return total != null ? total : 0.0;
    }

    @Override
    public List<PagoDto> obtenerTodosLosPagos() {
        log.info("Obteniendo todos los pagos");
        return pagoRepository.findAllOrderByFechaPagoDesc().stream()
                .map(pagoMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public PagoDto obtenerPagoPorNumeroTransaccion(String numeroTransaccion) throws Exception {
        Pago pago = pagoRepository.findByNumeroTransaccion(numeroTransaccion)
                .orElseThrow(() -> new Exception("Pago no encontrado con número de transacción: " + numeroTransaccion));
        return pagoMapper.toDto(pago);
    }
}