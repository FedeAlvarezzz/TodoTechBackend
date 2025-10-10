package co.todotech.controller;

import co.todotech.model.dto.MensajeDto;
import co.todotech.model.dto.detalleorden.CreateDetalleOrdenDto;
import co.todotech.model.dto.detalleorden.DetalleOrdenDto;
import co.todotech.model.dto.detalleorden.EliminarDetalleRequest;
import co.todotech.service.DetalleOrdenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/detalles-orden")
public class DetalleOrdenController {

    private final DetalleOrdenService detalleOrdenService;

    @PostMapping("/orden/{ordenId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('VENDEDOR')")
    public ResponseEntity<MensajeDto<DetalleOrdenDto>> crearDetalleOrden(
            @PathVariable Long ordenId,
            @Valid @RequestBody CreateDetalleOrdenDto dto) {
        try {
            DetalleOrdenDto detalleCreado = detalleOrdenService.crearDetalleOrden(dto, ordenId);
            return ResponseEntity.ok(new MensajeDto<>(false, "Detalle de orden creado exitosamente", detalleCreado));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MensajeDto<>(true, e.getMessage(), null));
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MensajeDto<DetalleOrdenDto>> obtenerDetalleOrden(@PathVariable Long id) {
        try {
            DetalleOrdenDto dto = detalleOrdenService.obtenerDetalleOrden(id);
            return ResponseEntity.ok(new MensajeDto<>(false, "Detalle de orden encontrado", dto));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MensajeDto<>(true, e.getMessage(), null));
        }
    }

    @GetMapping("/orden/{ordenId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MensajeDto<List<DetalleOrdenDto>>> obtenerDetallesPorOrden(@PathVariable Long ordenId) {
        try {
            List<DetalleOrdenDto> lista = detalleOrdenService.obtenerDetallesPorOrden(ordenId);
            return ResponseEntity.ok(new MensajeDto<>(false, "Detalles de orden obtenidos", lista));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MensajeDto<>(true, e.getMessage(), null));
        }
    }

    @PatchMapping("/{id}/cantidad")
    @PreAuthorize("hasRole('ADMIN') or hasRole('VENDEDOR')")
    public ResponseEntity<MensajeDto<DetalleOrdenDto>> actualizarCantidad(
            @PathVariable Long id,
            @RequestParam Integer cantidad) {
        try {
            DetalleOrdenDto detalleActualizado = detalleOrdenService.actualizarCantidad(id, cantidad);
            return ResponseEntity.ok(new MensajeDto<>(false, "Cantidad actualizada exitosamente", detalleActualizado));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MensajeDto<>(true, e.getMessage(), null));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('VENDEDOR')")
    public ResponseEntity<MensajeDto<DetalleOrdenDto>> actualizarDetalleOrden(@PathVariable Long id,
                                                                              @Valid @RequestBody DetalleOrdenDto dto) {
        try {
            DetalleOrdenDto detalleActualizado = detalleOrdenService.actualizarDetalleOrden(id, dto);
            return ResponseEntity.ok(new MensajeDto<>(false, "Detalle de orden actualizado exitosamente", detalleActualizado));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MensajeDto<>(true, e.getMessage(), null));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('VENDEDOR')")
    public ResponseEntity<MensajeDto<String>> eliminarDetalleOrden(@PathVariable Long id) {
        try {
            detalleOrdenService.eliminarDetalleOrden(id);
            return ResponseEntity.ok(new MensajeDto<>(false, "Detalle de orden eliminado exitosamente"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MensajeDto<>(true, e.getMessage()));
        }
    }

    @DeleteMapping("/producto-orden")
    @PreAuthorize("hasRole('ADMIN') or hasRole('VENDEDOR')")
    public ResponseEntity<MensajeDto<String>> eliminarDetallePorProductoYOrden(
            @Valid @RequestBody EliminarDetalleRequest request) {
        try {
            detalleOrdenService.eliminarDetallePorProductoYOrden(request);
            return ResponseEntity.ok(new MensajeDto<>(false, "Detalle de orden eliminado exitosamente"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MensajeDto<>(true, e.getMessage()));
        }
    }

    @GetMapping("/validar-stock/{productoId}/{cantidad}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MensajeDto<String>> validarStockDisponible(@PathVariable Long productoId,
                                                                     @PathVariable Integer cantidad) {
        try {
            detalleOrdenService.validarStockDisponible(productoId, cantidad);
            return ResponseEntity.ok(new MensajeDto<>(false, "Stock disponible para la cantidad solicitada"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MensajeDto<>(true, e.getMessage()));
        }
    }
}