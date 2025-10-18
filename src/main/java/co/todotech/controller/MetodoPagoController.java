package co.todotech.controller;

import co.todotech.model.dto.MensajeDto;
import co.todotech.model.dto.metodopago.MetodoPagoDto;
import co.todotech.model.enums.TipoMetodo;
import co.todotech.service.MetodoPagoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/metodos-pago")
public class MetodoPagoController {

    private final MetodoPagoService metodoPagoService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MensajeDto<MetodoPagoDto>> crearMetodoPago(@Valid @RequestBody MetodoPagoDto dto) {
        try {
            MetodoPagoDto metodoCreado = metodoPagoService.crearMetodoPago(dto);
            return ResponseEntity.ok(new MensajeDto<>(false, "Método de pago creado exitosamente", metodoCreado));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MensajeDto<>(true, e.getMessage(), null));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MensajeDto<MetodoPagoDto>> actualizarMetodoPago(@PathVariable Long id,
                                                                          @Valid @RequestBody MetodoPagoDto dto) {
        try {
            MetodoPagoDto metodoActualizado = metodoPagoService.actualizarMetodoPago(id, dto);
            return ResponseEntity.ok(new MensajeDto<>(false, "Método de pago actualizado exitosamente", metodoActualizado));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MensajeDto<>(true, e.getMessage(), null));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MensajeDto<String>> eliminarMetodoPago(@PathVariable Long id) {
        try {
            metodoPagoService.eliminarMetodoPago(id);
            return ResponseEntity.ok(new MensajeDto<>(false, "Método de pago eliminado exitosamente"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MensajeDto<>(true, e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MensajeDto<MetodoPagoDto>> obtenerMetodoPagoPorId(@PathVariable Long id) {
        try {
            MetodoPagoDto dto = metodoPagoService.obtenerMetodoPagoPorId(id);
            return ResponseEntity.ok(new MensajeDto<>(false, "Método de pago encontrado", dto));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MensajeDto<>(true, e.getMessage(), null));
        }
    }

    @GetMapping("/tipo/{tipo}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MensajeDto<List<MetodoPagoDto>>> obtenerMetodosPagoPorTipo(@PathVariable String tipo) {
        try {
            TipoMetodo tipoMetodo = TipoMetodo.valueOf(tipo.toUpperCase());
            List<MetodoPagoDto> lista = metodoPagoService.obtenerMetodosPagoPorTipo(tipoMetodo);
            return ResponseEntity.ok(new MensajeDto<>(false, "Métodos de pago por tipo obtenidos", lista));
        } catch (IllegalArgumentException iae) {
            return ResponseEntity.badRequest().body(new MensajeDto<>(true,
                    "Tipo de método inválido. Usa: EFECTIVO, TARJETA, CREDITO, TARJETA_DEBITO, REDCOMPRA", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MensajeDto<>(true, e.getMessage(), null));
        }
    }

    @GetMapping("/aprobacion/{aprobacion}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MensajeDto<List<MetodoPagoDto>>> obtenerMetodosPagoPorAprobacion(@PathVariable Boolean aprobacion) {
        try {
            List<MetodoPagoDto> lista = metodoPagoService.obtenerMetodosPagoPorAprobacion(aprobacion);
            return ResponseEntity.ok(new MensajeDto<>(false, "Métodos de pago por aprobación obtenidos", lista));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MensajeDto<>(true, e.getMessage(), null));
        }
    }

    @GetMapping("/comision-maxima/{comisionMaxima}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MensajeDto<List<MetodoPagoDto>>> obtenerMetodosPagoConComisionMenorIgual(@PathVariable Double comisionMaxima) {
        try {
            List<MetodoPagoDto> lista = metodoPagoService.obtenerMetodosPagoConComisionMenorIgual(comisionMaxima);
            return ResponseEntity.ok(new MensajeDto<>(false, "Métodos de pago con comisión menor o igual obtenidos", lista));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MensajeDto<>(true, e.getMessage(), null));
        }
    }

    @GetMapping("/aprobados-ordenados")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MensajeDto<List<MetodoPagoDto>>> obtenerMetodosAprobadosOrdenadosPorComision() {
        try {
            List<MetodoPagoDto> lista = metodoPagoService.obtenerMetodosAprobadosOrdenadosPorComision();
            return ResponseEntity.ok(new MensajeDto<>(false, "Métodos aprobados ordenados por comisión", lista));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MensajeDto<>(true, e.getMessage(), null));
        }
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MensajeDto<List<MetodoPagoDto>>> obtenerTodosLosMetodosPago() {
        try {
            List<MetodoPagoDto> lista = metodoPagoService.obtenerTodosLosMetodosPago();
            return ResponseEntity.ok(new MensajeDto<>(false, "Todos los métodos de pago obtenidos exitosamente", lista));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MensajeDto<>(true, e.getMessage(), null));
        }
    }
}