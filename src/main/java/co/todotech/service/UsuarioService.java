package co.todotech.service;

import co.todotech.model.dto.usuario.LoginResponse;
import co.todotech.model.dto.usuario.UsuarioDto;

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
}
