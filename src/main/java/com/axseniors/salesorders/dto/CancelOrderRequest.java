package com.axseniors.salesorders.dto;

import com.axseniors.salesorders.domain.CancelReason;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CancelOrderRequest {
    @NotNull
    private CancelReason reason;

    private String note;
}
