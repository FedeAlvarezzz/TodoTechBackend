package co.todotech.service.impl;

import co.todotech.mapper.ClienteMapper;
import co.todotech.mapper.DetalleOrdenMapper;
import co.todotech.mapper.OrdenMapper;
import co.todotech.mapper.UsuarioMapper;
import co.todotech.model.dto.cliente.ClienteDto;
import co.todotech.model.dto.detalleorden.DetalleOrdenDto;
import co.todotech.model.dto.ordenventa.CreateOrdenDto;
import co.todotech.model.dto.ordenventa.OrdenConDetallesDto;
import co.todotech.model.dto.ordenventa.OrdenDto;
import co.todotech.model.dto.usuario.UsuarioDto;
import co.todotech.model.entities.Cliente;
import co.todotech.model.entities.DetalleOrden;
import co.todotech.model.entities.Orden;
import co.todotech.model.entities.Usuario;
import co.todotech.model.enums.EstadoOrden;
import co.todotech.model.enums.TipoCliente;
import co.todotech.model.enums.TipoUsuario;
import co.todotech.repository.ClienteRepository;
import co.todotech.repository.OrdenRepository;
import co.todotech.repository.UsuarioRepository;
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
class OrdenServiceImplTest {

    @Mock
    private OrdenRepository ordenRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private OrdenMapper ordenMapper;

    @Mock
    private DetalleOrdenMapper detalleOrdenMapper;

    @Mock
    private ClienteMapper clienteMapper;

    @Mock
    private UsuarioMapper usuarioMapper;

    @InjectMocks
    private OrdenServiceImpl ordenService;

    private CreateOrdenDto createOrdenDto;
    private OrdenDto ordenDto;
    private OrdenConDetallesDto ordenConDetallesDto;
    private Orden orden;
    private Cliente cliente;
    private Usuario vendedor;
    private ClienteDto clienteDto;
    private UsuarioDto vendedorDto;
    private DetalleOrden detalleOrden;
    private DetalleOrdenDto detalleOrdenDto;

    @BeforeEach
    void setUp() {
        // Configurar datos de prueba comunes
        cliente = Cliente.builder()
                .id(1L)
                .nombre("Juan Pérez")
                .cedula("123456789")
                .correo("juan@example.com")
                .telefono("3001234567")
                .direccion("Calle 123")
                .tipoCliente(TipoCliente.NATURAL)
                .descuentoAplicable(5.0)
                .build();

        vendedor = Usuario.builder()
                .id(1L)
                .nombre("Carlos Vendedor")
                .cedula("987654321")
                .correo("carlos@empresa.com")
                .telefono("3007654321")
                .nombreUsuario("cvendedor")
                .tipoUsuario(TipoUsuario.VENDEDOR)
                .estado(true)
                .build();

        clienteDto = new ClienteDto(
                1L,
                "Juan Pérez",
                "123456789",
                "juan@example.com",
                "3001234567",
                "Calle 123",
                LocalDateTime.now(),
                TipoCliente.NATURAL,
                5.0
        );

        vendedorDto = new UsuarioDto();
        vendedorDto.setId(1L);
        vendedorDto.setNombre("Carlos Vendedor");
        vendedorDto.setCedula("987654321");
        vendedorDto.setCorreo("carlos@empresa.com");
        vendedorDto.setTelefono("3007654321");
        vendedorDto.setNombreUsuario("cvendedor");
        vendedorDto.setTipoUsuario(TipoUsuario.VENDEDOR);
        vendedorDto.setEstado(true);

        createOrdenDto = new CreateOrdenDto(1L, 1L, 10.0);

        orden = Orden.builder()
                .id(1L)
                .numeroOrden("ORD-20231201-ABC123")
                .fecha(LocalDateTime.now())
                .cliente(cliente)
                .vendedor(vendedor)
                .estado(EstadoOrden.PENDIENTE)
                .subtotal(1000.0)
                .descuento(10.0)
                .impuestos(19.8) // 2% sobre (1000 - 10) = 990
                .total(1009.8)
                .observaciones("Orden de prueba")
                .build();

        ordenDto = new OrdenDto(
                1L,
                "ORD-20231201-ABC123",
                LocalDateTime.now(),
                clienteDto,
                vendedorDto,
                EstadoOrden.PENDIENTE,
                1000.0,
                10.0,
                19.8,
                1009.8,
                "Orden de prueba"
        );

        detalleOrden = DetalleOrden.builder()
                .id(1L)
                .orden(orden)
                .cantidad(2)
                .precioUnitario(500.0)
                .subtotal(1000.0)
                .build();

        detalleOrdenDto = new DetalleOrdenDto(
                1L,
                null, // ProductoDto mockeado
                2,
                500.0,
                1000.0
        );

        ordenConDetallesDto = new OrdenConDetallesDto(
                1L,
                "ORD-20231201-ABC123",
                LocalDateTime.now(),
                clienteDto,
                vendedorDto,
                Arrays.asList(detalleOrdenDto),
                EstadoOrden.PENDIENTE,
                1000.0,
                10.0,
                19.8,
                1009.8,
                "Orden de prueba"
        );

        // Agregar detalles a la orden
        orden.setProductos(Arrays.asList(detalleOrden));
    }

    @Test
    @DisplayName("Debería crear orden exitosamente cuando datos son válidos")
    void testCrearOrdenExitoso() {
        // Arrange
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(vendedor));
        when(ordenRepository.save(any(Orden.class))).thenReturn(orden);
        when(ordenMapper.toDto(any(Orden.class))).thenReturn(ordenDto);

        // Act
        OrdenDto resultado = ordenService.crearOrden(createOrdenDto);

        // Assert
        assertNotNull(resultado);
        assertEquals(1L, resultado.id());
        verify(clienteRepository).findById(1L);
        verify(usuarioRepository).findById(1L);
        verify(ordenRepository).save(any(Orden.class));
        verify(ordenMapper).toDto(any(Orden.class));
    }

    @Test
    @DisplayName("Debería crear orden con descuento cero cuando no se proporciona")
    void testCrearOrdenSinDescuento() {
        // Arrange
        CreateOrdenDto createSinDescuento = new CreateOrdenDto(1L, 1L, null);
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(vendedor));
        when(ordenRepository.save(any(Orden.class))).thenReturn(orden);
        when(ordenMapper.toDto(any(Orden.class))).thenReturn(ordenDto);

        // Act
        OrdenDto resultado = ordenService.crearOrden(createSinDescuento);

        // Assert
        assertNotNull(resultado);
        verify(ordenRepository).save(argThat(orden -> orden.getDescuento() == 0.0));
    }

    @Test
    @DisplayName("Debería lanzar excepción cuando cliente no existe")
    void testCrearOrdenConClienteNoEncontrado() {
        // Arrange
        when(clienteRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            ordenService.crearOrden(createOrdenDto);
        });

        assertEquals("Cliente no encontrado con ID: 1", exception.getMessage());
        verify(ordenRepository, never()).save(any(Orden.class));
    }

    @Test
    @DisplayName("Debería lanzar excepción cuando vendedor no existe")
    void testCrearOrdenConVendedorNoEncontrado() {
        // Arrange
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            ordenService.crearOrden(createOrdenDto);
        });

        assertEquals("Vendedor no encontrado con ID: 1", exception.getMessage());
        verify(ordenRepository, never()).save(any(Orden.class));
    }

    @Test
    @DisplayName("Debería obtener orden con detalles exitosamente")
    void testObtenerOrdenConDetalles() {
        // Arrange
        when(ordenRepository.findByIdWithDetalles(1L)).thenReturn(Optional.of(orden));
        when(detalleOrdenMapper.toDto(any(DetalleOrden.class))).thenReturn(detalleOrdenDto);
        when(clienteMapper.toDto(cliente)).thenReturn(clienteDto);
        when(usuarioMapper.toDtoSafe(vendedor)).thenReturn(vendedorDto);

        // Act
        OrdenConDetallesDto resultado = ordenService.obtenerOrdenConDetalles(1L);

        // Assert
        assertNotNull(resultado);
        assertEquals(1L, resultado.id());
        verify(ordenRepository).findByIdWithDetalles(1L);
        verify(detalleOrdenMapper).toDto(any(DetalleOrden.class));
    }

    @Test
    @DisplayName("Debería lanzar excepción cuando orden no existe al obtener con detalles")
    void testObtenerOrdenConDetallesNoEncontrada() {
        // Arrange
        when(ordenRepository.findByIdWithDetalles(1L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            ordenService.obtenerOrdenConDetalles(1L);
        });

        assertEquals("Orden no encontrada con ID: 1", exception.getMessage());
    }

    @Test
    @DisplayName("Debería obtener orden por ID exitosamente")
    void testObtenerOrden() {
        // Arrange
        when(ordenRepository.findById(1L)).thenReturn(Optional.of(orden));
        when(ordenMapper.toDto(orden)).thenReturn(ordenDto);

        // Act
        OrdenDto resultado = ordenService.obtenerOrden(1L);

        // Assert
        assertNotNull(resultado);
        assertEquals(1L, resultado.id());
        verify(ordenRepository).findById(1L);
        verify(ordenMapper).toDto(orden);
    }

    @Test
    @DisplayName("Debería lanzar excepción cuando orden no existe")
    void testObtenerOrdenNoEncontrada() {
        // Arrange
        when(ordenRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            ordenService.obtenerOrden(1L);
        });

        assertEquals("Orden no encontrada con ID: 1", exception.getMessage());
    }

    @Test
    @DisplayName("Debería obtener todas las órdenes exitosamente")
    void testObtenerTodasLasOrdenes() {
        // Arrange
        List<Orden> ordenes = Arrays.asList(orden);
        when(ordenRepository.findAll()).thenReturn(ordenes);
        when(ordenMapper.toDto(orden)).thenReturn(ordenDto);

        // Act
        List<OrdenDto> resultados = ordenService.obtenerTodasLasOrdenes();

        // Assert
        assertEquals(1, resultados.size());
        verify(ordenRepository).findAll();
        verify(ordenMapper).toDto(orden);
    }

    @Test
    @DisplayName("Debería obtener órdenes por cliente exitosamente")
    void testObtenerOrdenesPorCliente() {
        // Arrange
        List<Orden> ordenes = Arrays.asList(orden);
        when(ordenRepository.findByClienteId(1L)).thenReturn(ordenes);
        when(ordenMapper.toDto(orden)).thenReturn(ordenDto);

        // Act
        List<OrdenDto> resultados = ordenService.obtenerOrdenesPorCliente(1L);

        // Assert
        assertEquals(1, resultados.size());
        verify(ordenRepository).findByClienteId(1L);
        verify(ordenMapper).toDto(orden);
    }

    @Test
    @DisplayName("Debería actualizar orden exitosamente cuando estado no es CERRADA")
    void testActualizarOrdenExitoso() {
        // Arrange
        when(ordenRepository.findById(1L)).thenReturn(Optional.of(orden));
        when(ordenRepository.save(any(Orden.class))).thenReturn(orden);
        when(ordenMapper.toDto(any(Orden.class))).thenReturn(ordenDto);

        // Act
        OrdenDto resultado = ordenService.actualizarOrden(1L, ordenDto);

        // Assert
        assertNotNull(resultado);
        verify(ordenRepository).findById(1L);
        verify(ordenMapper).updateOrdenFromDto(ordenDto, orden);
        verify(ordenRepository).save(orden);
    }

    @Test
    @DisplayName("Debería lanzar excepción al actualizar orden en estado CERRADA")
    void testActualizarOrdenConEstadoCerrada() {
        // Arrange
        orden.setEstado(EstadoOrden.CERRADA);
        when(ordenRepository.findById(1L)).thenReturn(Optional.of(orden));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            ordenService.actualizarOrden(1L, ordenDto);
        });

        assertEquals("No se puede modificar una orden en estado CERRADA", exception.getMessage());
        verify(ordenRepository, never()).save(any(Orden.class));
    }

    @Test
    @DisplayName("Debería actualizar estado de orden exitosamente con transición válida")
    void testActualizarEstadoOrdenExitoso() {
        // Arrange
        when(ordenRepository.findById(1L)).thenReturn(Optional.of(orden));
        when(ordenRepository.save(any(Orden.class))).thenReturn(orden);
        when(ordenMapper.toDto(any(Orden.class))).thenReturn(ordenDto);

        // Act
        OrdenDto resultado = ordenService.actualizarEstadoOrden(1L, EstadoOrden.AGREGANDOPRODUCTOS);

        // Assert
        assertNotNull(resultado);
        verify(ordenRepository).findById(1L);
        verify(ordenRepository).save(orden);
    }

    @Test
    @DisplayName("Debería lanzar excepción al actualizar estado de orden CERRADA")
    void testActualizarEstadoOrdenConEstadoCerrada() {
        // Arrange
        orden.setEstado(EstadoOrden.CERRADA);
        when(ordenRepository.findById(1L)).thenReturn(Optional.of(orden));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            ordenService.actualizarEstadoOrden(1L, EstadoOrden.PAGADA);
        });

        assertEquals("No se puede modificar el estado de una orden CERRADA", exception.getMessage());
        verify(ordenRepository, never()).save(any(Orden.class));
    }

    @Test
    @DisplayName("Debería lanzar excepción al saltar de PENDIENTE a ENTREGADA")
    void testActualizarEstadoOrdenSaltoInvalidoPendienteAEntregada() {
        // Arrange
        when(ordenRepository.findById(1L)).thenReturn(Optional.of(orden));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            ordenService.actualizarEstadoOrden(1L, EstadoOrden.ENTREGADA);
        });

        assertTrue(exception.getMessage().contains("No se puede saltar de PENDIENTE a ENTREGADA"));
        verify(ordenRepository, never()).save(any(Orden.class));
    }

    @Test
    @DisplayName("Debería eliminar orden exitosamente cuando está en estado PENDIENTE")
    void testEliminarOrdenExitoso() {
        // Arrange
        when(ordenRepository.findById(1L)).thenReturn(Optional.of(orden));
        doNothing().when(ordenRepository).delete(orden);

        // Act
        ordenService.eliminarOrden(1L);

        // Assert
        verify(ordenRepository).findById(1L);
        verify(ordenRepository).delete(orden);
    }

    @Test
    @DisplayName("Debería lanzar excepción al eliminar orden que no está en estado PENDIENTE")
    void testEliminarOrdenConEstadoNoPendiente() {
        // Arrange
        orden.setEstado(EstadoOrden.PAGADA);
        when(ordenRepository.findById(1L)).thenReturn(Optional.of(orden));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            ordenService.eliminarOrden(1L);
        });

        assertEquals("Solo se pueden eliminar órdenes en estado PENDIENTE. Estado actual: PAGADA", exception.getMessage());
        verify(ordenRepository, never()).delete(any(Orden.class));
    }

    @Test
    @DisplayName("Debería aplicar descuento exitosamente cuando datos son válidos")
    void testAplicarDescuentoExitoso() {
        // Arrange
        Double porcentajeDescuento = 10.0;
        when(ordenRepository.findById(1L)).thenReturn(Optional.of(orden));
        when(ordenRepository.save(any(Orden.class))).thenReturn(orden);
        when(ordenMapper.toDto(any(Orden.class))).thenReturn(ordenDto);

        // Act
        OrdenDto resultado = ordenService.aplicarDescuento(1L, porcentajeDescuento);

        // Assert
        assertNotNull(resultado);
        verify(ordenRepository).findById(1L);
        verify(ordenRepository).save(orden);
    }

    @Test
    @DisplayName("Debería lanzar excepción al aplicar descuento a orden no PENDIENTE")
    void testAplicarDescuentoConEstadoNoPendiente() {
        // Arrange
        orden.setEstado(EstadoOrden.PAGADA);
        when(ordenRepository.findById(1L)).thenReturn(Optional.of(orden));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            ordenService.aplicarDescuento(1L, 10.0);
        });

        assertEquals("Solo se pueden aplicar descuentos a órdenes en estado PENDIENTE", exception.getMessage());
        verify(ordenRepository, never()).save(any(Orden.class));
    }

    @Test
    @DisplayName("Debería lanzar excepción al aplicar descuento con porcentaje inválido")
    void testAplicarDescuentoConPorcentajeInvalido() {
        // Arrange
        when(ordenRepository.findById(1L)).thenReturn(Optional.of(orden));

        // Act & Assert - Porcentaje negativo
        RuntimeException exceptionNegativo = assertThrows(RuntimeException.class, () -> {
            ordenService.aplicarDescuento(1L, -5.0);
        });
        assertEquals("El porcentaje de descuento debe estar entre 0 y 100", exceptionNegativo.getMessage());

        // Act & Assert - Porcentaje mayor a 100
        RuntimeException exceptionMayor = assertThrows(RuntimeException.class, () -> {
            ordenService.aplicarDescuento(1L, 150.0);
        });
        assertEquals("El porcentaje de descuento debe estar entre 0 y 100", exceptionMayor.getMessage());

        verify(ordenRepository, never()).save(any(Orden.class));
    }

    @Test
    @DisplayName("Debería obtener órdenes por estado exitosamente")
    void testObtenerOrdenesPorEstado() {
        // Arrange
        List<Orden> ordenes = Arrays.asList(orden);
        when(ordenRepository.findByEstado(EstadoOrden.PENDIENTE)).thenReturn(ordenes);
        when(ordenMapper.toDto(orden)).thenReturn(ordenDto);

        // Act
        List<OrdenDto> resultados = ordenService.obtenerOrdenesPorEstado(EstadoOrden.PENDIENTE);

        // Assert
        assertEquals(1, resultados.size());
        verify(ordenRepository).findByEstado(EstadoOrden.PENDIENTE);
        verify(ordenMapper).toDto(orden);
    }

    @Test
    @DisplayName("Debería marcar orden como pagada exitosamente")
    void testMarcarComoPagada() {
        // Arrange
        when(ordenRepository.findById(1L)).thenReturn(Optional.of(orden));
        when(ordenRepository.save(any(Orden.class))).thenReturn(orden);
        when(ordenMapper.toDto(any(Orden.class))).thenReturn(ordenDto);

        // Act
        OrdenDto resultado = ordenService.marcarComoPagada(1L);

        // Assert
        assertNotNull(resultado);
        verify(ordenRepository).findById(1L);
        verify(ordenRepository).save(argThat(orden -> orden.getEstado() == EstadoOrden.PAGADA));
    }

    @Test
    @DisplayName("Debería marcar orden como entregada exitosamente")
    void testMarcarComoEntregada() {
        // Arrange
        orden.setEstado(EstadoOrden.PAGADA); // Estado previo necesario
        when(ordenRepository.findById(1L)).thenReturn(Optional.of(orden));
        when(ordenRepository.save(any(Orden.class))).thenReturn(orden);
        when(ordenMapper.toDto(any(Orden.class))).thenReturn(ordenDto);

        // Act
        OrdenDto resultado = ordenService.marcarComoEntregada(1L);

        // Assert
        assertNotNull(resultado);
        verify(ordenRepository).findById(1L);
        verify(ordenRepository).save(argThat(orden -> orden.getEstado() == EstadoOrden.ENTREGADA));
    }

    @Test
    @DisplayName("Debería marcar orden como cerrada exitosamente")
    void testMarcarComoCerrada() {
        // Arrange
        orden.setEstado(EstadoOrden.ENTREGADA); // Estado previo necesario
        when(ordenRepository.findById(1L)).thenReturn(Optional.of(orden));
        when(ordenRepository.save(any(Orden.class))).thenReturn(orden);
        when(ordenMapper.toDto(any(Orden.class))).thenReturn(ordenDto);

        // Act
        OrdenDto resultado = ordenService.marcarComoCerrada(1L);

        // Assert
        assertNotNull(resultado);
        verify(ordenRepository).findById(1L);
        verify(ordenRepository).save(argThat(orden -> orden.getEstado() == EstadoOrden.CERRADA));
    }

    @Test
    @DisplayName("Debería obtener órdenes por vendedor exitosamente")
    void testObtenerOrdenesPorVendedor() {
        // Arrange
        List<Orden> ordenes = Arrays.asList(orden);
        when(usuarioRepository.existsById(1L)).thenReturn(true);
        when(ordenRepository.findByVendedorId(1L)).thenReturn(ordenes);
        when(ordenMapper.toDto(orden)).thenReturn(ordenDto);

        // Act
        List<OrdenDto> resultados = ordenService.obtenerOrdenesPorVendedor(1L);

        // Assert
        assertEquals(1, resultados.size());
        verify(usuarioRepository).existsById(1L);
        verify(ordenRepository).findByVendedorId(1L);
        verify(ordenMapper).toDto(orden);
    }

    @Test
    @DisplayName("Debería lanzar excepción al obtener órdenes por vendedor no existente")
    void testObtenerOrdenesPorVendedorNoEncontrado() {
        // Arrange
        when(usuarioRepository.existsById(1L)).thenReturn(false);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            ordenService.obtenerOrdenesPorVendedor(1L);
        });

        assertEquals("Vendedor no encontrado con ID: 1", exception.getMessage());
        verify(ordenRepository, never()).findByVendedorId(anyLong());
    }

    @Test
    @DisplayName("Debería marcar orden como agregando productos exitosamente")
    void testMarcarComoAgregandoProductos() {
        // Arrange
        when(ordenRepository.findById(1L)).thenReturn(Optional.of(orden));
        when(ordenRepository.save(any(Orden.class))).thenReturn(orden);
        when(ordenMapper.toDto(any(Orden.class))).thenReturn(ordenDto);

        // Act
        OrdenDto resultado = ordenService.marcarComoAgregandoProductos(1L);

        // Assert
        assertNotNull(resultado);
        verify(ordenRepository).findById(1L);
        verify(ordenRepository).save(argThat(orden -> orden.getEstado() == EstadoOrden.AGREGANDOPRODUCTOS));
    }

    @Test
    @DisplayName("Debería marcar orden como disponible para pago exitosamente")
    void testMarcarComoDisponibleParaPago() {
        // Arrange
        when(ordenRepository.findById(1L)).thenReturn(Optional.of(orden));
        when(ordenRepository.save(any(Orden.class))).thenReturn(orden);
        when(ordenMapper.toDto(any(Orden.class))).thenReturn(ordenDto);

        // Act
        OrdenDto resultado = ordenService.marcarComoDisponibleParaPago(1L);

        // Assert
        assertNotNull(resultado);
        verify(ordenRepository).findById(1L);
        verify(ordenRepository).save(argThat(orden -> orden.getEstado() == EstadoOrden.DISPONIBLEPARAPAGO));
    }

    @Test
    @DisplayName("Debería lanzar excepción al intentar retroceder estado de ENTREGADA a PAGADA")
    void testValidarTransicionEstadoRetrocesoInvalido() {
        // Arrange
        orden.setEstado(EstadoOrden.ENTREGADA);
        when(ordenRepository.findById(1L)).thenReturn(Optional.of(orden));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            ordenService.actualizarEstadoOrden(1L, EstadoOrden.PAGADA);
        });

        assertTrue(exception.getMessage().contains("No se puede retroceder el estado de la orden"));
        verify(ordenRepository, never()).save(any(Orden.class));
    }

    @Test
    @DisplayName("Debería generar número de orden con formato correcto")
    void testGenerarNumeroOrden() throws Exception {
        // Usar reflexión para probar el método privado
        var method = OrdenServiceImpl.class.getDeclaredMethod("generarNumeroOrden");
        method.setAccessible(true);

        // Act
        String numeroOrden = (String) method.invoke(ordenService);

        // Assert
        assertNotNull(numeroOrden);
        assertTrue(numeroOrden.startsWith("ORD-"));
        assertTrue(numeroOrden.length() > 15); // Formato: ORD-YYYYMMDD-UUID(8)
    }
}