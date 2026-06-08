package com.shiptrack.controller;
// Handles delivery personnel functions.
import java.util.List;

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
public class DriverController {

    private final ShipmentRepository shipmentRepository;

    public DriverController(ShipmentRepository shipmentRepository) {
        this.shipmentRepository = shipmentRepository;
    }

    @GetMapping("/driver/dashboard")
    public String driverDashboard(HttpSession session, Model model) {
        if (!SessionHelper.isLoggedIn(session)) { // Check if user is logged in
            return "redirect:/login";
        }

        if (!SessionHelper.hasRole(session, Role.DRIVER)) { // Check if user has the DRIVER role
            return "redirect:/unauthorized";
        }

        User driver = SessionHelper.getLoggedInUser(session); // Get the logged-in driver from the session

        // Fetch and count shipments assigned to this driver by status
        long assignedCount = shipmentRepository.countByDriver(driver);
        long pickedUpCount = shipmentRepository.countByDriverAndStatus(driver, ShipmentStatus.PICKED_UP);
        long inTransitCount = shipmentRepository.countByDriverAndStatus(driver, ShipmentStatus.IN_TRANSIT);
        long deliveredCount = shipmentRepository.countByDriverAndStatus(driver, ShipmentStatus.DELIVERED);

        // Fetch recent deliveries assigned to this driver sorted by most recent
        List<Shipment> recentDeliveries = shipmentRepository.findByDriverOrderByCreatedAtDesc(driver);

        // Add all the fetched data to the model to be displayed on the dashboard
        model.addAttribute("loggedInUser", driver);
        model.addAttribute("assignedCount", assignedCount);
        model.addAttribute("pickedUpCount", pickedUpCount);
        model.addAttribute("inTransitCount", inTransitCount);
        model.addAttribute("deliveredCount", deliveredCount);
        model.addAttribute("recentDeliveries", recentDeliveries);

        return "driver_dashboard";
    }

    @GetMapping("/driver/assigned-deliveries")
        public String assignedDeliveriesPage(HttpSession session, Model model) {
            if (!SessionHelper.isLoggedIn(session)) { // Check if user is logged in
                return "redirect:/login";
            }

            if (!SessionHelper.hasRole(session, Role.DRIVER)) { // Check if user has the DRIVER role
                return "redirect:/unauthorized";
            }

            User driver = SessionHelper.getLoggedInUser(session); // Get the logged-in driver from the session

            // Fetch deliveries assigned to this driver sorted by most recent
            List<Shipment> assignedDeliveries = shipmentRepository.findByDriverOrderByCreatedAtDesc(driver);

            model.addAttribute("assignedDeliveries", assignedDeliveries);
            return "driver_assigned_deliveries";  
    }

    @GetMapping("/driver/update-status")
    public String updateStatusPage(HttpSession session, Model model) {
        if (!SessionHelper.isLoggedIn(session)) { // Check if user is logged in
            return "redirect:/login";
        }

        if (!SessionHelper.hasRole(session, Role.DRIVER)) { // Check if user has the DRIVER role
            return "redirect:/unauthorized";
        }

        User driver = SessionHelper.getLoggedInUser(session); // Get the logged-in driver from the session

        // Fetch deliveries assigned to this driver sorted by most recent to show on the update status page
        List<Shipment> assignedDeliveries = shipmentRepository.findByDriverOrderByCreatedAtDesc(driver);

        model.addAttribute("assignedDeliveries", assignedDeliveries);
        return "driver_update_status";
    }

    @PostMapping("/driver/update-status")
    public String updateShipmentStatus(
            @RequestParam Long shipmentId,
            @RequestParam ShipmentStatus status,
            HttpSession session,
            Model model
    ) {
        if (!SessionHelper.isLoggedIn(session)) { // Check if user is logged in
            return "redirect:/login";
        }

        if (!SessionHelper.hasRole(session, Role.DRIVER)) { // Check if user has the DRIVER role
            return "redirect:/unauthorized";
        }

        User driver = SessionHelper.getLoggedInUser(session); // Get the logged-in driver from the session

        // Look up the shipment by its ID and validate that it exists
        Shipment shipment = shipmentRepository.findById(shipmentId).orElse(null);

        // Validate that the shipment exists and is assigned to the logged-in driver
        if (shipment == null) {
            // If shipment is not found, show error message and re-fetch assigned deliveries to show on the update status page after error
            model.addAttribute("error", "Shipment not found.");
            model.addAttribute("assignedDeliveries",
                    shipmentRepository.findByDriverOrderByCreatedAtDesc(driver));
            return "driver_update_status";
        }

        // Ensure that the shipment is assigned to the logged-in driver before allowing status update
        if (shipment.getDriver() == null || !shipment.getDriver().getId().equals(driver.getId())) {
            model.addAttribute("error", "This shipment is not assigned to you.");
            model.addAttribute("assignedDeliveries",
                    shipmentRepository.findByDriverOrderByCreatedAtDesc(driver));
            return "driver_update_status";
        }
        // Only allow drivers to set status to PICKED_UP, IN_TRANSIT, or DELIVERED
        if (status != ShipmentStatus.PICKED_UP &&
            status != ShipmentStatus.IN_TRANSIT &&
            status != ShipmentStatus.DELIVERED) {
            model.addAttribute("error", "Driver can only set status to Picked Up, In Transit, or Delivered.");
            model.addAttribute("assignedDeliveries",
                    shipmentRepository.findByDriverOrderByCreatedAtDesc(driver));
            return "driver_update_status";
        }

        shipment.setStatus(status); // Update the shipment status and save the changes
        shipmentRepository.save(shipment); // After successful update, show success message and re-fetch shipments assigned by this driver to show on the update status page

        // Show success message and re-fetch shipments assigned by this driver to show on the update status page after successful update
        model.addAttribute("success", "Shipment status updated successfully.");
        model.addAttribute("assignedDeliveries",
                shipmentRepository.findByDriverOrderByCreatedAtDesc(driver));
        return "driver_update_status";
    }
}