package com.axseniors.salesorders.dto;

import com.axseniors.salesorders.domain.SalesOrderStatus;
import com.axseniors.salesorders.domain.CancelReason;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Value
@Builder
public class OrderResponse {
    UUID id;
    String orderNumber;
    SalesOrderStatus status;
    CancelReason cancelReason;
    String cancelNote;
    UUID customerId;
    String customerName;
    LocalDate orderDate;
    LocalDate deliveryDate;
    BigDecimal orderTotal;
    BigDecimal orderSubtotal;
    BigDecimal orderDiscountTotal;
    List<OrderLineResponse> lines;
}
