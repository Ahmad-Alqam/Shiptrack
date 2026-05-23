package com.shiptrack.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.shiptrack.model.PasswordPolicy;
import com.shiptrack.model.Role;
import com.shiptrack.model.User;
import com.shiptrack.repository.PasswordPolicyRepository;
import com.shiptrack.repository.UserRepository;
import com.shiptrack.util.MyLogger;

@Controller
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;  // Use PasswordEncoder interface

    @Autowired
    private PasswordPolicyRepository passwordPolicyRepository;

    @GetMapping("/admin/dashboard")
    public String adminDashboard(Model model) {
        // Fetch all users and count them
        List<User> users = userRepository.findAll();
        long totalUsers = users.size();
        long totalCustomers = userRepository.countByRole(Role.CUSTOMER);
        long totalDispatchers = userRepository.countByRole(Role.DISPATCHER);
        long totalDrivers = userRepository.countByRole(Role.DRIVER);
        long totalAdmins = userRepository.countByRole(Role.ADMIN);
        long totalLockedUsers = userRepository.countByLocked(true);

        // Add data to the model for displaying in the view
        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("totalCustomers", totalCustomers);
        model.addAttribute("totalDispatchers", totalDispatchers);
        model.addAttribute("totalDrivers", totalDrivers);
        model.addAttribute("totalAdmins", totalAdmins);
        model.addAttribute("totalLockedUsers", totalLockedUsers);
        model.addAttribute("users", users);
        model.addAttribute("totalActiveShipments", 0);
        model.addAttribute("totalSecurityPolicies", 5);

        PasswordPolicy policy = passwordPolicyRepository.findAll().isEmpty()
        ? null
        : passwordPolicyRepository.findAll().get(0);

        model.addAttribute("policy", policy);

        MyLogger.writeToLog("Admin accessed dashboard");

        return "admin_dashboard"; // Render the admin dashboard page
    }

    // Admin can remove a user
    @PostMapping("/admin/removeUser")
    public String removeUser(@RequestParam Long userId, Model model) {
        User user = userRepository.findById(userId).orElse(null);

        if (user != null) {
            userRepository.delete(user);
            MyLogger.writeToLog("Admin removed user with ID: " + userId);
            model.addAttribute("success", "User removed successfully.");
        } else {
            model.addAttribute("error", "User not found.");
        }

        return "redirect:/admin/manage-users";
    }

    // Admin can lock/unlock a user account
    @PostMapping("/admin/lockUnlockUser")
    public String lockUnlockUser(@RequestParam Long userId, @RequestParam boolean lock, Model model) {
        User user = userRepository.findById(userId).orElse(null);

        if (user != null) {
            user.setLocked(lock);
            userRepository.save(user);
            String action = lock ? "locked" : "unlocked";
            MyLogger.writeToLog("Admin " + action + " user with ID: " + userId);
            model.addAttribute("success", "User " + action + " successfully.");
        } else {
            model.addAttribute("error", "User not found.");
        }

        return "redirect:/admin/manage-users";
    }

    @PostMapping("/admin/setPasswordStrength")
        public String setPasswordStrength(@RequestParam int minLength,
                                        @RequestParam int minUpperCase,
                                        @RequestParam int minLowerCase,
                                        @RequestParam int minDigit,
                                        @RequestParam int minSpecialChars,
                                        @RequestParam int maxLoginAttempts,
                                        Model model) {

            PasswordPolicy policy;

            if (passwordPolicyRepository.findAll().isEmpty()) {
                policy = new PasswordPolicy();
            } else {
                policy = passwordPolicyRepository.findAll().get(0);
            }

            policy.setMinLength(minLength);
            policy.setMinUpperCase(minUpperCase);
            policy.setMinLowerCase(minLowerCase);
            policy.setMinDigit(minDigit);
            policy.setMinSpecialChars(minSpecialChars);
            policy.setMaxLoginAttempts(maxLoginAttempts);

            passwordPolicyRepository.save(policy);

            MyLogger.writeToLog("Admin updated password security policy.");

            return "redirect:/admin/security-settings?success=Policy updated successfully";
}

    @GetMapping("/admin/create-dispatcher")
    public String createDispatcherPage(Model model,
            @RequestParam(value = "success", required = false) String success) {

        PasswordPolicy policy = passwordPolicyRepository.findAll().isEmpty()
                ? null
                : passwordPolicyRepository.findAll().get(0);

        model.addAttribute("policy", policy);
        model.addAttribute("success", success);

        return "admin_create_dispatcher";
    }

    @GetMapping("/admin/create-driver")
        public String createDriverPage(Model model,
                @RequestParam(value = "success", required = false) String success) {

            PasswordPolicy policy = passwordPolicyRepository.findAll().isEmpty()
                    ? null
                    : passwordPolicyRepository.findAll().get(0);

            model.addAttribute("policy", policy);
            model.addAttribute("success", success);

            return "admin_create_driver";
        }

    @GetMapping("/admin/manage-users")
        public String manageUsersPage(Model model) {
            List<User> manageableUsers = userRepository.findByRole(Role.CUSTOMER);
            manageableUsers.addAll(userRepository.findByRole(Role.DISPATCHER));
            manageableUsers.addAll(userRepository.findByRole(Role.DRIVER));

            model.addAttribute("users", manageableUsers);
            return "admin_manage_users";
        }

    @GetMapping("/admin/security-settings")
        public String securitySettingsPage(Model model,
                                        @RequestParam(value = "success", required = false) String success) {

            PasswordPolicy policy = passwordPolicyRepository.findAll().isEmpty()
                    ? null
                    : passwordPolicyRepository.findAll().get(0);

            model.addAttribute("policy", policy);

            if (success != null) {
                model.addAttribute("success", success);
            }

            return "admin_security_settings";
        }

    @PostMapping("/admin/create-dispatcher")
        public String createDispatcher(
                @RequestParam String fullName,
                @RequestParam String idNumber,
                @RequestParam String contactNumber,
                @RequestParam String username,
                @RequestParam String password,
                Model model) {

            return createStaffUser(fullName, idNumber, contactNumber, username, password, Role.DISPATCHER, model);
        }

    @PostMapping("/admin/create-driver")
        public String createDriver(
                @RequestParam String fullName,
                @RequestParam String idNumber,
                @RequestParam String contactNumber,
                @RequestParam String username,
                @RequestParam String password,
                Model model) {

            return createStaffUser(fullName, idNumber, contactNumber, username, password, Role.DRIVER, model);
        }

        private String createStaffUser(String fullName, String idNumber, String contactNumber,
            
            String username, String password, Role role, Model model) {

            fullName = fullName.trim();
            idNumber = idNumber.trim();
            contactNumber = contactNumber.trim();
            username = username.trim();

            if (fullName.isEmpty() || idNumber.isEmpty() || contactNumber.isEmpty()
                    || username.isEmpty() || password.isEmpty()) {
                model.addAttribute("error", "All fields are required.");
                return role == Role.DISPATCHER ? "admin_create_dispatcher" : "admin_create_driver";
            }

            if (userRepository.existsByUsername(username)) {
                model.addAttribute("error", "Username already exists.");
                return role == Role.DISPATCHER ? "admin_create_dispatcher" : "admin_create_driver";
            }

            if (userRepository.existsByIdNumber(idNumber)) {
                model.addAttribute("error", "ID number already exists.");
                return role == Role.DISPATCHER ? "admin_create_dispatcher" : "admin_create_driver";
            }

            // Prevent SQL Injection / invalid input
            if (!username.matches("^[a-zA-Z0-9_]+$")) {
                model.addAttribute("error", "Invalid username format.");
                return role == Role.DISPATCHER ? "admin_create_dispatcher" : "admin_create_driver";
            }

            if (!contactNumber.matches("\\d{10,15}")) {
                model.addAttribute("error", "Invalid phone number.");
                return role == Role.DISPATCHER ? "admin_create_dispatcher" : "admin_create_driver";
            }

            if (!idNumber.matches("\\d+")) {
                model.addAttribute("error", "Invalid ID number.");
                return role == Role.DISPATCHER ? "admin_create_dispatcher" : "admin_create_driver";
            }

            PasswordPolicy policy = passwordPolicyRepository.findAll().isEmpty()
                ? null
                : passwordPolicyRepository.findAll().get(0);

            if (policy != null && !isPasswordValid(password, policy)) {
                model.addAttribute("error", "Password does not meet the current security policy.");
                return role == Role.DISPATCHER ? "admin_create_dispatcher" : "admin_create_driver";
            }

            User user = new User();
            user.setFullName(fullName);
            user.setIdNumber(idNumber);
            user.setContactNumber(contactNumber);
            user.setUsername(username);
            user.setPassword(passwordEncoder.encode(password));
            user.setRole(role);
            user.setLocked(false);
            user.setFailedAttempts(0);
            user.setEnabled(true);

            userRepository.save(user);

            MyLogger.writeToLog("Admin created " + role + " account: " + username);

            if (role == Role.DISPATCHER) {
                return "redirect:/admin/create-dispatcher?success=Dispatcher created successfully";
            } else {
                return "redirect:/admin/create-driver?success=Driver created successfully";
            }
        }

        private boolean isPasswordValid(String password, PasswordPolicy policy) {
            if (password.length() < policy.getMinLength()) {
                return false;
            }

            int upper = 0;
            int lower = 0;
            int digit = 0;
            int special = 0;

            for (char c : password.toCharArray()) {
                if (Character.isUpperCase(c)) {
                    upper++;
                } else if (Character.isLowerCase(c)) {
                    lower++;
                } else if (Character.isDigit(c)) {
                    digit++;
                } else {
                    special++;
                }
            }

            return upper >= policy.getMinUpperCase()
                    && lower >= policy.getMinLowerCase()
                    && digit >= policy.getMinDigit()
                    && special >= policy.getMinSpecialChars();
        }
}