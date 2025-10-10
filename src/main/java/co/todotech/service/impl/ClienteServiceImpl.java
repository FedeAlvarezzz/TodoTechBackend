package co.todotech.service.impl;

import co.todotech.mapper.ClienteMapper;
import co.todotech.model.dto.cliente.ClienteDto;
import co.todotech.model.entities.Cliente;
import co.todotech.model.enums.TipoCliente;
import co.todotech.repository.ClienteRepository;
import co.todotech.service.ClienteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClienteServiceImpl implements ClienteService {

    private final ClienteMapper clienteMapper;
    private final ClienteRepository clienteRepository;

    @Override
    @Transactional
    public ClienteDto crearCliente(ClienteDto dto) throws Exception {
        log.info("Creando cliente: {}", dto.nombre());

        // Validar unicidad de cédula
        if (clienteRepository.existsByCedula(dto.cedula())) {
            throw new Exception("Ya existe un cliente con la cédula: " + dto.cedula());
        }

        // Validar unicidad de correo si se proporciona
        if (dto.correo() != null && !dto.correo().isBlank() &&
                clienteRepository.existsByCorreo(dto.correo())) {
            throw new Exception("Ya existe un cliente con el correo: " + dto.correo());
        }

        Cliente cliente = clienteMapper.toEntity(dto);
        clienteRepository.save(cliente);

        log.info("Cliente creado exitosamente: id={}, cedula={}", cliente.getId(), cliente.getCedula());
        return clienteMapper.toDto(cliente);
    }

    @Override
    @Transactional
    public ClienteDto actualizarCliente(Long id, ClienteDto dto) throws Exception {
        log.info("Actualizando cliente id={}", id);

        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new Exception("Cliente no encontrado con ID: " + id));

        // Validar unicidad de cédula contra otros registros
        if (!cliente.getCedula().equals(dto.cedula()) &&
                clienteRepository.existsByCedulaAndIdNot(dto.cedula(), id)) {
            throw new Exception("Ya existe otro cliente con la cédula: " + dto.cedula());
        }

        // Validar unicidad de correo contra otros registros
        if (dto.correo() != null && !dto.correo().isBlank() &&
                !dto.correo().equals(cliente.getCorreo()) &&
                clienteRepository.existsByCorreoAndIdNot(dto.correo(), id)) {
            throw new Exception("Ya existe otro cliente con el correo: " + dto.correo());
        }

        clienteMapper.updateClienteFromDto(dto, cliente);
        clienteRepository.save(cliente);

        log.info("Cliente actualizado: id={}, cedula={}", cliente.getId(), cliente.getCedula());
        return clienteMapper.toDto(cliente);
    }

    @Override
    @Transactional
    public void eliminarCliente(Long id) throws Exception {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new Exception("Cliente no encontrado con ID: " + id));

        // Aquí podrías agregar validaciones adicionales (ej: si tiene órdenes activas)

        clienteRepository.delete(cliente);
        log.info("Cliente eliminado: {}", id);
    }

    @Override
    public ClienteDto obtenerClientePorId(Long id) throws Exception {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new Exception("Cliente no encontrado con ID: " + id));
        return clienteMapper.toDto(cliente);
    }

    @Override
    public ClienteDto obtenerClientePorCedula(String cedula) throws Exception {
        Cliente cliente = clienteRepository.findByCedula(cedula)
                .orElseThrow(() -> new Exception("Cliente no encontrado con cédula: " + cedula));
        return clienteMapper.toDto(cliente);
    }

    @Override
    public ClienteDto obtenerClientePorCorreo(String correo) throws Exception {
        Cliente cliente = clienteRepository.findByCorreo(correo)
                .orElseThrow(() -> new Exception("Cliente no encontrado con correo: " + correo));
        return clienteMapper.toDto(cliente);
    }

    @Override
    public List<ClienteDto> obtenerClientesPorTipo(TipoCliente tipoCliente) {
        return clienteRepository.findByTipoCliente(tipoCliente).stream()
                .map(clienteMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ClienteDto> obtenerClientesPorNombre(String nombre) {
        return clienteRepository.findByNombreContaining(nombre).stream()
                .map(clienteMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ClienteDto> obtenerClientesRegistradosDespuesDe(LocalDateTime fecha) {
        return clienteRepository.findByFechaRegistroAfter(fecha).stream()
                .map(clienteMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ClienteDto> obtenerClientesRegistradosEntre(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        return clienteRepository.findByFechaRegistroBetween(fechaInicio, fechaFin).stream()
                .map(clienteMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public long contarClientesPorTipo(TipoCliente tipoCliente) {
        return clienteRepository.countByTipoCliente(tipoCliente);
    }
}