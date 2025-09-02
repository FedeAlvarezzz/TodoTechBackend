package co.todotech.service;

import co.todotech.model.dto.usuario.UsuarioDto;

public interface UsuarioService {

    UsuarioDto obtenerUsuarioPorId(Long id) throws Exception;
    UsuarioDto obtenerUsuarioPorCedula(String cedula) throws Exception;
    void crearUsuario(UsuarioDto dto) throws Exception;
    void actualizarUsuario(Long id, UsuarioDto dto) throws Exception;
    void eliminarUsuario(Long id) throws Exception;
}
