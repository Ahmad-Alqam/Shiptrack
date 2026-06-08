package com.shiptrack.controller;
// Handles customer functions.
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
        if (!SessionHelper.isLoggedIn(session)) { // Check if user is logged in
            return "redirect:/login";
        }

        if (!SessionHelper.hasRole(session, Role.CUSTOMER)) { // Check if user has the CUSTOMER role
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
        if (!SessionHelper.isLoggedIn(session)) { // Check if user is logged in
            return "redirect:/login";
        }

        if (!SessionHelper.hasRole(session, Role.CUSTOMER)) { // Check if user has the CUSTOMER role
            return "redirect:/unauthorized";
        }

        // Get the logged-in customer from the session
        User customer = SessionHelper.getLoggedInUser(session);

        pickupAddress = pickupAddress.trim();
        deliveryAddress = deliveryAddress.trim();
        packageType = packageType.trim();
        notes = (notes == null) ? "" : notes.trim();

        // Validate required fields
        if (pickupAddress.isEmpty() || deliveryAddress.isEmpty() || packageType.isEmpty()) {
            model.addAttribute("error", "All required fields must be filled.");
            return "customer_create_shipment";
        }

        // Validate package weight
        if (packageWeight == null || Double.isNaN(packageWeight) || Double.isInfinite(packageWeight) || packageWeight <= 0) {
            model.addAttribute("error", "Package weight must be greater than 0.");
            return "customer_create_shipment";
        }

        // Create a new shipment and save it to the database
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

    // Helper method to generate a unique tracking number
    private String generateTrackingNumber() {
        Random random = new Random();
        String trackingNumber;

        do {
            int number = 100000 + random.nextInt(900000); // Generate a random 6-digit number
            trackingNumber = "ST-" + number;
        } while (shipmentRepository.existsByTrackingNumber(trackingNumber)); // Ensure the tracking number is unique

        return trackingNumber;
    }

    @GetMapping("/customer/track-shipment")
    public String trackShipmentPage(HttpSession session) { // Check if user is logged in and has the CUSTOMER role before showing the tracking page
        if (!SessionHelper.isLoggedIn(session)) {
            return "redirect:/login";
        }

        if (!SessionHelper.hasRole(session, Role.CUSTOMER)) { // Check if user has the CUSTOMER role
            return "redirect:/unauthorized";
        }

        return "customer_track_shipment";
    }

    @PostMapping("/customer/track-shipment")
        public String trackShipment(
                @RequestParam String trackingNumber, // Get the tracking number from the form input
                HttpSession session, // Check if user is logged in and has the CUSTOMER role before processing the tracking request
                Model model
        ) {
            if (!SessionHelper.isLoggedIn(session)) {
                return "redirect:/login"; // Redirect to login page if user is not logged in
            }

            if (!SessionHelper.hasRole(session, Role.CUSTOMER)) {
                return "redirect:/unauthorized"; // Redirect to unauthorized page if user does not have the CUSTOMER role
            }

            trackingNumber = trackingNumber.trim();

            if (trackingNumber.isEmpty()) { // Validate that the tracking number is not empty
                model.addAttribute("error", "Tracking number is required.");
                return "customer_track_shipment";
            }

            Optional<Shipment> optionalShipment = shipmentRepository.findByTrackingNumber(trackingNumber); // Look up the shipment by tracking number

            if (optionalShipment.isEmpty()) { // If shipment is not found, show an error message
                model.addAttribute("error", "Shipment not found.");
                return "customer_track_shipment";
            }

            // Check if the shipment belongs to the logged-in customer
            Shipment shipment = optionalShipment.get();
            if (!shipment.getCustomer().getId().equals(SessionHelper.getUserId(session))) {
                model.addAttribute("error", "You are not the owner of this shipment.");
                return "customer_track_shipment";
            }

            // If shipment is found and belongs to the customer, show the shipment details
            model.addAttribute("shipment", shipment);
            model.addAttribute("success", "Shipment found successfully.");
            return "customer_track_shipment";
        }
    }