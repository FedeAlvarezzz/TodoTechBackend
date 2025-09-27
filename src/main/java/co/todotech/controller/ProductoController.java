package co.todotech.controller;

import co.todotech.model.dto.MensajeDto;
import co.todotech.model.dto.producto.ProductoDto;
import co.todotech.model.enums.EstadoProducto;
import co.todotech.service.ProductoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/productos")
public class ProductoController {

    private final ProductoService productoService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MensajeDto<String>> crearProducto(@Valid @RequestBody ProductoDto dto) {
        try {
            productoService.crearProducto(dto);
            return ResponseEntity.ok(new MensajeDto<>(false, "Producto creado exitosamente"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MensajeDto<>(true, e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MensajeDto<String>> actualizarProducto(@PathVariable Long id,
                                                                 @Valid @RequestBody ProductoDto dto) {
        try {
            // Recomendado: service.actualizarProducto(id, dto)
            productoService.actualizarProducto(id, dto);
            return ResponseEntity.ok(new MensajeDto<>(false, "Producto actualizado exitosamente"));
        } catch (UnsupportedOperationException uoe) {
            // Si tu service aún NO tiene la firma con DTO
            return ResponseEntity.badRequest().body(new MensajeDto<>(true,
                    "Actualización no disponible: " + uoe.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MensajeDto<>(true, e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MensajeDto<String>> eliminarProducto(@PathVariable Long id) {
        try {
            productoService.eliminarProducto(id);
            return ResponseEntity.ok(new MensajeDto<>(false, "Producto eliminado exitosamente"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MensajeDto<>(true, e.getMessage()));
        }
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MensajeDto<String>> cambiarEstadoProducto(@PathVariable Long id) {
        try {
            productoService.cambiarEstadoProducto(id);
            return ResponseEntity.ok(new MensajeDto<>(false, "Estado del producto actualizado"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MensajeDto<>(true, e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MensajeDto<ProductoDto>> obtenerProductoPorId(@PathVariable Long id) {
        try {
            ProductoDto dto = productoService.obtenerProductoPorId(id);
            return ResponseEntity.ok(new MensajeDto<>(false, "Producto encontrado", dto));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MensajeDto<>(true, e.getMessage(), null));
        }
    }

    @GetMapping("/codigo/{codigo}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MensajeDto<ProductoDto>> obtenerProductoPorCodigo(@PathVariable String codigo) {
        try {
            ProductoDto dto = productoService.obtenerProductoPorCodigo(codigo);
            return ResponseEntity.ok(new MensajeDto<>(false, "Producto encontrado por código", dto));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MensajeDto<>(true, e.getMessage(), null));
        }
    }

    @GetMapping("/nombre/{nombre}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MensajeDto<ProductoDto>> obtenerProductoPorNombre(@PathVariable String nombre) {
        try {
            ProductoDto dto = productoService.obtenerProductoPorNombre(nombre);
            return ResponseEntity.ok(new MensajeDto<>(false, "Producto encontrado por nombre", dto));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MensajeDto<>(true, e.getMessage(), null));
        }
    }

    @GetMapping("/estado/{estado}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MensajeDto<List<ProductoDto>>> obtenerProductoPorEstado(@PathVariable String estado) {
        try {
            EstadoProducto ep = EstadoProducto.valueOf(estado.toUpperCase());
            List<ProductoDto> lista = productoService.obtenerProductoPorEstado(ep);
            return ResponseEntity.ok(new MensajeDto<>(false, "Productos por estado obtenidos", lista));
        } catch (IllegalArgumentException iae) {
            return ResponseEntity.badRequest().body(new MensajeDto<>(true,
                    "Estado inválido. Usa: ACTIVO, INACTIVO, DESCONTINUADO, AGOTADO", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MensajeDto<>(true, e.getMessage(), null));
        }
    }

    @GetMapping("/categoria/{categoriaId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MensajeDto<List<ProductoDto>>> obtenerProductoPorCategoria(@PathVariable Long categoriaId) {
        try {
            List<ProductoDto> lista = productoService.obtenerProductoPorCategoriaId(categoriaId);
            return ResponseEntity.ok(new MensajeDto<>(false, "Productos por categoría obtenidos", lista));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MensajeDto<>(true, e.getMessage(), null));
        }
    }
}