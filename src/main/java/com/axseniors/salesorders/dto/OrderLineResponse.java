package com.axseniors.salesorders.dto;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.util.UUID;

@Value
@Builder
public class OrderLineResponse {
    UUID lineId;
    UUID productId;
    String productCode;
    String productName;
    Integer quantity;
    BigDecimal unitPrice;
    BigDecimal discountPct;
    BigDecimal lineTotal;
}
