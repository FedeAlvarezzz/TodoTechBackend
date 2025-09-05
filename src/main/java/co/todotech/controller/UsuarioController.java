package co.todotech.controller;

import co.todotech.model.dto.MensajeDto;
import co.todotech.model.dto.usuario.UsuarioDto;
import co.todotech.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    @PostMapping
    public ResponseEntity<MensajeDto<String>> crearUsuario(@RequestBody UsuarioDto dto) {
        try {
            usuarioService.crearUsuario(dto);
            return ResponseEntity.ok(new MensajeDto<>(false, "Usuario creado exitosamente"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MensajeDto<>(true, e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<MensajeDto<String>> actualizarUsuario(
            @PathVariable("id") Long id,  // ← Agrega el nombre explícito aquí
            @RequestBody UsuarioDto dto) {
        try {
            usuarioService.actualizarUsuario(id, dto);
            return ResponseEntity.ok(new MensajeDto<>(false, "Usuario actualizado exitosamente"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MensajeDto<>(true, e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<MensajeDto<String>> eliminarUsuario(
            @PathVariable("id") Long id) {  // ← Y aquí también
        try {
            usuarioService.eliminarUsuario(id);
            return ResponseEntity.ok(new MensajeDto<>(false, "Usuario eliminado exitosamente"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MensajeDto<>(true, e.getMessage()));
        }
    }
}