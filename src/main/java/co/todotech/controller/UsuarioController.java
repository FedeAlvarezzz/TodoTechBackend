package co.todotech.controller;

import co.todotech.model.dto.MensajeDto;
import co.todotech.model.dto.usuario.LoginResponse;
import co.todotech.model.dto.usuario.UsuarioDto;
import co.todotech.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    // Login
    @PostMapping("/login")
    public ResponseEntity<MensajeDto<LoginResponse>> login(
            @RequestParam("nombreUsuario") String nombreUsuario,
            @RequestParam("contrasena") String contrasena) {
        try {
            LoginResponse loginResponse = usuarioService.login(nombreUsuario, contrasena);
            return ResponseEntity.ok(new MensajeDto<>(false, "Login exitoso", loginResponse));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MensajeDto<>(true, e.getMessage(), null));
        }
    }

    // Obtener todos los usuarios
    @GetMapping
    public ResponseEntity<MensajeDto<List<UsuarioDto>>> obtenerTodosLosUsuarios() {
        try {
            List<UsuarioDto> usuarios = usuarioService.obtenerTodosLosUsuarios();
            return ResponseEntity.ok(new MensajeDto<>(false, "Usuarios obtenidos exitosamente", usuarios));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MensajeDto<>(true, e.getMessage(), null));
        }
    }

    // Obtener usuarios activos
    @GetMapping("/activos")
    public ResponseEntity<MensajeDto<List<UsuarioDto>>> obtenerUsuariosActivos() {
        try {
            List<UsuarioDto> usuarios = usuarioService.obtenerUsuariosActivos();
            return ResponseEntity.ok(new MensajeDto<>(false, "Usuarios activos obtenidos exitosamente", usuarios));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MensajeDto<>(true, e.getMessage(), null));
        }
    }

    // Obtener usuarios inactivos
    @GetMapping("/inactivos")
    public ResponseEntity<MensajeDto<List<UsuarioDto>>> obtenerUsuariosInactivos() {
        try {
            List<UsuarioDto> usuarios = usuarioService.obtenerUsuariosInactivos();
            return ResponseEntity.ok(new MensajeDto<>(false, "Usuarios inactivos obtenidos exitosamente", usuarios));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MensajeDto<>(true, e.getMessage(), null));
        }
    }

    // Cambiar estado de usuario (activar/desactivar)
    @PatchMapping("/{id}/estado")
    public ResponseEntity<MensajeDto<String>> cambiarEstadoUsuario(
            @PathVariable("id") Long id,
            @RequestParam boolean estado) {
        try {
            usuarioService.cambiarEstadoUsuario(id, estado);
            String mensaje = estado ? "Usuario activado exitosamente" : "Usuario desactivado exitosamente";
            return ResponseEntity.ok(new MensajeDto<>(false, mensaje));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MensajeDto<>(true, e.getMessage()));
        }
    }

    // Endpoints existentes (mantener)
    @PostMapping
    public ResponseEntity<MensajeDto<String>> crearUsuario(@RequestBody UsuarioDto dto) {
        try {
            usuarioService.crearUsuario(dto);
            return ResponseEntity.ok(new MensajeDto<>(false, "Usuario creado exitosamente"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MensajeDto<>(true, e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<MensajeDto<UsuarioDto>> obtenerUsuarioPorId(@PathVariable("id") Long id) {
        try {
            UsuarioDto usuarioDto = usuarioService.obtenerUsuarioPorId(id);
            return ResponseEntity.ok(new MensajeDto<>(false, "Usuario encontrado", usuarioDto));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MensajeDto<>(true, e.getMessage(), null));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<MensajeDto<String>> actualizarUsuario(
            @PathVariable("id") Long id,
            @RequestBody UsuarioDto dto) {
        try {
            usuarioService.actualizarUsuario(id, dto);
            return ResponseEntity.ok(new MensajeDto<>(false, "Usuario actualizado exitosamente"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MensajeDto<>(true, e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<MensajeDto<String>> eliminarUsuario(@PathVariable("id") Long id) {
        try {
            usuarioService.eliminarUsuario(id);
            return ResponseEntity.ok(new MensajeDto<>(false, "Usuario eliminado exitosamente"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MensajeDto<>(true, e.getMessage()));
        }
    }
}