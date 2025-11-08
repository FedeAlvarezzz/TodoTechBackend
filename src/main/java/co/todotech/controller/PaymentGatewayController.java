package co.todotech.controller;

import co.todotech.model.dto.MensajeDto;
import co.todotech.model.dto.pasarela.PaymentIntentRequestDto;
import co.todotech.model.dto.pasarela.PaymentIntentResponseDto;
import co.todotech.model.dto.pasarela.PaymentConfirmationDto;
import co.todotech.model.enums.TipoMetodo;
import co.todotech.service.PaymentGatewayService;
import co.todotech.service.impl.PaymentGatewayFactory;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payment-gateway")
public class PaymentGatewayController {

    private final PaymentGatewayFactory paymentGatewayFactory;

    @PostMapping("/create-payment-intent")
    @PreAuthorize("hasAnyRole('ADMIN', 'CAJERO')")
    public ResponseEntity<MensajeDto<PaymentIntentResponseDto>> createPaymentIntent(
            @Valid @RequestBody PaymentIntentRequestDto request) {
        try {
            log.info("Creating payment intent for order: {}, amount: {}",
                    request.orderId(), request.amount());

            PaymentGatewayService paymentService =
                    paymentGatewayFactory.getPaymentService(request.paymentMethodType());

            PaymentIntentResponseDto response = paymentService.createPaymentIntent(request);

            if (response.errorMessage() != null) {
                return ResponseEntity.badRequest()
                        .body(new MensajeDto<>(true, response.errorMessage(), null));
            }

            return ResponseEntity.ok()
                    .body(new MensajeDto<>(false, "Payment intent created successfully", response));

        } catch (Exception e) {
            log.error("Error creating payment intent: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(new MensajeDto<>(true, e.getMessage(), null));
        }
    }

    @PostMapping("/confirm-payment")
    @PreAuthorize("hasAnyRole('ADMIN', 'CAJERO')")
    public ResponseEntity<MensajeDto<PaymentIntentResponseDto>> confirmPayment(
            @Valid @RequestBody PaymentConfirmationDto confirmation) {
        try {
            log.info("Confirming payment: {}", confirmation.paymentIntentId());

            // ✅ CORREGIDO: Determinar servicio dinámicamente
            PaymentGatewayService paymentService = determinePaymentService(confirmation.paymentIntentId());

            PaymentIntentResponseDto response = paymentService.confirmPayment(confirmation);

            if (response.errorMessage() != null) {
                return ResponseEntity.badRequest()
                        .body(new MensajeDto<>(true, response.errorMessage(), null));
            }

            return ResponseEntity.ok()
                    .body(new MensajeDto<>(false, "Payment confirmed successfully", response));

        } catch (Exception e) {
            log.error("Error confirming payment: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(new MensajeDto<>(true, e.getMessage(), null));
        }
    }

    @GetMapping("/payment-status/{paymentIntentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CAJERO')")
    public ResponseEntity<MensajeDto<PaymentIntentResponseDto>> getPaymentStatus(
            @PathVariable("paymentIntentId") String paymentIntentId) { // ✅ CORREGIDO: Nombre explícito del parámetro
        try {
            log.info("Getting payment status for: {}", paymentIntentId);

            // ✅ CORREGIDO: Determinar servicio dinámicamente
            PaymentGatewayService paymentService = determinePaymentService(paymentIntentId);

            PaymentIntentResponseDto response = paymentService.getPaymentStatus(paymentIntentId);

            return ResponseEntity.ok()
                    .body(new MensajeDto<>(false, "Payment status retrieved", response));

        } catch (Exception e) {
            log.error("Error getting payment status: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(new MensajeDto<>(true, e.getMessage(), null));
        }
    }

    /**
     * ✅ MÉTODO AUXILIAR: Determina el servicio de pago basado en el ID del payment intent
     */
    private PaymentGatewayService determinePaymentService(String paymentIntentId) {
        // Lógica para identificar el proveedor por el formato del ID
        if (paymentIntentId.startsWith("pi_") ||  // Stripe Payment Intent
                paymentIntentId.startsWith("ch_") ||  // Stripe Charge
                paymentIntentId.startsWith("seti_") || // Stripe Setup Intent
                paymentIntentId.startsWith("cs_")) {   // Stripe Checkout Session
            return paymentGatewayFactory.getPaymentService(TipoMetodo.STRIPE);
        }

        // Por defecto, asumimos Stripe (puedes agregar más proveedores después)
        log.warn("No se pudo determinar el proveedor para paymentIntentId: {}, usando Stripe por defecto", paymentIntentId);
        return paymentGatewayFactory.getPaymentService(TipoMetodo.STRIPE);
    }
}