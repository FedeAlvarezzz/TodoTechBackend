package co.todotech.model.dto.pasarela.wombi;// WompiTransactionRequest.java

import com.fasterxml.jackson.annotation.JsonProperty;

public record WompiTransactionRequest(
        @JsonProperty("amount_in_cents") Long amountInCents,
        @JsonProperty("currency") String currency,
        @JsonProperty("reference") String reference,
        @JsonProperty("description") String description,
        @JsonProperty("customer_email") String customerEmail,
        @JsonProperty("payment_method_type") String paymentMethodType,
        @JsonProperty("redirect_url") String redirectUrl
) {
    public WompiTransactionRequest {
        if (paymentMethodType == null) {
            paymentMethodType = "CARD"; // Por defecto tarjeta
        }
    }
}
