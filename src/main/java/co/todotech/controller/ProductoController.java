package co.todotech.controller;

import co.todotech.model.dto.MensajeDto;
import co.todotech.model.dto.producto.AjusteStockRequest;
import co.todotech.model.dto.producto.CantidadRequest;
import co.todotech.model.dto.producto.ProductoDto;
import co.todotech.model.dto.producto.StockResponse;
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
        productoService.crearProducto(dto);
        return ResponseEntity.ok(new MensajeDto<>(false, "Producto creado exitosamente"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MensajeDto<String>> actualizarProducto(@PathVariable("id") Long id,
                                                                 @Valid @RequestBody ProductoDto dto) {
        productoService.actualizarProducto(id, dto);
        return ResponseEntity.ok(new MensajeDto<>(false, "Producto actualizado exitosamente"));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MensajeDto<List<ProductoDto>>> obtenerTodosLosProductos() {
        List<ProductoDto> lista = productoService.obtenerTodosLosProductos();
        return ResponseEntity.ok(new MensajeDto<>(false, "Todos los productos obtenidos", lista));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MensajeDto<String>> eliminarProducto(@PathVariable("id") Long id) {
        productoService.eliminarProducto(id);
        return ResponseEntity.ok(new MensajeDto<>(false, "Producto eliminado exitosamente"));
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MensajeDto<String>> cambiarEstadoProducto(@PathVariable("id") Long id) {
        productoService.cambiarEstadoProducto(id);
        return ResponseEntity.ok(new MensajeDto<>(false, "Estado del producto actualizado"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MensajeDto<ProductoDto>> obtenerProductoPorId(@PathVariable("id") Long id) {
        ProductoDto dto = productoService.obtenerProductoPorId(id);
        return ResponseEntity.ok(new MensajeDto<>(false, "Producto encontrado", dto));
    }

    @GetMapping("/codigo/{codigo}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MensajeDto<ProductoDto>> obtenerProductoPorCodigo(@PathVariable("codigo") String codigo) {
        ProductoDto dto = productoService.obtenerProductoPorCodigo(codigo);
        return ResponseEntity.ok(new MensajeDto<>(false, "Producto encontrado por código", dto));
    }

    @GetMapping("/nombre/{nombre}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MensajeDto<ProductoDto>> obtenerProductoPorNombre(@PathVariable("nombre") String nombre) {
        ProductoDto dto = productoService.obtenerProductoPorNombre(nombre);
        return ResponseEntity.ok(new MensajeDto<>(false, "Producto encontrado por nombre", dto));
    }

    @GetMapping("/estado/{estado}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MensajeDto<List<ProductoDto>>> obtenerProductoPorEstado(@PathVariable("estado") String estado) {
        try {
            EstadoProducto ep = EstadoProducto.valueOf(estado.toUpperCase());
            List<ProductoDto> lista = productoService.obtenerProductoPorEstado(ep);
            return ResponseEntity.ok(new MensajeDto<>(false, "Productos por estado obtenidos", lista));
        } catch (IllegalArgumentException iae) {
            return ResponseEntity.badRequest().body(new MensajeDto<>(true,
                    "Estado inválido. Usa: ACTIVO, INACTIVO, DESCONTINUADO, AGOTADO", null));
        }
    }

    @GetMapping("/categoria/{categoriaId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MensajeDto<List<ProductoDto>>> obtenerProductoPorCategoria(@PathVariable("categoriaId") Long categoriaId) {
        List<ProductoDto> lista = productoService.obtenerProductoPorCategoriaId(categoriaId);
        return ResponseEntity.ok(new MensajeDto<>(false, "Productos por categoría obtenidos", lista));
    }

    @GetMapping("/activos")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MensajeDto<List<ProductoDto>>> obtenerProductosActivos() {
        List<ProductoDto> lista = productoService.obtenerProductosActivos();
        return ResponseEntity.ok(new MensajeDto<>(false, "Productos activos obtenidos", lista));
    }

    @GetMapping("/disponibles")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MensajeDto<List<ProductoDto>>> obtenerProductosDisponibles() {
        List<ProductoDto> lista = productoService.obtenerProductosDisponibles();
        return ResponseEntity.ok(new MensajeDto<>(false, "Productos disponibles obtenidos", lista));
    }

    @GetMapping("/buscar")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MensajeDto<List<ProductoDto>>> buscarProductosPorNombre(@RequestParam("nombre") String nombre) {
        List<ProductoDto> lista = productoService.buscarProductosPorNombre(nombre);
        return ResponseEntity.ok(new MensajeDto<>(false, "Búsqueda completada", lista));
    }
    @PatchMapping("/{id}/stock/incrementar")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
    public ResponseEntity<MensajeDto<String>> incrementarStock(
            @PathVariable("id") Long id,
            @Valid @RequestBody CantidadRequest request) {
        productoService.incrementarStock(id, request.getCantidad());
        return ResponseEntity.ok(new MensajeDto<>(false, "Stock incrementado exitosamente"));
    }

    @PatchMapping("/{id}/stock/decrementar")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
    public ResponseEntity<MensajeDto<String>> decrementarStock(
            @PathVariable("id") Long id,
            @Valid @RequestBody CantidadRequest request) {
        productoService.decrementarStock(id, request.getCantidad());
        return ResponseEntity.ok(new MensajeDto<>(false, "Stock decrementado exitosamente"));
    }

    @PatchMapping("/{id}/stock")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
    public ResponseEntity<MensajeDto<String>> establecerStock(
            @PathVariable("id") Long id,
            @Valid @RequestBody CantidadRequest request) {
        productoService.establecerStock(id, request.getCantidad());
        return ResponseEntity.ok(new MensajeDto<>(false, "Stock establecido exitosamente"));
    }

    @PatchMapping("/{id}/stock/ajustar")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
    public ResponseEntity<MensajeDto<String>> ajustarStock(
            @PathVariable("id") Long id,
            @Valid @RequestBody AjusteStockRequest request) {
        productoService.ajustarStockProducto(id, request.getCantidad(), request.getOperacion());
        return ResponseEntity.ok(new MensajeDto<>(false, "Stock ajustado exitosamente"));
    }

    @GetMapping("/{id}/stock")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MensajeDto<StockResponse>> consultarStock(@PathVariable("id") Long id) {
        Integer stock = productoService.consultarStock(id);
        ProductoDto producto = productoService.obtenerProductoPorId(id);
        StockResponse response = new StockResponse(id, stock, producto.getNombre(), producto.getEstado().toString());
        return ResponseEntity.ok(new MensajeDto<>(false, "Stock consultado exitosamente", response));
    }

    // ===== ENDPOINTS PÚBLICOS PARA CATÁLOGO =====

    @GetMapping("/publicos/todos")
    public ResponseEntity<MensajeDto<List<ProductoDto>>> obtenerTodosLosProductosPublicos() {
        List<ProductoDto> lista = productoService.obtenerTodosLosProductos();
        return ResponseEntity.ok(new MensajeDto<>(false, "Todos los productos obtenidos", lista));
    }

    @GetMapping("/publicos/activos")
    public ResponseEntity<MensajeDto<List<ProductoDto>>> obtenerProductosActivosPublicos() {
        List<ProductoDto> lista = productoService.obtenerProductosActivos();
        return ResponseEntity.ok(new MensajeDto<>(false, "Productos activos obtenidos", lista));
    }

    @GetMapping("/publicos/disponibles")
    public ResponseEntity<MensajeDto<List<ProductoDto>>> obtenerProductosDisponiblesPublicos() {
        List<ProductoDto> lista = productoService.obtenerProductosDisponibles();
        return ResponseEntity.ok(new MensajeDto<>(false, "Productos disponibles obtenidos", lista));
    }

    @GetMapping("/publicos/categoria/{categoriaId}")
    public ResponseEntity<MensajeDto<List<ProductoDto>>> obtenerProductoPorCategoriaPublico(@PathVariable("categoriaId") Long categoriaId) {
        List<ProductoDto> lista = productoService.obtenerProductoPorCategoriaId(categoriaId);
        return ResponseEntity.ok(new MensajeDto<>(false, "Productos por categoría obtenidos", lista));
    }

    @GetMapping("/publicos/buscar")
    public ResponseEntity<MensajeDto<List<ProductoDto>>> buscarProductosPorNombrePublico(@RequestParam("nombre") String nombre) {
        List<ProductoDto> lista = productoService.buscarProductosPorNombre(nombre);
        return ResponseEntity.ok(new MensajeDto<>(false, "Búsqueda completada", lista));
    }

    @GetMapping("/publicos/{id}")
    public ResponseEntity<MensajeDto<ProductoDto>> obtenerProductoPorIdPublico(@PathVariable("id") Long id) {
        ProductoDto dto = productoService.obtenerProductoPorId(id);
        return ResponseEntity.ok(new MensajeDto<>(false, "Producto encontrado", dto));
    }
}