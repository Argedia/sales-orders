package com.axseniors.salesorders.service;

import com.axseniors.salesorders.domain.Customer;
import com.axseniors.salesorders.domain.Product;
import com.axseniors.salesorders.domain.SalesOrder;
import com.axseniors.salesorders.domain.SalesOrderLine;
import com.axseniors.salesorders.domain.SalesOrderStatus;
import com.axseniors.salesorders.domain.CancelReason;
import com.axseniors.salesorders.dto.OrderLineRequest;
import com.axseniors.salesorders.dto.OrderLineResponse;
import com.axseniors.salesorders.dto.OrderResponse;
import com.axseniors.salesorders.dto.OrderUpsertRequest;
import com.axseniors.salesorders.repo.CustomerRepo;
import com.axseniors.salesorders.repo.ProductRepo;
import com.axseniors.salesorders.repo.SalesOrderRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
@Transactional
public class SalesOrderService {

    private final CustomerRepo customerRepo;
    private final ProductRepo productRepo;
    private final SalesOrderRepo salesOrderRepo;

    @Transactional(readOnly = true)
    public List<OrderResponse> listAll() {
        return salesOrderRepo.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> listActive() {
        return salesOrderRepo.findAllByStatusNot(SalesOrderStatus.CANCELLED).stream().map(this::toResponse).toList();
    }

    public OrderResponse createOrder(OrderUpsertRequest request) {
        SalesOrder order = new SalesOrder();
        order.setStatus(SalesOrderStatus.DRAFT);
        populateOrder(order, request);
        SalesOrder saved = salesOrderRepo.save(order);
        return toResponse(saved);
    }

    public OrderResponse updateOrder(UUID orderId, OrderUpsertRequest request) {
        SalesOrder existing = salesOrderRepo.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
        ensureDraft(existing);
        populateOrder(existing, request);
        SalesOrder saved = salesOrderRepo.save(existing);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrder(UUID orderId) {
        SalesOrder order = salesOrderRepo.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
        return toResponse(order);
    }

    public OrderResponse confirmOrder(UUID orderId) {
        SalesOrder order = salesOrderRepo.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
        ensureDraft(order);
        order.setStatus(SalesOrderStatus.CONFIRMED);
        SalesOrder saved = salesOrderRepo.save(order);
        return toResponse(saved);
    }

    public OrderResponse cancelOrder(UUID orderId, com.axseniors.salesorders.dto.CancelOrderRequest request) {
        SalesOrder order = salesOrderRepo.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
        if (SalesOrderStatus.CANCELLED.equals(order.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Order already cancelled");
        }
        if (!SalesOrderStatus.DRAFT.equals(order.getStatus()) && !SalesOrderStatus.CONFIRMED.equals(order.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Order cannot be cancelled");
        }
        order.setCancelReason(request.getReason());
        order.setCancelNote(request.getNote());
        order.setStatus(SalesOrderStatus.CANCELLED);
        SalesOrder saved = salesOrderRepo.save(order);
        return toResponse(saved);
    }

    private void populateOrder(SalesOrder order, OrderUpsertRequest request) {
        if (StringUtils.hasText(request.getOrderNumber())) {
            order.setOrderNumber(request.getOrderNumber());
        } else if (!StringUtils.hasText(order.getOrderNumber())) {
            order.setOrderNumber(generateOrderNumber());
        }

        Customer customer = customerRepo.findById(request.getCustomerId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"));
        order.setCustomer(customer);
        order.setOrderDate(request.getOrderDate());
        order.setDeliveryDate(request.getDeliveryDate());

        List<SalesOrderLine> lines = new ArrayList<>();
        for (OrderLineRequest lineRequest : request.getLines()) {
            lines.add(buildLine(lineRequest));
        }
        order.replaceLines(lines);
    }

    private SalesOrderLine buildLine(OrderLineRequest lineRequest) {
        Product product = productRepo.findById(lineRequest.getProductId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));

        SalesOrderLine line = new SalesOrderLine();
        line.setProduct(product);
        line.setQuantity(lineRequest.getQuantity());
        line.setUnitPrice(lineRequest.getUnitPrice());
        line.setDiscountPct(lineRequest.getDiscountPct());
        return line;
    }

    private OrderResponse toResponse(SalesOrder order) {
        List<OrderLineResponse> lineResponses = new ArrayList<>();
        BigDecimal orderTotal = BigDecimal.ZERO;
        BigDecimal orderSubtotal = BigDecimal.ZERO;
        BigDecimal orderDiscountTotal = BigDecimal.ZERO;

        for (SalesOrderLine line : order.getLines()) {
            BigDecimal lineTotal = calculateLineTotal(line);
            orderTotal = orderTotal.add(lineTotal);
            BigDecimal lineSubtotal = line.getUnitPrice().multiply(BigDecimal.valueOf(line.getQuantity()));
            BigDecimal lineDiscount = lineSubtotal.subtract(lineTotal);
            orderSubtotal = orderSubtotal.add(lineSubtotal);
            orderDiscountTotal = orderDiscountTotal.add(lineDiscount);
            lineResponses.add(OrderLineResponse.builder()
                    .lineId(line.getId())
                    .productId(line.getProduct().getId())
                    .productCode(line.getProduct().getCode())
                    .productName(line.getProduct().getName())
                    .quantity(line.getQuantity())
                    .unitPrice(line.getUnitPrice())
                    .discountPct(line.getDiscountPct())
                    .lineTotal(lineTotal)
                    .build());
        }

        return OrderResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .status(order.getStatus())
                .cancelReason(order.getCancelReason())
                .cancelNote(order.getCancelNote())
                .customerId(order.getCustomer().getId())
                .customerName(order.getCustomer().getName())
                .orderDate(order.getOrderDate())
                .deliveryDate(order.getDeliveryDate())
                .orderTotal(orderTotal.setScale(2, RoundingMode.HALF_UP))
                .orderSubtotal(orderSubtotal.setScale(2, RoundingMode.HALF_UP))
                .orderDiscountTotal(orderDiscountTotal.setScale(2, RoundingMode.HALF_UP))
                .lines(lineResponses)
                .build();
    }

    private BigDecimal calculateLineTotal(SalesOrderLine line) {
        BigDecimal discountFactor = BigDecimal.ONE.subtract(
                line.getDiscountPct().divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP));
        return line.getUnitPrice()
                .multiply(BigDecimal.valueOf(line.getQuantity()))
                .multiply(discountFactor)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private void ensureDraft(SalesOrder order) {
        if (SalesOrderStatus.CONFIRMED.equals(order.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Confirmed orders cannot be edited");
        }
    }

    private String generateOrderNumber() {
        String datePart = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        String suffix = String.format("%04d", ThreadLocalRandom.current().nextInt(10_000));
        String candidate = "SO-" + datePart + "-" + suffix;
        while (salesOrderRepo.findByOrderNumber(candidate).isPresent()) {
            suffix = String.format("%04d", ThreadLocalRandom.current().nextInt(10_000));
            candidate = "SO-" + datePart + "-" + suffix;
        }
        return candidate;
    }
}
