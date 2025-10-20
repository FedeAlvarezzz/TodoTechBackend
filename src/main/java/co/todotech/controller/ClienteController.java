package co.todotech.controller;

import co.todotech.model.dto.MensajeDto;
import co.todotech.model.dto.cliente.ClienteDto;
import co.todotech.model.enums.TipoCliente;
import co.todotech.service.ClienteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/clientes")
public class ClienteController {

    private final ClienteService clienteService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
    public ResponseEntity<MensajeDto<ClienteDto>> crearCliente(@Valid @RequestBody ClienteDto dto) {
        try {
            ClienteDto clienteCreado = clienteService.crearCliente(dto);
            return ResponseEntity.ok(new MensajeDto<>(false, "Cliente creado exitosamente", clienteCreado));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MensajeDto<>(true, e.getMessage(), null));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
    public ResponseEntity<MensajeDto<ClienteDto>> actualizarCliente(@PathVariable("id") Long id,
                                                                    @Valid @RequestBody ClienteDto dto) {
        try {
            ClienteDto clienteActualizado = clienteService.actualizarCliente(id, dto);
            return ResponseEntity.ok(new MensajeDto<>(false, "Cliente actualizado exitosamente", clienteActualizado));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MensajeDto<>(true, e.getMessage(), null));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MensajeDto<String>> eliminarCliente(@PathVariable("id") Long id) {
        try {
            clienteService.eliminarCliente(id);
            return ResponseEntity.ok(new MensajeDto<>(false, "Cliente eliminado exitosamente"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MensajeDto<>(true, e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MensajeDto<ClienteDto>> obtenerClientePorId(@PathVariable("id") Long id) {
        try {
            ClienteDto dto = clienteService.obtenerClientePorId(id);
            return ResponseEntity.ok(new MensajeDto<>(false, "Cliente encontrado", dto));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MensajeDto<>(true, e.getMessage(), null));
        }
    }

    @GetMapping("/cedula/{cedula}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MensajeDto<ClienteDto>> obtenerClientePorCedula(@PathVariable("cedula") String cedula) {
        try {
            ClienteDto dto = clienteService.obtenerClientePorCedula(cedula);
            return ResponseEntity.ok(new MensajeDto<>(false, "Cliente encontrado por cédula", dto));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MensajeDto<>(true, e.getMessage(), null));
        }
    }

    @GetMapping("/correo/{correo}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MensajeDto<ClienteDto>> obtenerClientePorCorreo(@PathVariable("correo") String correo) {
        try {
            ClienteDto dto = clienteService.obtenerClientePorCorreo(correo);
            return ResponseEntity.ok(new MensajeDto<>(false, "Cliente encontrado por correo", dto));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MensajeDto<>(true, e.getMessage(), null));
        }
    }

    @GetMapping("/tipo/{tipo}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MensajeDto<List<ClienteDto>>> obtenerClientesPorTipo(@PathVariable("tipo") String tipo) {
        try {
            TipoCliente tipoCliente = TipoCliente.valueOf(tipo.toUpperCase());
            List<ClienteDto> lista = clienteService.obtenerClientesPorTipo(tipoCliente);
            return ResponseEntity.ok(new MensajeDto<>(false, "Clientes por tipo obtenidos", lista));
        } catch (IllegalArgumentException iae) {
            return ResponseEntity.badRequest().body(new MensajeDto<>(true,
                    "Tipo de cliente inválido. Usa: NATURAL, JURIDICO", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MensajeDto<>(true, e.getMessage(), null));
        }
    }

    @GetMapping("/nombre/{nombre}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MensajeDto<List<ClienteDto>>> obtenerClientesPorNombre(@PathVariable("nombre") String nombre) {
        try {
            List<ClienteDto> lista = clienteService.obtenerClientesPorNombre(nombre);
            return ResponseEntity.ok(new MensajeDto<>(false, "Clientes por nombre obtenidos", lista));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MensajeDto<>(true, e.getMessage(), null));
        }
    }

    @GetMapping("/registrados-despues/{fecha}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MensajeDto<List<ClienteDto>>> obtenerClientesRegistradosDespuesDe(
            @PathVariable("fecha") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fecha) {
        try {
            List<ClienteDto> lista = clienteService.obtenerClientesRegistradosDespuesDe(fecha);
            return ResponseEntity.ok(new MensajeDto<>(false, "Clientes registrados después de la fecha obtenidos", lista));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MensajeDto<>(true, e.getMessage(), null));
        }
    }

    @GetMapping("/registrados-entre")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MensajeDto<List<ClienteDto>>> obtenerClientesRegistradosEntre(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin) {
        try {
            List<ClienteDto> lista = clienteService.obtenerClientesRegistradosEntre(inicio, fin);
            return ResponseEntity.ok(new MensajeDto<>(false, "Clientes registrados en el rango obtenidos", lista));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MensajeDto<>(true, e.getMessage(), null));
        }
    }

    @GetMapping("/contar/tipo/{tipo}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MensajeDto<Long>> contarClientesPorTipo(@PathVariable("tipo") String tipo) {
        try {
            TipoCliente tipoCliente = TipoCliente.valueOf(tipo.toUpperCase());
            long cantidad = clienteService.contarClientesPorTipo(tipoCliente);
            return ResponseEntity.ok(new MensajeDto<>(false, "Conteo de clientes por tipo", cantidad));
        } catch (IllegalArgumentException iae) {
            return ResponseEntity.badRequest().body(new MensajeDto<>(true,
                    "Tipo de cliente inválido. Usa: NATURAL, JURIDICO", 0L));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MensajeDto<>(true, e.getMessage(), 0L));
        }
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MensajeDto<List<ClienteDto>>> obtenerTodosLosClientes() {
        try {
            List<ClienteDto> lista = clienteService.obtenerTodosLosClientes();
            return ResponseEntity.ok(new MensajeDto<>(false, "Todos los clientes obtenidos exitosamente", lista));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MensajeDto<>(true, e.getMessage(), null));
        }
    }
}