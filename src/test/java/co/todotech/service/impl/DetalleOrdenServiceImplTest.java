package co.todotech.service.impl;

import co.todotech.exception.detalleorden.*;
import co.todotech.exception.ordenventa.OrdenNotFoundException;
import co.todotech.exception.producto.ProductoNotFoundException;
import co.todotech.mapper.DetalleOrdenMapper;
import co.todotech.model.dto.detalleorden.CreateDetalleOrdenDto;
import co.todotech.model.dto.detalleorden.DetalleOrdenDto;
import co.todotech.model.dto.detalleorden.EliminarDetalleRequest;
import co.todotech.model.entities.DetalleOrden;
import co.todotech.model.entities.Orden;
import co.todotech.model.entities.Producto;
import co.todotech.model.enums.EstadoOrden;
import co.todotech.model.enums.EstadoProducto;
import co.todotech.repository.DetalleOrdenRepository;
import co.todotech.repository.OrdenRepository;
import co.todotech.repository.ProductoRepository;
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

    @InjectMocks
    private DetalleOrdenServiceImpl detalleOrdenService;

    private CreateDetalleOrdenDto createDetalleOrdenDto;
    private DetalleOrdenDto detalleOrdenDto;
    private DetalleOrden detalleOrden;
    private Orden orden;
    private Producto producto;

    @BeforeEach
    void setUp() {
        // Configurar datos de prueba comunes
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

        createDetalleOrdenDto = new CreateDetalleOrdenDto(1L, 2); // productoId, cantidad

        detalleOrdenDto = new DetalleOrdenDto(
                1L,
                null, // ProductoDto se mockeará
                2,
                1500000.0,
                3000000.0
        );

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
    @DisplayName("Debería crear detalle de orden exitosamente cuando datos son válidos")
    void testCrearDetalleOrdenExitoso() {
        // Arrange
        when(ordenRepository.findById(1L)).thenReturn(Optional.of(orden));
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        when(detalleOrdenRepository.findByOrdenIdAndProductoId(1L, 1L)).thenReturn(Optional.empty());
        when(detalleOrdenRepository.save(any(DetalleOrden.class))).thenReturn(detalleOrden);
        when(detalleOrdenMapper.toDto(any(DetalleOrden.class))).thenReturn(detalleOrdenDto);

        // Act
        DetalleOrdenDto resultado = detalleOrdenService.crearDetalleOrden(createDetalleOrdenDto, 1L);

        // Assert
        assertNotNull(resultado);
        verify(ordenRepository).findById(1L);
        // Se llama 2 veces: una en crearDetalleOrden y otra en validarStockDisponible
        verify(productoRepository, times(2)).findById(1L);
        verify(detalleOrdenRepository).findByOrdenIdAndProductoId(1L, 1L);
        verify(detalleOrdenRepository).save(any(DetalleOrden.class));
        verify(ordenRepository).save(orden);
    }

    @Test
    @DisplayName("Debería crear detalle de orden cuando orden está en estado AGREGANDOPRODUCTOS")
    void testCrearDetalleOrdenConEstadoAgregandoProductos() {
        // Arrange
        orden.setEstado(EstadoOrden.AGREGANDOPRODUCTOS);

        when(ordenRepository.findById(1L)).thenReturn(Optional.of(orden));
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        when(detalleOrdenRepository.findByOrdenIdAndProductoId(1L, 1L)).thenReturn(Optional.empty());
        when(detalleOrdenRepository.save(any(DetalleOrden.class))).thenReturn(detalleOrden);
        when(detalleOrdenMapper.toDto(any(DetalleOrden.class))).thenReturn(detalleOrdenDto);

        // Act
        DetalleOrdenDto resultado = detalleOrdenService.crearDetalleOrden(createDetalleOrdenDto, 1L);

        // Assert
        assertNotNull(resultado);
        verify(ordenRepository).findById(1L);
        // Se llama 2 veces: una en crearDetalleOrden y otra en validarStockDisponible
        verify(productoRepository, times(2)).findById(1L);
        verify(detalleOrdenRepository).save(any(DetalleOrden.class));
    }

    @Test
    @DisplayName("Debería lanzar excepción cuando orden no existe")
    void testCrearDetalleOrdenConOrdenNoEncontrada() {
        // Arrange
        when(ordenRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        OrdenNotFoundException exception = assertThrows(OrdenNotFoundException.class, () -> {
            detalleOrdenService.crearDetalleOrden(createDetalleOrdenDto, 1L);
        });

        assertEquals("Orden no encontrada con ID: 1", exception.getMessage());
        verify(detalleOrdenRepository, never()).save(any(DetalleOrden.class));
    }

    @Test
    @DisplayName("Debería lanzar excepción cuando orden está en estado DISPONIBLEPARAPAGO")
    void testCrearDetalleOrdenConEstadoDisponibleParaPago() {
        // Arrange
        orden.setEstado(EstadoOrden.DISPONIBLEPARAPAGO);
        when(ordenRepository.findById(1L)).thenReturn(Optional.of(orden));

        // Act & Assert
        DetalleOrdenEstadoException exception = assertThrows(DetalleOrdenEstadoException.class, () -> {
            detalleOrdenService.crearDetalleOrden(createDetalleOrdenDto, 1L);
        });

        assertTrue(exception.getMessage().contains("Solo se pueden agregar detalles a órdenes en estado PENDIENTE o AGREGANDOPRODUCTOS"));
        verify(detalleOrdenRepository, never()).save(any(DetalleOrden.class));
    }

    @Test
    @DisplayName("Debería lanzar excepción cuando orden está en estado PAGADA")
    void testCrearDetalleOrdenConEstadoPagada() {
        // Arrange
        orden.setEstado(EstadoOrden.PAGADA);
        when(ordenRepository.findById(1L)).thenReturn(Optional.of(orden));

        // Act & Assert
        DetalleOrdenEstadoException exception = assertThrows(DetalleOrdenEstadoException.class, () -> {
            detalleOrdenService.crearDetalleOrden(createDetalleOrdenDto, 1L);
        });

        assertTrue(exception.getMessage().contains("Solo se pueden agregar detalles a órdenes en estado PENDIENTE o AGREGANDOPRODUCTOS"));
        verify(detalleOrdenRepository, never()).save(any(DetalleOrden.class));
    }

    @Test
    @DisplayName("Debería lanzar excepción cuando orden está en estado ENTREGADA")
    void testCrearDetalleOrdenConEstadoEntregada() {
        // Arrange
        orden.setEstado(EstadoOrden.ENTREGADA);
        when(ordenRepository.findById(1L)).thenReturn(Optional.of(orden));

        // Act & Assert
        DetalleOrdenEstadoException exception = assertThrows(DetalleOrdenEstadoException.class, () -> {
            detalleOrdenService.crearDetalleOrden(createDetalleOrdenDto, 1L);
        });

        assertTrue(exception.getMessage().contains("Solo se pueden agregar detalles a órdenes en estado PENDIENTE o AGREGANDOPRODUCTOS"));
        verify(detalleOrdenRepository, never()).save(any(DetalleOrden.class));
    }

    @Test
    @DisplayName("Debería lanzar excepción cuando orden está en estado CERRADA")
    void testCrearDetalleOrdenConEstadoCerrada() {
        // Arrange
        orden.setEstado(EstadoOrden.CERRADA);
        when(ordenRepository.findById(1L)).thenReturn(Optional.of(orden));

        // Act & Assert
        DetalleOrdenEstadoException exception = assertThrows(DetalleOrdenEstadoException.class, () -> {
            detalleOrdenService.crearDetalleOrden(createDetalleOrdenDto, 1L);
        });

        assertTrue(exception.getMessage().contains("Solo se pueden agregar detalles a órdenes en estado PENDIENTE o AGREGANDOPRODUCTOS"));
        verify(detalleOrdenRepository, never()).save(any(DetalleOrden.class));
    }

    @Test
    @DisplayName("Debería lanzar excepción cuando producto no existe")
    void testCrearDetalleOrdenConProductoNoEncontrado() {
        // Arrange
        when(ordenRepository.findById(1L)).thenReturn(Optional.of(orden));
        when(productoRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        ProductoNotFoundException exception = assertThrows(ProductoNotFoundException.class, () -> {
            detalleOrdenService.crearDetalleOrden(createDetalleOrdenDto, 1L);
        });

        assertEquals("Producto no encontrado con ID: 1", exception.getMessage());
        verify(detalleOrdenRepository, never()).save(any(DetalleOrden.class));
    }

    @Test
    @DisplayName("Debería lanzar excepción cuando stock es insuficiente")
    void testCrearDetalleOrdenConStockInsuficiente() {
        // Arrange
        producto.setStock(1); // Stock insuficiente para cantidad 2
        when(ordenRepository.findById(1L)).thenReturn(Optional.of(orden));
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));

        // Act & Assert
        DetalleOrdenBusinessException exception = assertThrows(DetalleOrdenBusinessException.class, () -> {
            detalleOrdenService.crearDetalleOrden(createDetalleOrdenDto, 1L);
        });

        assertTrue(exception.getMessage().contains("Stock insuficiente"));
        verify(detalleOrdenRepository, never()).save(any(DetalleOrden.class));
    }

    @Test
    @DisplayName("Debería lanzar excepción cuando producto está inactivo")
    void testCrearDetalleOrdenConProductoInactivo() {
        // Arrange
        producto.setEstado(EstadoProducto.INACTIVO);
        when(ordenRepository.findById(1L)).thenReturn(Optional.of(orden));
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));

        // Act & Assert
        DetalleOrdenBusinessException exception = assertThrows(DetalleOrdenBusinessException.class, () -> {
            detalleOrdenService.crearDetalleOrden(createDetalleOrdenDto, 1L);
        });

        assertTrue(exception.getMessage().contains("no está disponible para la venta"));
        verify(detalleOrdenRepository, never()).save(any(DetalleOrden.class));
    }

    @Test
    @DisplayName("Debería lanzar excepción cuando ya existe detalle para el mismo producto")
    void testCrearDetalleOrdenConDetalleDuplicado() {
        // Arrange
        when(ordenRepository.findById(1L)).thenReturn(Optional.of(orden));
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        when(detalleOrdenRepository.findByOrdenIdAndProductoId(1L, 1L)).thenReturn(Optional.of(detalleOrden));

        // Act & Assert
        DetalleOrdenDuplicateException exception = assertThrows(DetalleOrdenDuplicateException.class, () -> {
            detalleOrdenService.crearDetalleOrden(createDetalleOrdenDto, 1L);
        });

        assertEquals("Ya existe un detalle para el producto ID: 1 en la orden ID: 1. Use actualizar cantidad en su lugar.", exception.getMessage());
        verify(detalleOrdenRepository, never()).save(any(DetalleOrden.class));
    }

    @Test
    @DisplayName("Debería obtener detalle de orden por ID exitosamente")
    void testObtenerDetalleOrden() {
        // Arrange
        when(detalleOrdenRepository.findById(1L)).thenReturn(Optional.of(detalleOrden));
        when(detalleOrdenMapper.toDto(any(DetalleOrden.class))).thenReturn(detalleOrdenDto);

        // Act
        DetalleOrdenDto resultado = detalleOrdenService.obtenerDetalleOrden(1L);

        // Assert
        assertNotNull(resultado);
        verify(detalleOrdenRepository).findById(1L);
        verify(detalleOrdenMapper).toDto(detalleOrden);
    }

    @Test
    @DisplayName("Debería lanzar excepción cuando detalle no existe por ID")
    void testObtenerDetalleOrdenNoEncontrado() {
        // Arrange
        when(detalleOrdenRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        DetalleOrdenNotFoundException exception = assertThrows(DetalleOrdenNotFoundException.class, () -> {
            detalleOrdenService.obtenerDetalleOrden(1L);
        });

        assertEquals("Detalle de orden no encontrado con ID: 1", exception.getMessage());
        verify(detalleOrdenRepository).findById(1L);
    }

    @Test
    @DisplayName("Debería obtener detalles por orden exitosamente")
    void testObtenerDetallesPorOrden() {
        // Arrange
        List<DetalleOrden> detalles = Arrays.asList(detalleOrden);
        when(ordenRepository.existsById(1L)).thenReturn(true);
        when(detalleOrdenRepository.findByOrdenId(1L)).thenReturn(detalles);
        when(detalleOrdenMapper.toDto(any(DetalleOrden.class))).thenReturn(detalleOrdenDto);

        // Act
        List<DetalleOrdenDto> resultados = detalleOrdenService.obtenerDetallesPorOrden(1L);

        // Assert
        assertEquals(1, resultados.size());
        verify(ordenRepository).existsById(1L);
        verify(detalleOrdenRepository).findByOrdenId(1L);
    }

    @Test
    @DisplayName("Debería lanzar excepción cuando orden no existe al obtener detalles")
    void testObtenerDetallesPorOrdenNoEncontrada() {
        // Arrange
        when(ordenRepository.existsById(1L)).thenReturn(false);

        // Act & Assert
        OrdenNotFoundException exception = assertThrows(OrdenNotFoundException.class, () -> {
            detalleOrdenService.obtenerDetallesPorOrden(1L);
        });

        assertEquals("Orden no encontrada con ID: 1", exception.getMessage());
        verify(detalleOrdenRepository, never()).findByOrdenId(anyLong());
    }

    @Test
    @DisplayName("Debería lanzar excepción cuando no hay detalles para la orden")
    void testObtenerDetallesPorOrdenSinDetalles() {
        // Arrange
        when(ordenRepository.existsById(1L)).thenReturn(true);
        when(detalleOrdenRepository.findByOrdenId(1L)).thenReturn(Arrays.asList());

        // Act & Assert
        DetallesNoEncontradosException exception = assertThrows(DetallesNoEncontradosException.class, () -> {
            detalleOrdenService.obtenerDetallesPorOrden(1L);
        });

        assertEquals("No se encontraron detalles para la orden con ID: 1. La orden aún no tiene detalles agregados.", exception.getMessage());
        verify(ordenRepository).existsById(1L);
        verify(detalleOrdenRepository).findByOrdenId(1L);
    }

    @Test
    @DisplayName("Debería actualizar cantidad exitosamente")
    void testActualizarCantidad() {
        // Arrange
        Integer nuevaCantidad = 3;
        when(detalleOrdenRepository.findById(1L)).thenReturn(Optional.of(detalleOrden));
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto)); // Para validarStockDisponible
        when(detalleOrdenRepository.save(any(DetalleOrden.class))).thenReturn(detalleOrden);
        when(detalleOrdenMapper.toDto(any(DetalleOrden.class))).thenReturn(detalleOrdenDto);

        // Act
        DetalleOrdenDto resultado = detalleOrdenService.actualizarCantidad(1L, nuevaCantidad);

        // Assert
        assertNotNull(resultado);
        verify(detalleOrdenRepository).findById(1L);
        verify(productoRepository).findById(1L); // Para validarStockDisponible
        verify(detalleOrdenRepository).save(detalleOrden);
        verify(ordenRepository).save(orden);
    }

    @Test
    @DisplayName("Debería lanzar excepción cuando cantidad es cero o negativa")
    void testActualizarCantidadConCantidadInvalida() {
        // Act & Assert
        DetalleOrdenBusinessException exception = assertThrows(DetalleOrdenBusinessException.class, () -> {
            detalleOrdenService.actualizarCantidad(1L, 0);
        });

        assertEquals("La cantidad debe ser mayor a 0", exception.getMessage());
        verify(detalleOrdenRepository, never()).save(any(DetalleOrden.class));
    }

    @Test
    @DisplayName("Debería lanzar excepción cuando orden está en estado DISPONIBLEPARAPAGO al actualizar cantidad")
    void testActualizarCantidadConEstadoDisponibleParaPago() {
        // Arrange
        orden.setEstado(EstadoOrden.DISPONIBLEPARAPAGO);
        when(detalleOrdenRepository.findById(1L)).thenReturn(Optional.of(detalleOrden));

        // Act & Assert
        DetalleOrdenEstadoException exception = assertThrows(DetalleOrdenEstadoException.class, () -> {
            detalleOrdenService.actualizarCantidad(1L, 3);
        });

        assertTrue(exception.getMessage().contains("Solo se pueden modificar detalles de órdenes en estado PENDIENTE o AGREGANDOPRODUCTOS"));
        verify(detalleOrdenRepository, never()).save(any(DetalleOrden.class));
    }

    @Test
    @DisplayName("Debería lanzar excepción cuando orden está en estado PAGADA al actualizar cantidad")
    void testActualizarCantidadConEstadoPagada() {
        // Arrange
        orden.setEstado(EstadoOrden.PAGADA);
        when(detalleOrdenRepository.findById(1L)).thenReturn(Optional.of(detalleOrden));

        // Act & Assert
        DetalleOrdenEstadoException exception = assertThrows(DetalleOrdenEstadoException.class, () -> {
            detalleOrdenService.actualizarCantidad(1L, 3);
        });

        assertTrue(exception.getMessage().contains("Solo se pueden modificar detalles de órdenes en estado PENDIENTE o AGREGANDOPRODUCTOS"));
        verify(detalleOrdenRepository, never()).save(any(DetalleOrden.class));
    }

    @Test
    @DisplayName("Debería actualizar detalle de orden exitosamente")
    void testActualizarDetalleOrden() {
        // Arrange
        when(detalleOrdenRepository.findById(1L)).thenReturn(Optional.of(detalleOrden));
        when(detalleOrdenRepository.save(any(DetalleOrden.class))).thenReturn(detalleOrden);
        when(detalleOrdenMapper.toDto(any(DetalleOrden.class))).thenReturn(detalleOrdenDto);

        // Act
        DetalleOrdenDto resultado = detalleOrdenService.actualizarDetalleOrden(1L, detalleOrdenDto);

        // Assert
        assertNotNull(resultado);
        verify(detalleOrdenRepository).findById(1L);
        verify(detalleOrdenMapper).updateDetalleOrdenFromDto(detalleOrdenDto, detalleOrden);
        verify(detalleOrdenRepository).save(detalleOrden);
        verify(ordenRepository).save(orden);
    }

    @Test
    @DisplayName("Debería eliminar detalle de orden exitosamente")
    void testEliminarDetalleOrden() {
        // Arrange
        when(detalleOrdenRepository.findById(1L)).thenReturn(Optional.of(detalleOrden));
        doNothing().when(detalleOrdenRepository).delete(detalleOrden);

        // Act
        detalleOrdenService.eliminarDetalleOrden(1L);

        // Assert
        verify(detalleOrdenRepository).findById(1L);
        verify(detalleOrdenRepository).delete(detalleOrden);
        verify(ordenRepository).save(orden);
    }

    @Test
    @DisplayName("Debería lanzar excepción al eliminar detalle cuando orden está en estado ENTREGADA")
    void testEliminarDetalleOrdenConEstadoEntregada() {
        // Arrange
        orden.setEstado(EstadoOrden.ENTREGADA);
        when(detalleOrdenRepository.findById(1L)).thenReturn(Optional.of(detalleOrden));

        // Act & Assert
        DetalleOrdenEstadoException exception = assertThrows(DetalleOrdenEstadoException.class, () -> {
            detalleOrdenService.eliminarDetalleOrden(1L);
        });

        assertTrue(exception.getMessage().contains("Solo se pueden eliminar detalles de órdenes en estado PENDIENTE o AGREGANDOPRODUCTOS"));
        verify(detalleOrdenRepository, never()).delete(any(DetalleOrden.class));
    }

    @Test
    @DisplayName("Debería lanzar excepción al eliminar detalle cuando orden está en estado CERRADA")
    void testEliminarDetalleOrdenConEstadoCerrada() {
        // Arrange
        orden.setEstado(EstadoOrden.CERRADA);
        when(detalleOrdenRepository.findById(1L)).thenReturn(Optional.of(detalleOrden));

        // Act & Assert
        DetalleOrdenEstadoException exception = assertThrows(DetalleOrdenEstadoException.class, () -> {
            detalleOrdenService.eliminarDetalleOrden(1L);
        });

        assertTrue(exception.getMessage().contains("Solo se pueden eliminar detalles de órdenes en estado PENDIENTE o AGREGANDOPRODUCTOS"));
        verify(detalleOrdenRepository, never()).delete(any(DetalleOrden.class));
    }

    @Test
    @DisplayName("Debería eliminar detalle por producto y orden exitosamente")
    void testEliminarDetallePorProductoYOrden() {
        // Arrange
        EliminarDetalleRequest request = new EliminarDetalleRequest(1L, 1L);
        when(detalleOrdenRepository.findByOrdenIdAndProductoId(1L, 1L)).thenReturn(Optional.of(detalleOrden));
        when(detalleOrdenRepository.findById(1L)).thenReturn(Optional.of(detalleOrden)); // Para eliminarDetalleOrden
        doNothing().when(detalleOrdenRepository).delete(detalleOrden);

        // Act
        detalleOrdenService.eliminarDetallePorProductoYOrden(request);

        // Assert
        verify(detalleOrdenRepository).findByOrdenIdAndProductoId(1L, 1L);
        verify(detalleOrdenRepository).findById(1L); // Para eliminarDetalleOrden
        verify(detalleOrdenRepository).delete(detalleOrden);
        verify(ordenRepository).save(orden);
    }

    @Test
    @DisplayName("Debería lanzar excepción cuando no existe detalle para producto y orden")
    void testEliminarDetallePorProductoYOrdenNoEncontrado() {
        // Arrange
        EliminarDetalleRequest request = new EliminarDetalleRequest(1L, 1L);
        when(detalleOrdenRepository.findByOrdenIdAndProductoId(1L, 1L)).thenReturn(Optional.empty());

        // Act & Assert
        DetalleOrdenNotFoundException exception = assertThrows(DetalleOrdenNotFoundException.class, () -> {
            detalleOrdenService.eliminarDetallePorProductoYOrden(request);
        });

        assertEquals("Detalle de orden no encontrado para orden ID: 1 y producto ID: 1", exception.getMessage());
        verify(detalleOrdenRepository, never()).delete(any(DetalleOrden.class));
    }

    @Test
    @DisplayName("Debería validar stock disponible exitosamente")
    void testValidarStockDisponible() {
        // Arrange
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));

        // Act & Assert - No debe lanzar excepción
        assertDoesNotThrow(() -> {
            detalleOrdenService.validarStockDisponible(1L, 5);
        });

        verify(productoRepository).findById(1L);
    }

    @Test
    @DisplayName("Debería lanzar excepción cuando stock es insuficiente en validación")
    void testValidarStockDisponibleInsuficiente() {
        // Arrange
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));

        // Act & Assert
        DetalleOrdenBusinessException exception = assertThrows(DetalleOrdenBusinessException.class, () -> {
            detalleOrdenService.validarStockDisponible(1L, 15); // Stock disponible: 10
        });

        assertTrue(exception.getMessage().contains("Stock insuficiente"));
        verify(productoRepository).findById(1L);
    }

    @Test
    @DisplayName("Debería obtener productos disponibles")
    void testObtenerProductosDisponibles() {
        // Arrange
        List<Producto> productos = Arrays.asList(producto);
        when(productoRepository.findProductosDisponibles()).thenReturn(productos);

        // Act
        List<Producto> resultados = detalleOrdenService.obtenerProductosDisponibles();

        // Assert
        assertEquals(1, resultados.size());
        verify(productoRepository).findProductosDisponibles();
    }

    @Test
    @DisplayName("Debería obtener productos por estado")
    void testObtenerProductosPorEstado() {
        // Arrange
        List<Producto> productos = Arrays.asList(producto);
        when(productoRepository.findAllByEstado(EstadoProducto.ACTIVO)).thenReturn(productos);

        // Act
        List<Producto> resultados = detalleOrdenService.obtenerProductosPorEstado(EstadoProducto.ACTIVO);

        // Assert
        assertEquals(1, resultados.size());
        verify(productoRepository).findAllByEstado(EstadoProducto.ACTIVO);
    }

    @Test
    @DisplayName("Debería retornar true cuando producto está disponible")
    void testEsProductoDisponible() {
        // Arrange
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));

        // Act
        boolean disponible = detalleOrdenService.esProductoDisponible(1L);

        // Assert
        assertTrue(disponible);
        verify(productoRepository).findById(1L);
    }

    @Test
    @DisplayName("Debería retornar false cuando producto no existe")
    void testEsProductoDisponibleNoEncontrado() {
        // Arrange
        when(productoRepository.findById(1L)).thenReturn(Optional.empty());

        // Act
        boolean disponible = detalleOrdenService.esProductoDisponible(1L);

        // Assert
        assertFalse(disponible);
        verify(productoRepository).findById(1L);
    }

    @Test
    @DisplayName("Debería retornar false cuando producto está inactivo")
    void testEsProductoDisponibleInactivo() {
        // Arrange
        producto.setEstado(EstadoProducto.INACTIVO);
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));

        // Act
        boolean disponible = detalleOrdenService.esProductoDisponible(1L);

        // Assert
        assertFalse(disponible);
        verify(productoRepository).findById(1L);
    }

    @Test
    @DisplayName("Debería retornar false cuando producto no tiene stock")
    void testEsProductoDisponibleSinStock() {
        // Arrange
        producto.setStock(0);
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));

        // Act
        boolean disponible = detalleOrdenService.esProductoDisponible(1L);

        // Assert
        assertFalse(disponible);
        verify(productoRepository).findById(1L);
    }

    @Test
    @DisplayName("Debería obtener stock disponible")
    void testObtenerStockDisponible() {
        // Arrange
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));

        // Act
        Integer stock = detalleOrdenService.obtenerStockDisponible(1L);

        // Assert
        assertEquals(10, stock);
        verify(productoRepository).findById(1L);
    }

    @Test
    @DisplayName("Debería retornar cero cuando producto no existe")
    void testObtenerStockDisponibleNoEncontrado() {
        // Arrange
        when(productoRepository.findById(1L)).thenReturn(Optional.empty());

        // Act
        Integer stock = detalleOrdenService.obtenerStockDisponible(1L);

        // Assert
        assertEquals(0, stock);
        verify(productoRepository).findById(1L);
    }

    @Test
    @DisplayName("Debería lanzar excepción cuando detalle no existe al actualizar")
    void testActualizarDetalleOrdenNoEncontrado() {
        // Arrange
        when(detalleOrdenRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        DetalleOrdenNotFoundException exception = assertThrows(DetalleOrdenNotFoundException.class, () -> {
            detalleOrdenService.actualizarDetalleOrden(1L, detalleOrdenDto);
        });

        assertEquals("Detalle de orden no encontrado con ID: 1", exception.getMessage());
        verify(detalleOrdenRepository, never()).save(any(DetalleOrden.class));
    }

    @Test
    @DisplayName("Debería lanzar excepción cuando orden está en estado DISPONIBLEPARAPAGO al actualizar")
    void testActualizarDetalleOrdenConEstadoDisponibleParaPago() {
        // Arrange
        orden.setEstado(EstadoOrden.DISPONIBLEPARAPAGO);
        when(detalleOrdenRepository.findById(1L)).thenReturn(Optional.of(detalleOrden));

        // Act & Assert
        DetalleOrdenEstadoException exception = assertThrows(DetalleOrdenEstadoException.class, () -> {
            detalleOrdenService.actualizarDetalleOrden(1L, detalleOrdenDto);
        });

        assertTrue(exception.getMessage().contains("Solo se pueden modificar detalles de órdenes en estado PENDIENTE o AGREGANDOPRODUCTOS"));
        verify(detalleOrdenRepository, never()).save(any(DetalleOrden.class));
    }

    @Test
    @DisplayName("Debería lanzar excepción cuando orden está en estado ENTREGADA al actualizar")
    void testActualizarDetalleOrdenConEstadoEntregada() {
        // Arrange
        orden.setEstado(EstadoOrden.ENTREGADA);
        when(detalleOrdenRepository.findById(1L)).thenReturn(Optional.of(detalleOrden));

        // Act & Assert
        DetalleOrdenEstadoException exception = assertThrows(DetalleOrdenEstadoException.class, () -> {
            detalleOrdenService.actualizarDetalleOrden(1L, detalleOrdenDto);
        });

        assertTrue(exception.getMessage().contains("Solo se pueden modificar detalles de órdenes en estado PENDIENTE o AGREGANDOPRODUCTOS"));
        verify(detalleOrdenRepository, never()).save(any(DetalleOrden.class));
    }
}