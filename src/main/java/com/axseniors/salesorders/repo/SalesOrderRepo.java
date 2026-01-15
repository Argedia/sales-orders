package com.axseniors.salesorders.repo;

import com.axseniors.salesorders.domain.SalesOrder;
import com.axseniors.salesorders.domain.SalesOrderStatus;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SalesOrderRepo extends JpaRepository<SalesOrder, UUID> {
    Optional<SalesOrder> findByOrderNumber(String orderNumber);

    List<SalesOrder> findAllByStatusNot(SalesOrderStatus status);
}
