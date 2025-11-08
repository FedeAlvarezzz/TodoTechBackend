package co.todotech.model.dto.pasarela.wombi;// WompiPaymentMethodExtra.java


import com.fasterxml.jackson.annotation.JsonProperty;

public record WompiPaymentMethodExtra(
        @JsonProperty("name") String name,
        @JsonProperty("card_brand") String cardBrand
) {}