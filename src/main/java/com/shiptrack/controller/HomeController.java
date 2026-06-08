package com.shiptrack.controller;
// Handles general navigation pages.
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.shiptrack.model.Role;
import com.shiptrack.model.Shipment;
import com.shiptrack.model.ShipmentStatus;
import com.shiptrack.model.User;
import com.shiptrack.repository.ShipmentRepository;
import com.shiptrack.util.SessionHelper;

import jakarta.servlet.http.HttpSession;

@Controller
public class HomeController {

    private final ShipmentRepository shipmentRepository;

    public HomeController(ShipmentRepository shipmentRepository) {
        this.shipmentRepository = shipmentRepository;
    }

    @GetMapping("/")
    public String home(HttpSession session) {
        User user = SessionHelper.getLoggedInUser(session);

        if (user == null) { // If no user is logged in, redirect to login page
            return "redirect:/login";
        }

        switch (user.getRole()) { // Redirect users to their respective dashboards based on their role
            case CUSTOMER:
                return "redirect:/customer/dashboard";
            case DISPATCHER:
                return "redirect:/dispatcher/dashboard";
            case DRIVER:
                return "redirect:/driver/dashboard";
            case ADMIN:
                return "redirect:/admin/dashboard";
            default:
                return "redirect:/login";
        }
    }

    @GetMapping("/login")
    public String loginPage(HttpSession session) {
        if (SessionHelper.isLoggedIn(session)) { // If user is already logged in, redirect to home page
            return "redirect:/";
        }
        return "login";
    }

    @GetMapping("/customer/dashboard")
    public String customerDashboard(HttpSession session, Model model) {
        if (!SessionHelper.isLoggedIn(session)) { // Check if user is logged in
            return "redirect:/login";
        }

        if (!SessionHelper.hasRole(session, Role.CUSTOMER)) { // Check if user has the CUSTOMER role
            return "redirect:/unauthorized";
        }

        User user = SessionHelper.getLoggedInUser(session); // Get the logged-in customer from the session

        // Fetch and count shipments for this customer by status
        long totalShipments = shipmentRepository.countByCustomer(user);
        long pendingCount = shipmentRepository.countByCustomerAndStatus(user, ShipmentStatus.PENDING);
        long assignedCount = shipmentRepository.countByCustomerAndStatus(user, ShipmentStatus.ASSIGNED);
        long pickedUpCount = shipmentRepository.countByCustomerAndStatus(user, ShipmentStatus.PICKED_UP);
        long inTransitCount = shipmentRepository.countByCustomerAndStatus(user, ShipmentStatus.IN_TRANSIT);
        long deliveredCount = shipmentRepository.countByCustomerAndStatus(user, ShipmentStatus.DELIVERED);

        // Fetch recent shipments for this customer sorted by most recent to show on the dashboard
        List<Shipment> recentShipments = shipmentRepository.findByCustomerOrderByCreatedAtDesc(user).stream().limit(4).toList();

        // Add all the fetched data to the model to be displayed on the dashboard
        model.addAttribute("loggedInUser", user);
        model.addAttribute("totalShipments", totalShipments);
        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("assignedCount", assignedCount);
        model.addAttribute("pickedUpCount", pickedUpCount);
        model.addAttribute("inTransitCount", inTransitCount);
        model.addAttribute("deliveredCount", deliveredCount);
        model.addAttribute("recentShipments", recentShipments);

        return "customer_dashboard";
    }

    @GetMapping("/unauthorized")
    public String unauthorizedPage() { // A simple page to show when a user tries to access a page they are not authorized to view
        return "unauthorized";
    }
}