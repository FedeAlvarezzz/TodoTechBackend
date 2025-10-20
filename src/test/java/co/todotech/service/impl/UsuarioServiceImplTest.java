package co.todotech.service.impl;

import co.todotech.exception.usuario.*;
import co.todotech.mapper.UsuarioMapper;
import co.todotech.model.dto.usuario.LoginResponse;
import co.todotech.model.dto.usuario.UsuarioDto;
import co.todotech.model.entities.Usuario;
import co.todotech.model.enums.TipoUsuario;
import co.todotech.repository.UsuarioRepository;
import co.todotech.security.JwtUtil;
import co.todotech.security.TokenBlacklistService;
import co.todotech.utils.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceImplTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private UsuarioMapper usuarioMapper;

    @Mock
    private EmailService emailService;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private TokenBlacklistService tokenBlacklistService;

    @InjectMocks
    private UsuarioServiceImpl usuarioService;

    private UsuarioDto usuarioDto;
    private Usuario usuario;
    private Usuario usuarioAdmin;

    @BeforeEach
    void setUp() {
        // Configurar datos de prueba comunes
        usuarioDto = new UsuarioDto();
        usuarioDto.setId(1L);
        usuarioDto.setNombre("Juan Pérez");
        usuarioDto.setCedula("123456789");
        usuarioDto.setCorreo("juan@example.com");
        usuarioDto.setTelefono("3001234567");
        usuarioDto.setNombreUsuario("juanperez");
        usuarioDto.setContrasena("password123");
        usuarioDto.setTipoUsuario(TipoUsuario.VENDEDOR);
        usuarioDto.setEstado(true);

        usuario = Usuario.builder()
                .id(1L)
                .nombre("Juan Pérez")
                .cedula("123456789")
                .correo("juan@example.com")
                .telefono("3001234567")
                .nombreUsuario("juanperez")
                .contrasena("encodedPassword")
                .tipoUsuario(TipoUsuario.VENDEDOR)
                .estado(true)
                .fechaCreacion(LocalDateTime.now())
                .build();

        usuarioAdmin = Usuario.builder()
                .id(2L)
                .nombre("Admin User")
                .cedula("987654321")
                .correo("admin@example.com")
                .telefono("3007654321")
                .nombreUsuario("admin")
                .contrasena("encodedAdminPassword")
                .tipoUsuario(TipoUsuario.ADMIN)
                .estado(true)
                .fechaCreacion(LocalDateTime.now())
                .build();
    }

    // Pruebas unitarias para el servicio de usuarios (UsuarioService)
// Se usa JUnit 5 y Mockito para simular el comportamiento de dependencias externas
// como el repositorio, el codificador de contraseñas y el servicio de notificaciones.

    @Test
    @DisplayName("Debería hacer login exitosamente con credenciales válidas")
    void testLoginExitoso() {
        // Arrange (preparación)
        // Se definen credenciales válidas
        String nombreUsuario = "juanperez";
        String contrasena = "password123";

        // Se simula que el repositorio encuentra un usuario con ese nombre
        when(usuarioRepository.findByNombreUsuario(nombreUsuario))
                .thenReturn(Optional.of(usuario));

        // Se simula que la contraseña ingresada coincide con la almacenada (verificación exitosa)
        when(passwordEncoder.matches(contrasena, usuario.getContrasena()))
                .thenReturn(true);

        // Se simula la generación de un token JWT válido
        when(jwtUtil.generateToken(anyString(), anyLong(), anyString()))
                .thenReturn("jwt-token");

        // Act (ejecución)
        // Se llama al método de login del servicio
        LoginResponse response = usuarioService.login(nombreUsuario, contrasena);

        // Assert (verificación)
        // Se validan los valores esperados en la respuesta
        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals(1L, response.getUserId());
        assertEquals("juanperez", response.getUsername());
        assertEquals("Juan Pérez", response.getNombre());
        assertEquals(TipoUsuario.VENDEDOR, response.getRole());
        assertEquals("Login exitoso", response.getMensaje());

        // Se verifica que se llamaron los métodos necesarios
        verify(usuarioRepository).findByNombreUsuario(nombreUsuario);
        verify(passwordEncoder).matches(contrasena, usuario.getContrasena());
        verify(jwtUtil).generateToken("juanperez", 1L, "VENDEDOR");
    }

    @Test
    @DisplayName("Debería enviar notificación de ingreso cuando usuario es ADMIN")
    void testLoginAdminConNotificacion() throws Exception {
        // Arrange
        String nombreUsuario = "admin";
        String contrasena = "admin123";

        // Se simula la existencia del usuario administrador
        when(usuarioRepository.findByNombreUsuario(nombreUsuario))
                .thenReturn(Optional.of(usuarioAdmin));

        // Se simula coincidencia de contraseña
        when(passwordEncoder.matches(contrasena, usuarioAdmin.getContrasena()))
                .thenReturn(true);

        // Se genera un token ficticio para el admin
        when(jwtUtil.generateToken(anyString(), anyLong(), anyString()))
                .thenReturn("jwt-token-admin");

        // Se simula el envío exitoso de la notificación de ingreso al admin
        doNothing().when(emailService)
                .sendAdminLoginNotification(anyString(), anyString(), anyString());

        // Act
        LoginResponse response = usuarioService.login(nombreUsuario, contrasena);

        // Assert
        assertNotNull(response);
        assertEquals("jwt-token-admin", response.getToken());

        // Se verifica que el servicio de correo fue llamado con los parámetros correctos
        verify(emailService).sendAdminLoginNotification(
                eq("admin@example.com"),
                eq("Admin User"),
                anyString()
        );
    }

    @Test
    @DisplayName("No debería fallar login si notificación de admin falla")
    void testLoginAdminCuandoNotificacionFalla() throws Exception {
        // Arrange
        String nombreUsuario = "admin";
        String contrasena = "admin123";

        // Se simula que el usuario admin existe
        when(usuarioRepository.findByNombreUsuario(nombreUsuario))
                .thenReturn(Optional.of(usuarioAdmin));

        // Contraseña correcta
        when(passwordEncoder.matches(contrasena, usuarioAdmin.getContrasena()))
                .thenReturn(true);

        // Se genera token con éxito
        when(jwtUtil.generateToken(anyString(), anyLong(), anyString()))
                .thenReturn("jwt-token-admin");

        // Se fuerza un error al intentar enviar el correo (para probar manejo de excepciones)
        doThrow(new Exception("Error de email")).when(emailService)
                .sendAdminLoginNotification(anyString(), anyString(), anyString());

        // Act
        LoginResponse response = usuarioService.login(nombreUsuario, contrasena);

        // Assert
        // A pesar del error en el envío de correo, el login debe completarse correctamente
        assertNotNull(response);
        assertEquals("jwt-token-admin", response.getToken());

        // Se verifica que sí se intentó enviar la notificación
        verify(emailService).sendAdminLoginNotification(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Debería lanzar excepción cuando usuario no existe en login")
    void testLoginUsuarioNoEncontrado() {
        // Arrange
        String nombreUsuario = "usuarioinexistente";
        String contrasena = "password123";

        // Se simula que no se encuentra ningún usuario con ese nombre
        when(usuarioRepository.findByNombreUsuario(nombreUsuario))
                .thenReturn(Optional.empty());

        // Act & Assert
        // Se espera que el servicio lance una excepción específica
        UsuarioNotFoundException exception = assertThrows(UsuarioNotFoundException.class, () -> {
            usuarioService.login(nombreUsuario, contrasena);
        });

        // Se verifica el mensaje de la excepción
        assertEquals("Usuario no encontrado", exception.getMessage());

        // Se confirma que no se intentó verificar contraseñas, ya que el usuario no existe
        verify(usuarioRepository).findByNombreUsuario(nombreUsuario);
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    @DisplayName("Debería lanzar excepción cuando contraseña es incorrecta")
    void testLoginContrasenaIncorrecta() {
        // Arrange
        String nombreUsuario = "juanperez";
        String contrasena = "contrasenaIncorrecta";

        // Se simula que el usuario existe
        when(usuarioRepository.findByNombreUsuario(nombreUsuario))
                .thenReturn(Optional.of(usuario));

        // Se simula que la contraseña no coincide
        when(passwordEncoder.matches(contrasena, usuario.getContrasena()))
                .thenReturn(false);

        // Act & Assert
        // Se espera una excepción de autenticación
        AuthenticationException exception = assertThrows(AuthenticationException.class, () -> {
            usuarioService.login(nombreUsuario, contrasena);
        });

        // Se valida el mensaje
        assertEquals("Contraseña incorrecta", exception.getMessage());

        // Se verifican las llamadas realizadas
        verify(usuarioRepository).findByNombreUsuario(nombreUsuario);
        verify(passwordEncoder).matches(contrasena, usuario.getContrasena());
    }

    @Test
    @DisplayName("Debería lanzar excepción cuando usuario está inactivo")
    void testLoginUsuarioInactivo() {
        // Arrange
        String nombreUsuario = "juanperez";
        String contrasena = "password123";

        // Se marca el usuario como inactivo
        usuario.setEstado(false);

        // El usuario existe y la contraseña es correcta
        when(usuarioRepository.findByNombreUsuario(nombreUsuario))
                .thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches(contrasena, usuario.getContrasena()))
                .thenReturn(true);

        // Act & Assert
        // Se espera una excepción de estado inactivo
        UsuarioEstadoException exception = assertThrows(UsuarioEstadoException.class, () -> {
            usuarioService.login(nombreUsuario, contrasena);
        });

        assertEquals("Usuario inactivo. Contacte al administrador", exception.getMessage());
    }

    @Test
    @DisplayName("Debería crear usuario exitosamente cuando datos son válidos")
    void testCrearUsuarioExitoso() {
        // Arrange
        // Se simula que no existen conflictos de datos (cedula, correo, usuario)
        when(usuarioRepository.existsByCedula(anyString())).thenReturn(false);
        when(usuarioRepository.existsByCorreo(anyString())).thenReturn(false);
        when(usuarioRepository.existsByNombreUsuario(anyString())).thenReturn(false);

        // Se mapea correctamente el DTO a la entidad
        when(usuarioMapper.toEntity(any(UsuarioDto.class))).thenReturn(usuario);

        // Se simula el proceso de encriptación de la contraseña
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");

        // Se simula el guardado exitoso del usuario
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

        // Act
        usuarioService.crearUsuario(usuarioDto);

        // Assert
        // Se verifican todas las validaciones y operaciones esperadas
        verify(usuarioRepository).existsByCedula("123456789");
        verify(usuarioRepository).existsByCorreo("juan@example.com");
        verify(usuarioRepository).existsByNombreUsuario("juanperez");
        verify(usuarioMapper).toEntity(usuarioDto);
        verify(passwordEncoder).encode("password123");
        verify(usuarioRepository).save(usuario);
    }


    @Test
    @DisplayName("Debería lanzar excepción cuando cédula ya existe al crear usuario")
    void testCrearUsuarioConCedulaDuplicada() {
        // Arrange
        when(usuarioRepository.existsByCedula(anyString())).thenReturn(true);

        // Act & Assert
        UsuarioDuplicateException exception = assertThrows(UsuarioDuplicateException.class, () -> {
            usuarioService.crearUsuario(usuarioDto);
        });

        assertEquals("Ya existe un usuario con la cédula: 123456789", exception.getMessage());
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Debería lanzar excepción cuando correo ya existe al crear usuario")
    void testCrearUsuarioConCorreoDuplicado() {
        // Arrange
        when(usuarioRepository.existsByCedula(anyString())).thenReturn(false);
        when(usuarioRepository.existsByCorreo(anyString())).thenReturn(true);

        // Act & Assert
        UsuarioDuplicateException exception = assertThrows(UsuarioDuplicateException.class, () -> {
            usuarioService.crearUsuario(usuarioDto);
        });

        assertEquals("Ya existe un usuario con el correo: juan@example.com", exception.getMessage());
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Debería lanzar excepción cuando contraseña es nula al crear usuario")
    void testCrearUsuarioConContrasenaNula() {
        // Arrange
        usuarioDto.setContrasena(null);

        // Act & Assert
        UsuarioBusinessException exception = assertThrows(UsuarioBusinessException.class, () -> {
            usuarioService.crearUsuario(usuarioDto);
        });

        assertEquals("La contraseña es requerida para crear un usuario", exception.getMessage());
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Debería actualizar usuario exitosamente")
    void testActualizarUsuarioExitoso() {
        // Arrange
        UsuarioDto dtoActualizado = new UsuarioDto();
        dtoActualizado.setNombre("Juan Pérez Actualizado");
        dtoActualizado.setCedula("123456789");
        dtoActualizado.setCorreo("juan.actualizado@example.com");
        dtoActualizado.setNombreUsuario("juanperez");
        dtoActualizado.setEstado(true);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        // CORRECCIÓN: Solo mockear lo que realmente se usa (el correo no cambia, así que no se llamará a existsByCorreoAndIdNot)
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

        // Act
        usuarioService.actualizarUsuario(1L, dtoActualizado);

        // Assert
        verify(usuarioRepository).findById(1L);
        verify(usuarioMapper).updateUsuarioFromDto(dtoActualizado, usuario);
        verify(usuarioRepository).save(usuario);
    }

    @Test
    @DisplayName("Debería actualizar contraseña cuando se solicita explícitamente")
    void testActualizarUsuarioConCambioContrasena() {
        // Arrange
        UsuarioDto dtoActualizado = new UsuarioDto();
        dtoActualizado.setCambiarContrasena(true);
        dtoActualizado.setContrasena("nuevaPassword");
        dtoActualizado.setCedula("123456789");
        dtoActualizado.setCorreo("juan@example.com");
        dtoActualizado.setNombreUsuario("juanperez");
        dtoActualizado.setEstado(true);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        // CORRECCIÓN: Solo mockear lo necesario (no se cambian credenciales, así que no se llamará a los exists...)
        when(passwordEncoder.encode(anyString())).thenReturn("encodedNewPassword");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

        // Act
        usuarioService.actualizarUsuario(1L, dtoActualizado);

        // Assert
        verify(passwordEncoder).encode("nuevaPassword");
        verify(usuarioRepository).save(usuario);
    }

    @Test
    @DisplayName("Debería lanzar excepción cuando no se proporciona nueva contraseña al cambiar")
    void testActualizarUsuarioCambioContrasenaSinPassword() {
        // Arrange
        UsuarioDto dtoActualizado = new UsuarioDto();
        dtoActualizado.setCambiarContrasena(true);
        dtoActualizado.setContrasena(null);
        dtoActualizado.setCedula("123456789");
        dtoActualizado.setEstado(true);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        // CORRECCIÓN: No mockear existsByCedulaAndIdNot ya que no se usa en este caso

        // Act & Assert
        UsuarioBusinessException exception = assertThrows(UsuarioBusinessException.class, () -> {
            usuarioService.actualizarUsuario(1L, dtoActualizado);
        });

        assertEquals("Se solicitó cambiar contraseña pero no se proporcionó una nueva contraseña", exception.getMessage());
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Debería cambiar estado de usuario exitosamente")
    void testCambiarEstadoUsuario() {
        // Arrange
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

        // Act
        usuarioService.cambiarEstadoUsuario(1L, false);

        // Assert
        verify(usuarioRepository).findById(1L);
        verify(usuarioRepository).save(usuario);
        assertFalse(usuario.isEstado());
    }

    @Test
    @DisplayName("Debería obtener usuario por ID exitosamente")
    void testObtenerUsuarioPorId() {
        // Arrange
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(usuarioMapper.toDto(any(Usuario.class))).thenReturn(usuarioDto);

        // Act
        UsuarioDto resultado = usuarioService.obtenerUsuarioPorId(1L);

        // Assert
        assertNotNull(resultado);
        assertEquals("Juan Pérez", resultado.getNombre());
        verify(usuarioRepository).findById(1L);
        verify(usuarioMapper).toDto(usuario);
    }

    @Test
    @DisplayName("Debería obtener usuario por cédula exitosamente")
    void testObtenerUsuarioPorCedula() {
        // Arrange
        when(usuarioRepository.findByCedula("123456789")).thenReturn(Optional.of(usuario));
        when(usuarioMapper.toDto(any(Usuario.class))).thenReturn(usuarioDto);

        // Act
        UsuarioDto resultado = usuarioService.obtenerUsuarioPorCedula("123456789");

        // Assert
        assertNotNull(resultado);
        assertEquals("123456789", resultado.getCedula());
        verify(usuarioRepository).findByCedula("123456789");
    }

    @Test
    @DisplayName("Debería solicitar recordatorio de contraseña exitosamente")
    void testSolicitarRecordatorioContrasena() throws Exception {
        // Arrange
        String correo = "vendedor@example.com";
        List<TipoUsuario> tiposPermitidos = Arrays.asList(
                TipoUsuario.VENDEDOR, TipoUsuario.CAJERO, TipoUsuario.DESPACHADOR
        );

        when(usuarioRepository.findByCorreoAndTipoUsuarioIn(correo, tiposPermitidos))
                .thenReturn(Optional.of(usuario));
        doNothing().when(emailService).sendPasswordReminder(anyString(), anyString(), anyString(), anyString());

        // Act
        usuarioService.solicitarRecordatorioContrasena(correo);

        // Assert
        verify(usuarioRepository).findByCorreoAndTipoUsuarioIn(correo, tiposPermitidos);
        verify(emailService).sendPasswordReminder(
                eq("juan@example.com"),
                eq("Juan Pérez"),
                eq("juanperez"),
                eq("Por razones de seguridad, contacte al administrador para restablecer su contraseña")
        );
    }

    @Test
    @DisplayName("Debería lanzar EmailException cuando envío de recordatorio falla")
    void testSolicitarRecordatorioContrasenaConErrorDeEmail() throws Exception {
        // Arrange
        String correo = "vendedor@example.com";
        List<TipoUsuario> tiposPermitidos = Arrays.asList(
                TipoUsuario.VENDEDOR, TipoUsuario.CAJERO, TipoUsuario.DESPACHADOR
        );

        when(usuarioRepository.findByCorreoAndTipoUsuarioIn(correo, tiposPermitidos))
                .thenReturn(Optional.of(usuario));
        doThrow(new Exception("Error SMTP")).when(emailService).sendPasswordReminder(anyString(), anyString(), anyString(), anyString());

        // Act & Assert
        EmailException exception = assertThrows(EmailException.class, () -> {
            usuarioService.solicitarRecordatorioContrasena(correo);
        });

        assertTrue(exception.getMessage().contains("Error al enviar el recordatorio por correo"));
        verify(usuarioRepository).findByCorreoAndTipoUsuarioIn(correo, tiposPermitidos);
        verify(emailService).sendPasswordReminder(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Debería lanzar excepción cuando correo está vacío en recordatorio")
    void testSolicitarRecordatorioContrasenaCorreoVacio() {
        // Act & Assert
        UsuarioBusinessException exception = assertThrows(UsuarioBusinessException.class, () -> {
            usuarioService.solicitarRecordatorioContrasena("   ");
        });

        assertEquals("El correo electrónico es requerido", exception.getMessage());
    }

    @Test
    @DisplayName("Debería lanzar excepción cuando usuario no tiene permisos para recordatorio")
    void testSolicitarRecordatorioContrasenaUsuarioNoPermitido() {
        // Arrange
        String correo = "admin@example.com";
        List<TipoUsuario> tiposPermitidos = Arrays.asList(
                TipoUsuario.VENDEDOR, TipoUsuario.CAJERO, TipoUsuario.DESPACHADOR
        );

        when(usuarioRepository.findByCorreoAndTipoUsuarioIn(correo, tiposPermitidos))
                .thenReturn(Optional.empty());

        // Act & Assert
        UsuarioNotFoundException exception = assertThrows(UsuarioNotFoundException.class, () -> {
            usuarioService.solicitarRecordatorioContrasena(correo);
        });

        assertEquals("No se encontró un usuario activo con ese correo electrónico o no tiene permisos para solicitar recordatorio", exception.getMessage());
    }

    @Test
    @DisplayName("Debería obtener usuarios por tipo exitosamente")
    void testObtenerUsuariosPorTipo() {
        // Arrange
        List<Usuario> usuarios = Arrays.asList(usuario);
        when(usuarioRepository.findByTipoUsuario(TipoUsuario.VENDEDOR)).thenReturn(usuarios);
        when(usuarioMapper.toDto(any(Usuario.class))).thenReturn(usuarioDto);

        // Act
        List<UsuarioDto> resultados = usuarioService.obtenerUsuariosPorTipo(TipoUsuario.VENDEDOR);

        // Assert
        assertEquals(1, resultados.size());
        verify(usuarioRepository).findByTipoUsuario(TipoUsuario.VENDEDOR);
    }

    @Test
    @DisplayName("Debería obtener todos los usuarios exitosamente")
    void testObtenerTodosLosUsuarios() {
        // Arrange
        List<Usuario> usuarios = Arrays.asList(usuario);
        when(usuarioRepository.findAll()).thenReturn(usuarios);
        when(usuarioMapper.toDtoSafe(any(Usuario.class))).thenReturn(usuarioDto);

        // Act
        List<UsuarioDto> resultados = usuarioService.obtenerTodosLosUsuarios();

        // Assert
        assertEquals(1, resultados.size());
        verify(usuarioRepository).findAll();
        verify(usuarioMapper, times(1)).toDtoSafe(usuario);
    }

    @Test
    @DisplayName("Debería obtener usuarios activos exitosamente")
    void testObtenerUsuariosActivos() {
        // Arrange
        List<Usuario> usuarios = Arrays.asList(usuario);
        when(usuarioRepository.findByEstado(true)).thenReturn(usuarios);
        when(usuarioMapper.toDtoSafe(any(Usuario.class))).thenReturn(usuarioDto);

        // Act
        List<UsuarioDto> resultados = usuarioService.obtenerUsuariosActivos();

        // Assert
        assertEquals(1, resultados.size());
        verify(usuarioRepository).findByEstado(true);
    }

    @Test
    @DisplayName("Debería eliminar usuario exitosamente")
    void testEliminarUsuario() {
        // Arrange
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        doNothing().when(usuarioRepository).delete(usuario);

        // Act
        usuarioService.eliminarUsuario(1L);

        // Assert
        verify(usuarioRepository).findById(1L);
        verify(usuarioRepository).delete(usuario);
    }

    @Test
    @DisplayName("Debería buscar usuarios por nombre exitosamente")
    void testBuscarUsuariosPorNombre() {
        // Arrange
        List<Usuario> usuarios = Arrays.asList(usuario);
        when(usuarioRepository.findByNombreContainingIgnoreCase("Juan")).thenReturn(usuarios);
        when(usuarioMapper.toDto(any(Usuario.class))).thenReturn(usuarioDto);

        // Act
        List<UsuarioDto> resultados = usuarioService.buscarUsuariosPorNombre("Juan");

        // Assert
        assertEquals(1, resultados.size());
        verify(usuarioRepository).findByNombreContainingIgnoreCase("Juan");
    }
}