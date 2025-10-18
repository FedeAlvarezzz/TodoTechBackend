package co.todotech.service.impl;

import co.todotech.exception.producto.ProductoBusinessException;
import co.todotech.exception.producto.ProductoDuplicateException;
import co.todotech.exception.producto.ProductoNotFoundException;
import co.todotech.mapper.ProductoMapper;
import co.todotech.model.dto.producto.ProductoDto;
import co.todotech.model.entities.Categoria;
import co.todotech.model.entities.Producto;
import co.todotech.model.enums.EstadoProducto;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductoServiceImplTest {

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private ProductoMapper productoMapper;

    @InjectMocks
    private ProductoServiceImpl productoService;

    private ProductoDto productoDto;
    private Producto producto;
    private Categoria categoria;

    @BeforeEach
    void setUp() {
        // Configurar datos de prueba comunes
        categoria = Categoria.builder()
                .id(1L)
                .nombre("Electrónicos")
                .build();

        productoDto = new ProductoDto();
        productoDto.setNombre("Laptop Gaming");
        productoDto.setCodigo("LAP-001");
        productoDto.setDescripcion("Laptop para gaming");
        productoDto.setCategoria(categoria);
        productoDto.setPrecio(1500.0);
        productoDto.setStock(10);
        productoDto.setMarca("Dell");
        productoDto.setGarantia(12);
        productoDto.setImagenUrl("http://imagen.com/laptop.jpg");

        producto = Producto.builder()
                .id(1L)
                .nombre("Laptop Gaming")
                .codigo("LAP-001")
                .descripcion("Laptop para gaming")
                .categoria(categoria)
                .precio(1500.0)
                .stock(10)
                .marca("Dell")
                .garantia(12)
                .imagenUrl("http://imagen.com/laptop.jpg")
                .estado(EstadoProducto.ACTIVO)
                .build();
    }

    @Test
    @DisplayName("Debería crear producto exitosamente cuando datos son válidos")
    void testCrearProductoExitoso() {
        // Arrange
        when(productoRepository.existsByCodigo(anyString())).thenReturn(false);
        when(productoRepository.existsByNombre(anyString())).thenReturn(false);
        when(productoMapper.toEntity(any(ProductoDto.class))).thenReturn(producto);
        when(productoRepository.save(any(Producto.class))).thenReturn(producto);

        // Act
        productoService.crearProducto(productoDto);

        // Assert
        verify(productoRepository).existsByCodigo("LAP-001");
        verify(productoRepository).existsByNombre("Laptop Gaming");
        verify(productoMapper).toEntity(productoDto);
        verify(productoRepository).save(producto);
    }

    @Test
    @DisplayName("Debería lanzar excepción cuando código ya existe")
    void testCrearProductoConCodigoDuplicado() {
        // Arrange
        when(productoRepository.existsByCodigo(anyString())).thenReturn(true);

        // Act & Assert
        ProductoDuplicateException exception = assertThrows(ProductoDuplicateException.class, () -> {
            productoService.crearProducto(productoDto);
        });

        // CORRECCIÓN: Verificar el mensaje en lugar de getCampo() y getValor()
        assertEquals("Ya existe un producto con código: LAP-001", exception.getMessage());
        verify(productoRepository, never()).save(any(Producto.class));
    }

    @Test
    @DisplayName("Debería lanzar excepción cuando nombre ya existe")
    void testCrearProductoConNombreDuplicado() {
        // Arrange
        when(productoRepository.existsByCodigo(anyString())).thenReturn(false);
        when(productoRepository.existsByNombre(anyString())).thenReturn(true);

        // Act & Assert
        ProductoDuplicateException exception = assertThrows(ProductoDuplicateException.class, () -> {
            productoService.crearProducto(productoDto);
        });

        // CORRECCIÓN: Verificar el mensaje en lugar de getCampo() y getValor()
        assertEquals("Ya existe un producto con nombre: Laptop Gaming", exception.getMessage());
        verify(productoRepository, never()).save(any(Producto.class));
    }

    @Test
    @DisplayName("Debería lanzar excepción cuando precio es menor o igual a cero")
    void testCrearProductoConPrecioInvalido() {
        // Arrange
        productoDto.setPrecio(0.0);

        // Act & Assert
        ProductoBusinessException exception = assertThrows(ProductoBusinessException.class, () -> {
            productoService.crearProducto(productoDto);
        });

        assertEquals("El precio debe ser mayor a 0", exception.getMessage());
        verify(productoRepository, never()).save(any(Producto.class));
    }

    @Test
    @DisplayName("Debería obtener producto por ID exitosamente")
    void testObtenerProductoPorId() {
        // Arrange
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        when(productoMapper.toDto(any(Producto.class))).thenReturn(productoDto);

        // Act
        ProductoDto resultado = productoService.obtenerProductoPorId(1L);

        // Assert
        assertNotNull(resultado);
        assertEquals("Laptop Gaming", resultado.getNombre());
        verify(productoRepository).findById(1L);
        verify(productoMapper).toDto(producto);
    }

    @Test
    @DisplayName("Debería lanzar excepción cuando producto no existe por ID")
    void testObtenerProductoPorIdNoEncontrado() {
        // Arrange
        when(productoRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        ProductoNotFoundException exception = assertThrows(ProductoNotFoundException.class, () -> {
            productoService.obtenerProductoPorId(1L);
        });

        assertTrue(exception.getMessage().contains("1"));
        verify(productoRepository).findById(1L);
    }

    @Test
    @DisplayName("Debería obtener producto por código exitosamente")
    void testObtenerProductoPorCodigo() {
        // Arrange
        when(productoRepository.findByCodigo("LAP-001")).thenReturn(Optional.of(producto));
        when(productoMapper.toDto(any(Producto.class))).thenReturn(productoDto);

        // Act
        ProductoDto resultado = productoService.obtenerProductoPorCodigo("LAP-001");

        // Assert
        assertNotNull(resultado);
        assertEquals("LAP-001", resultado.getCodigo());
        verify(productoRepository).findByCodigo("LAP-001");
    }

    @Test
    @DisplayName("Debería lanzar excepción cuando código de búsqueda está vacío")
    void testObtenerProductoPorCodigoVacio() {
        // Act & Assert
        ProductoBusinessException exception = assertThrows(ProductoBusinessException.class, () -> {
            productoService.obtenerProductoPorCodigo("   ");
        });

        assertEquals("El código de búsqueda no puede estar vacío", exception.getMessage());
    }

    @Test
    @DisplayName("Debería actualizar producto exitosamente")
    void testActualizarProducto() {
        // Arrange
        ProductoDto dtoActualizado = new ProductoDto();
        dtoActualizado.setNombre("Laptop Gaming Pro");
        dtoActualizado.setCodigo("LAP-001");
        dtoActualizado.setPrecio(1600.0);
        dtoActualizado.setStock(15);
        dtoActualizado.setCategoria(categoria);

        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        // Solo mantener esta línea si es necesaria
        when(productoRepository.existsByNombreAndIdNot(anyString(), anyLong())).thenReturn(false);
        when(productoRepository.save(any(Producto.class))).thenReturn(producto);

        // Act
        productoService.actualizarProducto(1L, dtoActualizado);

        // Assert
        verify(productoRepository).findById(1L);
        verify(productoMapper).updateProductoFromDto(dtoActualizado, producto);
        verify(productoRepository).save(producto);
    }

    @Test
    @DisplayName("Debería lanzar excepción al actualizar con código duplicado")
    void testActualizarProductoConCodigoDuplicado() {
        // Arrange
        ProductoDto dtoActualizado = new ProductoDto();
        dtoActualizado.setCodigo("LAP-002"); // Código diferente al original
        dtoActualizado.setCategoria(categoria);

        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        when(productoRepository.existsByCodigoAndIdNot("LAP-002", 1L)).thenReturn(true);

        // Act & Assert
        ProductoDuplicateException exception = assertThrows(ProductoDuplicateException.class, () -> {
            productoService.actualizarProducto(1L, dtoActualizado);
        });

        assertEquals("Ya existe un producto con código: LAP-002", exception.getMessage());
    }

    @Test
    @DisplayName("Debería cambiar estado de ACTIVO a INACTIVO")
    void testCambiarEstadoProductoDeActivoAInactivo() {
        // Arrange
        producto.setEstado(EstadoProducto.ACTIVO);
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        when(productoRepository.save(any(Producto.class))).thenReturn(producto);

        // Act
        productoService.cambiarEstadoProducto(1L);

        // Assert
        verify(productoRepository).save(producto);
        // El estado debería cambiar a INACTIVO en el servicio
    }

    @Test
    @DisplayName("Debería eliminar producto exitosamente")
    void testEliminarProducto() {
        // Arrange
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        doNothing().when(productoRepository).delete(producto);

        // Act
        productoService.eliminarProducto(1L);

        // Assert
        verify(productoRepository).findById(1L);
        verify(productoRepository).delete(producto);
    }

    @Test
    @DisplayName("Debería obtener productos por estado")
    void testObtenerProductosPorEstado() {
        // Arrange
        List<Producto> productos = Arrays.asList(producto);
        when(productoRepository.findAllByEstado(EstadoProducto.ACTIVO)).thenReturn(productos);
        when(productoMapper.toDto(any(Producto.class))).thenReturn(productoDto);

        // Act
        List<ProductoDto> resultados = productoService.obtenerProductoPorEstado(EstadoProducto.ACTIVO);

        // Assert
        assertEquals(1, resultados.size());
        verify(productoRepository).findAllByEstado(EstadoProducto.ACTIVO);
    }

    @Test
    @DisplayName("Debería buscar productos por nombre")
    void testBuscarProductosPorNombre() {
        // Arrange
        List<Producto> productos = Arrays.asList(producto);
        when(productoRepository.findByNombreContainingIgnoreCase("Laptop")).thenReturn(productos);
        when(productoMapper.toDto(any(Producto.class))).thenReturn(productoDto);

        // Act
        List<ProductoDto> resultados = productoService.buscarProductosPorNombre("Laptop");

        // Assert
        assertEquals(1, resultados.size());
        verify(productoRepository).findByNombreContainingIgnoreCase("Laptop");
    }

    @Test
    @DisplayName("Debería obtener todos los productos")
    void testObtenerTodosLosProductos() {
        // Arrange
        List<Producto> productos = Arrays.asList(producto);
        when(productoRepository.findAll()).thenReturn(productos);
        when(productoMapper.toDto(any(Producto.class))).thenReturn(productoDto);

        // Act
        List<ProductoDto> resultados = productoService.obtenerTodosLosProductos();

        // Assert
        assertEquals(1, resultados.size());
        verify(productoRepository).findAll();
    }

    @Test
    @DisplayName("Debería obtener productos activos")
    void testObtenerProductosActivos() {
        // Arrange
        List<Producto> productos = Arrays.asList(producto);
        when(productoRepository.findAllByEstado(EstadoProducto.ACTIVO)).thenReturn(productos);
        when(productoMapper.toDto(any(Producto.class))).thenReturn(productoDto);

        // Act
        List<ProductoDto> resultados = productoService.obtenerProductosActivos();

        // Assert
        assertEquals(1, resultados.size());
        verify(productoRepository).findAllByEstado(EstadoProducto.ACTIVO);
    }

    @Test
    @DisplayName("Debería lanzar excepción cuando ID es nulo al obtener producto")
    void testObtenerProductoPorIdNulo() {
        // Act & Assert
        ProductoBusinessException exception = assertThrows(ProductoBusinessException.class, () -> {
            productoService.obtenerProductoPorId(null);
        });

        assertEquals("El ID del producto no puede ser nulo", exception.getMessage());
    }

    @Test
    @DisplayName("Debería lanzar excepción cuando categoría es nula al crear producto")
    void testCrearProductoConCategoriaNula() {
        // Arrange
        productoDto.setCategoria(null);

        // Act & Assert
        ProductoBusinessException exception = assertThrows(ProductoBusinessException.class, () -> {
            productoService.crearProducto(productoDto);
        });

        assertEquals("La categoría es obligatoria", exception.getMessage());
    }
}