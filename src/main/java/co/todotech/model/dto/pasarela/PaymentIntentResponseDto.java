// PaymentIntentResponseDto.java - NUEVO
package co.todotech.model.dto.pasarela;

import java.util.Map;

public record PaymentIntentResponseDto(
        String clientSecret,
        String paymentIntentId,
        String status,
        Boolean requiresAction,
        String nextActionType,
        String errorMessage,
        Map<String, Object> additionalData
) {}