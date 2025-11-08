package co.todotech.model.dto.pasarela.wombi;// WompiPaymentMethod.java


import com.fasterxml.jackson.annotation.JsonProperty;

public record WompiPaymentMethod(
        @JsonProperty("type") String type,
        @JsonProperty("extra") WompiPaymentMethodExtra extra
) {}