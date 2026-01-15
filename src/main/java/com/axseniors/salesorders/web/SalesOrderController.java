package com.axseniors.salesorders.web;

import com.axseniors.salesorders.domain.Customer;
import com.axseniors.salesorders.domain.Product;
import com.axseniors.salesorders.domain.SalesOrderStatus;
import com.axseniors.salesorders.dto.CancelOrderRequest;
import com.axseniors.salesorders.dto.CustomerRequest;
import com.axseniors.salesorders.dto.OrderResponse;
import com.axseniors.salesorders.dto.OrderUpsertRequest;
import com.axseniors.salesorders.repo.CustomerRepo;
import com.axseniors.salesorders.repo.ProductRepo;
import com.axseniors.salesorders.service.SalesOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.UUID;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api")
@RequiredArgsConstructor
public class SalesOrderController {

    private final CustomerRepo customerRepo;
    private final ProductRepo productRepo;
    private final SalesOrderService salesOrderService;

    @GetMapping("/customers")
    public List<Customer> getCustomers() {
        return customerRepo.findAll();
    }

    @PostMapping("/customers")
    @ResponseStatus(HttpStatus.CREATED)
    public Customer createCustomer(@Valid @RequestBody CustomerRequest request) {
        Customer customer = new Customer(
                request.getName(),
                request.getContactName(),
                request.getEmail(),
                request.getPhone(),
                request.getAddress(),
                request.getCity(),
                request.getTaxId()
        );
        return customerRepo.save(customer);
    }
    
    @PutMapping("/customers/{id}")
    public Customer updateCustomer(@PathVariable UUID id, @Valid @RequestBody CustomerRequest request) {
        Customer existing = customerRepo.findById(id)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Customer not found"));
        existing.setName(request.getName());
        existing.setContactName(request.getContactName());
        existing.setEmail(request.getEmail());
        existing.setPhone(request.getPhone());
        existing.setAddress(request.getAddress());
        existing.setCity(request.getCity());
        existing.setTaxId(request.getTaxId());
        return customerRepo.save(existing);
    }

    @DeleteMapping("/customers/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCustomer(@PathVariable UUID id) {
        try {
            customerRepo.deleteById(id);
        } catch (DataIntegrityViolationException ex) {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.CONFLICT, "Customer is used by orders");
        }
    }

    @GetMapping("/products")
    public List<Product> getProducts() {
        return productRepo.findAll();
    }

    @GetMapping("/orders")
    public List<OrderResponse> listOrders(@RequestParam(value = "includeCancelled", defaultValue = "false") boolean includeCancelled) {
        if (includeCancelled) {
            return salesOrderService.listAll();
        }
        return salesOrderService.listActive();
    }

    @PostMapping("/orders")
    public OrderResponse createOrder(@Valid @RequestBody OrderUpsertRequest request) {
        return salesOrderService.createOrder(request);
    }

    @GetMapping("/orders/{id}")
    public OrderResponse getOrder(@PathVariable UUID id) {
        return salesOrderService.getOrder(id);
    }

    @PutMapping("/orders/{id}")
    public OrderResponse updateOrder(@PathVariable UUID id, @Valid @RequestBody OrderUpsertRequest request) {
        return salesOrderService.updateOrder(id, request);
    }

    @PostMapping("/orders/{id}/confirm")
    public OrderResponse confirmOrder(@PathVariable UUID id) {
        return salesOrderService.confirmOrder(id);
    }

    @PostMapping("/orders/{id}/cancel")
    public OrderResponse cancelOrder(@PathVariable UUID id, @Valid @RequestBody CancelOrderRequest request) {
        return salesOrderService.cancelOrder(id, request);
    }
}
