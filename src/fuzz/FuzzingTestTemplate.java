package com.shiptrack.fuzz;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.MockedStatic;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.ui.Model;

import com.code_intelligence.jazzer.api.FuzzedDataProvider;
import com.shiptrack.controller.AuthController;
import com.shiptrack.controller.CustomerController;
import com.shiptrack.model.PasswordPolicy;
import com.shiptrack.model.Role;
import com.shiptrack.model.Shipment;
import com.shiptrack.model.User;
import com.shiptrack.repository.PasswordPolicyRepository;
import com.shiptrack.repository.ShipmentRepository;
import com.shiptrack.repository.UserRepository;
import com.shiptrack.util.SessionHelper;

import jakarta.servlet.http.HttpSession;

public class FuzzingTestTemplate {

    public static void fuzzerTestOneInput(FuzzedDataProvider data) {
        fuzzLoginFunction(data);
        fuzzCreateShipmentFunction(data);
    }

    private static void fuzzLoginFunction(FuzzedDataProvider data) {
        UserRepository userRepository = mock(UserRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        PasswordPolicyRepository passwordPolicyRepository = mock(PasswordPolicyRepository.class);
        Model model = mock(Model.class);
        HttpSession session = mock(HttpSession.class);

        AuthController authController = new AuthController(userRepository, passwordEncoder);
        ReflectionTestUtils.setField(authController, "passwordPolicyRepository", passwordPolicyRepository);

        String username = data.consumeString(100);
        String password = data.consumeString(100);
        boolean userExists = data.consumeBoolean();
        boolean passwordMatches = data.consumeBoolean();

        PasswordPolicy policy = new PasswordPolicy();
        policy.setMaxLoginAttempts(3);
        when(passwordPolicyRepository.findAll()).thenReturn(List.of(policy));

        if (userExists) {
            User user = new User();
            user.setId(1L);
            user.setUsername(username);
            user.setPassword("hashedPassword");
            user.setLocked(false);
            user.setFailedAttempts(0);

            int roleChoice = data.consumeInt(0, 3);
            if (roleChoice == 0) {
                user.setRole(Role.CUSTOMER);
            } else if (roleChoice == 1) {
                user.setRole(Role.DISPATCHER);
            } else if (roleChoice == 2) {
                user.setRole(Role.DRIVER);
            } else {
                user.setRole(Role.ADMIN);
            }

            when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
            when(passwordEncoder.matches(password, "hashedPassword")).thenReturn(passwordMatches);
        } else {
            when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        }

        String result = authController.login(username, password, model, session);

        if (result == null || result.trim().isEmpty()) {
            throw new RuntimeException("Login returned null or empty result.");
        }

        if (!result.equals("login")
                && !result.equals("redirect:/customer/dashboard")
                && !result.equals("redirect:/dispatcher/dashboard")
                && !result.equals("redirect:/driver/dashboard")
                && !result.equals("redirect:/admin/dashboard")) {
            throw new RuntimeException("Unexpected login result: " + result);
        }
    }

    private static void fuzzCreateShipmentFunction(FuzzedDataProvider data) {
        ShipmentRepository shipmentRepository = mock(ShipmentRepository.class);
        Model model = mock(Model.class);
        HttpSession session = mock(HttpSession.class);

        CustomerController customerController = new CustomerController(shipmentRepository);

        User customer = new User();
        customer.setId(1L);
        customer.setUsername("customer1");
        customer.setRole(Role.CUSTOMER);

        String pickupAddress = data.consumeString(100);
        String deliveryAddress = data.consumeString(100);
        String packageType = data.consumeString(50);
        Double packageWeight = data.consumeDouble();
        String notes = data.consumeString(200);

        when(shipmentRepository.existsByTrackingNumber(anyString())).thenReturn(false);

        try (MockedStatic<SessionHelper> mockedSession = mockStatic(SessionHelper.class)) {
            mockedSession.when(() -> SessionHelper.isLoggedIn(session)).thenReturn(true);
            mockedSession.when(() -> SessionHelper.hasRole(session, Role.CUSTOMER)).thenReturn(true);
            mockedSession.when(() -> SessionHelper.getLoggedInUser(session)).thenReturn(customer);

            String result = customerController.createShipment(
                    pickupAddress,
                    deliveryAddress,
                    packageType,
                    packageWeight,
                    notes,
                    session,
                    model
            );

            if (result == null || result.trim().isEmpty()) {
                throw new RuntimeException("Create shipment returned null or empty result.");
            }

            if (!result.equals("customer_create_shipment")) {
                throw new RuntimeException("Unexpected create shipment result: " + result);
            }

            boolean invalidRequiredFields =
                    pickupAddress == null || pickupAddress.trim().isEmpty()
                            || deliveryAddress == null || deliveryAddress.trim().isEmpty()
                            || packageType == null || packageType.trim().isEmpty();

            boolean invalidWeight =
                    packageWeight == null
                            || Double.isNaN(packageWeight)
                            || Double.isInfinite(packageWeight)
                            || packageWeight <= 0;

            if (invalidRequiredFields || invalidWeight) {
                verify(shipmentRepository, never()).save(any(Shipment.class));
            }
        }
    }
}