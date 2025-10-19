package co.todotech.controller;

import co.todotech.model.dto.MensajeDto;
import co.todotech.model.dto.ordenventa.CreateOrdenDto;
import co.todotech.model.dto.ordenventa.OrdenConDetallesDto;
import co.todotech.model.dto.ordenventa.OrdenDto;
import co.todotech.model.enums.EstadoOrden;
import co.todotech.service.OrdenService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.PositiveOrZero;
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

    // ✅ CORREGIDO: Agregar nombre explícito al @PathVariable
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MensajeDto<OrdenDto>> obtenerOrden(@PathVariable("id") Long id) {
        try {
            OrdenDto orden = ordenService.obtenerOrden(id);
            return ResponseEntity.ok(new MensajeDto<>(false, "Orden encontrada", orden));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MensajeDto<>(true, e.getMessage(), null));
        }
    }

    @GetMapping("/{id}/detalles")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MensajeDto<OrdenConDetallesDto>> obtenerOrdenConDetalles(
            @PathVariable("id") Long id) {
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

    // ✅ CORREGIDO: Agregar nombre explícito al @PathVariable
    @GetMapping("/cliente/{clienteId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MensajeDto<List<OrdenDto>>> obtenerOrdenesPorCliente(@PathVariable("clienteId") Long clienteId) {
        try {
            List<OrdenDto> ordenes = ordenService.obtenerOrdenesPorCliente(clienteId);
            return ResponseEntity.ok(new MensajeDto<>(false, "Órdenes del cliente obtenidas", ordenes));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MensajeDto<>(true, e.getMessage(), null));
        }
    }

    // ✅ CORREGIDO: Agregar nombre explícito al @PathVariable
    @GetMapping("/estado/{estado}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MensajeDto<List<OrdenDto>>> obtenerOrdenesPorEstado(@PathVariable("estado") EstadoOrden estado) {
        try {
            List<OrdenDto> ordenes = ordenService.obtenerOrdenesPorEstado(estado);
            return ResponseEntity.ok(new MensajeDto<>(false, "Órdenes por estado obtenidas", ordenes));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MensajeDto<>(true, e.getMessage(), null));
        }
    }

    // ✅ CORREGIDO: Agregar nombre explícito al @PathVariable
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('VENDEDOR')")
    public ResponseEntity<MensajeDto<OrdenDto>> actualizarOrden(@PathVariable("id") Long id,
                                                                @Valid @RequestBody OrdenDto ordenDto) {
        try {
            OrdenDto ordenActualizada = ordenService.actualizarOrden(id, ordenDto);
            return ResponseEntity.ok(new MensajeDto<>(false, "Orden actualizada exitosamente", ordenActualizada));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MensajeDto<>(true, e.getMessage(), null));
        }
    }

    // ✅ CORREGIDO: Agregar nombre explícito al @PathVariable
    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasRole('ADMIN') or hasRole('VENDEDOR')")
    public ResponseEntity<MensajeDto<OrdenDto>> actualizarEstadoOrden(@PathVariable("id") Long id,
                                                                      @RequestParam EstadoOrden nuevoEstado) {
        try {
            OrdenDto ordenActualizada = ordenService.actualizarEstadoOrden(id, nuevoEstado);
            return ResponseEntity.ok(new MensajeDto<>(false, "Estado de orden actualizado", ordenActualizada));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MensajeDto<>(true, e.getMessage(), null));
        }
    }

    // ✅ CORREGIDO: Agregar nombre explícito al @PathVariable
    @PatchMapping("/{id}/pagada")
    @PreAuthorize("hasRole('ADMIN') or hasRole('VENDEDOR')")
    public ResponseEntity<MensajeDto<OrdenDto>> marcarComoPagada(@PathVariable("id") Long id) {
        try {
            OrdenDto ordenActualizada = ordenService.marcarComoPagada(id);
            return ResponseEntity.ok(new MensajeDto<>(false, "Orden marcada como pagada", ordenActualizada));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MensajeDto<>(true, e.getMessage(), null));
        }
    }

    // ✅ CORREGIDO: Agregar nombre explícito al @PathVariable
    @PatchMapping("/{id}/entregada")
    @PreAuthorize("hasRole('ADMIN') or hasRole('VENDEDOR')")
    public ResponseEntity<MensajeDto<OrdenDto>> marcarComoEntregada(@PathVariable("id") Long id) {
        try {
            OrdenDto ordenActualizada = ordenService.marcarComoEntregada(id);
            return ResponseEntity.ok(new MensajeDto<>(false, "Orden marcada como entregada", ordenActualizada));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MensajeDto<>(true, e.getMessage(), null));
        }
    }

    // ✅ CORREGIDO: Agregar nombre explícito al @PathVariable
    @PatchMapping("/{id}/cerrada")
    @PreAuthorize("hasRole('ADMIN') or hasRole('VENDEDOR')")
    public ResponseEntity<MensajeDto<OrdenDto>> marcarComoCerrada(@PathVariable("id") Long id) {
        try {
            OrdenDto ordenActualizada = ordenService.marcarComoCerrada(id);
            return ResponseEntity.ok(new MensajeDto<>(false, "Orden marcada como cerrada", ordenActualizada));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MensajeDto<>(true, e.getMessage(), null));
        }
    }

    // ✅ CORREGIDO: Agregar nombre explícito al @PathVariable
    @PatchMapping("/{id}/descuento")
    @PreAuthorize("hasRole('ADMIN') or hasRole('VENDEDOR')")
    public ResponseEntity<MensajeDto<OrdenDto>> aplicarDescuento(@PathVariable("id") Long id,
                                                                 @RequestParam Double porcentajeDescuento) {
        try {
            OrdenDto ordenActualizada = ordenService.aplicarDescuento(id, porcentajeDescuento);
            return ResponseEntity.ok(new MensajeDto<>(false, "Descuento aplicado exitosamente", ordenActualizada));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MensajeDto<>(true, e.getMessage(), null));
        }
    }

    // ✅ CORREGIDO: Agregar nombre explícito al @PathVariable
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MensajeDto<String>> eliminarOrden(@PathVariable("id") Long id) {
        try {
            ordenService.eliminarOrden(id);
            return ResponseEntity.ok(new MensajeDto<>(false, "Orden eliminada exitosamente"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MensajeDto<>(true, e.getMessage()));
        }
    }

    @GetMapping("/vendedor/{vendedorId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MensajeDto<List<OrdenDto>>> obtenerOrdenesPorVendedor(
            @PathVariable("vendedorId") Long vendedorId) {
        try {
            List<OrdenDto> ordenes = ordenService.obtenerOrdenesPorVendedor(vendedorId);
            return ResponseEntity.ok(new MensajeDto<>(false, "Órdenes del vendedor obtenidas", ordenes));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MensajeDto<>(true, e.getMessage(), null));
        }
    }

    @PatchMapping("/{id}/agregando-productos")
    @PreAuthorize("hasRole('ADMIN') or hasRole('VENDEDOR')")
    public ResponseEntity<MensajeDto<OrdenDto>> marcarComoAgregandoProductos(
            @PathVariable("id") Long id) {
        try {
            OrdenDto ordenActualizada = ordenService.marcarComoAgregandoProductos(id);
            return ResponseEntity.ok(new MensajeDto<>(false, "Orden marcada como agregando productos", ordenActualizada));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MensajeDto<>(true, e.getMessage(), null));
        }
    }

    @PatchMapping("/{id}/disponible-pago")
    @PreAuthorize("hasRole('ADMIN') or hasRole('VENDEDOR')")
    public ResponseEntity<MensajeDto<OrdenDto>> marcarComoDisponibleParaPago(
            @PathVariable("id") Long id) {
        try {
            OrdenDto ordenActualizada = ordenService.marcarComoDisponibleParaPago(id);
            return ResponseEntity.ok(new MensajeDto<>(false, "Orden marcada como disponible para pago", ordenActualizada));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MensajeDto<>(true, e.getMessage(), null));
        }
    }

    // ✅ CORREGIDO: Agregar nombre explícito al @RequestParam
    @PatchMapping("/{id}/total")
    @PreAuthorize("hasRole('ADMIN') or hasRole('VENDEDOR')")
    public ResponseEntity<MensajeDto<OrdenDto>> actualizarTotalOrden(
            @PathVariable("id") Long id,
            @RequestParam("total") @PositiveOrZero(message = "El total debe ser mayor o igual a 0") Double total) {
        try {
            OrdenDto ordenActualizada = ordenService.actualizarTotalOrden(id, total);
            return ResponseEntity.ok(new MensajeDto<>(false, "Total de orden actualizado exitosamente", ordenActualizada));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MensajeDto<>(true, e.getMessage(), null));
        }
    }
}