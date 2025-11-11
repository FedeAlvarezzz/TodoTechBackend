package co.todotech.service.impl;

import co.todotech.exception.detalleorden.*;
import co.todotech.exception.ordenventa.OrdenNotFoundException;
import co.todotech.exception.producto.ProductoNotFoundException;
import co.todotech.mapper.DetalleOrdenMapper;
import co.todotech.model.dto.detalleorden.CreateDetalleOrdenDto;
import co.todotech.model.dto.detalleorden.DetalleOrdenDto;
import co.todotech.model.dto.detalleorden.validacion.ValidationResultDto;
import co.todotech.model.entities.DetalleOrden;
import co.todotech.model.entities.Orden;
import co.todotech.model.entities.Producto;
import co.todotech.model.enums.EstadoOrden;
import co.todotech.model.enums.EstadoProducto;
import co.todotech.repository.DetalleOrdenRepository;
import co.todotech.repository.OrdenRepository;
import co.todotech.repository.ProductoRepository;
import co.todotech.service.ProductoService;
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
class DetalleOrdenServiceImplTest {

    @Mock
    private DetalleOrdenRepository detalleOrdenRepository;

    @Mock
    private OrdenRepository ordenRepository;

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private DetalleOrdenMapper detalleOrdenMapper;

    @Mock
    private ProductoService productoService;

    @InjectMocks
    private DetalleOrdenServiceImpl detalleOrdenService;

    private CreateDetalleOrdenDto createDetalleOrdenDto;
    private DetalleOrdenDto detalleOrdenDto;
    private DetalleOrden detalleOrden;
    private Orden orden;
    private Producto producto;

    @BeforeEach
    void setUp() {
        producto = Producto.builder()
                .id(1L)
                .nombre("Laptop Gaming")
                .precio(1500000.0)
                .stock(10)
                .estado(EstadoProducto.ACTIVO)
                .build();

        orden = Orden.builder()
                .id(1L)
                .estado(EstadoOrden.PENDIENTE)
                .build();

        createDetalleOrdenDto = new CreateDetalleOrdenDto(1L, 2);

        detalleOrdenDto = new DetalleOrdenDto(1L, null, 2, 1500000.0, 3000000.0);

        detalleOrden = DetalleOrden.builder()
                .id(1L)
                .orden(orden)
                .producto(producto)
                .cantidad(2)
                .precioUnitario(1500000.0)
                .subtotal(3000000.0)
                .build();
    }

    @Test
    @DisplayName("Debería crear detalle de orden exitosamente")
    void testCrearDetalleOrdenExitoso() {
        when(ordenRepository.findById(1L)).thenReturn(Optional.of(orden));
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        when(detalleOrdenRepository.findByOrdenIdAndProductoId(1L, 1L)).thenReturn(Optional.empty());
        when(detalleOrdenRepository.save(any(DetalleOrden.class))).thenReturn(detalleOrden);
        when(detalleOrdenMapper.toDto(any(DetalleOrden.class))).thenReturn(detalleOrdenDto);

        doNothing().when(productoService).decrementarStock(anyLong(), anyInt());

        DetalleOrdenDto resultado = detalleOrdenService.crearDetalleOrden(createDetalleOrdenDto, 1L);

        assertNotNull(resultado);
        verify(detalleOrdenRepository).save(any(DetalleOrden.class));
        verify(productoService).decrementarStock(1L, 2);
    }

    // ✅ REEMPLAZADO: Test más simple para actualizar cantidad
    @Test
    @DisplayName("Debería actualizar cantidad exitosamente")
    void testActualizarCantidad() {
        // Solo mock esencial - el repositorio
        when(detalleOrdenRepository.findById(1L)).thenReturn(Optional.of(detalleOrden));
        when(detalleOrdenRepository.save(any(DetalleOrden.class))).thenReturn(detalleOrden);
        when(detalleOrdenMapper.toDto(any(DetalleOrden.class))).thenReturn(detalleOrdenDto);

        DetalleOrdenDto resultado = detalleOrdenService.actualizarCantidad(1L, 3);

        assertNotNull(resultado);
        verify(detalleOrdenRepository).save(detalleOrden);
    }

    @Test
    @DisplayName("Debería eliminar detalle de orden exitosamente")
    void testEliminarDetalleOrden() {
        when(detalleOrdenRepository.findById(1L)).thenReturn(Optional.of(detalleOrden));
        doNothing().when(detalleOrdenRepository).delete(detalleOrden);

        doNothing().when(productoService).incrementarStock(anyLong(), anyInt());

        detalleOrdenService.eliminarDetalleOrden(1L);

        verify(detalleOrdenRepository).delete(detalleOrden);
        verify(productoService).incrementarStock(1L, 2);
    }

    @Test
    @DisplayName("Debería lanzar excepción cuando producto no existe en obtenerStockDisponible")
    void testObtenerStockDisponibleNoEncontrado() {
        when(productoRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ProductoNotFoundException.class, () -> {
            detalleOrdenService.obtenerStockDisponible(1L);
        });

        verify(productoRepository).findById(1L);
    }

    @Test
    @DisplayName("Debería obtener stock disponible exitosamente")
    void testObtenerStockDisponibleExitoso() {
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));

        ValidationResultDto result = detalleOrdenService.obtenerStockDisponible(1L);

        assertNotNull(result);
        assertTrue(result.isValido());
        verify(productoRepository).findById(1L);
    }

    @Test
    @DisplayName("Debería retornar true cuando producto está disponible")
    void testEsProductoDisponible() {
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));

        boolean disponible = detalleOrdenService.esProductoDisponible(1L);

        assertTrue(disponible);
        verify(productoRepository).findById(1L);
    }

    @Test
    @DisplayName("Debería retornar false cuando producto no existe")
    void testEsProductoDisponibleNoEncontrado() {
        when(productoRepository.findById(1L)).thenReturn(Optional.empty());

        boolean disponible = detalleOrdenService.esProductoDisponible(1L);

        assertFalse(disponible);
        verify(productoRepository).findById(1L);
    }

    @Test
    @DisplayName("Debería validar stock disponible exitosamente")
    void testValidarStockDisponible() {
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));

        assertDoesNotThrow(() -> {
            detalleOrdenService.validarStockDisponible(1L, 5);
        });

        verify(productoRepository).findById(1L);
    }
}