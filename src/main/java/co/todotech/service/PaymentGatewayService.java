package co.todotech.service;

import co.todotech.model.dto.pasarela.PaymentIntentRequestDto;
import co.todotech.model.dto.pasarela.PaymentIntentResponseDto;
import co.todotech.model.dto.pasarela.PaymentConfirmationDto;
import co.todotech.model.enums.TipoMetodo;

public interface PaymentGatewayService {
    PaymentIntentResponseDto createPaymentIntent(PaymentIntentRequestDto request);
    PaymentIntentResponseDto confirmPayment(PaymentConfirmationDto confirmation);
    PaymentIntentResponseDto getPaymentStatus(String paymentIntentId);

    boolean supports(TipoMetodo paymentMethodType);
}