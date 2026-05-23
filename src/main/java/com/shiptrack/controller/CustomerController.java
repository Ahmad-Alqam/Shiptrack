package com.shiptrack.controller;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.shiptrack.model.Role;
import com.shiptrack.model.Shipment;
import com.shiptrack.model.ShipmentStatus;
import com.shiptrack.model.User;
import com.shiptrack.repository.ShipmentRepository;
import com.shiptrack.util.SessionHelper;

import jakarta.servlet.http.HttpSession;

@Controller
public class CustomerController {

    private final ShipmentRepository shipmentRepository;

    public CustomerController(ShipmentRepository shipmentRepository) {
        this.shipmentRepository = shipmentRepository;
    }

    @GetMapping("/customer/create-shipment")
    public String createShipmentPage(HttpSession session) {
        if (!SessionHelper.isLoggedIn(session)) {
            return "redirect:/login";
        }

        if (!SessionHelper.hasRole(session, Role.CUSTOMER)) {
            return "redirect:/unauthorized";
        }

        
        return "customer_create_shipment";
    }

    @PostMapping("/customer/create-shipment")
    public String createShipment(
            @RequestParam String pickupAddress,
            @RequestParam String deliveryAddress,
            @RequestParam String packageType,
            @RequestParam Double packageWeight,
            @RequestParam(required = false) String notes,
            HttpSession session,
            Model model
    ) {
        if (!SessionHelper.isLoggedIn(session)) {
            return "redirect:/login";
        }

        if (!SessionHelper.hasRole(session, Role.CUSTOMER)) {
            return "redirect:/unauthorized";
        }

        User customer = SessionHelper.getLoggedInUser(session);

        pickupAddress = pickupAddress.trim();
        deliveryAddress = deliveryAddress.trim();
        packageType = packageType.trim();
        notes = (notes == null) ? "" : notes.trim();

        if (pickupAddress.isEmpty() || deliveryAddress.isEmpty() || packageType.isEmpty()) {
            model.addAttribute("error", "All required fields must be filled.");
            return "customer_create_shipment";
        }

        if (packageWeight == null || Double.isNaN(packageWeight) || Double.isInfinite(packageWeight) || packageWeight <= 0) {
            model.addAttribute("error", "Package weight must be greater than 0.");
            return "customer_create_shipment";
        }

        Shipment shipment = new Shipment();
        shipment.setTrackingNumber(generateTrackingNumber());
        shipment.setPickupAddress(pickupAddress);
        shipment.setDeliveryAddress(deliveryAddress);
        shipment.setPackageType(packageType);
        shipment.setPackageWeight(packageWeight);
        shipment.setNotes(notes);
        shipment.setStatus(ShipmentStatus.PENDING);
        shipment.setCustomer(customer);
        shipment.setCreatedAt(LocalDateTime.now());

        shipmentRepository.save(shipment);

        model.addAttribute("success", "Shipment created successfully. Tracking number: " + shipment.getTrackingNumber());
        return "customer_create_shipment";
    }

    private String generateTrackingNumber() {
        Random random = new Random();
        String trackingNumber;

        do {
            int number = 100000 + random.nextInt(900000);
            trackingNumber = "ST-" + number;
        } while (shipmentRepository.existsByTrackingNumber(trackingNumber));

        return trackingNumber;
    }

    @GetMapping("/customer/track-shipment")
public String trackShipmentPage(HttpSession session) {
    if (!SessionHelper.isLoggedIn(session)) {
        return "redirect:/login";
    }

    if (!SessionHelper.hasRole(session, Role.CUSTOMER)) {
        return "redirect:/unauthorized";
    }

    return "customer_track_shipment";
}

@PostMapping("/customer/track-shipment")
public String trackShipment(
        @RequestParam String trackingNumber,
        HttpSession session,
        Model model
) {
    if (!SessionHelper.isLoggedIn(session)) {
        return "redirect:/login";
    }

    if (!SessionHelper.hasRole(session, Role.CUSTOMER)) {
        return "redirect:/unauthorized";
    }

    trackingNumber = trackingNumber.trim();

    if (trackingNumber.isEmpty()) {
        model.addAttribute("error", "Tracking number is required.");
        return "customer_track_shipment";
    }

    Optional<Shipment> optionalShipment = shipmentRepository.findByTrackingNumber(trackingNumber);

    if (optionalShipment.isEmpty()) {
        model.addAttribute("error", "Shipment not found.");
        return "customer_track_shipment";
    }

    Shipment shipment = optionalShipment.get();

    model.addAttribute("shipment", shipment);
    model.addAttribute("success", "Shipment found successfully.");
    return "customer_track_shipment";
}
}