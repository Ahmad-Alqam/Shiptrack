package com.shiptrack.controller;

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

        if (user == null) {
            return "redirect:/login";
        }

        switch (user.getRole()) {
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
        if (SessionHelper.isLoggedIn(session)) {
            return "redirect:/";
        }
        return "login";
    }

    @GetMapping("/customer/dashboard")
    public String customerDashboard(HttpSession session, Model model) {
        if (!SessionHelper.isLoggedIn(session)) {
            return "redirect:/login";
        }

        if (!SessionHelper.hasRole(session, Role.CUSTOMER)) {
            return "redirect:/unauthorized";
        }

        User user = SessionHelper.getLoggedInUser(session);

        long totalShipments = shipmentRepository.countByCustomer(user);
        long pendingCount = shipmentRepository.countByCustomerAndStatus(user, ShipmentStatus.PENDING);
        long assignedCount = shipmentRepository.countByCustomerAndStatus(user, ShipmentStatus.ASSIGNED);
        long pickedUpCount = shipmentRepository.countByCustomerAndStatus(user, ShipmentStatus.PICKED_UP);
        long inTransitCount = shipmentRepository.countByCustomerAndStatus(user, ShipmentStatus.IN_TRANSIT);
        long deliveredCount = shipmentRepository.countByCustomerAndStatus(user, ShipmentStatus.DELIVERED);

        List<Shipment> recentShipments = shipmentRepository.findByCustomerOrderByCreatedAtDesc(user).stream().limit(4).toList();

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
    public String unauthorizedPage() {
        return "unauthorized";
    }
}