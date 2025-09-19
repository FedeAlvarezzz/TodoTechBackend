package co.todotech.service.impl;

import co.todotech.mapper.UsuarioMapper;
import co.todotech.model.dto.usuario.LoginResponse;
import co.todotech.model.dto.usuario.UsuarioDto;
import co.todotech.model.entities.Usuario;
import co.todotech.model.enums.TipoUsuario;
import co.todotech.repository.UsuarioRepository;
import co.todotech.security.JwtUtil;
import co.todotech.service.UsuarioService;
import co.todotech.utils.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioMapper usuarioMapper;
    private final UsuarioRepository usuarioRepository;
    private final EmailService emailService;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    @Override
    public LoginResponse login(String nombreUsuario, String contrasena) throws Exception {
        log.info("=== INICIO LOGIN ===");
        log.info("Usuario intentando login: {}", nombreUsuario);

        Usuario usuario = usuarioRepository.findByNombreUsuario(nombreUsuario)
                .orElseThrow(() -> new Exception("Usuario no encontrado"));

        log.info("Usuario encontrado: {} - Email: {}", usuario.getNombreUsuario(), usuario.getCorreo());

        if (!passwordEncoder.matches(contrasena, usuario.getContrasena())) {
            throw new Exception("Contraseña incorrecta");
        }

        if (!usuario.isEstado()) {
            throw new Exception("Usuario inactivo. Contacte al administrador");
        }

        String token = jwtUtil.generateToken(
                usuario.getNombreUsuario(),
                usuario.getId(),
                usuario.getTipoUsuario().name()
        );

        if (usuario.getTipoUsuario().name().equals("ADMIN")) {
            log.info("Usuario es ADMIN - enviando notificación SOLO a: {}", usuario.getCorreo());
            notificarIngresoAdmin(usuario);
        }

        log.info("=== FIN LOGIN EXITOSO ===");

        return new LoginResponse(
                token,
                "Bearer",
                usuario.getId(),
                usuario.getNombreUsuario(),
                usuario.getNombre(),
                usuario.getTipoUsuario(),
                "Login exitoso"
        );
    }

    @Override
    @Transactional(readOnly = true)
    public void solicitarRecordatorioContrasena(String correo) throws Exception {
        log.info("=== INICIO RECORDATORIO CONTRASEÑA ===");
        log.info("Correo solicitante: {}", correo);

        if (correo == null || correo.trim().isEmpty()) {
            throw new Exception("El correo electrónico es requerido");
        }

        List<TipoUsuario> tiposPermitidos = Arrays.asList(
                TipoUsuario.VENDEDOR,
                TipoUsuario.CAJERO,
                TipoUsuario.DESPACHADOR
        );

        Usuario usuario = usuarioRepository.findByCorreoAndTipoUsuarioIn(correo, tiposPermitidos)
                .orElseThrow(() -> new Exception("No se encontró un usuario activo con ese correo electrónico o no tiene permisos para solicitar recordatorio"));

        log.info("Usuario encontrado para recordatorio: {} - Email: {}", usuario.getNombreUsuario(), usuario.getCorreo());

        if (!usuario.isEstado()) {
            throw new Exception("El usuario está inactivo. Contacte al administrador");
        }

        try {
            log.info("Enviando recordatorio ÚNICAMENTE a: {}", usuario.getCorreo());

            emailService.sendPasswordReminder(
                    usuario.getCorreo(),
                    usuario.getNombre(),
                    usuario.getNombreUsuario(),
                    "Por razones de seguridad, contacte al administrador para restablecer su contraseña"
            );

            log.info("Recordatorio enviado exitosamente SOLO a: {}", usuario.getCorreo());
            log.info("=== FIN RECORDATORIO CONTRASEÑA ===");

        } catch (Exception e) {
            log.error("Error al enviar recordatorio de contraseña a {}: {}", correo, e.getMessage());
            throw new Exception("Error al enviar el recordatorio por correo: " + e.getMessage());
        }
    }

    @Override
    public UsuarioDto obtenerUsuarioPorId(Long id) throws Exception {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new Exception("Usuario no encontrado con ID: " + id));
        return usuarioMapper.toDto(usuario);
    }

    @Override
    public UsuarioDto obtenerUsuarioPorCedula(String cedula) throws Exception {
        Usuario usuario = usuarioRepository.findByCedula(cedula)
                .orElseThrow(() -> new Exception("Usuario no encontrado con cédula: " + cedula));
        return usuarioMapper.toDto(usuario);
    }


    private void notificarIngresoAdmin(Usuario admin) {
        try {
            String fechaHora = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            emailService.sendAdminLoginNotification(
                    admin.getCorreo(),
                    admin.getNombre(),
                    fechaHora
            );

            log.info("Notificación de ingreso enviada al admin: {}", admin.getNombreUsuario());
        } catch (Exception e) {
            log.error("Error al enviar notificación de ingreso al admin {}: {}",
                    admin.getNombreUsuario(), e.getMessage());
        }
    }

    @Override
    @Transactional
    public void cambiarEstadoUsuario(Long id, boolean estado) throws Exception {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new Exception("Usuario no encontrado con ID: " + id));

        usuario.setEstado(estado);
        usuarioRepository.save(usuario);

        log.info("Estado del usuario {} cambiado a: {}", id, estado ? "ACTIVO" : "INACTIVO");
    }

    @Override
    @Transactional
    public void crearUsuario(UsuarioDto dto) throws Exception {
        log.info("Creando usuario: {}", dto.getNombreUsuario());

        if (usuarioRepository.existsByCedula(dto.getCedula())) {
            throw new Exception("Ya existe un usuario con la cédula: " + dto.getCedula());
        }

        if (usuarioRepository.existsByCorreo(dto.getCorreo())) {
            throw new Exception("Ya existe un usuario con el correo: " + dto.getCorreo());
        }

        if (usuarioRepository.existsByNombreUsuario(dto.getNombreUsuario())) {
            throw new Exception("Ya existe un usuario con el nombre de usuario: " + dto.getNombreUsuario());
        }

        // Validar que la contraseña no sea nula o vacía al crear usuario
        if (dto.getContrasena() == null || dto.getContrasena().trim().isEmpty()) {
            throw new Exception("La contraseña es requerida para crear un usuario");
        }

        Usuario usuario = usuarioMapper.toEntity(dto);
        usuario.setEstado(true);

        // ENCRIPTAR LA CONTRASEÑA ANTES DE GUARDAR
        String contrasenaEncriptada = passwordEncoder.encode(dto.getContrasena());
        usuario.setContrasena(contrasenaEncriptada);

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

        if (!usuario.getNombreUsuario().equals(dto.getNombreUsuario()) &&
                usuarioRepository.existsByNombreUsuarioAndIdNot(dto.getNombreUsuario(), id)) {
            throw new Exception("Ya existe otro usuario con el nombre de usuario: " + dto.getNombreUsuario());
        }

        // Actualizar campos EXCEPTO la contraseña
        usuarioMapper.updateUsuarioFromDto(dto, usuario);
        usuario.setEstado(dto.getEstado());

        // Manejar cambio de contraseña SOLO si se solicita explícitamente
        if (Boolean.TRUE.equals(dto.getCambiarContrasena())) {
            if (dto.getContrasena() != null && !dto.getContrasena().trim().isEmpty()) {
                String contrasenaEncriptada = passwordEncoder.encode(dto.getContrasena());
                usuario.setContrasena(contrasenaEncriptada);
                log.info("Contraseña actualizada para usuario ID: {}", id);
            } else {
                throw new Exception("Se solicitó cambiar contraseña pero no se proporcionó una nueva contraseña");
            }
        }

        usuarioRepository.save(usuario);
        log.info("Usuario actualizado exitosamente: {}", usuario.getNombreUsuario());
    }

    @Override
    @Transactional
    public void eliminarUsuario(Long id) throws Exception {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new Exception("Usuario no encontrado con ID: " + id));

        usuarioRepository.delete(usuario);
        log.info("Usuario eliminado físicamente: {}", id);
    }

    @Override
    public List<UsuarioDto> obtenerUsuariosPorTipo(TipoUsuario tipoUsuario) throws Exception {
        List<Usuario> usuarios = usuarioRepository.findByTipoUsuario(tipoUsuario);
        if (usuarios.isEmpty()) {
            throw new Exception("No se encontraron usuarios del tipo: " + tipoUsuario);
        }
        return usuarios.stream()
                .map(usuarioMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<UsuarioDto> buscarUsuariosPorNombre(String nombre) throws Exception {
        List<Usuario> usuarios = usuarioRepository.findByNombreContainingIgnoreCase(nombre);
        if (usuarios.isEmpty()) {
            throw new Exception("No se encontraron usuarios con el nombre: " + nombre);
        }
        return usuarios.stream()
                .map(usuarioMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<UsuarioDto> buscarUsuariosPorCedula(String cedula) throws Exception {
        List<Usuario> usuarios = usuarioRepository.findByCedulaContaining(cedula);
        if (usuarios.isEmpty()) {
            throw new Exception("No se encontraron usuarios con la cédula: " + cedula);
        }
        return usuarios.stream()
                .map(usuarioMapper::toDtoSafe) // ← Cambiado a método seguro
                .collect(Collectors.toList());
    }

    @Override
    public List<UsuarioDto> obtenerTodosLosUsuarios() {
        log.info("=== INICIANDO obtenerTodosLosUsuarios ===");

        try {
            log.debug("Buscando todos los usuarios en repository...");
            List<Usuario> usuariosEntities = usuarioRepository.findAll();
            log.info("Número de usuarios encontrados en BD: {}", usuariosEntities.size());

            if (usuariosEntities.isEmpty()) {
                log.warn("No se encontraron usuarios en la base de datos");
                return Collections.emptyList();
            }

            log.debug("Iniciando mapeo de entities a DTOs...");
            List<UsuarioDto> usuariosDto = usuariosEntities.stream()
                    .map(usuario -> {
                        log.trace("Mapeando usuario ID: {}, Nombre: {}", usuario.getId(), usuario.getNombre());
                        log.trace("Contraseña en Entity: {}", usuario.getContrasena());

                        UsuarioDto dto = usuarioMapper.toDtoSafe(usuario);

                        log.trace("DTO mapeado - ID: {}, Contraseña en DTO: {}",
                                dto.getId(), dto.getContrasena());
                        log.trace("DTO completo: {}", dto.toString());

                        return dto;
                    })
                    .collect(Collectors.toList());

            log.info("Mapeo completado. Total DTOs generados: {}", usuariosDto.size());

            // Log final de verificación
            usuariosDto.forEach(dto -> {
                log.debug("DTO final - ID: {}, Nombre: {}, Contraseña: {}",
                        dto.getId(), dto.getNombre(), dto.getContrasena());
            });

            log.info("=== FINALIZANDO obtenerTodosLosUsuarios ===");
            return usuariosDto;

        } catch (Exception e) {
            log.error("Error en obtenerTodosLosUsuarios: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public List<UsuarioDto> obtenerUsuariosActivos() {
        return usuarioRepository.findByEstado(true).stream()
                .map(usuarioMapper::toDtoSafe) // ← Cambiado a método seguro
                .collect(Collectors.toList());
    }

    @Override
    public List<UsuarioDto> obtenerUsuariosInactivos() {
        return usuarioRepository.findByEstado(false).stream()
                .map(usuarioMapper::toDtoSafe) // ← Cambiado a método seguro
                .collect(Collectors.toList());
    }

    @Override
    public List<UsuarioDto> obtenerUsuariosPorFechaCreacion(LocalDateTime fechaInicio, LocalDateTime fechaFin) throws Exception {
        List<Usuario> usuarios = usuarioRepository.findByFechaCreacionBetween(fechaInicio, fechaFin);
        if (usuarios.isEmpty()) {
            throw new Exception("No se encontraron usuarios en el rango de fechas especificado");
        }
        return usuarios.stream()
                .map(usuarioMapper::toDtoSafe) // ← Cambiado a método seguro
                .collect(Collectors.toList());
    }

    @Override
    public List<UsuarioDto> obtenerUsuariosCreadosDespuesDe(LocalDateTime fecha) throws Exception {
        List<Usuario> usuarios = usuarioRepository.findByFechaCreacionAfter(fecha);
        if (usuarios.isEmpty()) {
            throw new Exception("No se encontraron usuarios creados después de: " + fecha);
        }
        return usuarios.stream()
                .map(usuarioMapper::toDtoSafe) // ← Cambiado a método seguro
                .collect(Collectors.toList());
    }

    @Override
    public List<UsuarioDto> obtenerUsuariosCreadosAntesDe(LocalDateTime fecha) throws Exception {
        List<Usuario> usuarios = usuarioRepository.findByFechaCreacionBefore(fecha);
        if (usuarios.isEmpty()) {
            throw new Exception("No se encontraron usuarios creados antes de: " + fecha);
        }
        return usuarios.stream()
                .map(usuarioMapper::toDtoSafe) // ← Cambiado a método seguro
                .collect(Collectors.toList());
    }
}