package co.todotech.service.impl;

import co.todotech.mapper.MetodoPagoMapper;
import co.todotech.model.dto.metodopago.MetodoPagoDto;
import co.todotech.model.entities.MetodoPago;
import co.todotech.model.enums.TipoMetodo;
import co.todotech.repository.MetodoPagoRepository;
import co.todotech.service.MetodoPagoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementación del servicio {@link MetodoPagoService}, encargada de manejar la lógica
 * de negocio relacionada con los métodos de pago en el sistema.
 *
 * Proporciona operaciones CRUD completas, validaciones y consultas personalizadas
 * sobre la entidad {@link MetodoPago}.
 *
 * Esta clase utiliza {@link MetodoPagoMapper} para transformar entre entidades
 * y objetos DTO, y {@link MetodoPagoRepository} para el acceso a la base de datos.
 *
 * Se utiliza la anotación {@link Transactional} para asegurar la integridad
 * de las transacciones en operaciones críticas.
 *
 * @author
 * @version 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MetodoPagoServiceImpl implements MetodoPagoService {

    /** Mapper para convertir entre entidades y DTOs de método de pago. */
    private final MetodoPagoMapper metodoPagoMapper;

    /** Repositorio que proporciona acceso a los datos persistentes de métodos de pago. */
    private final MetodoPagoRepository metodoPagoRepository;

    /**
     * Crea un nuevo método de pago en el sistema.
     *
     * @param dto Objeto de transferencia con los datos del método de pago a crear.
     * @return El método de pago creado como {@link MetodoPagoDto}.
     * @throws Exception si ya existe un método con el mismo tipo.
     */
    @Override
    @Transactional
    public MetodoPagoDto crearMetodoPago(MetodoPagoDto dto) throws Exception {
        log.info("Creando método de pago: {}", dto.metodo());

        // Verificar si ya existe un método con el mismo tipo para evitar duplicados
        if (metodoPagoRepository.existsByMetodo(dto.metodo())) {
            throw new Exception("Ya existe un método de pago con el tipo: " + dto.metodo());
        }

        // Convertir el DTO a entidad y guardarla en la base de datos
        MetodoPago metodoPago = metodoPagoMapper.toEntity(dto);
        metodoPagoRepository.save(metodoPago);

        log.info("Método de pago creado exitosamente: id={}, metodo={}",
                metodoPago.getId(), metodoPago.getMetodo());
        return metodoPagoMapper.toDto(metodoPago);
    }

    /**
     * Actualiza un método de pago existente.
     *
     * @param id  Identificador del método de pago a actualizar.
     * @param dto Datos actualizados del método de pago.
     * @return El método de pago actualizado como {@link MetodoPagoDto}.
     * @throws Exception si no se encuentra el método o ya existe otro con el mismo tipo.
     */
    @Override
    @Transactional
    public MetodoPagoDto actualizarMetodoPago(Long id, MetodoPagoDto dto) throws Exception {
        log.info("Actualizando método de pago id={}", id);

        // Buscar el método existente
        MetodoPago metodoPago = metodoPagoRepository.findById(id)
                .orElseThrow(() -> new Exception("Método de pago no encontrado con ID: " + id));

        // Validar que no exista otro método con el mismo tipo
        if (!metodoPago.getMetodo().equals(dto.metodo()) &&
                metodoPagoRepository.existsByMetodo(dto.metodo())) {
            throw new Exception("Ya existe otro método de pago con el tipo: " + dto.metodo());
        }

        // Actualizar los campos del método usando el mapper
        metodoPagoMapper.updateMetodoPagoFromDto(dto, metodoPago);
        metodoPagoRepository.save(metodoPago);

        log.info("Método de pago actualizado: id={}, metodo={}",
                metodoPago.getId(), metodoPago.getMetodo());
        return metodoPagoMapper.toDto(metodoPago);
    }

    /**
     * Elimina un método de pago de la base de datos.
     *
     * @param id Identificador del método de pago a eliminar.
     * @throws Exception si el método de pago no existe.
     */
    @Override
    @Transactional
    public void eliminarMetodoPago(Long id) throws Exception {
        MetodoPago metodoPago = metodoPagoRepository.findById(id)
                .orElseThrow(() -> new Exception("Método de pago no encontrado con ID: " + id));

        // Aquí se podrían incluir validaciones adicionales,
        // por ejemplo: verificar si el método está asociado a pagos existentes.

        metodoPagoRepository.delete(metodoPago);
        log.info("Método de pago eliminado: {}", id);
    }

    /**
     * Obtiene un método de pago específico por su ID.
     *
     * @param id Identificador del método de pago.
     * @return El método de pago encontrado como {@link MetodoPagoDto}.
     * @throws Exception si no se encuentra el método.
     */
    @Override
    public MetodoPagoDto obtenerMetodoPagoPorId(Long id) throws Exception {
        MetodoPago metodoPago = metodoPagoRepository.findById(id)
                .orElseThrow(() -> new Exception("Método de pago no encontrado con ID: " + id));
        return metodoPagoMapper.toDto(metodoPago);
    }

    /**
     * Obtiene una lista de métodos de pago filtrados por tipo.
     *
     * @param tipo Tipo de método de pago (por ejemplo, TARJETA, EFECTIVO, TRANSFERENCIA).
     * @return Lista de métodos de pago que coinciden con el tipo especificado.
     */
    @Override
    public List<MetodoPagoDto> obtenerMetodosPagoPorTipo(TipoMetodo tipo) {
        return metodoPagoRepository.findByMetodo(tipo).stream()
                .map(metodoPagoMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene una lista de métodos de pago según su estado de aprobación.
     *
     * @param aprobacion Valor booleano que indica si el método está aprobado.
     * @return Lista de métodos de pago aprobados o no aprobados.
     */
    @Override
    public List<MetodoPagoDto> obtenerMetodosPagoPorAprobacion(Boolean aprobacion) {
        return metodoPagoRepository.findByAprobacion(aprobacion).stream()
                .map(metodoPagoMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene todos los métodos de pago registrados en el sistema.
     *
     * @return Lista de todos los métodos de pago existentes.
     */
    @Override
    public List<MetodoPagoDto> obtenerTodosLosMetodosPago() {
        log.info("Obteniendo todos los métodos de pago");
        return metodoPagoRepository.findAll().stream()
                .map(metodoPagoMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene todos los métodos de pago cuya comisión sea menor o igual a un valor máximo.
     *
     * @param comisionMaxima Valor máximo permitido de comisión.
     * @return Lista de métodos de pago con comisión menor o igual al valor dado.
     */
    @Override
    public List<MetodoPagoDto> obtenerMetodosPagoConComisionMenorIgual(Double comisionMaxima) {
        return metodoPagoRepository.findByComisionLessThanEqual(comisionMaxima).stream()
                .map(metodoPagoMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene los métodos de pago aprobados, ordenados por su comisión.
     *
     * @return Lista de métodos aprobados, ordenados de menor a mayor comisión.
     */
    @Override
    public List<MetodoPagoDto> obtenerMetodosAprobadosOrdenadosPorComision() {
        return metodoPagoRepository.findMetodosAprobadosOrderByComision().stream()
                .map(metodoPagoMapper::toDto)
                .collect(Collectors.toList());
    }
}