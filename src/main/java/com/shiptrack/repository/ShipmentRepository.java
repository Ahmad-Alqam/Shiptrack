package com.shiptrack.repository;
// Handles database operations for shipments.
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.shiptrack.model.Shipment;
import com.shiptrack.model.ShipmentStatus;
import com.shiptrack.model.User;

public interface ShipmentRepository extends JpaRepository<Shipment, Long> {

    // Custom query methods for finding shipments based on various criteria
    Optional<Shipment> findByTrackingNumber(String trackingNumber);
    List<Shipment> findByCustomer(User customer);
    List<Shipment> findByCustomerOrderByCreatedAtDesc(User customer);
    List<Shipment> findByDriver(User driver);
    List<Shipment> findByStatus(ShipmentStatus status);
    List<Shipment> findByDispatcherOrderByCreatedAtDesc(User dispatcher);
    List<Shipment> findByStatusOrderByCreatedAtDesc(ShipmentStatus status);

    boolean existsByTrackingNumber(String trackingNumber); // Check if a shipment with the given tracking number already exists

    long countByCustomer(User customer); // Count the number of shipments for a specific customer
    long countByCustomerAndStatus(User customer, ShipmentStatus status); // Count the number of shipments for a specific customer with a specific status

    long countByDriver(User driver); // Count the number of shipments assigned to a specific driver
    long countByDriverAndStatus(User driver, ShipmentStatus status); // Count the number of shipments assigned to a specific driver with a specific status
    List<Shipment> findByDriverOrderByCreatedAtDesc(User driver); // Find shipments assigned to a specific driver, sorted by creation date in descending order

    long countByStatus(ShipmentStatus status); // Count the number of shipments with a specific status
    long countByDispatcher(User dispatcher); // Count the number of shipments assigned to a specific dispatcher
    long countByDispatcherAndStatus(User dispatcher, ShipmentStatus status); // Count the number of shipments assigned to a specific dispatcher with a specific status

    @Override
    Optional<Shipment> findById(Long id);

    boolean existsByDispatcher(User dispatcher); // Check if there are any shipments assigned to a specific dispatcher
    boolean existsByDriver(User driver); // Check if there are any shipments assigned to a specific driver
    boolean existsByCustomer(User customer); // Check if there are any shipments associated with a specific customer
}