package co.todotech.service.impl;

import co.todotech.mapper.UsuarioMapper;
import co.todotech.model.dto.usuario.LoginResponse;
import co.todotech.model.dto.usuario.UsuarioDto;
import co.todotech.model.entities.Usuario;
import co.todotech.model.enums.TipoUsuario;
import co.todotech.repository.UsuarioRepository;
import co.todotech.service.UsuarioService;

import co.todotech.utils.impl.EmailServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioMapper usuarioMapper;
    private final UsuarioRepository usuarioRepository;
    private final EmailServiceImpl emailService;

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

    @Override
    public List<UsuarioDto> obtenerTodosLosUsuarios() {
        return usuarioRepository.findAll().stream()
                .map(usuarioMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<UsuarioDto> obtenerUsuariosActivos() {
        return usuarioRepository.findByEstado(true).stream()
                .map(usuarioMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<UsuarioDto> obtenerUsuariosInactivos() {
        return usuarioRepository.findByEstado(false).stream()
                .map(usuarioMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public LoginResponse login(String nombreUsuario, String contrasena) throws Exception {
        Usuario usuario = usuarioRepository.findByNombreUsuario(nombreUsuario)
                .orElseThrow(() -> new Exception("Usuario no encontrado"));

        if (!usuario.getContrasena().equals(contrasena)) {
            throw new Exception("Contraseña incorrecta");
        }

        if (!usuario.isEstado()) {
            throw new Exception("Usuario inactivo. Contacte al administrador");
        }

        // Notificar por email si es administrador
        if (usuario.getTipoUsuario().name().equals("ADMIN")) {
            notificarIngresoAdmin(usuario);
        }

        // Crear respuesta con información del usuario
        return new LoginResponse(
                "Login exitoso",
                usuario.getTipoUsuario(),
                usuario.getNombre(),
                usuario.getNombreUsuario()
        );
    }

    private void notificarIngresoAdmin(Usuario admin) {
        try {
            String fechaHora = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            emailService.enviarNotificacionAdminLogin(
                    admin.getCorreo(),
                    admin.getNombre(),
                    fechaHora
            );

            log.info("Notificación de ingreso enviada al admin: {}", admin.getNombreUsuario());
        } catch (Exception e) {
            log.error("Error al enviar notificación de ingreso al admin {}: {}",
                    admin.getNombreUsuario(), e.getMessage());
            // No lanzamos excepción para no afectar el flujo de login
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

        // Verificar si ya existe usuario con cédula, correo o nombre de usuario
        if (usuarioRepository.existsByCedula(dto.getCedula())) {
            throw new Exception("Ya existe un usuario con la cédula: " + dto.getCedula());
        }

        if (usuarioRepository.existsByCorreo(dto.getCorreo())) {
            throw new Exception("Ya existe un usuario con el correo: " + dto.getCorreo());
        }

        if (usuarioRepository.existsByNombreUsuario(dto.getNombreUsuario())) {
            throw new Exception("Ya existe un usuario con el nombre de usuario: " + dto.getNombreUsuario());
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

        if (!usuario.getNombreUsuario().equals(dto.getNombreUsuario()) &&
                usuarioRepository.existsByNombreUsuarioAndIdNot(dto.getNombreUsuario(), id)) {
            throw new Exception("Ya existe otro usuario con el nombre de usuario: " + dto.getNombreUsuario());
        }

        usuarioMapper.updateUsuarioFromDto(dto, usuario);

        // Actualizar el estado manualmente si lo estás ignorando en el mapper
        usuario.setEstado(dto.getEstado()); // ← Añade esta línea

        usuarioRepository.save(usuario);
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
                .map(usuarioMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<UsuarioDto> obtenerUsuariosPorFechaCreacion(LocalDateTime fechaInicio, LocalDateTime fechaFin) throws Exception {
        List<Usuario> usuarios = usuarioRepository.findByFechaCreacionBetween(fechaInicio, fechaFin);
        if (usuarios.isEmpty()) {
            throw new Exception("No se encontraron usuarios en el rango de fechas especificado");
        }
        return usuarios.stream()
                .map(usuarioMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<UsuarioDto> obtenerUsuariosCreadosDespuesDe(LocalDateTime fecha) throws Exception {
        List<Usuario> usuarios = usuarioRepository.findByFechaCreacionAfter(fecha);
        if (usuarios.isEmpty()) {
            throw new Exception("No se encontraron usuarios creados después de: " + fecha);
        }
        return usuarios.stream()
                .map(usuarioMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<UsuarioDto> obtenerUsuariosCreadosAntesDe(LocalDateTime fecha) throws Exception {
        List<Usuario> usuarios = usuarioRepository.findByFechaCreacionBefore(fecha);
        if (usuarios.isEmpty()) {
            throw new Exception("No se encontraron usuarios creados antes de: " + fecha);
        }
        return usuarios.stream()
                .map(usuarioMapper::toDto)
                .collect(Collectors.toList());
    }


    @Override
    @Transactional
    public void solicitarRecordatorioContrasena(String correo) throws Exception {
        log.info("📧 Solicitando recordatorio de contraseña para el correo: {}", correo);

        // Definir los tipos de usuario permitidos (VENDEDOR, CAJERO, DESPACHADOR)
        List<TipoUsuario> tiposPermitidos = List.of(
                TipoUsuario.VENDEDOR,
                TipoUsuario.CAJERO,
                TipoUsuario.DESPACHADOR
        );

        // Buscar usuario por correo
        Optional<Usuario> usuarioOpt = usuarioRepository.findByCorreo(correo);

        if (usuarioOpt.isEmpty()) {
            throw new Exception("❌ No estás registrado en el sistema");
        }

        Usuario usuario = usuarioOpt.get();

        // Verificar que sea de los tipos permitidos
        if (!tiposPermitidos.contains(usuario.getTipoUsuario())) {
            throw new Exception("❌ No tienes permisos para esta operación. Solo disponible para vendedores, cajeros y despachadores");
        }

        if (!usuario.isEstado()) {
            throw new Exception("❌ Tu cuenta está inactiva. Contacta al administrador");
        }

        // Generar código de verificación
        String codigoVerificacion = generarCodigoVerificacion();

        // Enviar email con el recordatorio de contraseña
        try {
            emailService.enviarRecordatorioContrasena(
                    usuario.getCorreo(),
                    usuario.getNombre(),
                    usuario.getContrasena(),
                    codigoVerificacion
            );

            log.info("✅ Recordatorio de contraseña enviado exitosamente a: {}", correo);

        } catch (Exception e) {
            log.error("❌ Error al enviar recordatorio de contraseña: {}", e.getMessage());
            throw new Exception("Error al enviar el recordatorio. Intenta nuevamente.");
        }
    }

    private String generarCodigoVerificacion() {
        // Generar un código de 6 dígitos
        return String.format("%06d", (int) (Math.random() * 1000000));
    }
}