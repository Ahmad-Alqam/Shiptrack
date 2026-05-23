package com.shiptrack.controller;

import java.util.List;
import java.util.Optional;

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
import com.shiptrack.repository.UserRepository;
import com.shiptrack.util.SessionHelper;

import jakarta.servlet.http.HttpSession;

@Controller
public class DispatcherController {

    private final ShipmentRepository shipmentRepository;
    private final UserRepository userRepository;

    public DispatcherController(ShipmentRepository shipmentRepository, UserRepository userRepository) {
        this.shipmentRepository = shipmentRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/dispatcher/dashboard")
    public String dispatcherDashboard(HttpSession session, Model model) {
        if (!SessionHelper.isLoggedIn(session)) {
            return "redirect:/login";
        }

        if (!SessionHelper.hasRole(session, Role.DISPATCHER)) {
            return "redirect:/unauthorized";
        }

        User dispatcher = SessionHelper.getLoggedInUser(session);

        // Fetch and count data for the dispatcher
        long pendingRequests = shipmentRepository.countByStatus(ShipmentStatus.PENDING);
        long assignedDeliveries = shipmentRepository.countByDispatcherAndStatus(dispatcher, ShipmentStatus.ASSIGNED);
        long deliveredToday = shipmentRepository.countByDispatcherAndStatus(dispatcher, ShipmentStatus.DELIVERED);
        long availableDrivers = userRepository.countByRole(Role.DRIVER);

        // Get recent shipments for dispatcher
        List<Shipment> recentAssignments = shipmentRepository.findByDispatcherOrderByCreatedAtDesc(dispatcher);

        model.addAttribute("loggedInUser", dispatcher);
        model.addAttribute("pendingRequests", pendingRequests);
        model.addAttribute("assignedDeliveries", assignedDeliveries);
        model.addAttribute("deliveredToday", deliveredToday);
        model.addAttribute("availableDrivers", availableDrivers);
        model.addAttribute("recentAssignments", recentAssignments);

        return "dispatcher_dashboard";
    }

    @GetMapping("/dispatcher/assign-shipment")
    public String assignShipmentPage(HttpSession session, Model model) {
        if (!SessionHelper.isLoggedIn(session)) {
            return "redirect:/login";
        }

        if (!SessionHelper.hasRole(session, Role.DISPATCHER)) {
            return "redirect:/unauthorized";
        }

        // Fetch pending shipments and available drivers
        List<Shipment> pendingShipments = shipmentRepository.findByStatusOrderByCreatedAtDesc(ShipmentStatus.PENDING);
        List<User> drivers = userRepository.findByRole(Role.DRIVER);

        model.addAttribute("pendingShipments", pendingShipments);
        model.addAttribute("drivers", drivers);

        return "dispatcher_assign_shipment";
    }

    @PostMapping("/dispatcher/assign-shipment")
    public String assignShipment(
            @RequestParam Long shipmentId,
            @RequestParam Long driverId,
            HttpSession session,
            Model model
    ) {
        if (!SessionHelper.isLoggedIn(session)) {
            return "redirect:/login";
        }

        if (!SessionHelper.hasRole(session, Role.DISPATCHER)) {
            return "redirect:/unauthorized";
        }

        User dispatcher = SessionHelper.getLoggedInUser(session);

        // Fetch shipment and driver from DB
        Optional<Shipment> shipmentOptional = shipmentRepository.findById(shipmentId);
        Optional<User> driverOptional = userRepository.findById(driverId);

        if (shipmentOptional.isEmpty() || driverOptional.isEmpty()) {
            model.addAttribute("error", "Shipment or driver not found.");
            model.addAttribute("pendingShipments", shipmentRepository.findByStatusOrderByCreatedAtDesc(ShipmentStatus.PENDING));
            model.addAttribute("drivers", userRepository.findByRole(Role.DRIVER));
            return "dispatcher_assign_shipment";
        }

        Shipment shipment = shipmentOptional.get();
        User driver = driverOptional.get();

        // Ensure only PENDING shipments can be assigned
        if (shipment.getStatus() != ShipmentStatus.PENDING) {
            model.addAttribute("error", "Only pending shipments can be assigned.");
            model.addAttribute("pendingShipments", shipmentRepository.findByStatusOrderByCreatedAtDesc(ShipmentStatus.PENDING));
            model.addAttribute("drivers", userRepository.findByRole(Role.DRIVER));
            return "dispatcher_assign_shipment";
        }

        // Assign shipment to driver and dispatcher, then update status
        shipment.setDriver(driver);
        shipment.setDispatcher(dispatcher);
        shipment.setStatus(ShipmentStatus.ASSIGNED);

        shipmentRepository.save(shipment);

        model.addAttribute("success", "Shipment assigned successfully.");
        model.addAttribute("pendingShipments", shipmentRepository.findByStatusOrderByCreatedAtDesc(ShipmentStatus.PENDING));
        model.addAttribute("drivers", userRepository.findByRole(Role.DRIVER));
        return "dispatcher_assign_shipment";
    }

    @GetMapping("/dispatcher/update-status")
        public String updateStatusPage(HttpSession session, Model model) {
        if (!SessionHelper.isLoggedIn(session)) {
            return "redirect:/login";
        }

        if (!SessionHelper.hasRole(session, Role.DISPATCHER)) {
            return "redirect:/unauthorized";
        }

        User dispatcher = SessionHelper.getLoggedInUser(session);

        List<Shipment> shipments = shipmentRepository.findByDispatcherOrderByCreatedAtDesc(dispatcher);

        model.addAttribute("shipments", shipments);

        return "dispatcher_update_status";
}

@PostMapping("/dispatcher/update-status")
public String updateShipmentStatus(
        @RequestParam Long shipmentId,
        @RequestParam ShipmentStatus status,
        HttpSession session,
        Model model) {

    if (!SessionHelper.isLoggedIn(session)) {
        return "redirect:/login";
    }

    if (!SessionHelper.hasRole(session, Role.DISPATCHER)) {
        return "redirect:/unauthorized";
    }

    User dispatcher = SessionHelper.getLoggedInUser(session);

    Optional<Shipment> shipmentOptional = shipmentRepository.findById(shipmentId);

    if (shipmentOptional.isEmpty()) {
        model.addAttribute("error", "Shipment not found.");
        model.addAttribute("shipments", shipmentRepository.findByDispatcherOrderByCreatedAtDesc(dispatcher));
        return "dispatcher_update_status";
    }

    Shipment shipment = shipmentOptional.get();

    if (shipment.getDispatcher() == null || !shipment.getDispatcher().getId().equals(dispatcher.getId())) {
        model.addAttribute("error", "You can only update shipments assigned by you.");
        model.addAttribute("shipments", shipmentRepository.findByDispatcherOrderByCreatedAtDesc(dispatcher));
        return "dispatcher_update_status";
    }

    if (status != ShipmentStatus.PENDING &&
        status != ShipmentStatus.IN_TRANSIT &&
        status != ShipmentStatus.DELIVERED) {
        model.addAttribute("error", "Dispatcher can only set status to Pending, In Transit, or Delivered.");
        model.addAttribute("shipments", shipmentRepository.findByDispatcherOrderByCreatedAtDesc(dispatcher));
        return "dispatcher_update_status";
    }

    shipment.setStatus(status);
    shipmentRepository.save(shipment);

    model.addAttribute("success", "Shipment status updated successfully.");
    model.addAttribute("shipments", shipmentRepository.findByDispatcherOrderByCreatedAtDesc(dispatcher));

    return "dispatcher_update_status";
}
}