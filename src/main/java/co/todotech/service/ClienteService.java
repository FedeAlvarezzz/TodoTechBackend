package co.todotech.service;

import co.todotech.model.dto.cliente.ClienteDto;
import co.todotech.model.enums.TipoCliente;

import java.time.LocalDateTime;
import java.util.List;

public interface ClienteService {

    ClienteDto crearCliente(ClienteDto clienteDto) throws Exception;
    ClienteDto actualizarCliente(Long id, ClienteDto clienteDto) throws Exception;
    void eliminarCliente(Long id) throws Exception;

    ClienteDto obtenerClientePorId(Long id) throws Exception;
    ClienteDto obtenerClientePorCedula(String cedula) throws Exception;
    ClienteDto obtenerClientePorCorreo(String correo) throws Exception;

    List<ClienteDto> obtenerClientesPorTipo(TipoCliente tipoCliente);
    List<ClienteDto> obtenerClientesPorNombre(String nombre);
    List<ClienteDto> obtenerClientesRegistradosDespuesDe(LocalDateTime fecha);
    List<ClienteDto> obtenerClientesRegistradosEntre(LocalDateTime fechaInicio, LocalDateTime fechaFin);

    long contarClientesPorTipo(TipoCliente tipoCliente);
}