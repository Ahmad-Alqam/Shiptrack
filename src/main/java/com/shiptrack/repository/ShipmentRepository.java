package com.shiptrack.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.shiptrack.model.Shipment;
import com.shiptrack.model.ShipmentStatus;
import com.shiptrack.model.User;

public interface ShipmentRepository extends JpaRepository<Shipment, Long> {

    Optional<Shipment> findByTrackingNumber(String trackingNumber);
    List<Shipment> findByCustomer(User customer);
    List<Shipment> findByCustomerOrderByCreatedAtDesc(User customer);
    List<Shipment> findByDriver(User driver);
    List<Shipment> findByStatus(ShipmentStatus status);
    List<Shipment> findByDispatcherOrderByCreatedAtDesc(User dispatcher);
    List<Shipment> findByStatusOrderByCreatedAtDesc(ShipmentStatus status);

    boolean existsByTrackingNumber(String trackingNumber);

    long countByCustomer(User customer);
    long countByCustomerAndStatus(User customer, ShipmentStatus status);

    long countByDriver(User driver); // <-- Add this
    long countByDriverAndStatus(User driver, ShipmentStatus status); // <-- Add this
    List<Shipment> findByDriverOrderByCreatedAtDesc(User driver);

    long countByStatus(ShipmentStatus status);
    long countByDispatcher(User dispatcher);
    long countByDispatcherAndStatus(User dispatcher, ShipmentStatus status);

    @Override
    Optional<Shipment> findById(Long id);
}