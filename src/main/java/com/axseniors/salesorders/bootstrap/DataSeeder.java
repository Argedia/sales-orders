package com.axseniors.salesorders.bootstrap;

import com.axseniors.salesorders.domain.Customer;
import com.axseniors.salesorders.domain.Product;
import com.axseniors.salesorders.repo.CustomerRepo;
import com.axseniors.salesorders.repo.ProductRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final CustomerRepo customerRepo;
    private final ProductRepo productRepo;

    @Override
    public void run(String... args) {
        seedCustomers();
        seedProducts();
    }

    private void seedCustomers() {
        if (customerRepo.count() > 0) {
            return;
        }
        customerRepo.saveAll(List.of(
                new Customer("Autopartes Norte", "Laura Gómez", "contacto@autopartesnorte.com", "+57 3001234567", "Av. Central 123", "Bogotá", "900123456"),
                new Customer("Motores Express", "Juan Pérez", "ventas@motoresexpress.com", "+57 3107654321", "Calle 45 #12-34", "Medellín", "901234567"),
                new Customer("Repuestos del Valle", "Carolina Ruiz", "info@repuestosvalle.com", "+57 3159876543", "Cra. 10 #45-21", "Cali", "800987654"),
                new Customer("Transmisiones ACME", "Andrés Torres", "atencion@transacme.com", "+57 3501239876", "Zona Industrial 7", "Barranquilla", "901112233"),
                new Customer("Frenos y Más", "Paula Silva", "ventas@frenosymas.com", "+57 3024567890", "Autopista Norte Km 12", "Bogotá", "900555666")
        ));
    }

    private void seedProducts() {
        if (productRepo.count() > 0) {
            return;
        }
        productRepo.saveAll(List.of(
                new Product("REP-001", "Filtro de aire", new BigDecimal("25.00")),
                new Product("REP-002", "Bujía estándar", new BigDecimal("8.50")),
                new Product("REP-003", "Pastillas de freno", new BigDecimal("45.90")),
                new Product("REP-004", "Alternador", new BigDecimal("180.00")),
                new Product("REP-005", "Radiador", new BigDecimal("210.50")),
                new Product("REP-006", "Amortiguador", new BigDecimal("95.00")),
                new Product("REP-007", "Kit de embrague", new BigDecimal("320.00")),
                new Product("REP-008", "Aceite sintético 5W30", new BigDecimal("35.75")),
                new Product("REP-009", "Batería 12V", new BigDecimal("120.00")),
                new Product("REP-010", "Correa de distribución", new BigDecimal("65.00"))
        ));
    }
}
