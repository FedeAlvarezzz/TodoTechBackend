package co.todotech.controller;

import co.todotech.model.dto.MensajeDto;
import co.todotech.model.dto.ordenventa.CreateOrdenDto;
import co.todotech.model.dto.ordenventa.OrdenConDetallesDto;
import co.todotech.model.dto.ordenventa.OrdenDto;
import co.todotech.model.enums.EstadoOrden;
import co.todotech.service.OrdenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/ordenes")
public class OrdenController {

    private final OrdenService ordenService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('VENDEDOR')")
    public ResponseEntity<MensajeDto<OrdenDto>> crearOrden(@Valid @RequestBody CreateOrdenDto createOrdenDto) {
        try {
            OrdenDto ordenCreada = ordenService.crearOrden(createOrdenDto);
            return ResponseEntity.ok(new MensajeDto<>(false, "Orden creada exitosamente", ordenCreada));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MensajeDto<>(true, e.getMessage(), null));
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MensajeDto<OrdenDto>> obtenerOrden(@PathVariable Long id) {
        try {
            OrdenDto orden = ordenService.obtenerOrden(id);
            return ResponseEntity.ok(new MensajeDto<>(false, "Orden encontrada", orden));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MensajeDto<>(true, e.getMessage(), null));
        }
    }

    @GetMapping("/{id}/detalles")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MensajeDto<OrdenConDetallesDto>> obtenerOrdenConDetalles(@PathVariable Long id) {
        try {
            OrdenConDetallesDto orden = ordenService.obtenerOrdenConDetalles(id);
            return ResponseEntity.ok(new MensajeDto<>(false, "Orden con detalles encontrada", orden));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MensajeDto<>(true, e.getMessage(), null));
        }
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MensajeDto<List<OrdenDto>>> obtenerTodasLasOrdenes() {
        try {
            List<OrdenDto> ordenes = ordenService.obtenerTodasLasOrdenes();
            return ResponseEntity.ok(new MensajeDto<>(false, "Órdenes obtenidas", ordenes));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MensajeDto<>(true, e.getMessage(), null));
        }
    }

    @GetMapping("/cliente/{clienteId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MensajeDto<List<OrdenDto>>> obtenerOrdenesPorCliente(@PathVariable Long clienteId) {
        try {
            List<OrdenDto> ordenes = ordenService.obtenerOrdenesPorCliente(clienteId);
            return ResponseEntity.ok(new MensajeDto<>(false, "Órdenes del cliente obtenidas", ordenes));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MensajeDto<>(true, e.getMessage(), null));
        }
    }

    @GetMapping("/estado/{estado}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MensajeDto<List<OrdenDto>>> obtenerOrdenesPorEstado(@PathVariable EstadoOrden estado) {
        try {
            List<OrdenDto> ordenes = ordenService.obtenerOrdenesPorEstado(estado);
            return ResponseEntity.ok(new MensajeDto<>(false, "Órdenes por estado obtenidas", ordenes));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MensajeDto<>(true, e.getMessage(), null));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('VENDEDOR')")
    public ResponseEntity<MensajeDto<OrdenDto>> actualizarOrden(@PathVariable Long id,
                                                                @Valid @RequestBody OrdenDto ordenDto) {
        try {
            OrdenDto ordenActualizada = ordenService.actualizarOrden(id, ordenDto);
            return ResponseEntity.ok(new MensajeDto<>(false, "Orden actualizada exitosamente", ordenActualizada));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MensajeDto<>(true, e.getMessage(), null));
        }
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasRole('ADMIN') or hasRole('VENDEDOR')")
    public ResponseEntity<MensajeDto<OrdenDto>> actualizarEstadoOrden(@PathVariable Long id,
                                                                      @RequestParam EstadoOrden nuevoEstado) {
        try {
            OrdenDto ordenActualizada = ordenService.actualizarEstadoOrden(id, nuevoEstado);
            return ResponseEntity.ok(new MensajeDto<>(false, "Estado de orden actualizado", ordenActualizada));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MensajeDto<>(true, e.getMessage(), null));
        }
    }

    @PatchMapping("/{id}/pagada")
    @PreAuthorize("hasRole('ADMIN') or hasRole('VENDEDOR')")
    public ResponseEntity<MensajeDto<OrdenDto>> marcarComoPagada(@PathVariable Long id) {
        try {
            OrdenDto ordenActualizada = ordenService.marcarComoPagada(id);
            return ResponseEntity.ok(new MensajeDto<>(false, "Orden marcada como pagada", ordenActualizada));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MensajeDto<>(true, e.getMessage(), null));
        }
    }

    @PatchMapping("/{id}/entregada")
    @PreAuthorize("hasRole('ADMIN') or hasRole('VENDEDOR')")
    public ResponseEntity<MensajeDto<OrdenDto>> marcarComoEntregada(@PathVariable Long id) {
        try {
            OrdenDto ordenActualizada = ordenService.marcarComoEntregada(id);
            return ResponseEntity.ok(new MensajeDto<>(false, "Orden marcada como entregada", ordenActualizada));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MensajeDto<>(true, e.getMessage(), null));
        }
    }

    @PatchMapping("/{id}/cerrada")
    @PreAuthorize("hasRole('ADMIN') or hasRole('VENDEDOR')")
    public ResponseEntity<MensajeDto<OrdenDto>> marcarComoCerrada(@PathVariable Long id) {
        try {
            OrdenDto ordenActualizada = ordenService.marcarComoCerrada(id);
            return ResponseEntity.ok(new MensajeDto<>(false, "Orden marcada como cerrada", ordenActualizada));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MensajeDto<>(true, e.getMessage(), null));
        }
    }

    @PatchMapping("/{id}/descuento")
    @PreAuthorize("hasRole('ADMIN') or hasRole('VENDEDOR')")
    public ResponseEntity<MensajeDto<OrdenDto>> aplicarDescuento(@PathVariable Long id,
                                                                 @RequestParam Double porcentajeDescuento) {
        try {
            OrdenDto ordenActualizada = ordenService.aplicarDescuento(id, porcentajeDescuento);
            return ResponseEntity.ok(new MensajeDto<>(false, "Descuento aplicado exitosamente", ordenActualizada));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MensajeDto<>(true, e.getMessage(), null));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MensajeDto<String>> eliminarOrden(@PathVariable Long id) {
        try {
            ordenService.eliminarOrden(id);
            return ResponseEntity.ok(new MensajeDto<>(false, "Orden eliminada exitosamente"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MensajeDto<>(true, e.getMessage()));
        }
    }
}