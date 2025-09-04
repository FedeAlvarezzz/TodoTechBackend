package co.todotech.service.impl;

import co.todotech.mapper.UsuarioMapper;
import co.todotech.model.dto.usuario.UsuarioDto;
import co.todotech.model.entities.Usuario;
import co.todotech.repository.UsuarioRepository;
import co.todotech.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioMapper usuarioMapper;
    private final UsuarioRepository usuarioRepository;

    @Override
    public UsuarioDto obtenerUsuarioPorId(Long id) throws Exception {
        Usuario usuario = usuarioRepository.findById(id).orElse(null);
        if (usuario == null) {
            throw new Exception("Usuario no encontrado");
        }
        return usuarioMapper.toDto(usuario);
    }

    @Override
    public UsuarioDto obtenerUsuarioPorCedula(String cedula) throws Exception {
        Usuario usuario = usuarioRepository.findAll().stream()
                .filter(u -> u.getCedula().equals(cedula))
                .findFirst()
                .orElse(null);
        if (usuario == null) {
            throw new Exception("Usuario no encontrado");
        }
        return usuarioMapper.toDto(usuario);
    }

    @Override
    public void crearUsuario(UsuarioDto dto) throws Exception {
        Usuario usuario = usuarioMapper.toEntity(dto);
        if (usuario == null) {
            throw new Exception("Error al crear usuario");
        }
        usuario.setEstado(true);
        usuarioRepository.save(usuario);
    }

    @Override
    public void actualizarUsuario(Long id, UsuarioDto dto) throws Exception {
        Usuario usuario = usuarioRepository.findById(id).orElse(null);
        if (usuario == null) {
            throw new Exception("Usuario no encontrado");
        }
        usuario.setNombre(dto.nombre());
        usuario.setCedula(dto.cedula());
        usuario.setCorreo(dto.correo());
        usuario.setTelefono(dto.telefono());
        usuario.setNombreUsuario(dto.nombreUsuario());  // Actualizado el nombre del método
        usuario.setContrasena(dto.contrasena());
        usuarioRepository.save(usuario);
    }

    @Override
    public void eliminarUsuario(Long id) throws Exception {
        Usuario usuario = usuarioRepository.findById(id).orElse(null);
        if (usuario == null) {
            throw new Exception("Usuario no encontrado");
        }
        usuario.setEstado(false);
        usuarioRepository.save(usuario);
    }
}