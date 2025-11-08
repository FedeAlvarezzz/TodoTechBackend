// PaymentConfirmationDto.java - NUEVO
package co.todotech.model.dto.pasarela;

import jakarta.validation.constraints.NotNull;

import java.util.Map;

public record PaymentConfirmationDto(
        @NotNull(message = "El ID del payment intent no puede ser nulo")
        String paymentIntentId,

        String paymentMethodId,

        Map<String, Object> confirmationData
) {}