package co.todotech.model.dto.pasarela;

import co.todotech.model.enums.TipoMetodo;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.Map;

public record PaymentIntentRequestDto(
        @NotNull(message = "El monto no puede ser nulo")
        @Positive(message = "El monto debe ser mayor a cero")
        Double amount,

        @NotNull(message = "La moneda no puede ser nula")
        String currency,

        @NotNull(message = "El método de pago no puede ser nulo")
        TipoMetodo paymentMethodType,

        @NotNull(message = "El ID de la orden no puede ser nulo")
        Long orderId,

        String customerEmail,

        Map<String, String> metadata
) {}