package co.todotech.service.impl;

import co.todotech.mapper.CategoriaMapper;
import co.todotech.model.dto.categoria.CategoriaDto;
import co.todotech.model.entities.Categoria;
import co.todotech.repository.CategoriaRepository;
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
class CategoriaServiceImplTest {

    @Mock
    private CategoriaRepository categoriaRepository;

    @Mock
    private CategoriaMapper categoriaMapper;

    @InjectMocks
    private CategoriaServiceImpl categoriaService;

    private CategoriaDto categoriaDto;
    private Categoria categoria;

    @BeforeEach
    void setUp() {
        // Configurar datos de prueba comunes
        categoriaDto = new CategoriaDto();
        categoriaDto.setId(1L);
        categoriaDto.setNombre("Electrónicos");

        categoria = Categoria.builder()
                .id(1L)
                .nombre("Electrónicos")
                .build();
    }

    @Test
    @DisplayName("Debería crear categoría exitosamente cuando datos son válidos")
    void testCrearCategoriaExitoso() throws Exception {
        // Arrange
        when(categoriaRepository.existsByNombre(anyString())).thenReturn(false);
        when(categoriaMapper.toEntity(any(CategoriaDto.class))).thenReturn(categoria);
        when(categoriaRepository.save(any(Categoria.class))).thenReturn(categoria);
        when(categoriaMapper.toDto(any(Categoria.class))).thenReturn(categoriaDto);

        // Act
        CategoriaDto resultado = categoriaService.crearCategoria(categoriaDto);

        // Assert
        assertNotNull(resultado);
        verify(categoriaRepository).existsByNombre("Electrónicos");
        verify(categoriaMapper).toEntity(categoriaDto);
        verify(categoriaRepository).save(categoria);
        verify(categoriaMapper).toDto(categoria);
    }

    @Test
    @DisplayName("Debería lanzar excepción cuando nombre está vacío")
    void testCrearCategoriaConNombreVacio() {
        // Arrange
        categoriaDto.setNombre("   ");

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            categoriaService.crearCategoria(categoriaDto);
        });

        assertEquals("El nombre de la categoría es obligatorio", exception.getMessage());
        verify(categoriaRepository, never()).save(any(Categoria.class));
    }

    @Test
    @DisplayName("Debería lanzar excepción cuando nombre es nulo")
    void testCrearCategoriaConNombreNulo() {
        // Arrange
        categoriaDto.setNombre(null);

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            categoriaService.crearCategoria(categoriaDto);
        });

        assertEquals("El nombre de la categoría es obligatorio", exception.getMessage());
        verify(categoriaRepository, never()).save(any(Categoria.class));
    }

    @Test
    @DisplayName("Debería lanzar excepción cuando nombre ya existe")
    void testCrearCategoriaConNombreDuplicado() {
        // Arrange
        when(categoriaRepository.existsByNombre(anyString())).thenReturn(true);

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            categoriaService.crearCategoria(categoriaDto);
        });

        assertEquals("Ya existe una categoría con el nombre: Electrónicos", exception.getMessage());
        verify(categoriaRepository, never()).save(any(Categoria.class));
    }

    @Test
    @DisplayName("Debería actualizar categoría exitosamente")
    void testActualizarCategoriaExitoso() throws Exception {
        // Arrange
        CategoriaDto dtoActualizado = new CategoriaDto();
        dtoActualizado.setNombre("Electrónicos Actualizados");

        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        when(categoriaRepository.existsByNombreAndIdNot(anyString(), anyLong())).thenReturn(false);
        when(categoriaRepository.save(any(Categoria.class))).thenReturn(categoria);
        when(categoriaMapper.toDto(any(Categoria.class))).thenReturn(dtoActualizado);

        // Act
        CategoriaDto resultado = categoriaService.actualizarCategoria(1L, dtoActualizado);

        // Assert
        assertNotNull(resultado);
        verify(categoriaRepository).findById(1L);
        verify(categoriaRepository).existsByNombreAndIdNot("Electrónicos Actualizados", 1L);
        verify(categoriaMapper).updateCategoriaFromDto(dtoActualizado, categoria);
        verify(categoriaRepository).save(categoria);
    }

    @Test
    @DisplayName("Debería actualizar categoría sin validar nombre cuando es el mismo")
    void testActualizarCategoriaConMismoNombre() throws Exception {
        // Arrange
        CategoriaDto dtoActualizado = new CategoriaDto();
        dtoActualizado.setNombre("Electrónicos"); // Mismo nombre

        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        // No se llama a existsByNombreAndIdNot porque el nombre es el mismo
        when(categoriaRepository.save(any(Categoria.class))).thenReturn(categoria);
        when(categoriaMapper.toDto(any(Categoria.class))).thenReturn(dtoActualizado);

        // Act
        CategoriaDto resultado = categoriaService.actualizarCategoria(1L, dtoActualizado);

        // Assert
        assertNotNull(resultado);
        verify(categoriaRepository, never()).existsByNombreAndIdNot(anyString(), anyLong());
        verify(categoriaRepository).save(categoria);
    }

    @Test
    @DisplayName("Debería lanzar excepción al actualizar categoría no encontrada")
    void testActualizarCategoriaNoEncontrada() {
        // Arrange
        when(categoriaRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            categoriaService.actualizarCategoria(1L, categoriaDto);
        });

        assertEquals("Categoría no encontrada con ID: 1", exception.getMessage());
        verify(categoriaRepository, never()).save(any(Categoria.class));
    }

    @Test
    @DisplayName("Debería lanzar excepción cuando nombre está vacío al actualizar")
    void testActualizarCategoriaConNombreVacio() {
        // Arrange
        CategoriaDto dtoActualizado = new CategoriaDto();
        dtoActualizado.setNombre("   ");

        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            categoriaService.actualizarCategoria(1L, dtoActualizado);
        });

        assertEquals("El nombre de la categoría no puede estar vacío", exception.getMessage());
        verify(categoriaRepository, never()).save(any(Categoria.class));
    }

    @Test
    @DisplayName("Debería lanzar excepción cuando nombre duplicado al actualizar")
    void testActualizarCategoriaConNombreDuplicado() {
        // Arrange
        CategoriaDto dtoActualizado = new CategoriaDto();
        dtoActualizado.setNombre("Nuevo Nombre");

        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        when(categoriaRepository.existsByNombreAndIdNot(anyString(), anyLong())).thenReturn(true);

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            categoriaService.actualizarCategoria(1L, dtoActualizado);
        });

        assertEquals("Ya existe otra categoría con el nombre: Nuevo Nombre", exception.getMessage());
        verify(categoriaRepository, never()).save(any(Categoria.class));
    }

    @Test
    @DisplayName("Debería eliminar categoría exitosamente cuando no tiene productos")
    void testEliminarCategoriaExitoso() throws Exception {
        // Arrange
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        when(categoriaRepository.countProductosByCategoriaId(1L)).thenReturn(0L);
        doNothing().when(categoriaRepository).delete(categoria);

        // Act
        categoriaService.eliminarCategoria(1L);

        // Assert
        verify(categoriaRepository).findById(1L);
        verify(categoriaRepository).countProductosByCategoriaId(1L);
        verify(categoriaRepository).delete(categoria);
    }

    @Test
    @DisplayName("Debería lanzar excepción al eliminar categoría no encontrada")
    void testEliminarCategoriaNoEncontrada() {
        // Arrange
        when(categoriaRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            categoriaService.eliminarCategoria(1L);
        });

        assertEquals("Categoría no encontrada con ID: 1", exception.getMessage());
        verify(categoriaRepository, never()).delete(any(Categoria.class));
    }

    @Test
    @DisplayName("Debería lanzar excepción al eliminar categoría con productos asociados")
    void testEliminarCategoriaConProductos() {
        // Arrange
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        when(categoriaRepository.countProductosByCategoriaId(1L)).thenReturn(5L);

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            categoriaService.eliminarCategoria(1L);
        });

        assertEquals("No se puede eliminar la categoría porque tiene 5 producto(s) asociado(s)", exception.getMessage());
        verify(categoriaRepository, never()).delete(any(Categoria.class));
    }

    @Test
    @DisplayName("Debería obtener categoría por ID exitosamente")
    void testObtenerCategoriaPorId() throws Exception {
        // Arrange
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        when(categoriaMapper.toDto(any(Categoria.class))).thenReturn(categoriaDto);

        // Act
        CategoriaDto resultado = categoriaService.obtenerCategoriaPorId(1L);

        // Assert
        assertNotNull(resultado);
        assertEquals("Electrónicos", resultado.getNombre());
        verify(categoriaRepository).findById(1L);
        verify(categoriaMapper).toDto(categoria);
    }

    @Test
    @DisplayName("Debería lanzar excepción cuando categoría no existe por ID")
    void testObtenerCategoriaPorIdNoEncontrada() {
        // Arrange
        when(categoriaRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            categoriaService.obtenerCategoriaPorId(1L);
        });

        assertEquals("Categoría no encontrada con ID: 1", exception.getMessage());
        verify(categoriaRepository).findById(1L);
    }

    @Test
    @DisplayName("Debería obtener categoría por nombre exitosamente")
    void testObtenerCategoriaPorNombre() throws Exception {
        // Arrange
        when(categoriaRepository.findByNombreIgnoreCase("Electrónicos")).thenReturn(Optional.of(categoria));
        when(categoriaMapper.toDto(any(Categoria.class))).thenReturn(categoriaDto);

        // Act
        CategoriaDto resultado = categoriaService.obtenerCategoriaPorNombre("Electrónicos");

        // Assert
        assertNotNull(resultado);
        assertEquals("Electrónicos", resultado.getNombre());
        verify(categoriaRepository).findByNombreIgnoreCase("Electrónicos");
    }

    @Test
    @DisplayName("Debería lanzar excepción cuando nombre de búsqueda está vacío")
    void testObtenerCategoriaPorNombreVacio() {
        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            categoriaService.obtenerCategoriaPorNombre("   ");
        });

        assertEquals("El nombre de búsqueda no puede estar vacío", exception.getMessage());
        verify(categoriaRepository, never()).findByNombreIgnoreCase(anyString());
    }

    @Test
    @DisplayName("Debería lanzar excepción cuando categoría no existe por nombre")
    void testObtenerCategoriaPorNombreNoEncontrada() {
        // Arrange
        when(categoriaRepository.findByNombreIgnoreCase("Inexistente")).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            categoriaService.obtenerCategoriaPorNombre("Inexistente");
        });

        assertEquals("Categoría no encontrada con nombre: Inexistente", exception.getMessage());
        verify(categoriaRepository).findByNombreIgnoreCase("Inexistente");
    }

    @Test
    @DisplayName("Debería obtener todas las categorías")
    void testObtenerTodasLasCategorias() {
        // Arrange
        List<Categoria> categorias = Arrays.asList(categoria);
        when(categoriaRepository.findAll()).thenReturn(categorias);
        when(categoriaMapper.toDto(any(Categoria.class))).thenReturn(categoriaDto);

        // Act
        List<CategoriaDto> resultados = categoriaService.obtenerTodasLasCategorias();

        // Assert
        assertEquals(1, resultados.size());
        verify(categoriaRepository).findAll();
        verify(categoriaMapper).toDto(categoria);
    }

    @Test
    @DisplayName("Debería obtener categorías con productos")
    void testObtenerCategoriasConProductos() {
        // Arrange
        Categoria categoriaConProductos = Categoria.builder()
                .id(2L)
                .nombre("Ropa")
                .build();

        Categoria categoriaSinProductos = Categoria.builder()
                .id(3L)
                .nombre("Accesorios")
                .build();

        List<Categoria> todasCategorias = Arrays.asList(categoriaConProductos, categoriaSinProductos);

        when(categoriaRepository.findAll()).thenReturn(todasCategorias);
        when(categoriaRepository.countProductosByCategoriaId(2L)).thenReturn(3L);
        when(categoriaRepository.countProductosByCategoriaId(3L)).thenReturn(0L);
        when(categoriaMapper.toDto(categoriaConProductos)).thenReturn(new CategoriaDto(2L, "Ropa"));

        // Act
        List<CategoriaDto> resultados = categoriaService.obtenerCategoriasConProductos();

        // Assert
        assertEquals(1, resultados.size());
        assertEquals("Ropa", resultados.get(0).getNombre());
        verify(categoriaRepository).findAll();
        verify(categoriaRepository).countProductosByCategoriaId(2L);
        verify(categoriaRepository).countProductosByCategoriaId(3L);
    }

    @Test
    @DisplayName("Debería retornar lista vacía cuando no hay categorías con productos")
    void testObtenerCategoriasConProductosListaVacia() {
        // Arrange
        Categoria categoriaSinProductos = Categoria.builder()
                .id(1L)
                .nombre("Sin Productos")
                .build();

        List<Categoria> categorias = Arrays.asList(categoriaSinProductos);
        when(categoriaRepository.findAll()).thenReturn(categorias);
        when(categoriaRepository.countProductosByCategoriaId(1L)).thenReturn(0L);

        // Act
        List<CategoriaDto> resultados = categoriaService.obtenerCategoriasConProductos();

        // Assert
        assertTrue(resultados.isEmpty());
        verify(categoriaRepository).findAll();
        verify(categoriaRepository).countProductosByCategoriaId(1L);
    }

    @Test
    @DisplayName("Debería manejar correctamente cuando countProductosByCategoriaId retorna null")
    void testObtenerCategoriasConProductosCountNull() {
        // Arrange
        Categoria categoriaConCountNull = Categoria.builder()
                .id(1L)
                .nombre("Categoria Count Null")
                .build();

        List<Categoria> categorias = Arrays.asList(categoriaConCountNull);
        when(categoriaRepository.findAll()).thenReturn(categorias);
        when(categoriaRepository.countProductosByCategoriaId(1L)).thenReturn(null);

        // Act
        List<CategoriaDto> resultados = categoriaService.obtenerCategoriasConProductos();

        // Assert
        assertTrue(resultados.isEmpty());
        verify(categoriaRepository).findAll();
        verify(categoriaRepository).countProductosByCategoriaId(1L);
    }

    @Test
    @DisplayName("Debería actualizar categoría cuando nombre es nulo (solo otros campos)")
    void testActualizarCategoriaConNombreNulo() throws Exception {
        // Arrange
        CategoriaDto dtoActualizado = new CategoriaDto();
        dtoActualizado.setNombre(null); // Nombre nulo, no se valida unicidad

        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        // No se llama a existsByNombreAndIdNot porque el nombre es nulo
        when(categoriaRepository.save(any(Categoria.class))).thenReturn(categoria);
        when(categoriaMapper.toDto(any(Categoria.class))).thenReturn(dtoActualizado);

        // Act
        CategoriaDto resultado = categoriaService.actualizarCategoria(1L, dtoActualizado);

        // Assert
        assertNotNull(resultado);
        verify(categoriaRepository, never()).existsByNombreAndIdNot(anyString(), anyLong());
        verify(categoriaRepository).save(categoria);
    }

    @Test
    @DisplayName("Debería crear categoría con nombre con espacios que se recortan")
    void testCrearCategoriaConEspaciosEnNombre() throws Exception {
        // Arrange
        CategoriaDto dtoConEspacios = new CategoriaDto();
        dtoConEspacios.setNombre("  Electrónicos  ");

        when(categoriaRepository.existsByNombre(anyString())).thenReturn(false);
        when(categoriaMapper.toEntity(any(CategoriaDto.class))).thenReturn(categoria);
        when(categoriaRepository.save(any(Categoria.class))).thenReturn(categoria);
        when(categoriaMapper.toDto(any(Categoria.class))).thenReturn(categoriaDto);

        // Act
        CategoriaDto resultado = categoriaService.crearCategoria(dtoConEspacios);

        // Assert
        assertNotNull(resultado);
        // Verifica que se llamó con el nombre recortado
        verify(categoriaRepository).existsByNombre("Electrónicos");
        verify(categoriaRepository).save(categoria);
    }

    @Test
    @DisplayName("Debería actualizar categoría con nombre con espacios que se recortan")
    void testActualizarCategoriaConEspaciosEnNombre() throws Exception {
        // Arrange
        CategoriaDto dtoConEspacios = new CategoriaDto();
        dtoConEspacios.setNombre("  Nuevo Nombre  ");

        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        when(categoriaRepository.existsByNombreAndIdNot(anyString(), anyLong())).thenReturn(false);
        when(categoriaRepository.save(any(Categoria.class))).thenReturn(categoria);
        when(categoriaMapper.toDto(any(Categoria.class))).thenReturn(dtoConEspacios);

        // Act
        CategoriaDto resultado = categoriaService.actualizarCategoria(1L, dtoConEspacios);

        // Assert
        assertNotNull(resultado);
        // Verifica que se llamó con el nombre recortado
        verify(categoriaRepository).existsByNombreAndIdNot("Nuevo Nombre", 1L);
        verify(categoriaRepository).save(categoria);
    }
}