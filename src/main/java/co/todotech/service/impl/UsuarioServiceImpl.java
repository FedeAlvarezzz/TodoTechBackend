package co.todotech.service.impl;

import co.todotech.mapper.UsuarioMapper;
import co.todotech.model.dto.usuario.UsuarioDto;
import co.todotech.model.entities.Usuario;
import co.todotech.repository.UsuarioRepository;
import co.todotech.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioMapper usuarioMapper;
    private final UsuarioRepository usuarioRepository;

    @Override
    public UsuarioDto obtenerUsuarioPorId(Long id) throws Exception {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new Exception("Usuario no encontrado con ID: " + id));
        return usuarioMapper.toDto(usuario);
    }

    @Override
    public UsuarioDto obtenerUsuarioPorCedula(String cedula) throws Exception {
        return null;
    }

    @Override
    @Transactional
    public void crearUsuario(UsuarioDto dto) throws Exception {
        log.info("Creando usuario: {}", dto.getNombreUsuario());

        // Verificar si ya existe usuario con cédula o correo
        if (usuarioRepository.existsByCedula(dto.getCedula())) {
            throw new Exception("Ya existe un usuario con la cédula: " + dto.getCedula());
        }

        if (usuarioRepository.existsByCorreo(dto.getCorreo())) {
            throw new Exception("Ya existe un usuario con el correo: " + dto.getCorreo());
        }

        Usuario usuario = usuarioMapper.toEntity(dto);
        usuario.setEstado(true); // Activo por defecto

        usuarioRepository.save(usuario);
        log.info("Usuario creado exitosamente: {}", usuario.getNombreUsuario());
    }

    @Override
    @Transactional
    public void actualizarUsuario(Long id, UsuarioDto dto) throws Exception {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new Exception("Usuario no encontrado con ID: " + id));

        // Verificar si la cédula/correo ya existen en otros usuarios
        if (!usuario.getCedula().equals(dto.getCedula()) &&
                usuarioRepository.existsByCedulaAndIdNot(dto.getCedula(), id)) {
            throw new Exception("Ya existe otro usuario con la cédula: " + dto.getCedula());
        }

        if (!usuario.getCorreo().equals(dto.getCorreo()) &&
                usuarioRepository.existsByCorreoAndIdNot(dto.getCorreo(), id)) {
            throw new Exception("Ya existe otro usuario con el correo: " + dto.getCorreo());
        }

        usuarioMapper.updateUsuarioFromDto(dto, usuario);
        usuarioRepository.save(usuario);
    }

    @Override
    @Transactional
    public void eliminarUsuario(Long id) throws Exception {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new Exception("Usuario no encontrado con ID: " + id));

        usuarioRepository.delete(usuario);  // Eliminación física
        log.info("Usuario eliminado físicamente: {}", id);
    }
}