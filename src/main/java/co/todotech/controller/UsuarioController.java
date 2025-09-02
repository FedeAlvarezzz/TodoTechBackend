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

    @PostMapping("/crear")
    public ResponseEntity<MensajeDto<String>> crearUsuario(@RequestBody UsuarioDto dto) throws Exception {
        usuarioService.crearUsuario(dto);
        return ResponseEntity.ok(new MensajeDto<>(false,"Usuario creado exitosamente"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MensajeDto<String>> actualizarUsuario(@PathVariable Long id, @RequestBody UsuarioDto dto) throws Exception {
        usuarioService.actualizarUsuario(id, dto);
        return ResponseEntity.ok(new MensajeDto<>(false,"Usuario actualizado exitosamente"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MensajeDto<String>> obtenerUsuarioPorId(@PathVariable Long id) throws Exception {
        UsuarioDto usuarioDto = usuarioService.obtenerUsuarioPorId(id);
        return ResponseEntity.ok(new MensajeDto<>(false, "Usuario encontrado"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<MensajeDto<String>> eliminarUsuario(@PathVariable Long id) throws Exception {
        usuarioService.eliminarUsuario(id);
        return ResponseEntity.ok(new MensajeDto<>(false,"Usuario eliminado exitosamente"));
    }
}
