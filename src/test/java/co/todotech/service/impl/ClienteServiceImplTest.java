package co.todotech.service.impl;

import co.todotech.mapper.ClienteMapper;
import co.todotech.model.dto.cliente.ClienteDto;
import co.todotech.model.entities.Cliente;
import co.todotech.model.enums.TipoCliente;
import co.todotech.repository.ClienteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClienteServiceImplTest {

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private ClienteMapper clienteMapper;

    @InjectMocks
    private ClienteServiceImpl clienteService;

    private ClienteDto clienteDto;
    private Cliente cliente;
    private final LocalDateTime fechaRegistro = LocalDateTime.now();

    @BeforeEach
    void setUp() {
        // Configurar datos de prueba comunes
        clienteDto = new ClienteDto(
                "Juan Pérez",
                "123456789",
                "juan@example.com",
                "+573001234567",
                "Calle 123 #45-67",
                TipoCliente.NATURAL,
                10.0
        );

        cliente = Cliente.builder()
                .id(1L)
                .nombre("Juan Pérez")
                .cedula("123456789")
                .correo("juan@example.com")
                .telefono("+573001234567")
                .direccion("Calle 123 #45-67")
                .fechaRegistro(fechaRegistro)
                .tipoCliente(TipoCliente.NATURAL)
                .descuentoAplicable(10.0)
                .build();
    }

    @Test
    @DisplayName("Debería crear cliente exitosamente cuando datos son válidos")
    void testCrearClienteExitoso() throws Exception {
        // Arrange
        when(clienteRepository.existsByCedula(anyString())).thenReturn(false);
        when(clienteRepository.existsByCorreo(anyString())).thenReturn(false);
        when(clienteMapper.toEntity(any(ClienteDto.class))).thenReturn(cliente);
        when(clienteRepository.save(any(Cliente.class))).thenReturn(cliente);
        when(clienteMapper.toDto(any(Cliente.class))).thenReturn(clienteDto);

        // Act
        ClienteDto resultado = clienteService.crearCliente(clienteDto);

        // Assert
        assertNotNull(resultado);
        verify(clienteRepository).existsByCedula("123456789");
        verify(clienteRepository).existsByCorreo("juan@example.com");
        verify(clienteMapper).toEntity(clienteDto);
        verify(clienteRepository).save(cliente);
        verify(clienteMapper).toDto(cliente);
    }

    @Test
    @DisplayName("Debería lanzar excepción cuando cédula ya existe")
    void testCrearClienteConCedulaDuplicada() {
        // Arrange
        when(clienteRepository.existsByCedula(anyString())).thenReturn(true);

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            clienteService.crearCliente(clienteDto);
        });

        assertEquals("Ya existe un cliente con la cédula: 123456789", exception.getMessage());
        verify(clienteRepository, never()).save(any(Cliente.class));
    }

    @Test
    @DisplayName("Debería lanzar excepción cuando correo ya existe")
    void testCrearClienteConCorreoDuplicado() {
        // Arrange
        when(clienteRepository.existsByCedula(anyString())).thenReturn(false);
        when(clienteRepository.existsByCorreo(anyString())).thenReturn(true);

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            clienteService.crearCliente(clienteDto);
        });

        assertEquals("Ya existe un cliente con el correo: juan@example.com", exception.getMessage());
        verify(clienteRepository, never()).save(any(Cliente.class));
    }

    @Test
    @DisplayName("Debería crear cliente exitosamente cuando correo es nulo")
    void testCrearClienteConCorreoNulo() throws Exception {
        // Arrange
        ClienteDto dtoSinCorreo = new ClienteDto(
                "Juan Pérez", "123456789", null, "+573001234567",
                "Calle 123 #45-67", TipoCliente.NATURAL, 10.0
        );

        when(clienteRepository.existsByCedula(anyString())).thenReturn(false);
        when(clienteMapper.toEntity(any(ClienteDto.class))).thenReturn(cliente);
        when(clienteRepository.save(any(Cliente.class))).thenReturn(cliente);
        when(clienteMapper.toDto(any(Cliente.class))).thenReturn(dtoSinCorreo);

        // Act
        ClienteDto resultado = clienteService.crearCliente(dtoSinCorreo);

        // Assert
        assertNotNull(resultado);
        verify(clienteRepository).existsByCedula("123456789");
        verify(clienteRepository, never()).existsByCorreo(anyString());
        verify(clienteRepository).save(cliente);
    }

    @Test
    @DisplayName("Debería crear cliente exitosamente cuando correo está en blanco")
    void testCrearClienteConCorreoEnBlanco() throws Exception {
        // Arrange
        ClienteDto dtoCorreoBlanco = new ClienteDto(
                "Juan Pérez", "123456789", "   ", "+573001234567",
                "Calle 123 #45-67", TipoCliente.NATURAL, 10.0
        );

        when(clienteRepository.existsByCedula(anyString())).thenReturn(false);
        when(clienteMapper.toEntity(any(ClienteDto.class))).thenReturn(cliente);
        when(clienteRepository.save(any(Cliente.class))).thenReturn(cliente);
        when(clienteMapper.toDto(any(Cliente.class))).thenReturn(dtoCorreoBlanco);

        // Act
        ClienteDto resultado = clienteService.crearCliente(dtoCorreoBlanco);

        // Assert
        assertNotNull(resultado);
        verify(clienteRepository).existsByCedula("123456789");
        verify(clienteRepository, never()).existsByCorreo(anyString());
        verify(clienteRepository).save(cliente);
    }

    @Test
    @DisplayName("Debería actualizar cliente exitosamente")
    void testActualizarClienteExitoso() throws Exception {
        // Arrange
        ClienteDto dtoActualizado = new ClienteDto(
                "Juan Pérez Actualizado", "123456789", "juan.actualizado@example.com",
                "+573001234568", "Calle Actualizada 123", TipoCliente.JURIDICO, 15.0
        );

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(clienteRepository.existsByCorreoAndIdNot(anyString(), anyLong())).thenReturn(false);
        when(clienteRepository.save(any(Cliente.class))).thenReturn(cliente);
        when(clienteMapper.toDto(any(Cliente.class))).thenReturn(dtoActualizado);

        // Act
        ClienteDto resultado = clienteService.actualizarCliente(1L, dtoActualizado);

        // Assert
        assertNotNull(resultado);
        verify(clienteRepository).findById(1L);
        verify(clienteRepository).existsByCorreoAndIdNot("juan.actualizado@example.com", 1L);
        verify(clienteMapper).updateClienteFromDto(dtoActualizado, cliente);
        verify(clienteRepository).save(cliente);
    }

    @Test
    @DisplayName("Debería lanzar excepción al actualizar cliente no encontrado")
    void testActualizarClienteNoEncontrado() {
        // Arrange
        when(clienteRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            clienteService.actualizarCliente(1L, clienteDto);
        });

        assertEquals("Cliente no encontrado con ID: 1", exception.getMessage());
        verify(clienteRepository, never()).save(any(Cliente.class));
    }

    @Test
    @DisplayName("Debería lanzar excepción al actualizar con cédula duplicada")
    void testActualizarClienteConCedulaDuplicada() {
        // Arrange
        ClienteDto dtoActualizado = new ClienteDto(
                "Juan Pérez", "987654321", "juan@example.com",
                "+573001234567", "Calle 123 #45-67", TipoCliente.NATURAL, 10.0
        );

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(clienteRepository.existsByCedulaAndIdNot(anyString(), anyLong())).thenReturn(true);

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            clienteService.actualizarCliente(1L, dtoActualizado);
        });

        assertEquals("Ya existe otro cliente con la cédula: 987654321", exception.getMessage());
        verify(clienteRepository, never()).save(any(Cliente.class));
    }

    @Test
    @DisplayName("Debería lanzar excepción al actualizar con correo duplicado")
    void testActualizarClienteConCorreoDuplicado() {
        // Arrange
        ClienteDto dtoActualizado = new ClienteDto(
                "Juan Pérez", "123456789", "nuevo.correo@example.com",
                "+573001234567", "Calle 123 #45-67", TipoCliente.NATURAL, 10.0
        );

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(clienteRepository.existsByCorreoAndIdNot(anyString(), anyLong())).thenReturn(true);

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            clienteService.actualizarCliente(1L, dtoActualizado);
        });

        assertEquals("Ya existe otro cliente con el correo: nuevo.correo@example.com", exception.getMessage());
        verify(clienteRepository, never()).save(any(Cliente.class));
    }

    @Test
    @DisplayName("Debería eliminar cliente exitosamente")
    void testEliminarCliente() throws Exception {
        // Arrange
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        doNothing().when(clienteRepository).delete(cliente);

        // Act
        clienteService.eliminarCliente(1L);

        // Assert
        verify(clienteRepository).findById(1L);
        verify(clienteRepository).delete(cliente);
    }

    @Test
    @DisplayName("Debería lanzar excepción al eliminar cliente no encontrado")
    void testEliminarClienteNoEncontrado() {
        // Arrange
        when(clienteRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            clienteService.eliminarCliente(1L);
        });

        assertEquals("Cliente no encontrado con ID: 1", exception.getMessage());
        verify(clienteRepository, never()).delete(any(Cliente.class));
    }

    @Test
    @DisplayName("Debería obtener cliente por ID exitosamente")
    void testObtenerClientePorId() throws Exception {
        // Arrange
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(clienteMapper.toDto(any(Cliente.class))).thenReturn(clienteDto);

        // Act
        ClienteDto resultado = clienteService.obtenerClientePorId(1L);

        // Assert
        assertNotNull(resultado);
        assertEquals("Juan Pérez", resultado.nombre());
        assertEquals(TipoCliente.NATURAL, resultado.tipoCliente());
        verify(clienteRepository).findById(1L);
        verify(clienteMapper).toDto(cliente);
    }

    @Test
    @DisplayName("Debería lanzar excepción cuando cliente no existe por ID")
    void testObtenerClientePorIdNoEncontrado() {
        // Arrange
        when(clienteRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            clienteService.obtenerClientePorId(1L);
        });

        assertTrue(exception.getMessage().contains("Cliente no encontrado con ID: 1"));
        verify(clienteRepository).findById(1L);
    }

    @Test
    @DisplayName("Debería obtener cliente por cédula exitosamente")
    void testObtenerClientePorCedula() throws Exception {
        // Arrange
        when(clienteRepository.findByCedula("123456789")).thenReturn(Optional.of(cliente));
        when(clienteMapper.toDto(any(Cliente.class))).thenReturn(clienteDto);

        // Act
        ClienteDto resultado = clienteService.obtenerClientePorCedula("123456789");

        // Assert
        assertNotNull(resultado);
        assertEquals("123456789", resultado.cedula());
        verify(clienteRepository).findByCedula("123456789");
    }

    @Test
    @DisplayName("Debería lanzar excepción cuando cliente no existe por cédula")
    void testObtenerClientePorCedulaNoEncontrado() {
        // Arrange
        when(clienteRepository.findByCedula("999999999")).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            clienteService.obtenerClientePorCedula("999999999");
        });

        assertEquals("Cliente no encontrado con cédula: 999999999", exception.getMessage());
        verify(clienteRepository).findByCedula("999999999");
    }

    @Test
    @DisplayName("Debería obtener cliente por correo exitosamente")
    void testObtenerClientePorCorreo() throws Exception {
        // Arrange
        when(clienteRepository.findByCorreo("juan@example.com")).thenReturn(Optional.of(cliente));
        when(clienteMapper.toDto(any(Cliente.class))).thenReturn(clienteDto);

        // Act
        ClienteDto resultado = clienteService.obtenerClientePorCorreo("juan@example.com");

        // Assert
        assertNotNull(resultado);
        assertEquals("juan@example.com", resultado.correo());
        verify(clienteRepository).findByCorreo("juan@example.com");
    }

    @Test
    @DisplayName("Debería lanzar excepción cuando cliente no existe por correo")
    void testObtenerClientePorCorreoNoEncontrado() {
        // Arrange
        when(clienteRepository.findByCorreo("noexiste@example.com")).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            clienteService.obtenerClientePorCorreo("noexiste@example.com");
        });

        assertEquals("Cliente no encontrado con correo: noexiste@example.com", exception.getMessage());
        verify(clienteRepository).findByCorreo("noexiste@example.com");
    }

    @Test
    @DisplayName("Debería obtener clientes por tipo NATURAL")
    void testObtenerClientesPorTipoNatural() {
        // Arrange
        List<Cliente> clientes = Arrays.asList(cliente);
        when(clienteRepository.findByTipoCliente(TipoCliente.NATURAL)).thenReturn(clientes);
        when(clienteMapper.toDto(any(Cliente.class))).thenReturn(clienteDto);

        // Act
        List<ClienteDto> resultados = clienteService.obtenerClientesPorTipo(TipoCliente.NATURAL);

        // Assert
        assertEquals(1, resultados.size());
        verify(clienteRepository).findByTipoCliente(TipoCliente.NATURAL);
    }

    @Test
    @DisplayName("Debería obtener clientes por tipo JURIDICO")
    void testObtenerClientesPorTipoJuridico() {
        // Arrange
        Cliente clienteJuridico = Cliente.builder()
                .id(2L)
                .nombre("Empresa XYZ")
                .cedula("900123456")
                .tipoCliente(TipoCliente.JURIDICO)
                .build();

        ClienteDto dtoJuridico = new ClienteDto(
                "Empresa XYZ", "900123456", "empresa@xyz.com",
                "+573001111111", "Carrera 100 #15-20", TipoCliente.JURIDICO, 5.0
        );

        List<Cliente> clientes = Arrays.asList(clienteJuridico);
        when(clienteRepository.findByTipoCliente(TipoCliente.JURIDICO)).thenReturn(clientes);
        when(clienteMapper.toDto(clienteJuridico)).thenReturn(dtoJuridico);

        // Act
        List<ClienteDto> resultados = clienteService.obtenerClientesPorTipo(TipoCliente.JURIDICO);

        // Assert
        assertEquals(1, resultados.size());
        assertEquals(TipoCliente.JURIDICO, resultados.get(0).tipoCliente());
        verify(clienteRepository).findByTipoCliente(TipoCliente.JURIDICO);
    }

    @Test
    @DisplayName("Debería obtener clientes por nombre")
    void testObtenerClientesPorNombre() {
        // Arrange
        List<Cliente> clientes = Arrays.asList(cliente);
        when(clienteRepository.findByNombreContaining("Juan")).thenReturn(clientes);
        when(clienteMapper.toDto(any(Cliente.class))).thenReturn(clienteDto);

        // Act
        List<ClienteDto> resultados = clienteService.obtenerClientesPorNombre("Juan");

        // Assert
        assertEquals(1, resultados.size());
        verify(clienteRepository).findByNombreContaining("Juan");
    }

    @Test
    @DisplayName("Debería obtener clientes registrados después de fecha")
    void testObtenerClientesRegistradosDespuesDe() {
        // Arrange
        LocalDateTime fecha = LocalDateTime.now().minusDays(7);
        List<Cliente> clientes = Arrays.asList(cliente);
        when(clienteRepository.findByFechaRegistroAfter(fecha)).thenReturn(clientes);
        when(clienteMapper.toDto(any(Cliente.class))).thenReturn(clienteDto);

        // Act
        List<ClienteDto> resultados = clienteService.obtenerClientesRegistradosDespuesDe(fecha);

        // Assert
        assertEquals(1, resultados.size());
        verify(clienteRepository).findByFechaRegistroAfter(fecha);
    }

    @Test
    @DisplayName("Debería obtener clientes registrados entre fechas")
    void testObtenerClientesRegistradosEntre() {
        // Arrange
        LocalDateTime fechaInicio = LocalDateTime.now().minusDays(30);
        LocalDateTime fechaFin = LocalDateTime.now();
        List<Cliente> clientes = Arrays.asList(cliente);
        when(clienteRepository.findByFechaRegistroBetween(fechaInicio, fechaFin)).thenReturn(clientes);
        when(clienteMapper.toDto(any(Cliente.class))).thenReturn(clienteDto);

        // Act
        List<ClienteDto> resultados = clienteService.obtenerClientesRegistradosEntre(fechaInicio, fechaFin);

        // Assert
        assertEquals(1, resultados.size());
        verify(clienteRepository).findByFechaRegistroBetween(fechaInicio, fechaFin);
    }

    @Test
    @DisplayName("Debería contar clientes por tipo NATURAL")
    void testContarClientesPorTipoNatural() {
        // Arrange
        when(clienteRepository.countByTipoCliente(TipoCliente.NATURAL)).thenReturn(8L);

        // Act
        long resultado = clienteService.contarClientesPorTipo(TipoCliente.NATURAL);

        // Assert
        assertEquals(8L, resultado);
        verify(clienteRepository).countByTipoCliente(TipoCliente.NATURAL);
    }

    @Test
    @DisplayName("Debería contar clientes por tipo JURIDICO")
    void testContarClientesPorTipoJuridico() {
        // Arrange
        when(clienteRepository.countByTipoCliente(TipoCliente.JURIDICO)).thenReturn(3L);

        // Act
        long resultado = clienteService.contarClientesPorTipo(TipoCliente.JURIDICO);

        // Assert
        assertEquals(3L, resultado);
        verify(clienteRepository).countByTipoCliente(TipoCliente.JURIDICO);
    }

    @Test
    @DisplayName("Debería obtener todos los clientes")
    void testObtenerTodosLosClientes() {
        // Arrange
        List<Cliente> clientes = Arrays.asList(cliente);
        when(clienteRepository.findAllOrderedByFechaRegistro()).thenReturn(clientes);
        when(clienteMapper.toDto(any(Cliente.class))).thenReturn(clienteDto);

        // Act
        List<ClienteDto> resultados = clienteService.obtenerTodosLosClientes();

        // Assert
        assertEquals(1, resultados.size());
        verify(clienteRepository).findAllOrderedByFechaRegistro();
    }

    @Test
    @DisplayName("Debería actualizar cliente manteniendo misma cédula sin validar duplicados")
    void testActualizarClienteMismaCedula() throws Exception {
        // Arrange
        ClienteDto dtoMismaCedula = new ClienteDto(
                "Juan Pérez Actualizado", "123456789", "juan.actualizado@example.com",
                "+573001234568", "Calle Actualizada 123", TipoCliente.JURIDICO, 15.0
        );

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(clienteRepository.existsByCorreoAndIdNot(anyString(), anyLong())).thenReturn(false);
        when(clienteRepository.save(any(Cliente.class))).thenReturn(cliente);
        when(clienteMapper.toDto(any(Cliente.class))).thenReturn(dtoMismaCedula);

        // Act
        ClienteDto resultado = clienteService.actualizarCliente(1L, dtoMismaCedula);

        // Assert
        assertNotNull(resultado);
        verify(clienteRepository, never()).existsByCedulaAndIdNot(anyString(), anyLong());
        verify(clienteRepository).save(cliente);
    }

    @Test
    @DisplayName("Debería actualizar cliente con correo nulo sin validar duplicados")
    void testActualizarClienteConCorreoNulo() throws Exception {
        // Arrange
        ClienteDto dtoSinCorreo = new ClienteDto(
                "Juan Pérez Modificado", "123456789", null, "+573001234567",
                "Calle 123 #45-67", TipoCliente.NATURAL, 10.0
        );

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        // NO stub de existsByCedulaAndIdNot porque la cédula es la misma
        when(clienteRepository.save(any(Cliente.class))).thenReturn(cliente);
        when(clienteMapper.toDto(any(Cliente.class))).thenReturn(dtoSinCorreo);

        // Act
        ClienteDto resultado = clienteService.actualizarCliente(1L, dtoSinCorreo);

        // Assert
        assertNotNull(resultado);
        // No se deben llamar las validaciones de unicidad porque:
        // - La cédula es la misma (no necesita validación)
        // - El correo es nulo (no necesita validación)
        verify(clienteRepository, never()).existsByCedulaAndIdNot(anyString(), anyLong());
        verify(clienteRepository, never()).existsByCorreoAndIdNot(anyString(), anyLong());
        verify(clienteRepository).save(cliente);
    }

    @Test
    @DisplayName("Debería actualizar cliente con correo en blanco sin validar duplicados")
    void testActualizarClienteConCorreoEnBlanco() throws Exception {
        // Arrange
        ClienteDto dtoCorreoBlanco = new ClienteDto(
                "Juan Pérez Modificado", "123456789", "   ", "+573001234567",
                "Calle 123 #45-67", TipoCliente.NATURAL, 10.0
        );

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        // NO stub de existsByCedulaAndIdNot porque la cédula es la misma
        when(clienteRepository.save(any(Cliente.class))).thenReturn(cliente);
        when(clienteMapper.toDto(any(Cliente.class))).thenReturn(dtoCorreoBlanco);

        // Act
        ClienteDto resultado = clienteService.actualizarCliente(1L, dtoCorreoBlanco);

        // Assert
        assertNotNull(resultado);
        // No se deben llamar las validaciones de unicidad porque:
        // - La cédula es la misma (no necesita validación)
        // - El correo está en blanco (no necesita validación)
        verify(clienteRepository, never()).existsByCedulaAndIdNot(anyString(), anyLong());
        verify(clienteRepository, never()).existsByCorreoAndIdNot(anyString(), anyLong());
        verify(clienteRepository).save(cliente);
    }

    @Test
    @DisplayName("Debería actualizar cliente con nueva cédula validando duplicados")
    void testActualizarClienteConNuevaCedula() throws Exception {
        // Arrange
        ClienteDto dtoNuevaCedula = new ClienteDto(
                "Juan Pérez", "987654321", "juan@example.com",
                "+573001234567", "Calle 123 #45-67", TipoCliente.NATURAL, 10.0
        );

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(clienteRepository.existsByCedulaAndIdNot(anyString(), anyLong())).thenReturn(false);
        when(clienteRepository.save(any(Cliente.class))).thenReturn(cliente);
        when(clienteMapper.toDto(any(Cliente.class))).thenReturn(dtoNuevaCedula);

        // Act
        ClienteDto resultado = clienteService.actualizarCliente(1L, dtoNuevaCedula);

        // Assert
        assertNotNull(resultado);
        verify(clienteRepository).existsByCedulaAndIdNot("987654321", 1L);
        verify(clienteRepository, never()).existsByCorreoAndIdNot(anyString(), anyLong());
        verify(clienteRepository).save(cliente);
    }

    @Test
    @DisplayName("Debería actualizar cliente sin validaciones cuando cédula y correo son iguales")
    void testActualizarClienteSinCambiosEnCedulaYCorreo() throws Exception {
        // Arrange - Mismos datos que el cliente original
        ClienteDto dtoSinCambios = new ClienteDto(
                "Juan Pérez Modificado", "123456789", "juan@example.com",
                "+573001234567", "Nueva Dirección", TipoCliente.NATURAL, 12.0
        );

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        // No se necesitan stubs de existsBy... porque cédula y correo son iguales
        when(clienteRepository.save(any(Cliente.class))).thenReturn(cliente);
        when(clienteMapper.toDto(any(Cliente.class))).thenReturn(dtoSinCambios);

        // Act
        ClienteDto resultado = clienteService.actualizarCliente(1L, dtoSinCambios);

        // Assert
        assertNotNull(resultado);
        // No se deben llamar las validaciones de unicidad
        verify(clienteRepository, never()).existsByCedulaAndIdNot(anyString(), anyLong());
        verify(clienteRepository, never()).existsByCorreoAndIdNot(anyString(), anyLong());
        verify(clienteRepository).save(cliente);
    }

    @Test
    @DisplayName("Debería actualizar cliente con nueva cédula y correo nulo validando solo cédula")
    void testActualizarClienteConNuevaCedulaYCorreoNulo() throws Exception {
        // Arrange
        ClienteDto dtoNuevaCedulaCorreoNulo = new ClienteDto(
                "Juan Pérez", "987654321", null, "+573001234567",
                "Calle 123 #45-67", TipoCliente.NATURAL, 10.0
        );

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(clienteRepository.existsByCedulaAndIdNot(anyString(), anyLong())).thenReturn(false);
        when(clienteRepository.save(any(Cliente.class))).thenReturn(cliente);
        when(clienteMapper.toDto(any(Cliente.class))).thenReturn(dtoNuevaCedulaCorreoNulo);

        // Act
        ClienteDto resultado = clienteService.actualizarCliente(1L, dtoNuevaCedulaCorreoNulo);

        // Assert
        assertNotNull(resultado);
        // Solo se valida la cédula porque cambió
        verify(clienteRepository).existsByCedulaAndIdNot("987654321", 1L);
        // No se valida el correo porque es nulo
        verify(clienteRepository, never()).existsByCorreoAndIdNot(anyString(), anyLong());
        verify(clienteRepository).save(cliente);
    }

    @Test
    @DisplayName("Debería actualizar cliente con misma cédula y nuevo correo validando solo correo")
    void testActualizarClienteConMismaCedulaYNuevoCorreo() throws Exception {
        // Arrange
        ClienteDto dtoNuevoCorreo = new ClienteDto(
                "Juan Pérez", "123456789", "nuevo.correo@example.com",
                "+573001234567", "Calle 123 #45-67", TipoCliente.NATURAL, 10.0
        );

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(clienteRepository.existsByCorreoAndIdNot(anyString(), anyLong())).thenReturn(false);
        when(clienteRepository.save(any(Cliente.class))).thenReturn(cliente);
        when(clienteMapper.toDto(any(Cliente.class))).thenReturn(dtoNuevoCorreo);

        // Act
        ClienteDto resultado = clienteService.actualizarCliente(1L, dtoNuevoCorreo);

        // Assert
        assertNotNull(resultado);
        // No se valida la cédula porque es la misma
        verify(clienteRepository, never()).existsByCedulaAndIdNot(anyString(), anyLong());
        // Solo se valida el correo porque cambió
        verify(clienteRepository).existsByCorreoAndIdNot("nuevo.correo@example.com", 1L);
        verify(clienteRepository).save(cliente);
    }
}