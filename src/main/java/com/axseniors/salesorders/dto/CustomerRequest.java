package com.axseniors.salesorders.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class CustomerRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String contactName;

    @Email
    @NotBlank
    private String email;

    @NotBlank
    @Pattern(regexp = "[0-9+\\-() ]{7,20}", message = "Teléfono inválido")
    private String phone;

    private String address;

    private String city;

    private String taxId;
}
