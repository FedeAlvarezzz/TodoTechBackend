package co.todotech.model.dto.pasarela.wombi;// WompiTransactionResponse.java


import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

public record WompiTransactionResponse(
        @JsonProperty("data") WompiTransactionData data
) {}