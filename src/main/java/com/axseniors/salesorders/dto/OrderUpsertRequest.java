package com.axseniors.salesorders.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
public class OrderUpsertRequest {

    private String orderNumber;

    @NotNull
    private UUID customerId;

    @NotNull
    private LocalDate orderDate;

    private LocalDate deliveryDate;

    @NotNull
    @Valid
    @Size(min = 1)
    private List<OrderLineRequest> lines;
}
