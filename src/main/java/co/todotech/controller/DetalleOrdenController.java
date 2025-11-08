package co.todotech.controller;

import co.todotech.model.dto.MensajeDto;
import co.todotech.model.dto.detalleorden.CreateDetalleOrdenDto;
import co.todotech.model.dto.detalleorden.DetalleOrdenDto;
import co.todotech.model.dto.detalleorden.EliminarDetalleRequest;
import co.todotech.model.dto.detalleorden.validacion.BulkStockValidationRequest;
import co.todotech.model.dto.detalleorden.validacion.BulkValidationResultDto;
import co.todotech.model.dto.detalleorden.validacion.StockValidationRequest;
import co.todotech.model.dto.detalleorden.validacion.ValidationResultDto;
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

    // 🔹 CORREGIDO: Agregar name explícito en @PathVariable
    @PostMapping("/orden/{ordenId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('VENDEDOR')")
    public ResponseEntity<MensajeDto<DetalleOrdenDto>> crearDetalleOrden(
            @PathVariable("ordenId") Long ordenId, // ← CORREGIDO AQUÍ
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
    public ResponseEntity<MensajeDto<DetalleOrdenDto>> obtenerDetalleOrden(
            @PathVariable("id") Long id) { // ← CORREGIDO AQUÍ
        try {
            DetalleOrdenDto dto = detalleOrdenService.obtenerDetalleOrden(id);
            return ResponseEntity.ok(new MensajeDto<>(false, "Detalle de orden encontrado", dto));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MensajeDto<>(true, e.getMessage(), null));
        }
    }

    @GetMapping("/orden/{ordenId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MensajeDto<List<DetalleOrdenDto>>> obtenerDetallesPorOrden(
            @PathVariable("ordenId") Long ordenId) { // ← CORREGIDO AQUÍ
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
            @PathVariable("id") Long id,
            @RequestParam("cantidad") Integer cantidad) { // ← CORREGIDO AQUÍ
        try {
            DetalleOrdenDto detalleActualizado = detalleOrdenService.actualizarCantidad(id, cantidad);
            return ResponseEntity.ok(new MensajeDto<>(false, "Cantidad actualizada exitosamente", detalleActualizado));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MensajeDto<>(true, e.getMessage(), null));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('VENDEDOR')")
    public ResponseEntity<MensajeDto<DetalleOrdenDto>> actualizarDetalleOrden(
            @PathVariable("id") Long id, // ← CORREGIDO AQUÍ
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
    public ResponseEntity<MensajeDto<String>> eliminarDetalleOrden(
            @PathVariable("id") Long id) { // ← CORREGIDO AQUÍ
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
    public ResponseEntity<MensajeDto<String>> validarStockDisponible(
            @PathVariable("productoId") Long productoId, // ← CORREGIDO AQUÍ
            @PathVariable("cantidad") Integer cantidad) { // ← CORREGIDO AQUÍ
        try {
            detalleOrdenService.validarStockDisponible(productoId, cantidad);
            return ResponseEntity.ok(new MensajeDto<>(false, "Stock disponible para la cantidad solicitada"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MensajeDto<>(true, e.getMessage()));
        }
    }


    // Agregar estos métodos al DetalleOrdenController

    @PostMapping("/validar-stock")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MensajeDto<ValidationResultDto>> validarStockParaDetalle(
            @Valid @RequestBody StockValidationRequest request) {
        try {
            ValidationResultDto resultado = detalleOrdenService.validarStockParaDetalle(request);
            return ResponseEntity.ok(new MensajeDto<>(false, "Validación de stock completada", resultado));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MensajeDto<>(true, e.getMessage(), null));
        }
    }

    @PostMapping("/validar-stock-multiple")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MensajeDto<BulkValidationResultDto>> validarStockMultiple(
            @Valid @RequestBody BulkStockValidationRequest request) {
        try {
            BulkValidationResultDto resultado = detalleOrdenService.validarStockMultiple(request);
            return ResponseEntity.ok(new MensajeDto<>(false, "Validación múltiple de stock completada", resultado));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MensajeDto<>(true, e.getMessage(), null));
        }
    }

    @GetMapping("/stock-critico")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MensajeDto<List<ValidationResultDto>>> obtenerProductosStockCritico() {
        try {
            List<ValidationResultDto> productosCriticos = detalleOrdenService.obtenerProductosStockCritico();
            return ResponseEntity.ok(new MensajeDto<>(false, "Productos con stock crítico obtenidos", productosCriticos));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MensajeDto<>(true, e.getMessage(), null));
        }
    }

    @GetMapping("/stock-disponible/{productoId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MensajeDto<ValidationResultDto>> obtenerStockDisponible(
            @PathVariable("productoId") Long productoId) {
        try {
            ValidationResultDto resultado = detalleOrdenService.obtenerStockDisponible(productoId);
            return ResponseEntity.ok(new MensajeDto<>(false, "Stock disponible obtenido", resultado));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MensajeDto<>(true, e.getMessage(), null));
        }
    }
}