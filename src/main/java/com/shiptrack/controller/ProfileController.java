package com.shiptrack.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.shiptrack.model.User;
import com.shiptrack.repository.UserRepository;
import com.shiptrack.util.SessionHelper;

import jakarta.servlet.http.HttpSession;

@Controller
public class ProfileController {

    private final UserRepository userRepository;

    public ProfileController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/profile")
    public String profilePage(HttpSession session, Model model) {
        if (!SessionHelper.isLoggedIn(session)) {
            return "redirect:/login";
        }

        User user = SessionHelper.getLoggedInUser(session);
        model.addAttribute("user", user);
        model.addAttribute("roleLabel", getRoleLabel(user));
        return "profile";
    }

    @PostMapping("/profile")
    public String updateProfile(
            @RequestParam String fullName,
            @RequestParam String contactNumber,
            @RequestParam String username,
            HttpSession session,
            Model model
    ) {
        if (!SessionHelper.isLoggedIn(session)) {
            return "redirect:/login";
        }

        User sessionUser = SessionHelper.getLoggedInUser(session);
        User user = userRepository.findById(sessionUser.getId()).orElse(null);

        if (user == null) {
            session.invalidate();
            return "redirect:/login";
        }

        fullName = fullName.trim();
        contactNumber = contactNumber.trim();
        username = username.trim();

        if (fullName.isEmpty() || contactNumber.isEmpty() || username.isEmpty()) {
            model.addAttribute("error", "All fields are required.");
            model.addAttribute("user", user);
            model.addAttribute("roleLabel", getRoleLabel(user));
            return "profile";
        }

        if (!user.getUsername().equals(username) && userRepository.existsByUsername(username)) {
            model.addAttribute("error", "Username already exists.");
            model.addAttribute("user", user);
            model.addAttribute("roleLabel", getRoleLabel(user));
            return "profile";
        }

        user.setFullName(fullName);
        user.setContactNumber(contactNumber);
        user.setUsername(username);

        userRepository.save(user);

        // refresh session user
        session.setAttribute("user", user);

        model.addAttribute("success", "Profile updated successfully.");
        model.addAttribute("user", user);
        model.addAttribute("roleLabel", getRoleLabel(user));
        return "profile";
    }

    private String getRoleLabel(User user) {
        switch (user.getRole()) {
            case CUSTOMER:
                return "Customer";
            case DISPATCHER:
                return "Dispatcher";
            case DRIVER:
                return "Driver";
            case ADMIN:
                return "Admin";
            default:
                return "Unknown";
        }
    }
}