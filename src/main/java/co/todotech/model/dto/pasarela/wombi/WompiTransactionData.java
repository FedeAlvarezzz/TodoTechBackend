package co.todotech.model.dto.pasarela.wombi;// WompiTransactionData.java


import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

public record WompiTransactionData(
        @JsonProperty("id") String id,
        @JsonProperty("created_at") LocalDateTime createdAt,
        @JsonProperty("amount_in_cents") Long amountInCents,
        @JsonProperty("reference") String reference,
        @JsonProperty("customer_email") String customerEmail,
        @JsonProperty("currency") String currency,
        @JsonProperty("payment_method_type") String paymentMethodType,
        @JsonProperty("payment_method") WompiPaymentMethod paymentMethod,
        @JsonProperty("status") String status,
        @JsonProperty("status_message") String statusMessage,
        @JsonProperty("merchant") WompiMerchant merchant,
        @JsonProperty("redirect_url") String redirectUrl
) {}