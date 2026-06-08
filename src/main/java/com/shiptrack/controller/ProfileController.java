package com.shiptrack.controller;
// Handles user profile functions.
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
        if (!SessionHelper.isLoggedIn(session)) { // Check if user is logged in
            return "redirect:/login";
        }

        User user = SessionHelper.getLoggedInUser(session); // Get the logged-in user from the session

        // Add user details and role label to the model to be displayed on the profile page
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
        if (!SessionHelper.isLoggedIn(session)) { // Check if user is logged in
            return "redirect:/login";
        }

        User sessionUser = SessionHelper.getLoggedInUser(session); // Get the logged-in user from the session to ensure we are updating the correct user's profile
        User user = userRepository.findById(sessionUser.getId()).orElse(null); // Fetch the user from the database to ensure we have the most up-to-date information

        if (user == null) { // If user is not found in the database (which should not happen if they are logged in), invalidate the session and redirect to login page
            session.invalidate();
            return "redirect:/login";
        }

        fullName = fullName.trim();
        contactNumber = contactNumber.trim();
        username = username.trim();

        // Validate that all fields are filled out
        if (fullName.isEmpty() || contactNumber.isEmpty() || username.isEmpty()) {
            model.addAttribute("error", "All fields are required.");
            model.addAttribute("user", user);
            model.addAttribute("roleLabel", getRoleLabel(user));
            return "profile";
        }
        
        // Check if the new username is already taken by another user (excluding the current user)
        if (!user.getUsername().equals(username) // Check if the username is being changed
             && userRepository.existsByUsername(username)) { // Check if the new username already exists in the database
            model.addAttribute("error", "Username already exists.");
            model.addAttribute("user", user);
            model.addAttribute("roleLabel", getRoleLabel(user));
            return "profile";
        }

        // Update the user's profile information and save it to the database
        user.setFullName(fullName);
        user.setContactNumber(contactNumber);
        user.setUsername(username);

        userRepository.save(user);

        // refresh session user
        session.setAttribute("user", user);

        // Add success message and updated user details to the model to be displayed on the profile page after successful update
        model.addAttribute("success", "Profile updated successfully.");
        model.addAttribute("user", user);
        model.addAttribute("roleLabel", getRoleLabel(user));
        return "profile";
    }

    // A helper method to convert the user's role enum to a more user-friendly label for display on the profile page
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