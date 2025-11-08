package co.todotech.model.dto.pasarela.wombi;// WompiMerchant.java


import com.fasterxml.jackson.annotation.JsonProperty;

public record WompiMerchant(
        @JsonProperty("name") String name
) {}