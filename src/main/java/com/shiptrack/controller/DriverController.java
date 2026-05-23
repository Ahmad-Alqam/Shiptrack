package com.shiptrack.controller;

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
        if (!SessionHelper.isLoggedIn(session)) {
            return "redirect:/login";
        }

        if (!SessionHelper.hasRole(session, Role.DRIVER)) {
            return "redirect:/unauthorized";
        }

        User driver = SessionHelper.getLoggedInUser(session);

        long assignedCount = shipmentRepository.countByDriver(driver);
        long pickedUpCount = shipmentRepository.countByDriverAndStatus(driver, ShipmentStatus.PICKED_UP);
        long inTransitCount = shipmentRepository.countByDriverAndStatus(driver, ShipmentStatus.IN_TRANSIT);
        long deliveredCount = shipmentRepository.countByDriverAndStatus(driver, ShipmentStatus.DELIVERED);

        List<Shipment> recentDeliveries = shipmentRepository.findByDriverOrderByCreatedAtDesc(driver);

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
            if (!SessionHelper.isLoggedIn(session)) {
                return "redirect:/login";
            }

            if (!SessionHelper.hasRole(session, Role.DRIVER)) {
                return "redirect:/unauthorized";
            }

            User driver = SessionHelper.getLoggedInUser(session);
            List<Shipment> assignedDeliveries = shipmentRepository.findByDriverOrderByCreatedAtDesc(driver);

            model.addAttribute("assignedDeliveries", assignedDeliveries);
            return "driver_assigned_deliveries";  
    }

    @GetMapping("/driver/update-status")
    public String updateStatusPage(HttpSession session, Model model) {
        if (!SessionHelper.isLoggedIn(session)) {
            return "redirect:/login";
        }

        if (!SessionHelper.hasRole(session, Role.DRIVER)) {
            return "redirect:/unauthorized";
        }

        User driver = SessionHelper.getLoggedInUser(session);
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
        if (!SessionHelper.isLoggedIn(session)) {
            return "redirect:/login";
        }

        if (!SessionHelper.hasRole(session, Role.DRIVER)) {
            return "redirect:/unauthorized";
        }

        User driver = SessionHelper.getLoggedInUser(session);
        Shipment shipment = shipmentRepository.findById(shipmentId).orElse(null);

        if (shipment == null) {
            model.addAttribute("error", "Shipment not found.");
            model.addAttribute("assignedDeliveries",
                    shipmentRepository.findByDriverOrderByCreatedAtDesc(driver));
            return "driver_update_status";
        }

        if (shipment.getDriver() == null || !shipment.getDriver().getId().equals(driver.getId())) {
            model.addAttribute("error", "This shipment is not assigned to you.");
            model.addAttribute("assignedDeliveries",
                    shipmentRepository.findByDriverOrderByCreatedAtDesc(driver));
            return "driver_update_status";
        }

        shipment.setStatus(status);
        shipmentRepository.save(shipment);

        model.addAttribute("success", "Shipment status updated successfully.");
        model.addAttribute("assignedDeliveries",
                shipmentRepository.findByDriverOrderByCreatedAtDesc(driver));
        return "driver_update_status";
    }
}