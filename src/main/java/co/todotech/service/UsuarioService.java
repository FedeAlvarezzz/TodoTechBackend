package co.todotech.service;

import co.todotech.model.dto.usuario.LoginResponse;
import co.todotech.model.dto.usuario.UsuarioDto;
import co.todotech.model.enums.TipoUsuario;

import java.time.LocalDateTime;
import java.util.List;

public interface UsuarioService {

    UsuarioDto obtenerUsuarioPorId(Long id) throws Exception;
    UsuarioDto obtenerUsuarioPorCedula(String cedula) throws Exception;
    List<UsuarioDto> obtenerTodosLosUsuarios();
    List<UsuarioDto> obtenerUsuariosActivos();
    List<UsuarioDto> obtenerUsuariosInactivos();
    LoginResponse login(String nombreUsuario, String contrasena) throws Exception;
    void cambiarEstadoUsuario(Long id, boolean estado) throws Exception;
    void crearUsuario(UsuarioDto dto) throws Exception;
    void actualizarUsuario(Long id, UsuarioDto dto) throws Exception;
    void eliminarUsuario(Long id) throws Exception;

    List<UsuarioDto> obtenerUsuariosPorTipo(TipoUsuario tipoUsuario) throws Exception;
    List<UsuarioDto> buscarUsuariosPorNombre(String nombre) throws Exception;
    List<UsuarioDto> buscarUsuariosPorCedula(String cedula) throws Exception;
    List<UsuarioDto> obtenerUsuariosPorFechaCreacion(LocalDateTime fechaInicio, LocalDateTime fechaFin) throws Exception;
    List<UsuarioDto> obtenerUsuariosCreadosDespuesDe(LocalDateTime fecha) throws Exception;
    List<UsuarioDto> obtenerUsuariosCreadosAntesDe(LocalDateTime fecha) throws Exception;

    void solicitarRecordatorioContrasena(String correo) throws Exception;
}
