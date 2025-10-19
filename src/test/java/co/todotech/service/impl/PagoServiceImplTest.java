package co.todotech.service.impl;

import co.todotech.mapper.PagoMapper;
import co.todotech.model.dto.pago.PagoDto;
import co.todotech.model.entities.*;
import co.todotech.model.enums.EstadoPago;
import co.todotech.model.enums.TipoMetodo;
import co.todotech.model.enums.TipoUsuario;
import co.todotech.repository.MetodoPagoRepository;
import co.todotech.repository.OrdenRepository;
import co.todotech.repository.PagoRepository;
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
class PagoServiceImplTest {

    @Mock
    private PagoRepository pagoRepository;

    @Mock
    private PagoMapper pagoMapper;

    @Mock
    private OrdenRepository ordenVentaRepository;

    @Mock
    private MetodoPagoRepository metodoPagoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private PagoServiceImpl pagoService;

    private PagoDto pagoDto;
    private Pago pago;
    private Orden ordenVenta;
    private MetodoPago metodoPago;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        // Configurar datos de prueba comunes
        ordenVenta = Orden.builder()
                .id(1L)
                .build();

        metodoPago = MetodoPago.builder()
                .id(1L)
                .metodo(TipoMetodo.TARJETA)
                .aprobacion(true)
                .comision(2.5)
                .build();

        usuario = Usuario.builder()
                .id(1L)
                .nombre("Juan Pérez")
                .tipoUsuario(TipoUsuario.VENDEDOR)
                .build();

        pagoDto = new PagoDto(
                1L, // ordenVentaId
                150000.0, // monto
                1L, // metodoPagoId
                "TRX-123456", // numeroTransaccion
                1L, // usuarioId
                "comprobante.jpg", // comprobante
                EstadoPago.APROBADO // estadoPago
        );

        pago = Pago.builder()
                .id(1L)
                .ordenVenta(ordenVenta)
                .monto(150000.0)
                .metodoPago(metodoPago)
                .numeroTransaccion("TRX-123456")
                .fechaPago(LocalDateTime.now())
                .usuario(usuario)
                .comprobante("comprobante.jpg")
                .estadoPago(EstadoPago.APROBADO)
                .build();
    }

    @Test
    @DisplayName("Debería crear pago exitosamente cuando datos son válidos")
    void testCrearPagoExitoso() throws Exception {
        // Arrange
        when(ordenVentaRepository.findById(1L)).thenReturn(Optional.of(ordenVenta));
        when(metodoPagoRepository.findById(1L)).thenReturn(Optional.of(metodoPago));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(pagoRepository.findByNumeroTransaccion("TRX-123456")).thenReturn(Optional.empty());
        when(pagoMapper.toEntity(any(PagoDto.class))).thenReturn(pago);
        when(pagoRepository.save(any(Pago.class))).thenReturn(pago);
        when(pagoMapper.toDto(any(Pago.class))).thenReturn(pagoDto);

        // Act
        PagoDto resultado = pagoService.crearPago(pagoDto);

        // Assert
        assertNotNull(resultado);
        verify(ordenVentaRepository).findById(1L);
        verify(metodoPagoRepository).findById(1L);
        verify(usuarioRepository).findById(1L);
        verify(pagoRepository).findByNumeroTransaccion("TRX-123456");
        verify(pagoMapper).toEntity(pagoDto);
        verify(pagoRepository).save(pago);
        verify(pagoMapper).toDto(pago);
    }

    @Test
    @DisplayName("Debería crear pago sin número de transacción")
    void testCrearPagoSinNumeroTransaccion() throws Exception {
        // Arrange
        PagoDto dtoSinTransaccion = new PagoDto(
                1L, 150000.0, 1L, null, 1L, "comprobante.jpg", EstadoPago.APROBADO
        );

        when(ordenVentaRepository.findById(1L)).thenReturn(Optional.of(ordenVenta));
        when(metodoPagoRepository.findById(1L)).thenReturn(Optional.of(metodoPago));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(pagoMapper.toEntity(any(PagoDto.class))).thenReturn(pago);
        when(pagoRepository.save(any(Pago.class))).thenReturn(pago);
        when(pagoMapper.toDto(any(Pago.class))).thenReturn(dtoSinTransaccion);

        // Act
        PagoDto resultado = pagoService.crearPago(dtoSinTransaccion);

        // Assert
        assertNotNull(resultado);
        verify(pagoRepository, never()).findByNumeroTransaccion(anyString());
        verify(pagoRepository).save(pago);
    }

    @Test
    @DisplayName("Debería lanzar excepción cuando orden de venta no existe")
    void testCrearPagoConOrdenVentaNoEncontrada() {
        // Arrange
        when(ordenVentaRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            pagoService.crearPago(pagoDto);
        });

        assertEquals("Orden de venta no encontrada con ID: 1", exception.getMessage());
        verify(pagoRepository, never()).save(any(Pago.class));
    }

    @Test
    @DisplayName("Debería lanzar excepción cuando método de pago no existe")
    void testCrearPagoConMetodoPagoNoEncontrado() {
        // Arrange
        when(ordenVentaRepository.findById(1L)).thenReturn(Optional.of(ordenVenta));
        when(metodoPagoRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            pagoService.crearPago(pagoDto);
        });

        assertEquals("Método de pago no encontrado con ID: 1", exception.getMessage());
        verify(pagoRepository, never()).save(any(Pago.class));
    }

    @Test
    @DisplayName("Debería lanzar excepción cuando usuario no existe")
    void testCrearPagoConUsuarioNoEncontrado() {
        // Arrange
        when(ordenVentaRepository.findById(1L)).thenReturn(Optional.of(ordenVenta));
        when(metodoPagoRepository.findById(1L)).thenReturn(Optional.of(metodoPago));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            pagoService.crearPago(pagoDto);
        });

        assertEquals("Usuario no encontrado con ID: 1", exception.getMessage());
        verify(pagoRepository, never()).save(any(Pago.class));
    }

    @Test
    @DisplayName("Debería lanzar excepción cuando número de transacción ya existe")
    void testCrearPagoConNumeroTransaccionDuplicado() {
        // Arrange
        when(ordenVentaRepository.findById(1L)).thenReturn(Optional.of(ordenVenta));
        when(metodoPagoRepository.findById(1L)).thenReturn(Optional.of(metodoPago));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(pagoRepository.findByNumeroTransaccion("TRX-123456")).thenReturn(Optional.of(pago));

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            pagoService.crearPago(pagoDto);
        });

        assertTrue(exception.getCause().getMessage().contains("Ya existe un pago con el número de transacción: TRX-123456"));
        verify(pagoRepository, never()).save(any(Pago.class));
    }

    @Test
    @DisplayName("Debería actualizar pago exitosamente")
    void testActualizarPagoExitoso() throws Exception {
        // Arrange
        PagoDto dtoActualizado = new PagoDto(
                2L, // nueva ordenVentaId
                200000.0, // nuevo monto
                2L, // nuevo metodoPagoId
                "TRX-789012", // nuevo numeroTransaccion
                2L, // nuevo usuarioId
                "nuevo_comprobante.jpg", // nuevo comprobante
                EstadoPago.RECHAZADO // nuevo estadoPago
        );

        Orden nuevaOrdenVenta = Orden.builder().id(2L).build();
        MetodoPago nuevoMetodoPago = MetodoPago.builder().id(2L).build();
        Usuario nuevoUsuario = Usuario.builder().id(2L).build();

        when(pagoRepository.findById(1L)).thenReturn(Optional.of(pago));
        when(ordenVentaRepository.findById(2L)).thenReturn(Optional.of(nuevaOrdenVenta));
        when(metodoPagoRepository.findById(2L)).thenReturn(Optional.of(nuevoMetodoPago));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(nuevoUsuario));
        when(pagoRepository.findByNumeroTransaccion("TRX-789012")).thenReturn(Optional.empty());
        when(pagoRepository.save(any(Pago.class))).thenReturn(pago);
        when(pagoMapper.toDto(any(Pago.class))).thenReturn(dtoActualizado);

        // Act
        PagoDto resultado = pagoService.actualizarPago(1L, dtoActualizado);

        // Assert
        assertNotNull(resultado);
        verify(pagoRepository).findById(1L);
        verify(ordenVentaRepository).findById(2L);
        verify(metodoPagoRepository).findById(2L);
        verify(usuarioRepository).findById(2L);
        verify(pagoRepository).findByNumeroTransaccion("TRX-789012");
        verify(pagoMapper).updatePagoFromDto(dtoActualizado, pago);
        verify(pagoRepository).save(pago);
    }

    @Test
    @DisplayName("Debería actualizar pago sin cambiar referencias")
    void testActualizarPagoSinCambiarReferencias() throws Exception {
        // Arrange
        PagoDto dtoMismasReferencias = new PagoDto(
                1L, // misma ordenVentaId
                200000.0, // nuevo monto
                1L, // mismo metodoPagoId
                "TRX-123456", // mismo numeroTransaccion
                1L, // mismo usuarioId
                "nuevo_comprobante.jpg", // nuevo comprobante
                EstadoPago.RECHAZADO // nuevo estadoPago
        );

        when(pagoRepository.findById(1L)).thenReturn(Optional.of(pago));
        // No se llaman a los repositorios de referencias porque son las mismas
        when(pagoRepository.save(any(Pago.class))).thenReturn(pago);
        when(pagoMapper.toDto(any(Pago.class))).thenReturn(dtoMismasReferencias);

        // Act
        PagoDto resultado = pagoService.actualizarPago(1L, dtoMismasReferencias);

        // Assert
        assertNotNull(resultado);
        verify(pagoRepository).findById(1L);
        verify(ordenVentaRepository, never()).findById(anyLong());
        verify(metodoPagoRepository, never()).findById(anyLong());
        verify(usuarioRepository, never()).findById(anyLong());
        verify(pagoRepository, never()).findByNumeroTransaccion(anyString());
        verify(pagoRepository).save(pago);
    }

    @Test
    @DisplayName("Debería eliminar pago exitosamente")
    void testEliminarPago() throws Exception {
        // Arrange
        when(pagoRepository.findById(1L)).thenReturn(Optional.of(pago));
        doNothing().when(pagoRepository).delete(pago);

        // Act
        pagoService.eliminarPago(1L);

        // Assert
        verify(pagoRepository).findById(1L);
        verify(pagoRepository).delete(pago);
    }

    @Test
    @DisplayName("Debería lanzar excepción al eliminar pago no encontrado")
    void testEliminarPagoNoEncontrado() {
        // Arrange
        when(pagoRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            pagoService.eliminarPago(1L);
        });

        assertEquals("Pago no encontrado con ID: 1", exception.getMessage());
        verify(pagoRepository, never()).delete(any(Pago.class));
    }

    @Test
    @DisplayName("Debería obtener pago por ID exitosamente")
    void testObtenerPagoPorId() throws Exception {
        // Arrange
        when(pagoRepository.findById(1L)).thenReturn(Optional.of(pago));
        when(pagoMapper.toDto(any(Pago.class))).thenReturn(pagoDto);

        // Act
        PagoDto resultado = pagoService.obtenerPagoPorId(1L);

        // Assert
        assertNotNull(resultado);
        assertEquals(150000.0, resultado.monto());
        verify(pagoRepository).findById(1L);
        verify(pagoMapper).toDto(pago);
    }

    @Test
    @DisplayName("Debería lanzar excepción cuando pago no existe por ID")
    void testObtenerPagoPorIdNoEncontrado() {
        // Arrange
        when(pagoRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            pagoService.obtenerPagoPorId(1L);
        });

        assertEquals("Pago no encontrado con ID: 1", exception.getMessage());
        verify(pagoRepository).findById(1L);
    }

    @Test
    @DisplayName("Debería obtener pagos por orden de venta")
    void testObtenerPagosPorOrdenVenta() {
        // Arrange
        List<Pago> pagos = Arrays.asList(pago);
        when(pagoRepository.findByOrdenVentaId(1L)).thenReturn(pagos);
        when(pagoMapper.toDto(any(Pago.class))).thenReturn(pagoDto);

        // Act
        List<PagoDto> resultados = pagoService.obtenerPagosPorOrdenVenta(1L);

        // Assert
        assertEquals(1, resultados.size());
        verify(pagoRepository).findByOrdenVentaId(1L);
    }

    @Test
    @DisplayName("Debería obtener pagos por estado")
    void testObtenerPagosPorEstado() {
        // Arrange
        List<Pago> pagos = Arrays.asList(pago);
        when(pagoRepository.findByEstadoPago(EstadoPago.APROBADO)).thenReturn(pagos);
        when(pagoMapper.toDto(any(Pago.class))).thenReturn(pagoDto);

        // Act
        List<PagoDto> resultados = pagoService.obtenerPagosPorEstado(EstadoPago.APROBADO);

        // Assert
        assertEquals(1, resultados.size());
        verify(pagoRepository).findByEstadoPago(EstadoPago.APROBADO);
    }

    @Test
    @DisplayName("Debería obtener pagos por usuario")
    void testObtenerPagosPorUsuario() {
        // Arrange
        List<Pago> pagos = Arrays.asList(pago);
        when(pagoRepository.findByUsuarioId(1L)).thenReturn(pagos);
        when(pagoMapper.toDto(any(Pago.class))).thenReturn(pagoDto);

        // Act
        List<PagoDto> resultados = pagoService.obtenerPagosPorUsuario(1L);

        // Assert
        assertEquals(1, resultados.size());
        verify(pagoRepository).findByUsuarioId(1L);
    }

    @Test
    @DisplayName("Debería obtener pagos por fecha")
    void testObtenerPagosPorFecha() {
        // Arrange
        LocalDateTime fechaInicio = LocalDateTime.now().minusDays(7);
        LocalDateTime fechaFin = LocalDateTime.now();
        List<Pago> pagos = Arrays.asList(pago);
        when(pagoRepository.findByFechaPagoBetween(fechaInicio, fechaFin)).thenReturn(pagos);
        when(pagoMapper.toDto(any(Pago.class))).thenReturn(pagoDto);

        // Act
        List<PagoDto> resultados = pagoService.obtenerPagosPorFecha(fechaInicio, fechaFin);

        // Assert
        assertEquals(1, resultados.size());
        verify(pagoRepository).findByFechaPagoBetween(fechaInicio, fechaFin);
    }

    @Test
    @DisplayName("Debería obtener pagos por monto mínimo")
    void testObtenerPagosPorMontoMinimo() {
        // Arrange
        List<Pago> pagos = Arrays.asList(pago);
        when(pagoRepository.findByMontoGreaterThanEqual(100000.0)).thenReturn(pagos);
        when(pagoMapper.toDto(any(Pago.class))).thenReturn(pagoDto);

        // Act
        List<PagoDto> resultados = pagoService.obtenerPagosPorMontoMinimo(100000.0);

        // Assert
        assertEquals(1, resultados.size());
        verify(pagoRepository).findByMontoGreaterThanEqual(100000.0);
    }

    @Test
    @DisplayName("Debería obtener pagos aprobados por orden de venta")
    void testObtenerPagosAprobadosPorOrdenVenta() {
        // Arrange
        List<Pago> pagos = Arrays.asList(pago);
        when(pagoRepository.findPagosAprobadosByOrdenVenta(1L)).thenReturn(pagos);
        when(pagoMapper.toDto(any(Pago.class))).thenReturn(pagoDto);

        // Act
        List<PagoDto> resultados = pagoService.obtenerPagosAprobadosPorOrdenVenta(1L);

        // Assert
        assertEquals(1, resultados.size());
        verify(pagoRepository).findPagosAprobadosByOrdenVenta(1L);
    }

    @Test
    @DisplayName("Debería obtener total de pagos aprobados por orden de venta")
    void testObtenerTotalPagosAprobadosPorOrdenVenta() {
        // Arrange
        when(pagoRepository.sumMontoAprobadoByOrdenVenta(1L)).thenReturn(150000.0);

        // Act
        Double total = pagoService.obtenerTotalPagosAprobadosPorOrdenVenta(1L);

        // Assert
        assertEquals(150000.0, total);
        verify(pagoRepository).sumMontoAprobadoByOrdenVenta(1L);
    }

    @Test
    @DisplayName("Debería retornar cero cuando no hay pagos aprobados")
    void testObtenerTotalPagosAprobadosPorOrdenVentaCero() {
        // Arrange
        when(pagoRepository.sumMontoAprobadoByOrdenVenta(1L)).thenReturn(null);

        // Act
        Double total = pagoService.obtenerTotalPagosAprobadosPorOrdenVenta(1L);

        // Assert
        assertEquals(0.0, total);
        verify(pagoRepository).sumMontoAprobadoByOrdenVenta(1L);
    }

    @Test
    @DisplayName("Debería obtener todos los pagos")
    void testObtenerTodosLosPagos() {
        // Arrange
        List<Pago> pagos = Arrays.asList(pago);
        when(pagoRepository.findAllOrderByFechaPagoDesc()).thenReturn(pagos);
        when(pagoMapper.toDto(any(Pago.class))).thenReturn(pagoDto);

        // Act
        List<PagoDto> resultados = pagoService.obtenerTodosLosPagos();

        // Assert
        assertEquals(1, resultados.size());
        verify(pagoRepository).findAllOrderByFechaPagoDesc();
    }

    @Test
    @DisplayName("Debería obtener pago por número de transacción exitosamente")
    void testObtenerPagoPorNumeroTransaccion() throws Exception {
        // Arrange
        when(pagoRepository.findByNumeroTransaccion("TRX-123456")).thenReturn(Optional.of(pago));
        when(pagoMapper.toDto(any(Pago.class))).thenReturn(pagoDto);

        // Act
        PagoDto resultado = pagoService.obtenerPagoPorNumeroTransaccion("TRX-123456");

        // Assert
        assertNotNull(resultado);
        assertEquals("TRX-123456", resultado.numeroTransaccion());
        verify(pagoRepository).findByNumeroTransaccion("TRX-123456");
        verify(pagoMapper).toDto(pago);
    }

    @Test
    @DisplayName("Debería lanzar excepción cuando pago no existe por número de transacción")
    void testObtenerPagoPorNumeroTransaccionNoEncontrado() {
        // Arrange
        when(pagoRepository.findByNumeroTransaccion("TRX-999999")).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            pagoService.obtenerPagoPorNumeroTransaccion("TRX-999999");
        });

        assertEquals("Pago no encontrado con número de transacción: TRX-999999", exception.getMessage());
        verify(pagoRepository).findByNumeroTransaccion("TRX-999999");
    }

    @Test
    @DisplayName("Debería retornar lista vacía cuando no hay pagos por orden de venta")
    void testObtenerPagosPorOrdenVentaListaVacia() {
        // Arrange
        when(pagoRepository.findByOrdenVentaId(999L)).thenReturn(Arrays.asList());

        // Act
        List<PagoDto> resultados = pagoService.obtenerPagosPorOrdenVenta(999L);

        // Assert
        assertTrue(resultados.isEmpty());
        verify(pagoRepository).findByOrdenVentaId(999L);
    }

    @Test
    @DisplayName("Debería crear pago con estado pendiente por defecto")
    void testCrearPagoConEstadoPendiente() throws Exception {
        // Arrange
        PagoDto dtoSinEstado = new PagoDto(
                1L, 150000.0, 1L, "TRX-123456", 1L, "comprobante.jpg", null
        );

        Pago pagoPendiente = Pago.builder()
                .id(1L)
                .ordenVenta(ordenVenta)
                .monto(150000.0)
                .metodoPago(metodoPago)
                .numeroTransaccion("TRX-123456")
                .fechaPago(LocalDateTime.now())
                .usuario(usuario)
                .comprobante("comprobante.jpg")
                .estadoPago(EstadoPago.PENDIENTE) // Estado por defecto
                .build();

        when(ordenVentaRepository.findById(1L)).thenReturn(Optional.of(ordenVenta));
        when(metodoPagoRepository.findById(1L)).thenReturn(Optional.of(metodoPago));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(pagoRepository.findByNumeroTransaccion("TRX-123456")).thenReturn(Optional.empty());
        when(pagoMapper.toEntity(any(PagoDto.class))).thenReturn(pagoPendiente);
        when(pagoRepository.save(any(Pago.class))).thenReturn(pagoPendiente);
        when(pagoMapper.toDto(any(Pago.class))).thenReturn(dtoSinEstado);

        // Act
        PagoDto resultado = pagoService.crearPago(dtoSinEstado);

        // Assert
        assertNotNull(resultado);
        verify(pagoRepository).save(pagoPendiente);
    }
}