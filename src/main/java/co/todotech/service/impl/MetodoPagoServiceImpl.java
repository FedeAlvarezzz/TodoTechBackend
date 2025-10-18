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

@Slf4j
@Service
@RequiredArgsConstructor
public class MetodoPagoServiceImpl implements MetodoPagoService {

    private final MetodoPagoMapper metodoPagoMapper;
    private final MetodoPagoRepository metodoPagoRepository;

    @Override
    @Transactional
    public MetodoPagoDto crearMetodoPago(MetodoPagoDto dto) throws Exception {
        log.info("Creando método de pago: {}", dto.metodo());

        // Validar que no exista un método con el mismo tipo
        if (metodoPagoRepository.existsByMetodo(dto.metodo())) {
            throw new Exception("Ya existe un método de pago con el tipo: " + dto.metodo());
        }

        MetodoPago metodoPago = metodoPagoMapper.toEntity(dto);
        metodoPagoRepository.save(metodoPago);

        log.info("Método de pago creado exitosamente: id={}, metodo={}", metodoPago.getId(), metodoPago.getMetodo());
        return metodoPagoMapper.toDto(metodoPago);
    }

    @Override
    @Transactional
    public MetodoPagoDto actualizarMetodoPago(Long id, MetodoPagoDto dto) throws Exception {
        log.info("Actualizando método de pago id={}", id);

        MetodoPago metodoPago = metodoPagoRepository.findById(id)
                .orElseThrow(() -> new Exception("Método de pago no encontrado con ID: " + id));

        // Validar que no exista otro método con el mismo tipo
        if (!metodoPago.getMetodo().equals(dto.metodo()) &&
                metodoPagoRepository.existsByMetodo(dto.metodo())) {
            throw new Exception("Ya existe otro método de pago con el tipo: " + dto.metodo());
        }

        metodoPagoMapper.updateMetodoPagoFromDto(dto, metodoPago);
        metodoPagoRepository.save(metodoPago);

        log.info("Método de pago actualizado: id={}, metodo={}", metodoPago.getId(), metodoPago.getMetodo());
        return metodoPagoMapper.toDto(metodoPago);
    }

    @Override
    @Transactional
    public void eliminarMetodoPago(Long id) throws Exception {
        MetodoPago metodoPago = metodoPagoRepository.findById(id)
                .orElseThrow(() -> new Exception("Método de pago no encontrado con ID: " + id));

        // Aquí podrías agregar validaciones adicionales (ej: si está siendo usado en pagos)

        metodoPagoRepository.delete(metodoPago);
        log.info("Método de pago eliminado: {}", id);
    }

    @Override
    public MetodoPagoDto obtenerMetodoPagoPorId(Long id) throws Exception {
        MetodoPago metodoPago = metodoPagoRepository.findById(id)
                .orElseThrow(() -> new Exception("Método de pago no encontrado con ID: " + id));
        return metodoPagoMapper.toDto(metodoPago);
    }

    @Override
    public List<MetodoPagoDto> obtenerMetodosPagoPorTipo(TipoMetodo tipo) {
        return metodoPagoRepository.findByMetodo(tipo).stream()
                .map(metodoPagoMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<MetodoPagoDto> obtenerMetodosPagoPorAprobacion(Boolean aprobacion) {
        return metodoPagoRepository.findByAprobacion(aprobacion).stream()
                .map(metodoPagoMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<MetodoPagoDto> obtenerTodosLosMetodosPago() {
        log.info("Obteniendo todos los métodos de pago");
        return metodoPagoRepository.findAll().stream()
                .map(metodoPagoMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<MetodoPagoDto> obtenerMetodosPagoConComisionMenorIgual(Double comisionMaxima) {
        return metodoPagoRepository.findByComisionLessThanEqual(comisionMaxima).stream()
                .map(metodoPagoMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<MetodoPagoDto> obtenerMetodosAprobadosOrdenadosPorComision() {
        return metodoPagoRepository.findMetodosAprobadosOrderByComision().stream()
                .map(metodoPagoMapper::toDto)
                .collect(Collectors.toList());
    }
}