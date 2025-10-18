package co.todotech.controller;

import co.todotech.model.dto.MensajeDto;
import co.todotech.model.dto.pago.PagoDto;
import co.todotech.model.enums.EstadoPago;
import co.todotech.service.PagoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/pagos")
public class PagoController {

    private final PagoService pagoService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
    public ResponseEntity<MensajeDto<PagoDto>> crearPago(@Valid @RequestBody PagoDto dto) {
        try {
            PagoDto pagoCreado = pagoService.crearPago(dto);
            return ResponseEntity.ok(new MensajeDto<>(false, "Pago creado exitosamente", pagoCreado));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MensajeDto<>(true, e.getMessage(), null));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
    public ResponseEntity<MensajeDto<PagoDto>> actualizarPago(@PathVariable Long id,
                                                              @Valid @RequestBody PagoDto dto) {
        try {
            PagoDto pagoActualizado = pagoService.actualizarPago(id, dto);
            return ResponseEntity.ok(new MensajeDto<>(false, "Pago actualizado exitosamente", pagoActualizado));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MensajeDto<>(true, e.getMessage(), null));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MensajeDto<String>> eliminarPago(@PathVariable Long id) {
        try {
            pagoService.eliminarPago(id);
            return ResponseEntity.ok(new MensajeDto<>(false, "Pago eliminado exitosamente"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MensajeDto<>(true, e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MensajeDto<PagoDto>> obtenerPagoPorId(@PathVariable Long id) {
        try {
            PagoDto dto = pagoService.obtenerPagoPorId(id);
            return ResponseEntity.ok(new MensajeDto<>(false, "Pago encontrado", dto));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MensajeDto<>(true, e.getMessage(), null));
        }
    }

    @GetMapping("/orden-venta/{ordenVentaId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MensajeDto<List<PagoDto>>> obtenerPagosPorOrdenVenta(@PathVariable Long ordenVentaId) {
        try {
            List<PagoDto> lista = pagoService.obtenerPagosPorOrdenVenta(ordenVentaId);
            return ResponseEntity.ok(new MensajeDto<>(false, "Pagos por orden de venta obtenidos", lista));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MensajeDto<>(true, e.getMessage(), null));
        }
    }

    @GetMapping("/estado/{estado}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MensajeDto<List<PagoDto>>> obtenerPagosPorEstado(@PathVariable String estado) {
        try {
            EstadoPago estadoPago = EstadoPago.valueOf(estado.toUpperCase());
            List<PagoDto> lista = pagoService.obtenerPagosPorEstado(estadoPago);
            return ResponseEntity.ok(new MensajeDto<>(false, "Pagos por estado obtenidos", lista));
        } catch (IllegalArgumentException iae) {
            return ResponseEntity.badRequest().body(new MensajeDto<>(true,
                    "Estado de pago inválido. Usa: PENDIENTE, APROBADO, RECHAZADO", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MensajeDto<>(true, e.getMessage(), null));
        }
    }

    @GetMapping("/usuario/{usuarioId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MensajeDto<List<PagoDto>>> obtenerPagosPorUsuario(@PathVariable Long usuarioId) {
        try {
            List<PagoDto> lista = pagoService.obtenerPagosPorUsuario(usuarioId);
            return ResponseEntity.ok(new MensajeDto<>(false, "Pagos por usuario obtenidos", lista));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MensajeDto<>(true, e.getMessage(), null));
        }
    }

    @GetMapping("/fecha")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MensajeDto<List<PagoDto>>> obtenerPagosPorFecha(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin) {
        try {
            List<PagoDto> lista = pagoService.obtenerPagosPorFecha(inicio, fin);
            return ResponseEntity.ok(new MensajeDto<>(false, "Pagos por fecha obtenidos", lista));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MensajeDto<>(true, e.getMessage(), null));
        }
    }

    @GetMapping("/monto-minimo/{montoMinimo}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MensajeDto<List<PagoDto>>> obtenerPagosPorMontoMinimo(@PathVariable Double montoMinimo) {
        try {
            List<PagoDto> lista = pagoService.obtenerPagosPorMontoMinimo(montoMinimo);
            return ResponseEntity.ok(new MensajeDto<>(false, "Pagos por monto mínimo obtenidos", lista));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MensajeDto<>(true, e.getMessage(), null));
        }
    }

    @GetMapping("/orden-venta/{ordenVentaId}/aprobados")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MensajeDto<List<PagoDto>>> obtenerPagosAprobadosPorOrdenVenta(@PathVariable Long ordenVentaId) {
        try {
            List<PagoDto> lista = pagoService.obtenerPagosAprobadosPorOrdenVenta(ordenVentaId);
            return ResponseEntity.ok(new MensajeDto<>(false, "Pagos aprobados por orden de venta obtenidos", lista));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MensajeDto<>(true, e.getMessage(), null));
        }
    }

    @GetMapping("/orden-venta/{ordenVentaId}/total-aprobado")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MensajeDto<Double>> obtenerTotalPagosAprobadosPorOrdenVenta(@PathVariable Long ordenVentaId) {
        try {
            Double total = pagoService.obtenerTotalPagosAprobadosPorOrdenVenta(ordenVentaId);
            return ResponseEntity.ok(new MensajeDto<>(false, "Total de pagos aprobados por orden de venta", total));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MensajeDto<>(true, e.getMessage(), 0.0));
        }
    }

    @GetMapping("/transaccion/{numeroTransaccion}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MensajeDto<PagoDto>> obtenerPagoPorNumeroTransaccion(@PathVariable String numeroTransaccion) {
        try {
            PagoDto dto = pagoService.obtenerPagoPorNumeroTransaccion(numeroTransaccion);
            return ResponseEntity.ok(new MensajeDto<>(false, "Pago encontrado por número de transacción", dto));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MensajeDto<>(true, e.getMessage(), null));
        }
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MensajeDto<List<PagoDto>>> obtenerTodosLosPagos() {
        try {
            List<PagoDto> lista = pagoService.obtenerTodosLosPagos();
            return ResponseEntity.ok(new MensajeDto<>(false, "Todos los pagos obtenidos exitosamente", lista));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MensajeDto<>(true, e.getMessage(), null));
        }
    }
}