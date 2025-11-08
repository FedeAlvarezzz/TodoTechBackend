package co.todotech.service.impl;

import co.todotech.model.enums.TipoMetodo;
import co.todotech.service.PaymentGatewayService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentGatewayFactory {

    private final List<PaymentGatewayService> paymentServices;

    public PaymentGatewayService getPaymentService(TipoMetodo paymentMethodType) {
        return paymentServices.stream()
                .filter(service -> service.supports(paymentMethodType))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "No payment service found for method: " + paymentMethodType
                ));
    }
}