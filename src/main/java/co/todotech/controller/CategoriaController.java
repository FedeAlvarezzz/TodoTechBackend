package co.todotech.controller;

import co.todotech.model.dto.MensajeDto;
import co.todotech.model.dto.categoria.CategoriaDto;
import co.todotech.service.CategoriaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/categorias")
public class CategoriaController {

    private final CategoriaService categoriaService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MensajeDto<CategoriaDto>> crearCategoria(@Valid @RequestBody CategoriaDto dto) {
        try {
            CategoriaDto categoriaCreada = categoriaService.crearCategoria(dto);
            return ResponseEntity.ok(new MensajeDto<>(false, "Categoría creada exitosamente", categoriaCreada));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MensajeDto<>(true, e.getMessage(), null));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MensajeDto<CategoriaDto>> actualizarCategoria(@PathVariable Long id,
                                                                        @Valid @RequestBody CategoriaDto dto) {
        try {
            CategoriaDto categoriaActualizada = categoriaService.actualizarCategoria(id, dto);
            return ResponseEntity.ok(new MensajeDto<>(false, "Categoría actualizada exitosamente", categoriaActualizada));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MensajeDto<>(true, e.getMessage(), null));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MensajeDto<String>> eliminarCategoria(@PathVariable Long id) {
        try {
            categoriaService.eliminarCategoria(id);
            return ResponseEntity.ok(new MensajeDto<>(false, "Categoría eliminada exitosamente"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MensajeDto<>(true, e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MensajeDto<CategoriaDto>> obtenerCategoriaPorId(@PathVariable Long id) {
        try {
            CategoriaDto dto = categoriaService.obtenerCategoriaPorId(id);
            return ResponseEntity.ok(new MensajeDto<>(false, "Categoría encontrada", dto));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MensajeDto<>(true, e.getMessage(), null));
        }
    }

    @GetMapping("/nombre/{nombre}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MensajeDto<CategoriaDto>> obtenerCategoriaPorNombre(@PathVariable String nombre) {
        try {
            CategoriaDto dto = categoriaService.obtenerCategoriaPorNombre(nombre);
            return ResponseEntity.ok(new MensajeDto<>(false, "Categoría encontrada por nombre", dto));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MensajeDto<>(true, e.getMessage(), null));
        }
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MensajeDto<List<CategoriaDto>>> obtenerTodasLasCategorias() {
        try {
            List<CategoriaDto> categorias = categoriaService.obtenerTodasLasCategorias();
            return ResponseEntity.ok(new MensajeDto<>(false, "Categorías obtenidas exitosamente", categorias));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MensajeDto<>(true, e.getMessage(), null));
        }
    }

    @GetMapping("/con-productos")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MensajeDto<List<CategoriaDto>>> obtenerCategoriasConProductos() {
        try {
            List<CategoriaDto> categorias = categoriaService.obtenerCategoriasConProductos();
            return ResponseEntity.ok(new MensajeDto<>(false, "Categorías con productos obtenidas exitosamente", categorias));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MensajeDto<>(true, e.getMessage(), null));
        }
    }
}