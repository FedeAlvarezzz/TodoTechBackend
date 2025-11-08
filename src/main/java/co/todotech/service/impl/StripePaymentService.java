package co.todotech.service.impl;


import co.todotech.model.dto.pasarela.PaymentIntentRequestDto;
import co.todotech.model.dto.pasarela.PaymentIntentResponseDto;
import co.todotech.model.dto.pasarela.PaymentConfirmationDto;
import co.todotech.model.enums.TipoMetodo;
import co.todotech.service.PaymentGatewayService;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Charge;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.PaymentIntentConfirmParams;
import com.stripe.param.ChargeListParams;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

@Slf4j
@Service
public class StripePaymentService implements PaymentGatewayService {

    @Value("${stripe.secret-key}")
    private String stripeSecretKey;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeSecretKey;
    }

    @Override
    public PaymentIntentResponseDto createPaymentIntent(PaymentIntentRequestDto request) {
        try {
            // Convertir a centavos (Stripe trabaja en la unidad más pequeña de la moneda)
            Long amountInCents = Math.round(request.amount() * 100);

            Map<String, String> initialMetadata = new HashMap<>();
            initialMetadata.put("order_id", request.orderId().toString());
            if (request.customerEmail() != null) {
                initialMetadata.put("customer_email", request.customerEmail());
            }
            if (request.metadata() != null) {
                initialMetadata.putAll(request.metadata());
            }

            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(amountInCents)
                    .setCurrency(request.currency().toLowerCase())
                    .setAutomaticPaymentMethods(
                            PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                    .setEnabled(true)
                                    .build()
                    )
                    .putAllMetadata(initialMetadata)
                    .build();

            PaymentIntent paymentIntent = PaymentIntent.create(params);

            // CORREGIDO: Usar HashMap en lugar de Map.of() para manejar valores nulos
            Map<String, Object> additionalData = new HashMap<>();
            additionalData.put("amount_received", paymentIntent.getAmountReceived());
            additionalData.put("payment_method_types", paymentIntent.getPaymentMethodTypes());
            additionalData.put("created", paymentIntent.getCreated());

            return new PaymentIntentResponseDto(
                    paymentIntent.getClientSecret(),
                    paymentIntent.getId(),
                    paymentIntent.getStatus(),
                    "requires_action".equals(paymentIntent.getStatus()),
                    paymentIntent.getNextAction() != null ? paymentIntent.getNextAction().getType() : null,
                    null,
                    additionalData // CORREGIDO
            );

        } catch (StripeException e) {
            log.error("Error creating Stripe payment intent: {}", e.getMessage(), e);
            return new PaymentIntentResponseDto(
                    null, null, "failed", false, null, e.getMessage(), null
            );
        }
    }

    @Override
    public PaymentIntentResponseDto confirmPayment(PaymentConfirmationDto confirmation) {
        try {
            PaymentIntent paymentIntent = PaymentIntent.retrieve(confirmation.paymentIntentId());

            if (confirmation.paymentMethodId() != null) {
                PaymentIntentConfirmParams params = PaymentIntentConfirmParams.builder()
                        .setPaymentMethod(confirmation.paymentMethodId())
                        .build();
                paymentIntent = paymentIntent.confirm(params);
            }

            // Obtener los cargos asociados al payment intent
            List<Map<String, Object>> chargesData = getChargesData(paymentIntent.getId());

            // CORREGIDO: Usar HashMap en lugar de Map.of() para manejar valores nulos
            Map<String, Object> additionalData = new HashMap<>();
            additionalData.put("amount_received", paymentIntent.getAmountReceived());
            additionalData.put("payment_method", paymentIntent.getPaymentMethod() != null ?
                    paymentIntent.getPaymentMethod() : "N/A");
            additionalData.put("charges", chargesData);
            additionalData.put("last_payment_error", paymentIntent.getLastPaymentError() != null ?
                    paymentIntent.getLastPaymentError().getMessage() : null);

            return new PaymentIntentResponseDto(
                    paymentIntent.getClientSecret(),
                    paymentIntent.getId(),
                    paymentIntent.getStatus(),
                    "requires_action".equals(paymentIntent.getStatus()),
                    paymentIntent.getNextAction() != null ? paymentIntent.getNextAction().getType() : null,
                    null,
                    additionalData // CORREGIDO
            );

        } catch (StripeException e) {
            log.error("Error confirming Stripe payment: {}", e.getMessage(), e);
            return new PaymentIntentResponseDto(
                    null, null, "failed", false, null, e.getMessage(), null
            );
        }
    }

    @Override
    public PaymentIntentResponseDto getPaymentStatus(String paymentIntentId) {
        try {
            PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);

            // Obtener los cargos asociados al payment intent
            List<Map<String, Object>> chargesData = getChargesData(paymentIntentId);

            // CORREGIDO: Usar HashMap en lugar de Map.of() para manejar valores nulos
            Map<String, Object> additionalData = new HashMap<>();
            additionalData.put("amount_received", paymentIntent.getAmountReceived());
            additionalData.put("payment_method", paymentIntent.getPaymentMethod() != null ?
                    paymentIntent.getPaymentMethod() : "N/A");
            additionalData.put("charges", chargesData);
            additionalData.put("created", paymentIntent.getCreated());
            additionalData.put("customer", paymentIntent.getCustomer());
            additionalData.put("description", paymentIntent.getDescription());
            additionalData.put("last_payment_error", paymentIntent.getLastPaymentError() != null ?
                    paymentIntent.getLastPaymentError().getMessage() : null);

            return new PaymentIntentResponseDto(
                    paymentIntent.getClientSecret(),
                    paymentIntent.getId(),
                    paymentIntent.getStatus(),
                    "requires_action".equals(paymentIntent.getStatus()),
                    paymentIntent.getNextAction() != null ? paymentIntent.getNextAction().getType() : null,
                    null,
                    additionalData // CORREGIDO
            );

        } catch (StripeException e) {
            log.error("Error getting Stripe payment status: {}", e.getMessage(), e);
            return new PaymentIntentResponseDto(
                    null, null, "failed", false, null, e.getMessage(), null
            );
        }
    }

    /**
     * Método auxiliar para obtener los cargos asociados a un PaymentIntent
     */
    private List<Map<String, Object>> getChargesData(String paymentIntentId) {
        try {
            ChargeListParams params = ChargeListParams.builder()
                    .setPaymentIntent(paymentIntentId)
                    .build();

            List<Charge> charges = Charge.list(params).getData();
            List<Map<String, Object>> chargesData = new ArrayList<>();

            for (Charge charge : charges) {
                Map<String, Object> chargeData = new HashMap<>();
                chargeData.put("id", charge.getId());
                chargeData.put("amount", charge.getAmount());
                chargeData.put("currency", charge.getCurrency());
                chargeData.put("status", charge.getStatus());
                chargeData.put("paid", charge.getPaid());
                chargeData.put("payment_method", charge.getPaymentMethod());
                chargeData.put("created", charge.getCreated());
                chargesData.add(chargeData);
            }

            return chargesData;

        } catch (StripeException e) {
            log.warn("Error retrieving charges for payment intent {}: {}", paymentIntentId, e.getMessage());
            return List.of(); // Retorna lista vacía en caso de error
        }
    }

    @Override
    public boolean supports(TipoMetodo paymentMethodType) {
        return paymentMethodType == TipoMetodo.STRIPE ||
                paymentMethodType == TipoMetodo.TARJETA_CREDITO ||
                paymentMethodType == TipoMetodo.TARJETA_DEBITO;
    }
}