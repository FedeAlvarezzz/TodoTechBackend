package co.todotech.service.impl;

import co.todotech.mapper.MetodoPagoMapper;
import co.todotech.model.dto.metodopago.MetodoPagoDto;
import co.todotech.model.entities.MetodoPago;
import co.todotech.model.enums.TipoMetodo;
import co.todotech.repository.MetodoPagoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MetodoPagoServiceImplTest {

    @Mock
    private MetodoPagoRepository metodoPagoRepository;

    @Mock
    private MetodoPagoMapper metodoPagoMapper;

    @InjectMocks
    private MetodoPagoServiceImpl metodoPagoService;

    private MetodoPagoDto metodoPagoDto;
    private MetodoPago metodoPago;

    @BeforeEach
    void setUp() {
        // Configurar datos de prueba comunes
        metodoPagoDto = new MetodoPagoDto(
                TipoMetodo.TARJETA,
                "Pago con tarjeta de crédito",
                true,
                2.5
        );

        metodoPago = MetodoPago.builder()
                .id(1L)
                .metodo(TipoMetodo.TARJETA)
                .descripcion("Pago con tarjeta de crédito")
                .aprobacion(true)
                .comision(2.5)
                .build();
    }

    @Test
    @DisplayName("Debería crear método de pago exitosamente cuando datos son válidos")
    void testCrearMetodoPagoExitoso() throws Exception {
        // Arrange
        when(metodoPagoRepository.existsByMetodo(any(TipoMetodo.class))).thenReturn(false);
        when(metodoPagoMapper.toEntity(any(MetodoPagoDto.class))).thenReturn(metodoPago);
        when(metodoPagoRepository.save(any(MetodoPago.class))).thenReturn(metodoPago);
        when(metodoPagoMapper.toDto(any(MetodoPago.class))).thenReturn(metodoPagoDto);

        // Act
        MetodoPagoDto resultado = metodoPagoService.crearMetodoPago(metodoPagoDto);

        // Assert
        assertNotNull(resultado);
        verify(metodoPagoRepository).existsByMetodo(TipoMetodo.TARJETA);
        verify(metodoPagoMapper).toEntity(metodoPagoDto);
        verify(metodoPagoRepository).save(metodoPago);
        verify(metodoPagoMapper).toDto(metodoPago);
    }

    @Test
    @DisplayName("Debería lanzar excepción cuando método de pago ya existe")
    void testCrearMetodoPagoConMetodoDuplicado() {
        // Arrange
        when(metodoPagoRepository.existsByMetodo(any(TipoMetodo.class))).thenReturn(true);

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            metodoPagoService.crearMetodoPago(metodoPagoDto);
        });

        assertEquals("Ya existe un método de pago con el tipo: TARJETA", exception.getMessage());
        verify(metodoPagoRepository, never()).save(any(MetodoPago.class));
    }

    @Test
    @DisplayName("Debería actualizar método de pago exitosamente")
    void testActualizarMetodoPagoExitoso() throws Exception {
        // Arrange
        MetodoPagoDto dtoActualizado = new MetodoPagoDto(
                TipoMetodo.TARJETA_DEBITO,
                "Pago con tarjeta de débito",
                true,
                1.5
        );

        when(metodoPagoRepository.findById(1L)).thenReturn(Optional.of(metodoPago));
        when(metodoPagoRepository.existsByMetodo(any(TipoMetodo.class))).thenReturn(false);
        when(metodoPagoRepository.save(any(MetodoPago.class))).thenReturn(metodoPago);
        when(metodoPagoMapper.toDto(any(MetodoPago.class))).thenReturn(dtoActualizado);

        // Act
        MetodoPagoDto resultado = metodoPagoService.actualizarMetodoPago(1L, dtoActualizado);

        // Assert
        assertNotNull(resultado);
        verify(metodoPagoRepository).findById(1L);
        verify(metodoPagoRepository).existsByMetodo(TipoMetodo.TARJETA_DEBITO);
        verify(metodoPagoMapper).updateMetodoPagoFromDto(dtoActualizado, metodoPago);
        verify(metodoPagoRepository).save(metodoPago);
    }

    @Test
    @DisplayName("Debería actualizar método de pago manteniendo mismo tipo sin validar duplicados")
    void testActualizarMetodoPagoMismoTipo() throws Exception {
        // Arrange
        MetodoPagoDto dtoMismoTipo = new MetodoPagoDto(
                TipoMetodo.TARJETA, // Mismo tipo
                "Descripción actualizada",
                true,
                2.0
        );

        when(metodoPagoRepository.findById(1L)).thenReturn(Optional.of(metodoPago));
        // No se llama a existsByMetodo porque el tipo es el mismo
        when(metodoPagoRepository.save(any(MetodoPago.class))).thenReturn(metodoPago);
        when(metodoPagoMapper.toDto(any(MetodoPago.class))).thenReturn(dtoMismoTipo);

        // Act
        MetodoPagoDto resultado = metodoPagoService.actualizarMetodoPago(1L, dtoMismoTipo);

        // Assert
        assertNotNull(resultado);
        verify(metodoPagoRepository, never()).existsByMetodo(any(TipoMetodo.class));
        verify(metodoPagoRepository).save(metodoPago);
    }

    @Test
    @DisplayName("Debería lanzar excepción al actualizar método de pago no encontrado")
    void testActualizarMetodoPagoNoEncontrado() {
        // Arrange
        when(metodoPagoRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            metodoPagoService.actualizarMetodoPago(1L, metodoPagoDto);
        });

        assertEquals("Método de pago no encontrado con ID: 1", exception.getMessage());
        verify(metodoPagoRepository, never()).save(any(MetodoPago.class));
    }

    @Test
    @DisplayName("Debería lanzar excepción al actualizar con tipo duplicado")
    void testActualizarMetodoPagoConTipoDuplicado() {
        // Arrange
        MetodoPagoDto dtoNuevoTipo = new MetodoPagoDto(
                TipoMetodo.EFECTIVO,
                "Pago en efectivo",
                true,
                0.0
        );

        when(metodoPagoRepository.findById(1L)).thenReturn(Optional.of(metodoPago));
        when(metodoPagoRepository.existsByMetodo(any(TipoMetodo.class))).thenReturn(true);

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            metodoPagoService.actualizarMetodoPago(1L, dtoNuevoTipo);
        });

        assertEquals("Ya existe otro método de pago con el tipo: EFECTIVO", exception.getMessage());
        verify(metodoPagoRepository, never()).save(any(MetodoPago.class));
    }

    @Test
    @DisplayName("Debería eliminar método de pago exitosamente")
    void testEliminarMetodoPago() throws Exception {
        // Arrange
        when(metodoPagoRepository.findById(1L)).thenReturn(Optional.of(metodoPago));
        doNothing().when(metodoPagoRepository).delete(metodoPago);

        // Act
        metodoPagoService.eliminarMetodoPago(1L);

        // Assert
        verify(metodoPagoRepository).findById(1L);
        verify(metodoPagoRepository).delete(metodoPago);
    }

    @Test
    @DisplayName("Debería lanzar excepción al eliminar método de pago no encontrado")
    void testEliminarMetodoPagoNoEncontrado() {
        // Arrange
        when(metodoPagoRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            metodoPagoService.eliminarMetodoPago(1L);
        });

        assertEquals("Método de pago no encontrado con ID: 1", exception.getMessage());
        verify(metodoPagoRepository, never()).delete(any(MetodoPago.class));
    }

    @Test
    @DisplayName("Debería obtener método de pago por ID exitosamente")
    void testObtenerMetodoPagoPorId() throws Exception {
        // Arrange
        when(metodoPagoRepository.findById(1L)).thenReturn(Optional.of(metodoPago));
        when(metodoPagoMapper.toDto(any(MetodoPago.class))).thenReturn(metodoPagoDto);

        // Act
        MetodoPagoDto resultado = metodoPagoService.obtenerMetodoPagoPorId(1L);

        // Assert
        assertNotNull(resultado);
        assertEquals(TipoMetodo.TARJETA, resultado.metodo());
        verify(metodoPagoRepository).findById(1L);
        verify(metodoPagoMapper).toDto(metodoPago);
    }

    @Test
    @DisplayName("Debería lanzar excepción cuando método de pago no existe por ID")
    void testObtenerMetodoPagoPorIdNoEncontrado() {
        // Arrange
        when(metodoPagoRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            metodoPagoService.obtenerMetodoPagoPorId(1L);
        });

        assertEquals("Método de pago no encontrado con ID: 1", exception.getMessage());
        verify(metodoPagoRepository).findById(1L);
    }

    @Test
    @DisplayName("Debería obtener métodos de pago por tipo")
    void testObtenerMetodosPagoPorTipo() {
        // Arrange
        List<MetodoPago> metodosPago = Arrays.asList(metodoPago);
        when(metodoPagoRepository.findByMetodo(TipoMetodo.TARJETA)).thenReturn(metodosPago);
        when(metodoPagoMapper.toDto(any(MetodoPago.class))).thenReturn(metodoPagoDto);

        // Act
        List<MetodoPagoDto> resultados = metodoPagoService.obtenerMetodosPagoPorTipo(TipoMetodo.TARJETA);

        // Assert
        assertEquals(1, resultados.size());
        verify(metodoPagoRepository).findByMetodo(TipoMetodo.TARJETA);
    }

    @Test
    @DisplayName("Debería obtener métodos de pago por aprobación")
    void testObtenerMetodosPagoPorAprobacion() {
        // Arrange
        List<MetodoPago> metodosPago = Arrays.asList(metodoPago);
        when(metodoPagoRepository.findByAprobacion(true)).thenReturn(metodosPago);
        when(metodoPagoMapper.toDto(any(MetodoPago.class))).thenReturn(metodoPagoDto);

        // Act
        List<MetodoPagoDto> resultados = metodoPagoService.obtenerMetodosPagoPorAprobacion(true);

        // Assert
        assertEquals(1, resultados.size());
        verify(metodoPagoRepository).findByAprobacion(true);
    }

    @Test
    @DisplayName("Debería obtener todos los métodos de pago")
    void testObtenerTodosLosMetodosPago() {
        // Arrange
        List<MetodoPago> metodosPago = Arrays.asList(metodoPago);
        when(metodoPagoRepository.findAll()).thenReturn(metodosPago);
        when(metodoPagoMapper.toDto(any(MetodoPago.class))).thenReturn(metodoPagoDto);

        // Act
        List<MetodoPagoDto> resultados = metodoPagoService.obtenerTodosLosMetodosPago();

        // Assert
        assertEquals(1, resultados.size());
        verify(metodoPagoRepository).findAll();
    }

    @Test
    @DisplayName("Debería obtener métodos de pago con comisión menor o igual")
    void testObtenerMetodosPagoConComisionMenorIgual() {
        // Arrange
        List<MetodoPago> metodosPago = Arrays.asList(metodoPago);
        when(metodoPagoRepository.findByComisionLessThanEqual(3.0)).thenReturn(metodosPago);
        when(metodoPagoMapper.toDto(any(MetodoPago.class))).thenReturn(metodoPagoDto);

        // Act
        List<MetodoPagoDto> resultados = metodoPagoService.obtenerMetodosPagoConComisionMenorIgual(3.0);

        // Assert
        assertEquals(1, resultados.size());
        verify(metodoPagoRepository).findByComisionLessThanEqual(3.0);
    }

    @Test
    @DisplayName("Debería obtener métodos aprobados ordenados por comisión")
    void testObtenerMetodosAprobadosOrdenadosPorComision() {
        // Arrange
        MetodoPago metodoEfectivo = MetodoPago.builder()
                .id(2L)
                .metodo(TipoMetodo.EFECTIVO)
                .descripcion("Pago en efectivo")
                .aprobacion(true)
                .comision(0.0)
                .build();

        MetodoPago metodoCredito = MetodoPago.builder()
                .id(3L)
                .metodo(TipoMetodo.CREDITO)
                .descripcion("Pago a crédito")
                .aprobacion(true)
                .comision(3.0)
                .build();

        List<MetodoPago> metodosPago = Arrays.asList(metodoEfectivo, metodoCredito);

        when(metodoPagoRepository.findMetodosAprobadosOrderByComision()).thenReturn(metodosPago);
        when(metodoPagoMapper.toDto(metodoEfectivo)).thenReturn(
                new MetodoPagoDto(TipoMetodo.EFECTIVO, "Pago en efectivo", true, 0.0)
        );
        when(metodoPagoMapper.toDto(metodoCredito)).thenReturn(
                new MetodoPagoDto(TipoMetodo.CREDITO, "Pago a crédito", true, 3.0)
        );

        // Act
        List<MetodoPagoDto> resultados = metodoPagoService.obtenerMetodosAprobadosOrdenadosPorComision();

        // Assert
        assertEquals(2, resultados.size());
        verify(metodoPagoRepository).findMetodosAprobadosOrderByComision();
    }

    @Test
    @DisplayName("Debería retornar lista vacía cuando no hay métodos de pago por tipo")
    void testObtenerMetodosPagoPorTipoListaVacia() {
        // Arrange
        when(metodoPagoRepository.findByMetodo(TipoMetodo.REDCOMPRA)).thenReturn(Arrays.asList());

        // Act
        List<MetodoPagoDto> resultados = metodoPagoService.obtenerMetodosPagoPorTipo(TipoMetodo.REDCOMPRA);

        // Assert
        assertTrue(resultados.isEmpty());
        verify(metodoPagoRepository).findByMetodo(TipoMetodo.REDCOMPRA);
    }

    @Test
    @DisplayName("Debería retornar lista vacía cuando no hay métodos de pago aprobados")
    void testObtenerMetodosPagoPorAprobacionListaVacia() {
        // Arrange
        when(metodoPagoRepository.findByAprobacion(false)).thenReturn(Arrays.asList());

        // Act
        List<MetodoPagoDto> resultados = metodoPagoService.obtenerMetodosPagoPorAprobacion(false);

        // Assert
        assertTrue(resultados.isEmpty());
        verify(metodoPagoRepository).findByAprobacion(false);
    }

    @Test
    @DisplayName("Debería retornar lista vacía cuando no hay métodos de pago con comisión menor")
    void testObtenerMetodosPagoConComisionMenorIgualListaVacia() {
        // Arrange
        when(metodoPagoRepository.findByComisionLessThanEqual(0.5)).thenReturn(Arrays.asList());

        // Act
        List<MetodoPagoDto> resultados = metodoPagoService.obtenerMetodosPagoConComisionMenorIgual(0.5);

        // Assert
        assertTrue(resultados.isEmpty());
        verify(metodoPagoRepository).findByComisionLessThanEqual(0.5);
    }

    @Test
    @DisplayName("Debería crear método de pago con diferentes tipos")
    void testCrearMetodoPagoConDiferentesTipos() throws Exception {
        // Arrange
        MetodoPagoDto dtoEfectivo = new MetodoPagoDto(
                TipoMetodo.EFECTIVO,
                "Pago en efectivo",
                true,
                0.0
        );

        MetodoPago metodoEfectivo = MetodoPago.builder()
                .id(2L)
                .metodo(TipoMetodo.EFECTIVO)
                .descripcion("Pago en efectivo")
                .aprobacion(true)
                .comision(0.0)
                .build();

        when(metodoPagoRepository.existsByMetodo(TipoMetodo.EFECTIVO)).thenReturn(false);
        when(metodoPagoMapper.toEntity(dtoEfectivo)).thenReturn(metodoEfectivo);
        when(metodoPagoRepository.save(metodoEfectivo)).thenReturn(metodoEfectivo);
        when(metodoPagoMapper.toDto(metodoEfectivo)).thenReturn(dtoEfectivo);

        // Act
        MetodoPagoDto resultado = metodoPagoService.crearMetodoPago(dtoEfectivo);

        // Assert
        assertNotNull(resultado);
        assertEquals(TipoMetodo.EFECTIVO, resultado.metodo());
        verify(metodoPagoRepository).existsByMetodo(TipoMetodo.EFECTIVO);
    }

    @Test
    @DisplayName("Debería manejar método de pago con comisión cero")
    void testCrearMetodoPagoConComisionCero() throws Exception {
        // Arrange
        MetodoPagoDto dtoComisionCero = new MetodoPagoDto(
                TipoMetodo.EFECTIVO,
                "Pago en efectivo",
                true,
                0.0
        );

        MetodoPago metodoComisionCero = MetodoPago.builder()
                .id(3L)
                .metodo(TipoMetodo.EFECTIVO)
                .descripcion("Pago en efectivo")
                .aprobacion(true)
                .comision(0.0)
                .build();

        when(metodoPagoRepository.existsByMetodo(TipoMetodo.EFECTIVO)).thenReturn(false);
        when(metodoPagoMapper.toEntity(dtoComisionCero)).thenReturn(metodoComisionCero);
        when(metodoPagoRepository.save(metodoComisionCero)).thenReturn(metodoComisionCero);
        when(metodoPagoMapper.toDto(metodoComisionCero)).thenReturn(dtoComisionCero);

        // Act
        MetodoPagoDto resultado = metodoPagoService.crearMetodoPago(dtoComisionCero);

        // Assert
        assertNotNull(resultado);
        assertEquals(0.0, resultado.comision());
        verify(metodoPagoRepository).existsByMetodo(TipoMetodo.EFECTIVO);
    }

    @Test
    @DisplayName("Debería manejar método de pago no aprobado")
    void testCrearMetodoPagoNoAprobado() throws Exception {
        // Arrange
        MetodoPagoDto dtoNoAprobado = new MetodoPagoDto(
                TipoMetodo.CREDITO,
                "Pago a crédito pendiente",
                false,
                5.0
        );

        MetodoPago metodoNoAprobado = MetodoPago.builder()
                .id(4L)
                .metodo(TipoMetodo.CREDITO)
                .descripcion("Pago a crédito pendiente")
                .aprobacion(false)
                .comision(5.0)
                .build();

        when(metodoPagoRepository.existsByMetodo(TipoMetodo.CREDITO)).thenReturn(false);
        when(metodoPagoMapper.toEntity(dtoNoAprobado)).thenReturn(metodoNoAprobado);
        when(metodoPagoRepository.save(metodoNoAprobado)).thenReturn(metodoNoAprobado);
        when(metodoPagoMapper.toDto(metodoNoAprobado)).thenReturn(dtoNoAprobado);

        // Act
        MetodoPagoDto resultado = metodoPagoService.crearMetodoPago(dtoNoAprobado);

        // Assert
        assertNotNull(resultado);
        assertFalse(resultado.aprobacion());
        verify(metodoPagoRepository).existsByMetodo(TipoMetodo.CREDITO);
    }
}