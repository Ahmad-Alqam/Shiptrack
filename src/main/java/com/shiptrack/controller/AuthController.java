package com.shiptrack.controller;

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
import com.shiptrack.util.MyLogger;  // Import MyLogger for logging

@Controller
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Autowired
    private PasswordPolicyRepository passwordPolicyRepository;

    
    // Register a new customer (Sign-Up)
    @PostMapping("/register/customer")
    public String registerCustomer(
            @RequestParam String fullName,
            @RequestParam String idNumber,
            @RequestParam String contactNumber,
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String confirmPassword,
            Model model) {

        fullName = fullName.trim();
        idNumber = idNumber.trim();
        contactNumber = contactNumber.trim();
        username = username.trim();

        // Input Validation
        if (fullName.isEmpty() || idNumber.isEmpty() || contactNumber.isEmpty()
                || username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            model.addAttribute("error", "All fields are required.");
            MyLogger.writeToLog("Failed sign-up attempt due to missing fields for username: " + username);  // Log failed attempt
            return "register_customer";
        }

        if (!isValidUsername(username)) {
            model.addAttribute("error", "Username can only contain letters, digits, and underscores.");
            MyLogger.writeToLog("Failed sign-up attempt: Invalid username format for username: " + username);  // Log failed attempt
            return "register_customer";
        }

        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "Passwords do not match.");
            MyLogger.writeToLog("Failed sign-up attempt: Passwords do not match for username: " + username);  // Log failed attempt
            return "register_customer";
        }

        if (userRepository.existsByUsername(username)) {
            model.addAttribute("error", "Username already exists.");
            MyLogger.writeToLog("Failed sign-up attempt: Username already exists for username: " + username);  // Log failed attempt
            return "register_customer";
        }

        if (userRepository.existsByIdNumber(idNumber)) {
            model.addAttribute("error", "ID number already exists.");
            MyLogger.writeToLog("Failed sign-up attempt: ID number already exists for username: " + username);  // Log failed attempt
            return "register_customer";
        }

        // Validate Contact Number (only digits)
        if (!isValidPhoneNumber(contactNumber)) {
            model.addAttribute("error", "Invalid phone number format.");
            MyLogger.writeToLog("Failed sign-up attempt: Invalid phone number for username: " + username);  // Log failed attempt
            return "register_customer";
        }

        // Validate ID Number (must be numeric)
        if (!isValidId(idNumber)) {
            model.addAttribute("error", "ID number must be numeric.");
            MyLogger.writeToLog("Failed sign-up attempt: Invalid ID number for username: " + username);
            return "register_customer";
        }

        // Validate password using current security policy from database
        PasswordPolicy policy = passwordPolicyRepository.findAll().isEmpty()
            ? null
            : passwordPolicyRepository.findAll().get(0);

        model.addAttribute("policy", policy);

        if (policy != null && !isPasswordValid(password, policy)) {
            model.addAttribute("error", "Password does not meet the current security policy.");
            MyLogger.writeToLog("Failed customer registration: password does not meet policy for username: " + username);
            return "register_customer";
        }
        

// Create and save the new user

        // Create and save the new user
        User user = new User();
        user.setFullName(fullName);
        user.setIdNumber(idNumber);
        user.setContactNumber(contactNumber);
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));  // Encrypt the password
        user.setRole(Role.CUSTOMER);
        user.setLocked(false);
        user.setFailedAttempts(0);
        user.setEnabled(true);

        userRepository.save(user);
        MyLogger.writeToLog("Successful registration for username: " + username);  // Log successful registration

        model.addAttribute("success", "Registration successful. You can now log in.");
        return "login";  // Redirect to login page after successful registration
    }

    // Login functionality
    @PostMapping("/login")
    public String login(
            @RequestParam String username,
            @RequestParam String password,
            Model model,
            jakarta.servlet.http.HttpSession session) {

        // Input Validation for username (alphanumeric and underscores only)
        if (!isValidUsername(username)) {
            model.addAttribute("error", "Invalid username format.");
            MyLogger.writeToLog("Failed login attempt: Invalid username format for username: " + username);  // Log failed login attempt
            return "login";
        }

        // Fetch user from the database
        java.util.Optional<User> optionalUser = userRepository.findByUsername(username);

        if (optionalUser.isEmpty()) {
            model.addAttribute("error", "Invalid username or password.");
            MyLogger.writeToLog("Failed login attempt with username: " + username);  // Log failed login attempt
            return "login";
        }

        User user = optionalUser.get();

        // Check if the account is locked
        if (user.isLocked()) {
            model.addAttribute("error", "Account is locked.");
            MyLogger.writeToLog("Failed login attempt: Account locked for username: " + username);  // Log failed login attempt
            return "login";
        }

        // 🔐 Password comparison
        if (!passwordEncoder.matches(password, user.getPassword())) {
            user.setFailedAttempts(user.getFailedAttempts() + 1);

            PasswordPolicy policy = passwordPolicyRepository.findAll().isEmpty()
        ? null
            : passwordPolicyRepository.findAll().get(0);

            int maxAttempts = policy != null ? policy.getMaxLoginAttempts() : 5;

            if (user.getFailedAttempts() >= maxAttempts) {
                user.setLocked(true);
                MyLogger.writeToLog("Account locked after failed attempts for username: " + username);
            }
            
            userRepository.save(user);

            model.addAttribute("error", "Invalid username or password.");
            MyLogger.writeToLog("Failed login attempt: Incorrect password for username: " + username);  // Log failed login attempt
            return "login";
        }

        // Reset failed attempts and save user after successful login
        user.setFailedAttempts(0);
        userRepository.save(user);

        session.setAttribute("user", user);  // Set user session
        MyLogger.writeToLog("Successful login for username: " + username);  // Log successful login

        // Redirect based on role
        switch (user.getRole()) {
            case CUSTOMER:
                MyLogger.writeToLog("Customer accessed dashboard");
                return "redirect:/customer/dashboard";
            case DISPATCHER:
                MyLogger.writeToLog("Dispatcher accessed dashboard");
                return "redirect:/dispatcher/dashboard";
            case DRIVER:
                MyLogger.writeToLog("Driver accessed dashboard");
                return "redirect:/driver/dashboard";
            case ADMIN:
                MyLogger.writeToLog("Admin accessed dashboard");
                return "redirect:/admin/dashboard";
            default:
                return "login";
        }
    }

    // Logout functionality
    @GetMapping("/logout")
    public String logout(jakarta.servlet.http.HttpSession session) {
        session.invalidate();  // Invalidate session
        MyLogger.writeToLog("User logged out successfully.");  // Log user logout
        return "redirect:/login";  // Redirect to login page
    }

    // Validation Methods

    // Validate Username (only alphanumeric and underscores)
    private boolean isValidUsername(String username) {
        return username.matches("^[a-zA-Z0-9_]+$");
    }

    // Validate Phone Number (digits only)
    private boolean isValidPhoneNumber(String phoneNumber) {
        return phoneNumber.matches("\\d{10,15}");  // Phone numbers of 10-15 digits
    }

    // Validate ID Number (numeric only)
    private boolean isValidId(String idNumber) {
        return idNumber.matches("\\d+");  // Only digits
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

    @GetMapping("/register/customer")
    public String registerCustomerPage(Model model) {
        PasswordPolicy policy = passwordPolicyRepository.findAll().isEmpty()
                ? null
                : passwordPolicyRepository.findAll().get(0);

        model.addAttribute("policy", policy);

        return "register_customer";
    }
}